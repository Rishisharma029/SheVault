from datetime import datetime
from typing import Optional, Tuple, Dict, Any, Set
from sqlalchemy.ext.asyncio import AsyncSession
from app.repositories.incidents import IncidentRepository
from app.repositories.users import UserRepository
from app.core.security import verify_secret
from app.core.exceptions import (
    NotFoundException,
    ForbiddenException,
    InvalidStateTransitionException,
    ValidationException,
    UnauthorizedException
)
from app.models.incident import Incident
from app.models.incident_event import IncidentEvent
from app.core.redis import redis_manager, STREAM_INCIDENTS, STREAM_DELIVERIES
from app.utils.time import utc_now

VALID_TRANSITIONS: Dict[str, Set[str]] = {
    "STARTING": {"ACTIVE_GRACE", "FAILED"},
    "ACTIVE_GRACE": {"SAFE_CANCELLED", "DURESS_CANCELLED", "CANCEL_AUTHENTICATION", "ESCALATING"},
    "CANCEL_AUTHENTICATION": {"SAFE_CANCELLED", "DURESS_CANCELLED", "ESCALATING"},
    "ESCALATING": {"ESCALATED", "NETWORK_RETRY", "DELIVERY_UNKNOWN", "FAILED"},
    "NETWORK_RETRY": {"ESCALATED", "FALLBACK_PENDING", "FAILED"},
    "FALLBACK_PENDING": {"ESCALATED", "DELIVERY_UNKNOWN", "FAILED"},
    "ESCALATED": {"ENDED", "FAILED"},
    "DELIVERY_UNKNOWN": {"ENDED", "ESCALATED"},
    "SAFE_CANCELLED": set(),
    "DURESS_CANCELLED": set(),
    "ENDED": set(),
    "FAILED": set()
}

class IncidentService:
    def __init__(self, session: AsyncSession):
        self.session = session
        self.incident_repo = IncidentRepository(session)
        self.user_repo = UserRepository(session)

    def validate_transition(self, current_status: str, target_status: str) -> None:
        allowed = VALID_TRANSITIONS.get(current_status, set())
        if target_status not in allowed:
            raise InvalidStateTransitionException(current_status, target_status)

    async def create_incident(
        self,
        user_id: str,
        session_id: str,
        activation_method: str,
        started_at: datetime,
        battery: Optional[int] = None,
        latitude: Optional[float] = None,
        longitude: Optional[float] = None,
        accuracy: Optional[float] = None,
        device_id: Optional[str] = None,
        connectivity_state: str = "ONLINE"
    ) -> Tuple[Incident, bool]:
        """
        Idempotent incident creation.
        Returns (Incident, is_new: bool).
        """
        existing = await self.incident_repo.get_by_user_and_session(user_id, session_id)
        if existing:
            return existing, False

        incident, event = await self.incident_repo.create_with_initial_event(
            user_id=user_id,
            session_id=session_id,
            activation_method=activation_method,
            started_at=started_at,
            battery=battery,
            latitude=latitude,
            longitude=longitude,
            accuracy=accuracy,
            device_id=device_id,
            connectivity_state=connectivity_state
        )

        # Publish to Redis stream
        await redis_manager.xadd(STREAM_INCIDENTS, {
            "incident_id": incident.id,
            "user_id": user_id,
            "event_type": "INCIDENT_CREATED",
            "status": incident.status,
            "timestamp": started_at.isoformat()
        })

        return incident, True

    async def get_incident(self, incident_id: str, user_id: str) -> Incident:
        incident = await self.incident_repo.get_by_id(incident_id)
        if not incident:
            raise NotFoundException("Incident", incident_id)
        if incident.user_id != user_id:
            raise ForbiddenException("You are not authorized to view this incident")
        return incident

    async def cancel_with_pin(self, incident_id: str, user_id: str, pin: str) -> Tuple[str, bool]:
        """
        Handles cancellation with dual Safe PIN / Duress PIN logic.
        Returns (effective_status, is_duress).
        """
        incident = await self.get_incident(incident_id, user_id)
        user = await self.user_repo.get_by_id(user_id)
        if not user:
            raise NotFoundException("User", user_id)

        now = utc_now()
        is_safe = verify_secret(pin, user.safe_pin_hash or "")
        is_duress = verify_secret(pin, user.duress_pin_hash or "")

        if not is_safe and not is_duress:
            raise UnauthorizedException("Invalid PIN entered")

        if is_duress:
            # Transition to DURESS_CANCELLED internally
            self.validate_transition(incident.status, "DURESS_CANCELLED")
            await self.incident_repo.update_status(incident_id, "DURESS_CANCELLED", cancel_type="DURESS")
            await self.incident_repo.add_event(incident_id, "DURESS_CANCELLED", now, {"duress": True})

            # Publish critical escalation to Redis
            await redis_manager.xadd(STREAM_DELIVERIES, {
                "incident_id": incident_id,
                "user_id": user_id,
                "event_type": "DURESS_ESCALATION",
                "priority": "CRITICAL",
                "duress": True,
                "timestamp": now.isoformat()
            })
            return "DURESS_CANCELLED", True
        else:
            # Genuine safe cancellation
            self.validate_transition(incident.status, "SAFE_CANCELLED")
            await self.incident_repo.update_status(incident_id, "SAFE_CANCELLED", cancel_type="SAFE")
            await self.incident_repo.add_event(incident_id, "SAFE_CANCELLED", now, {"safe": True})

            await redis_manager.xadd(STREAM_INCIDENTS, {
                "incident_id": incident_id,
                "user_id": user_id,
                "event_type": "SAFE_CANCELLED",
                "status": "SAFE_CANCELLED",
                "timestamp": now.isoformat()
            })
            return "SAFE_CANCELLED", False

    async def escalate_incident(self, incident_id: str, user_id: str) -> Incident:
        incident = await self.get_incident(incident_id, user_id)
        self.validate_transition(incident.status, "ESCALATING")

        now = utc_now()
        await self.incident_repo.update_status(incident_id, "ESCALATED")
        await self.incident_repo.add_event(incident_id, "ESCALATION_STARTED", now)

        await redis_manager.xadd(STREAM_DELIVERIES, {
            "incident_id": incident_id,
            "user_id": user_id,
            "event_type": "INCIDENT_ESCALATED",
            "priority": "HIGH",
            "duress": False,
            "timestamp": now.isoformat()
        })

        return incident

    async def end_incident(self, incident_id: str, user_id: str) -> Incident:
        incident = await self.get_incident(incident_id, user_id)
        self.validate_transition(incident.status, "ENDED")

        now = utc_now()
        await self.incident_repo.update_status(incident_id, "ENDED")
        await self.incident_repo.add_event(incident_id, "INCIDENT_ENDED", now)

        await redis_manager.xadd(STREAM_INCIDENTS, {
            "incident_id": incident_id,
            "user_id": user_id,
            "event_type": "INCIDENT_ENDED",
            "status": "ENDED",
            "timestamp": now.isoformat()
        })

        return incident
