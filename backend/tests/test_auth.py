import pytest
from httpx import AsyncClient

@pytest.mark.asyncio
async def test_register_and_login(client: AsyncClient):
    # Register
    res = await client.post(
        "/api/v1/auth/register",
        json={
            "name": "Priya Patel",
            "phone": "+919876543299",
            "email": "priya@example.com",
            "password": "StrongPassword123!"
        }
    )
    assert res.status_code == 201
    body = res.json()
    assert body["success"] is True
    assert "access_token" in body["data"]

    # Login
    login_res = await client.post(
        "/api/v1/auth/login",
        json={
            "email": "priya@example.com",
            "password": "StrongPassword123!"
        }
    )
    assert login_res.status_code == 200
    assert "access_token" in login_res.json()["data"]

@pytest.mark.asyncio
async def test_pin_setup_validation(client: AsyncClient):
    reg = await client.post(
        "/api/v1/auth/register",
        json={
            "name": "Alex Vance",
            "phone": "+1234567890",
            "email": "alex@example.com",
            "password": "Password123!"
        }
    )
    token = reg.json()["data"]["access_token"]

    # Attempt identical safe and duress PINs
    identical_res = await client.post(
        "/api/v1/auth/pins",
        headers={"Authorization": f"Bearer {token}"},
        json={"safe_pin": "1234", "duress_pin": "1234"}
    )
    assert identical_res.status_code == 422

    # Valid different PINs
    valid_res = await client.post(
        "/api/v1/auth/pins",
        headers={"Authorization": f"Bearer {token}"},
        json={"safe_pin": "1234", "duress_pin": "9999"}
    )
    assert valid_res.status_code == 200
