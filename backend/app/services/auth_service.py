from typing import Tuple, Optional
from sqlalchemy.ext.asyncio import AsyncSession
from app.repositories.users import UserRepository
from app.core.security import hash_secret, verify_secret, create_access_token, create_refresh_token, decode_token
from app.core.exceptions import UnauthorizedException, DuplicateResourceException, NotFoundException, ValidationException
from app.models.user import User
from app.models.device import Device
from app.core.config import settings

class AuthService:
    def __init__(self, session: AsyncSession):
        self.session = session
        self.user_repo = UserRepository(session)

    async def register(self, name: str, phone: str, email: str, password: str) -> Tuple[User, str, str]:
        existing = await self.user_repo.get_by_email(email)
        if existing:
            raise DuplicateResourceException("user", "email", email)

        pw_hash = hash_secret(password)
        user = await self.user_repo.create(name, phone, email, pw_hash)
        
        access_token = create_access_token(user.id)
        refresh_token = create_refresh_token(user.id)
        return user, access_token, refresh_token

    async def login(self, email: str, password: str) -> Tuple[User, str, str]:
        user = await self.user_repo.get_by_email(email)
        if not user or not verify_secret(password, user.password_hash):
            raise UnauthorizedException("Invalid email or password")

        access_token = create_access_token(user.id)
        refresh_token = create_refresh_token(user.id)
        return user, access_token, refresh_token

    async def refresh_tokens(self, refresh_token_str: str) -> Tuple[str, str]:
        payload = decode_token(refresh_token_str, is_refresh=True)
        user_id = payload.get("sub")
        if not user_id:
            raise UnauthorizedException("Invalid token payload")

        user = await self.user_repo.get_by_id(user_id)
        if not user or user.status != "ACTIVE":
            raise UnauthorizedException("User account is inactive or not found")

        new_access = create_access_token(user.id)
        new_refresh = create_refresh_token(user.id)
        return new_access, new_refresh

    async def setup_pins(self, user_id: str, safe_pin: str, duress_pin: str) -> None:
        user = await self.user_repo.get_by_id(user_id)
        if not user:
            raise NotFoundException("User", user_id)

        if safe_pin == duress_pin:
            raise ValidationException("Safe PIN and Duress PIN must be distinct")

        safe_hash = hash_secret(safe_pin)
        duress_hash = hash_secret(duress_pin)
        await self.user_repo.set_pins(user_id, safe_hash, duress_hash)

    async def register_device(
        self,
        user_id: str,
        device_uuid: str,
        platform: str,
        app_version: str,
        os_version: str,
        push_token: Optional[str] = None
    ) -> Device:
        return await self.user_repo.register_device(
            user_id=user_id,
            device_uuid=device_uuid,
            platform=platform,
            app_version=app_version,
            os_version=os_version,
            push_token=push_token
        )
