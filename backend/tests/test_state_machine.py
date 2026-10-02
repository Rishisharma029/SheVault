import pytest
from httpx import AsyncClient
from datetime import datetime, timezone

@pytest.mark.asyncio
async def test_invalid_state_transition_rejected(client: AsyncClient, registered_user: dict):
    token = registered_user["token"]
    now = datetime.now(timezone.utc).isoformat()

    create_res = await client.post(
        "/api/v1/incidents",
        headers={"Authorization": f"Bearer {token}"},
        json={
            "client_session_id": "test_transition_violation",
            "activation_method": "HOLD",
            "started_at": now
        }
    )
    inc_id = create_res.json()["data"]["incident_id"]

    # End the incident (ACTIVE_GRACE -> ENDED is invalid; must go ESCALATED -> ENDED, or cancel)
    invalid_end_res = await client.post(
        f"/api/v1/incidents/{inc_id}/end",
        headers={"Authorization": f"Bearer {token}"}
    )
    assert invalid_end_res.status_code == 409
    assert invalid_end_res.json()["error"]["code"] == "INVALID_STATE_TRANSITION"

    # Now escalate properly (ACTIVE_GRACE -> ESCALATING/ESCALATED)
    escalate_res = await client.post(
        f"/api/v1/incidents/{inc_id}/escalate",
        headers={"Authorization": f"Bearer {token}"}
    )
    assert escalate_res.status_code == 200
    assert escalate_res.json()["data"]["status"] == "ESCALATED"

    # From ESCALATED -> ENDED is valid!
    end_res = await client.post(
        f"/api/v1/incidents/{inc_id}/end",
        headers={"Authorization": f"Bearer {token}"}
    )
    assert end_res.status_code == 200
    assert end_res.json()["data"]["status"] == "ENDED"

    # Attempting to re-escalate an ENDED incident MUST fail with 409
    re_escalate_res = await client.post(
        f"/api/v1/incidents/{inc_id}/escalate",
        headers={"Authorization": f"Bearer {token}"}
    )
    assert re_escalate_res.status_code == 409
    assert re_escalate_res.json()["error"]["code"] == "INVALID_STATE_TRANSITION"
