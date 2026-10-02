from typing import Optional, List
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, update
from app.models.user import User
from app.models.device import Device
from app.core.security import hash_secret, verify_secret
from app.utils.time import utc_now

class UserRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def get_by_id(self, user_id: str) -> Optional[User]:
        stmt = select(User).where(User.id == user_id)
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()

    async def get_by_email(self, email: str) -> Optional[User]:
        stmt = select(User).where(User.email == email.lower().strip())
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()

    async def create(self, name: str, phone: str, email: str, password_hash: str) -> User:
        user = User(
            name=name.strip(),
            phone=phone.strip(),
            email=email.lower().strip(),
            password_hash=password_hash
        )
        self.session.add(user)
        await self.session.flush()
        return user

    async def set_pins(self, user_id: str, safe_pin_hash: str, duress_pin_hash: str) -> None:
        stmt = (
            update(User)
            .where(User.id == user_id)
            .values(
                safe_pin_hash=safe_pin_hash,
                duress_pin_hash=duress_pin_hash,
                updated_at=utc_now()
            )
        )
        await self.session.execute(stmt)

    async def register_device(
        self,
        user_id: str,
        device_uuid: str,
        platform: str,
        app_version: str,
        os_version: str,
        push_token: Optional[str] = None
    ) -> Device:
        stmt = select(Device).where(Device.user_id == user_id, Device.device_uuid == device_uuid)
        result = await self.session.execute(stmt)
        device = result.scalar_one_or_none()

        if device:
            device.platform = platform
            device.app_version = app_version
            device.os_version = os_version
            if push_token:
                device.push_token = push_token
            device.last_seen_at = utc_now()
        else:
            device = Device(
                user_id=user_id,
                device_uuid=device_uuid,
                platform=platform,
                app_version=app_version,
                os_version=os_version,
                push_token=push_token,
                last_seen_at=utc_now()
            )
            self.session.add(device)

        await self.session.flush()
        return device

    async def get_devices(self, user_id: str) -> List[Device]:
        stmt = select(Device).where(Device.user_id == user_id).order_by(Device.last_seen_at.desc())
        result = await self.session.execute(stmt)
        return list(result.scalars().all())
