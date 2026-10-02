from typing import Dict, Any, List
from sqlalchemy.ext.asyncio import AsyncSession
from app.repositories.contacts import ContactRepository
from app.repositories.incidents import IncidentRepository
from app.services.delivery_service import DeliveryService
from app.core.redis import redis_manager, STREAM_NOTIFICATIONS
from app.utils.time import utc_now
import logging

logger = logging.getLogger(__name__)

class EscalationService:
    def __init__(self, session: AsyncSession):
        self.session = session
        self.contact_repo = ContactRepository(session)
        self.incident_repo = IncidentRepository(session)
        self.delivery_service = DeliveryService(session)

    async def process_escalation(
        self,
        incident_id: str,
        user_id: str,
        is_duress: bool = False
    ) -> int:
        """
        Escalation Pipeline:
        1. Find active contacts for user
        2. Apply contact permissions
        3. Create delivery records & dispatch via DeliveryService
        4. Broadcast live notification to WebSocket gateway
        """
        contacts = await self.contact_repo.get_active_emergency_contacts(user_id)
        incident = await self.incident_repo.get_by_id(incident_id)
        if not incident:
            return 0

        dispatched_count = 0
        now = utc_now()

        for contact in contacts:
            perms = contact.permissions
            # Filter payload per permissions
            contact_payload: Dict[str, Any] = {
                "incident_id": incident_id,
                "user_name": incident.user.name if incident.user else "User",
                "timestamp": now.isoformat(),
                "priority": "CRITICAL" if is_duress else "HIGH",
                "duress": is_duress,
                "phone": contact.phone
            }

            if perms and perms.location and incident.last_latitude is not None:
                contact_payload["latitude"] = incident.last_latitude
                contact_payload["longitude"] = incident.last_longitude
                contact_payload["accuracy"] = incident.last_accuracy

            if perms and perms.battery and incident.last_battery is not None:
                contact_payload["battery"] = incident.last_battery

            # Dispatch delivery attempt
            success = await self.delivery_service.execute_delivery(
                incident_id=incident_id,
                recipient_id=contact.id,
                channel="SMS" if not is_duress else "COVERT_SMS",
                payload=contact_payload
            )
            if success:
                dispatched_count += 1

        # Broadcast live event to WebSocket room
        await redis_manager.xadd(STREAM_NOTIFICATIONS, {
            "target_room": f"incident:{incident_id}",
            "event_type": "incident.escalated",
            "data": {
                "incident_id": incident_id,
                "dispatched_contacts": dispatched_count,
                "timestamp": now.isoformat()
            }
        })

        return dispatched_count
