import asyncio
import json
import logging
from app.core.redis import redis_manager, STREAM_DELIVERIES
from app.core.database import AsyncSessionLocal
from app.services.escalation_service import EscalationService

logger = logging.getLogger(__name__)

async def run_delivery_worker():
    """
    Background worker that listens to STREAM_DELIVERIES, dispatches emergency alerts to trusted contacts,
    and acknowledges stream items.
    """
    logger.info("Starting delivery stream worker...")
    while True:
        try:
            if redis_manager.is_connected and redis_manager.client:
                entries = await redis_manager.client.xreadgroup(
                    "shevault_workers",
                    "delivery_worker_1",
                    {STREAM_DELIVERIES: ">"},
                    count=5,
                    block=2000
                )
                for stream_name, messages in entries:
                    for msg_id, data in messages:
                        incident_id = data.get("incident_id")
                        user_id = data.get("user_id")
                        duress = data.get("duress") in [True, "True", "true", 1]

                        if incident_id and user_id:
                            async with AsyncSessionLocal() as session:
                                escalation_svc = EscalationService(session)
                                await escalation_svc.process_escalation(
                                    incident_id=incident_id,
                                    user_id=user_id,
                                    is_duress=duress
                                )
                                await session.commit()

                        await redis_manager.xack(STREAM_DELIVERIES, "shevault_workers", msg_id)
            else:
                buffer = redis_manager._in_memory_streams.get(STREAM_DELIVERIES, [])
                while buffer:
                    item = buffer.pop(0)
                    data = item["data"]
                    incident_id = data.get("incident_id")
                    user_id = data.get("user_id")
                    duress = data.get("duress") in [True, "True", "true", 1]

                    if incident_id and user_id:
                        async with AsyncSessionLocal() as session:
                            escalation_svc = EscalationService(session)
                            await escalation_svc.process_escalation(
                                incident_id=incident_id,
                                user_id=user_id,
                                is_duress=duress
                            )
                            await session.commit()
                await asyncio.sleep(0.5)
        except asyncio.CancelledError:
            logger.info("Delivery worker stopped.")
            break
        except Exception as e:
            logger.error(f"Error in delivery worker loop: {e}")
            await asyncio.sleep(1.0)
