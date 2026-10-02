import pytest
from httpx import AsyncClient
from datetime import datetime, timezone

@pytest.mark.asyncio
async def test_location_upload_and_validation(client: AsyncClient, registered_user: dict):
    token = registered_user["token"]
    now = datetime.now(timezone.utc).isoformat()

    create_res = await client.post(
        "/api/v1/incidents",
        headers={"Authorization": f"Bearer {token}"},
        json={
            "client_session_id": "test_session_locations",
            "activation_method": "HOLD",
            "started_at": now
        }
    )
    inc_id = create_res.json()["data"]["incident_id"]

    # Upload valid location
    loc_res = await client.post(
        f"/api/v1/incidents/{inc_id}/locations",
        headers={"Authorization": f"Bearer {token}"},
        json={
            "timestamp": now,
            "latitude": 28.5355,
            "longitude": 77.3910,
            "accuracy": 4.5,
            "speed": 1.2,
            "bearing": 180.0,
            "provider": "FUSED"
        }
    )
    assert loc_res.status_code == 201
    assert loc_res.json()["data"]["latitude"] == 28.5355

    # Fetch latest location
    latest_res = await client.get(
        f"/api/v1/incidents/{inc_id}/locations/latest",
        headers={"Authorization": f"Bearer {token}"}
    )
    assert latest_res.status_code == 200
    assert latest_res.json()["data"]["accuracy"] == 4.5

    # Upload invalid latitude (-95.0) -> validation rejection
    invalid_loc = await client.post(
        f"/api/v1/incidents/{inc_id}/locations",
        headers={"Authorization": f"Bearer {token}"},
        json={
            "timestamp": now,
            "latitude": -95.0,
            "longitude": 77.3910,
            "accuracy": 4.5
        }
    )
    assert invalid_loc.status_code == 422
