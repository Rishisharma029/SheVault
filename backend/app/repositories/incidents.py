import json
from datetime import datetime
from typing import Optional, List, Tuple, Any, Dict
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, update, func
from sqlalchemy.orm import selectinload
from app.models.incident import Incident
from app.models.incident_event import IncidentEvent
from app.utils.time import utc_now

class IncidentRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def get_by_id(self, incident_id: str) -> Optional[Incident]:
        stmt = (
            select(Incident)
            .options(
                selectinload(Incident.events),
                selectinload(Incident.locations),
                selectinload(Incident.delivery_attempts)
            )
            .where(Incident.id == incident_id)
        )
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()

    async def get_by_user_and_session(self, user_id: str, session_id: str) -> Optional[Incident]:
        stmt = (
            select(Incident)
            .options(selectinload(Incident.events))
            .where(Incident.user_id == user_id, Incident.session_id == session_id)
        )
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()

    async def list_for_user(self, user_id: str, limit: int = 50, offset: int = 0) -> List[Incident]:
        stmt = (
            select(Incident)
            .where(Incident.user_id == user_id)
            .order_by(Incident.started_at.desc())
            .limit(limit)
            .offset(offset)
        )
        result = await self.session.execute(stmt)
        return list(result.scalars().all())

    async def create_with_initial_event(
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
    ) -> Tuple[Incident, IncidentEvent]:
        """Atomically persists the incident and its initial INCIDENT_CREATED event together."""
        incident = Incident(
            user_id=user_id,
            device_id=device_id,
            session_id=session_id,
            activation_method=activation_method,
            status="ACTIVE_GRACE", # Created directly in ACTIVE_GRACE
            started_at=started_at,
            grace_started_at=started_at,
            initial_battery=battery,
            last_battery=battery,
            last_latitude=latitude,
            last_longitude=longitude,
            last_accuracy=accuracy,
            connectivity_state=connectivity_state
        )
        self.session.add(incident)
        await self.session.flush()

        initial_payload = {
            "activation_method": activation_method,
            "session_id": session_id,
            "battery": battery,
            "latitude": latitude,
            "longitude": longitude,
            "accuracy": accuracy
        }

        event = IncidentEvent(
            incident_id=incident.id,
            event_type="INCIDENT_CREATED",
            occurred_at=started_at,
            sequence_number=1,
            payload_json=json.dumps(initial_payload)
        )
        self.session.add(event)
        await self.session.flush()

        return incident, event

    async def add_event(
        self,
        incident_id: str,
        event_type: str,
        occurred_at: datetime,
        payload: Optional[Dict[str, Any]] = None
    ) -> IncidentEvent:
        """Determines the next sequence number and appends an event to the incident timeline."""
        seq_stmt = select(func.coalesce(func.max(IncidentEvent.sequence_number), 0)).where(IncidentEvent.incident_id == incident_id)
        current_max = (await self.session.execute(seq_stmt)).scalar() or 0
        next_seq = current_max + 1

        event = IncidentEvent(
            incident_id=incident_id,
            event_type=event_type,
            occurred_at=occurred_at,
            sequence_number=next_seq,
            payload_json=json.dumps(payload) if payload else None
        )
        self.session.add(event)
        await self.session.flush()
        return event

    async def update_status(
        self,
        incident_id: str,
        new_status: str,
        cancel_type: Optional[str] = None
    ) -> Optional[Incident]:
        incident = await self.get_by_id(incident_id)
        if not incident:
            return None

        incident.status = new_status
        now = utc_now()
        if new_status in ["SAFE_CANCELLED", "DURESS_CANCELLED", "ENDED"]:
            incident.ended_at = now
            if cancel_type:
                incident.cancel_type = cancel_type
        elif new_status == "ESCALATED":
            incident.escalated_at = now
        incident.updated_at = now

        await self.session.flush()
        return incident

    async def update_telemetry(
        self,
        incident_id: str,
        battery: Optional[int] = None,
        latitude: Optional[float] = None,
        longitude: Optional[float] = None,
        accuracy: Optional[float] = None,
        connectivity_state: Optional[str] = None
    ) -> None:
        values: Dict[str, Any] = {"updated_at": utc_now()}
        if battery is not None:
            values["last_battery"] = battery
        if latitude is not None:
            values["last_latitude"] = latitude
        if longitude is not None:
            values["last_longitude"] = longitude
        if accuracy is not None:
            values["last_accuracy"] = accuracy
        if connectivity_state is not None:
            values["connectivity_state"] = connectivity_state

        stmt = update(Incident).where(Incident.id == incident_id).values(**values)
        await self.session.execute(stmt)

    async def get_events(self, incident_id: str) -> List[IncidentEvent]:
        stmt = (
            select(IncidentEvent)
            .where(IncidentEvent.incident_id == incident_id)
            .order_by(IncidentEvent.sequence_number.asc())
        )
        result = await self.session.execute(stmt)
        return list(result.scalars().all())
