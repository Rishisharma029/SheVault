import json
from datetime import datetime
from typing import Optional, List, Dict, Any
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, update
from app.models.delivery_attempt import DeliveryAttempt
from app.utils.time import utc_now

class DeliveryRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def create_attempt(
        self,
        incident_id: str,
        recipient_id: Optional[str] = None,
        channel: str = "WEBSOCKET",
        status: str = "QUEUED",
        metadata: Optional[Dict[str, Any]] = None
    ) -> DeliveryAttempt:
        attempt = DeliveryAttempt(
            incident_id=incident_id,
            recipient_id=recipient_id,
            channel=channel,
            status=status,
            attempted_at=utc_now(),
            metadata_json=json.dumps(metadata) if metadata else None
        )
        self.session.add(attempt)
        await self.session.flush()
        return attempt

    async def mark_acknowledged(
        self,
        delivery_id: str,
        provider_message_id: Optional[str] = None
    ) -> Optional[DeliveryAttempt]:
        stmt = select(DeliveryAttempt).where(DeliveryAttempt.id == delivery_id)
        result = await self.session.execute(stmt)
        attempt = result.scalar_one_or_none()
        if not attempt:
            return None

        attempt.status = "ACKNOWLEDGED"
        attempt.acknowledged_at = utc_now()
        if provider_message_id:
            attempt.provider_message_id = provider_message_id

        await self.session.flush()
        return attempt

    async def mark_failed(
        self,
        delivery_id: str,
        error_code: str,
        error_message: str
    ) -> Optional[DeliveryAttempt]:
        stmt = select(DeliveryAttempt).where(DeliveryAttempt.id == delivery_id)
        result = await self.session.execute(stmt)
        attempt = result.scalar_one_or_none()
        if not attempt:
            return None

        attempt.status = "FAILED"
        attempt.error_code = error_code
        attempt.error_message = error_message

        await self.session.flush()
        return attempt

    async def get_for_incident(self, incident_id: str) -> List[DeliveryAttempt]:
        stmt = (
            select(DeliveryAttempt)
            .where(DeliveryAttempt.incident_id == incident_id)
            .order_by(DeliveryAttempt.attempted_at.asc())
        )
        result = await self.session.execute(stmt)
        return list(result.scalars().all())
