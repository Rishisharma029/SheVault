import json
import logging
from typing import Optional, Dict, Any, List
import redis.asyncio as redis
from app.core.config import settings

logger = logging.getLogger(__name__)

STREAM_INCIDENTS = "shevault:incidents"
STREAM_DELIVERIES = "shevault:deliveries"
STREAM_NOTIFICATIONS = "shevault:notifications"

class RedisManager:
    def __init__(self):
        self.client: Optional[redis.Redis] = None
        self._in_memory_streams: Dict[str, List[Dict[str, Any]]] = {
            STREAM_INCIDENTS: [],
            STREAM_DELIVERIES: [],
            STREAM_NOTIFICATIONS: []
        }
        self.is_connected: bool = False

    async def connect(self):
        if not settings.REDIS_ENABLED:
            logger.info("Redis disabled via settings; operating with resilient in-memory stream buffer.")
            return

        try:
            self.client = redis.from_url(
                settings.REDIS_URL,
                decode_responses=True,
                socket_timeout=2.0
            )
            await self.client.ping()
            self.is_connected = True
            logger.info(f"Connected to Redis at {settings.REDIS_URL}")
            # Ensure consumer groups exist for each stream
            for stream in [STREAM_INCIDENTS, STREAM_DELIVERIES, STREAM_NOTIFICATIONS]:
                try:
                    await self.client.xgroup_create(stream, "shevault_workers", id="0", mkstream=True)
                except Exception:
                    # Consumer group already exists
                    pass
        except Exception as e:
            logger.warning(f"Could not connect to Redis ({e}). Using in-memory fallback stream.")
            self.is_connected = False
            self.client = None

    async def disconnect(self):
        if self.client:
            await self.client.close()
            self.is_connected = False

    async def xadd(self, stream: str, data: Dict[str, Any]) -> str:
        serialized_data = {k: json.dumps(v) if isinstance(v, (dict, list, bool)) else str(v) for k, v in data.items()}
        if self.is_connected and self.client:
            try:
                msg_id = await self.client.xadd(stream, serialized_data)
                return str(msg_id)
            except Exception as e:
                logger.error(f"Failed to publish to Redis stream {stream}: {e}")
        
        # In-memory fallback
        import uuid
        msg_id = f"{len(self._in_memory_streams.setdefault(stream, [])) + 1}-{uuid.uuid4().hex[:6]}"
        self._in_memory_streams[stream].append({"id": msg_id, "data": data})
        return msg_id

    async def xack(self, stream: str, group: str, message_id: str):
        if self.is_connected and self.client:
            try:
                await self.client.xack(stream, group, message_id)
            except Exception as e:
                logger.error(f"Failed to ACK message {message_id} on {stream}: {e}")

    async def health_check(self) -> bool:
        if not self.is_connected or not self.client:
            return False
        try:
            return await self.client.ping()
        except Exception:
            return False

redis_manager = RedisManager()
