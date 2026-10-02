from abc import ABC, abstractmethod
from typing import Dict, Any, Optional
import uuid
import logging
from sqlalchemy.ext.asyncio import AsyncSession
from app.repositories.deliveries import DeliveryRepository
from app.utils.time import utc_now

logger = logging.getLogger(__name__)

class DeliveryResult:
    def __init__(
        self,
        success: bool,
        provider_message_id: Optional[str] = None,
        error_code: Optional[str] = None,
        error_message: Optional[str] = None
    ):
        self.success = success
        self.provider_message_id = provider_message_id
        self.error_code = error_code
        self.error_message = error_message

class DeliveryProvider(ABC):
    @abstractmethod
    async def send(self, channel: str, recipient: str, message: Dict[str, Any]) -> DeliveryResult:
        pass

class MockDeliveryProvider(DeliveryProvider):
    """
    Mock Delivery Provider simulating high-reliability delivery with deterministic ACK response.
    """
    async def send(self, channel: str, recipient: str, message: Dict[str, Any]) -> DeliveryResult:
        msg_id = f"mock-msg-{uuid.uuid4().hex[:8]}"
        logger.info(f"[MockDeliveryProvider] Dispatched to {channel} ({recipient}) -> msg_id: {msg_id}")
        return DeliveryResult(success=True, provider_message_id=msg_id)

class DeliveryService:
    def __init__(self, session: AsyncSession, provider: Optional[DeliveryProvider] = None):
        self.session = session
        self.delivery_repo = DeliveryRepository(session)
        self.provider = provider or MockDeliveryProvider()

    async def execute_delivery(
        self,
        incident_id: str,
        recipient_id: Optional[str],
        channel: str,
        payload: Dict[str, Any]
    ) -> bool:
        # 1. Create QUEUED delivery record
        attempt = await self.delivery_repo.create_attempt(
            incident_id=incident_id,
            recipient_id=recipient_id,
            channel=channel,
            status="QUEUED",
            metadata=payload
        )

        # 2. Dispatch via provider
        recipient_addr = payload.get("phone", "unknown")
        result = await self.provider.send(channel, recipient_addr, payload)

        # 3. Update delivery record based on provider result
        if result.success:
            await self.delivery_repo.mark_acknowledged(attempt.id, result.provider_message_id)
            return True
        else:
            await self.delivery_repo.mark_failed(
                attempt.id,
                result.error_code or "DISPATCH_FAILED",
                result.error_message or "Provider failed to acknowledge delivery"
            )
            return False
