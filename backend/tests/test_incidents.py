import pytest
from httpx import AsyncClient
from datetime import datetime, timezone

@pytest.mark.asyncio
async def test_idempotent_incident_creation(client: AsyncClient, registered_user: dict):
    token = registered_user["token"]
    now = datetime.now(timezone.utc).isoformat()

    payload = {
        "client_session_id": "test_session_id_101",
        "activation_method": "HOLD",
        "started_at": now,
        "battery": 88,
        "location": {
            "latitude": 28.6139,
            "longitude": 77.2090,
            "accuracy": 5.0
        }
    }

    # First creation call
    res1 = await client.post(
        "/api/v1/incidents",
        headers={"Authorization": f"Bearer {token}"},
        json=payload
    )
    assert res1.status_code == 201
    data1 = res1.json()["data"]
    inc_id1 = data1["incident_id"]
    assert data1["status"] == "ACTIVE_GRACE"
    assert data1["accepted"] is True

    # Second creation call with identical session_id (idempotent retry)
    res2 = await client.post(
        "/api/v1/incidents",
        headers={"Authorization": f"Bearer {token}"},
        json=payload
    )
    assert res2.status_code == 201
    data2 = res2.json()["data"]
    assert data2["incident_id"] == inc_id1
    assert data2["status"] == "ACTIVE_GRACE"

@pytest.mark.asyncio
async def test_safe_cancellation(client: AsyncClient, registered_user: dict):
    token = registered_user["token"]
    now = datetime.now(timezone.utc).isoformat()

    create_res = await client.post(
        "/api/v1/incidents",
        headers={"Authorization": f"Bearer {token}"},
        json={
            "client_session_id": "test_session_safe_cancel",
            "activation_method": "HOLD",
            "started_at": now
        }
    )
    inc_id = create_res.json()["data"]["incident_id"]

    # Cancel with safe pin
    cancel_res = await client.post(
        f"/api/v1/incidents/{inc_id}/cancel",
        headers={"Authorization": f"Bearer {token}"},
        json={"pin": registered_user["safe_pin"]}
    )
    assert cancel_res.status_code == 200
    body = cancel_res.json()["data"]
    assert body["accepted"] is True
    assert body["status"] == "SAFE_CANCELLED"

    # Verify incident status via GET
    get_res = await client.get(
        f"/api/v1/incidents/{inc_id}",
        headers={"Authorization": f"Bearer {token}"}
    )
    assert get_res.json()["data"]["status"] == "SAFE_CANCELLED"
    assert get_res.json()["data"]["cancel_type"] == "SAFE"

@pytest.mark.asyncio
async def test_covert_duress_cancellation(client: AsyncClient, registered_user: dict):
    token = registered_user["token"]
    now = datetime.now(timezone.utc).isoformat()

    create_res = await client.post(
        "/api/v1/incidents",
        headers={"Authorization": f"Bearer {token}"},
        json={
            "client_session_id": "test_session_duress",
            "activation_method": "HOLD",
            "started_at": now
        }
    )
    inc_id = create_res.json()["data"]["incident_id"]

    # Enter duress pin (9999)
    duress_res = await client.post(
        f"/api/v1/incidents/{inc_id}/duress",
        headers={"Authorization": f"Bearer {token}"},
        json={"pin": registered_user["duress_pin"]}
    )
    assert duress_res.status_code == 200
    client_body = duress_res.json()["data"]

    # Invariant: Client response MUST appear completely legitimate and SAFE
    assert client_body["accepted"] is True
    assert client_body["status"] == "SAFE_CANCELLED"
    assert "duress" not in client_body

    # Invariant: Server internally marked DURESS_CANCELLED
    get_res = await client.get(
        f"/api/v1/incidents/{inc_id}",
        headers={"Authorization": f"Bearer {token}"}
    )
    server_data = get_res.json()["data"]
    assert server_data["status"] == "DURESS_CANCELLED"
    assert server_data["cancel_type"] == "DURESS"
