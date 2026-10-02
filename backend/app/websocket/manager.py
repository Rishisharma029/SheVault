from typing import Dict, List, Any
import json
import logging
from starlette.websockets import WebSocket

logger = logging.getLogger(__name__)

class ConnectionManager:
    def __init__(self):
        # Maps room_id (e.g., "incident:<id>") -> list of active WebSockets
        self.active_rooms: Dict[str, List[WebSocket]] = {}

    async def connect(self, room_id: str, websocket: WebSocket):
        await websocket.accept()
        if room_id not in self.active_rooms:
            self.active_rooms[room_id] = []
        self.active_rooms[room_id].append(websocket)
        logger.info(f"WebSocket connected to room '{room_id}' (Total in room: {len(self.active_rooms[room_id])})")

    def disconnect(self, room_id: str, websocket: WebSocket):
        if room_id in self.active_rooms:
            if websocket in self.active_rooms[room_id]:
                self.active_rooms[room_id].remove(websocket)
            if not self.active_rooms[room_id]:
                del self.active_rooms[room_id]
        logger.info(f"WebSocket disconnected from room '{room_id}'")

    async def broadcast_to_room(self, room_id: str, event_type: str, data: Dict[str, Any]):
        if room_id not in self.active_rooms:
            return

        message_str = json.dumps({"event": event_type, "data": data})
        dead_sockets = []

        for socket in self.active_rooms[room_id]:
            try:
                await socket.send_text(message_str)
            except Exception as e:
                logger.warning(f"Error broadcasting to socket in {room_id}: {e}")
                dead_sockets.append(socket)

        for dead in dead_sockets:
            self.disconnect(room_id, dead)

ws_manager = ConnectionManager()
