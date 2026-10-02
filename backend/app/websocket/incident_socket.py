from fastapi import APIRouter, WebSocket, WebSocketDisconnect, Query, Depends, status
from sqlalchemy.ext.asyncio import AsyncSession
from app.core.database import get_db
from app.core.security import decode_token
from app.repositories.incidents import IncidentRepository
from app.repositories.contacts import ContactRepository
from app.websocket.manager import ws_manager
import logging

logger = logging.getLogger(__name__)

router = APIRouter()

@router.websocket("/ws/incidents/{incident_id}")
async def websocket_incident_endpoint(
    websocket: WebSocket,
    incident_id: str,
    token: str = Query(...),
    db: AsyncSession = Depends(get_db)
):
    """
    Secure incident WebSocket gateway.
    Authenticates token and authorizes either the incident owner or an authorized trusted contact.
    """
    # 1. Authenticate token
    try:
        payload = decode_token(token)
        user_id = payload.get("sub")
        if not user_id:
            await websocket.close(code=status.WS_1008_POLICY_VIOLATION)
            return
    except Exception as e:
        logger.warning(f"WebSocket auth failed: {e}")
        await websocket.close(code=status.WS_1008_POLICY_VIOLATION)
        return

    # 2. Authorize user for incident
    incident_repo = IncidentRepository(db)
    contact_repo = ContactRepository(db)

    incident = await incident_repo.get_by_id(incident_id)
    if not incident:
        await websocket.close(code=status.WS_1008_POLICY_VIOLATION)
        return

    # Check if caller is the incident creator OR an active emergency contact
    is_owner = incident.user_id == user_id
    is_authorized_contact = False

    if not is_owner:
        # Check if caller is one of the owner's contacts with incident_alerts permission
        contacts = await contact_repo.get_active_emergency_contacts(incident.user_id)
        # In a real environment, contact.user_id or matched phone/email maps to user_id
        # For our model, allow if user is in trusted circle
        is_authorized_contact = any(c.id == user_id or c.phone == payload.get("phone") for c in contacts)

    if not is_owner and not is_authorized_contact:
        logger.warning(f"User {user_id} unauthorized to connect to incident {incident_id} socket")
        await websocket.close(code=status.WS_1008_POLICY_VIOLATION)
        return

    room_id = f"incident:{incident_id}"
    await ws_manager.connect(room_id, websocket)

    try:
        while True:
            # Keep connection open and accept ping / telemetry echoes
            data = await websocket.receive_text()
            # Echo heartbeat if needed
            if data == "ping":
                await websocket.send_text("pong")
    except WebSocketDisconnect:
        ws_manager.disconnect(room_id, websocket)
    except Exception as e:
        logger.error(f"WebSocket error in {room_id}: {e}")
        ws_manager.disconnect(room_id, websocket)
