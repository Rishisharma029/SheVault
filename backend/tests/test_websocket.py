import pytest
from starlette.testclient import TestClient
from datetime import datetime, timezone
from app.main import app
from app.core.security import create_access_token
from tests.conftest import TestSessionLocal
from app.repositories.users import UserRepository
from app.repositories.incidents import IncidentRepository


@pytest.mark.asyncio
async def test_websocket_authorization_and_connection():
    # 1. Seed user and incident in database
    async with TestSessionLocal() as session:
        user_repo = UserRepository(session)

        user = await user_repo.create(
            name="WS Test User",
            phone="+19998887777",
            email="wstest@example.com",
            password_hash="testhash"
        )
        
        inc_repo = IncidentRepository(session)
        incident, _ = await inc_repo.create_with_initial_event(
            user_id=user.id,
            session_id="ws_test_session_1",
            activation_method="HOLD",
            started_at=datetime.now(timezone.utc)
        )
        await session.commit()
        
        user_id = user.id
        incident_id = incident.id

    # 2. Test unauthorized connection (invalid token)
    client = TestClient(app)
    with pytest.raises(Exception):
        with client.websocket_connect(f"/api/v1/ws/incidents/{incident_id}?token=invalid_token") as ws:
            pass

    # 3. Test authorized connection (valid token for incident owner)
    valid_token = create_access_token(user_id)
    with client.websocket_connect(f"/api/v1/ws/incidents/{incident_id}?token={valid_token}") as ws:
        ws.send_text("ping")
        data = ws.receive_text()
        assert data == "pong"
