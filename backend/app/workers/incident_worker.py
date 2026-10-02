import asyncio
import json
import logging
from app.core.redis import redis_manager, STREAM_INCIDENTS
from app.websocket.manager import ws_manager

logger = logging.getLogger(__name__)

async def run_incident_worker():
    """
    Background worker that listens to STREAM_INCIDENTS and broadcasts events to WebSocket clients.
    """
    logger.info("Starting incident stream worker...")
    while True:
        try:
            if redis_manager.is_connected and redis_manager.client:
                # Read from Redis stream consumer group
                entries = await redis_manager.client.xreadgroup(
                    "shevault_workers",
                    "incident_worker_1",
                    {STREAM_INCIDENTS: ">"},
                    count=10,
                    block=2000
                )
                for stream_name, messages in entries:
                    for msg_id, data in messages:
                        incident_id = data.get("incident_id")
                        event_type = data.get("event_type")
                        if incident_id and event_type:
                            await ws_manager.broadcast_to_room(
                                f"incident:{incident_id}",
                                event_type,
                                data
                            )
                        await redis_manager.xack(STREAM_INCIDENTS, "shevault_workers", msg_id)
            else:
                # Check in-memory fallback buffer
                buffer = redis_manager._in_memory_streams.get(STREAM_INCIDENTS, [])
                while buffer:
                    item = buffer.pop(0)
                    data = item["data"]
                    incident_id = data.get("incident_id")
                    event_type = data.get("event_type")
                    if incident_id and event_type:
                        await ws_manager.broadcast_to_room(
                            f"incident:{incident_id}",
                            event_type,
                            data
                        )
                await asyncio.sleep(0.5)
        except asyncio.CancelledError:
            logger.info("Incident worker stopped.")
            break
        except Exception as e:
            logger.error(f"Error in incident worker loop: {e}")
            await asyncio.sleep(1.0)
