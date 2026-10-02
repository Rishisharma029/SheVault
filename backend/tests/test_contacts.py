import pytest
from httpx import AsyncClient

@pytest.mark.asyncio
async def test_trusted_contacts_and_permissions(client: AsyncClient, registered_user: dict):
    token = registered_user["token"]

    # 1. Add Contact
    add_res = await client.post(
        "/api/v1/contacts",
        headers={"Authorization": f"Bearer {token}"},
        json={
            "name": "Priya Sharma",
            "relationship": "Mother",
            "phone": "+919876543210",
            "priority": "PRIMARY",
            "permissions": {
                "incident_alerts": True,
                "location": True,
                "battery": True,
                "evidence": False
            }
        }
    )
    assert add_res.status_code == 201
    contact = add_res.json()["data"]
    contact_id = contact["id"]
    assert contact["name"] == "Priya Sharma"
    assert contact["permissions"]["location"] is True

    # 2. List contacts
    list_res = await client.get(
        "/api/v1/contacts",
        headers={"Authorization": f"Bearer {token}"}
    )
    assert list_res.status_code == 200
    assert len(list_res.json()["data"]) >= 1

    # 3. Update permissions
    perm_res = await client.put(
        f"/api/v1/contacts/{contact_id}/permissions",
        headers={"Authorization": f"Bearer {token}"},
        json={
            "incident_alerts": True,
            "location": False,
            "battery": True,
            "evidence": True
        }
    )
    assert perm_res.status_code == 200
    assert perm_res.json()["data"]["location"] is False
    assert perm_res.json()["data"]["evidence"] is True
