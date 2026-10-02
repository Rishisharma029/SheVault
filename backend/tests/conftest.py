import sys
import os
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

import pytest
import pytest_asyncio

import asyncio
from typing import AsyncGenerator
from httpx import AsyncClient, ASGITransport
from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker, AsyncSession

from app.core.database import Base, get_db
from app.core.config import settings
from app.main import app
from app.core.security import hash_secret
from app.models.user import User

TEST_DATABASE_URL = "sqlite+aiosqlite:///./test_shevault.db"

test_engine = create_async_engine(TEST_DATABASE_URL, echo=False)

TestSessionLocal = async_sessionmaker(
    bind=test_engine,
    class_=AsyncSession,
    expire_on_commit=False,
    autocommit=False,
    autoflush=False
)

@pytest_asyncio.fixture(scope="session")
def event_loop():
    loop = asyncio.new_event_loop()
    yield loop
    loop.close()

@pytest_asyncio.fixture(autouse=True)
async def init_db():
    async with test_engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    yield
    async with test_engine.begin() as conn:
        await conn.run_sync(Base.metadata.drop_all)

async def override_get_db() -> AsyncGenerator[AsyncSession, None]:
    async with TestSessionLocal() as session:
        try:
            yield session
            await session.commit()
        except Exception:
            await session.rollback()
            raise
        finally:
            await session.close()

app.dependency_overrides[get_db] = override_get_db

@pytest_asyncio.fixture
async def client() -> AsyncGenerator[AsyncClient, None]:
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        yield ac

@pytest_asyncio.fixture
async def registered_user(client: AsyncClient) -> dict:
    reg_res = await client.post(
        "/api/v1/auth/register",
        json={
            "name": "Sarah Miller",
            "phone": "+15551234567",
            "email": "sarah@example.com",
            "password": "Password123!"
        }
    )
    data = reg_res.json()["data"]
    token = data["access_token"]

    # Set up safe pin 1234 and duress pin 9999
    await client.post(
        "/api/v1/auth/pins",
        headers={"Authorization": f"Bearer {token}"},
        json={"safe_pin": "1234", "duress_pin": "9999"}
    )

    return {
        "email": "sarah@example.com",
        "token": token,
        "safe_pin": "1234",
        "duress_pin": "9999"
    }
