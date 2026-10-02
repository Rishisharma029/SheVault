from datetime import datetime
from typing import Optional, List
from sqlalchemy.ext.asyncio import AsyncSession
from app.repositories.checkins import CheckInRepository
from app.models.checkin import CheckIn
from app.core.exceptions import NotFoundException

class CheckInService:
    def __init__(self, session: AsyncSession):
        self.session = session
        self.checkin_repo = CheckInRepository(session)

    async def create_checkin(
        self,
        user_id: str,
        title: str,
        expected_at: datetime,
        destination: Optional[str] = None,
        contact_id: Optional[str] = None
    ) -> CheckIn:
        return await self.checkin_repo.create(
            user_id=user_id,
            title=title,
            expected_at=expected_at,
            destination=destination,
            contact_id=contact_id
        )

    async def list_checkins(self, user_id: str) -> List[CheckIn]:
        return await self.checkin_repo.list_for_user(user_id)

    async def complete_checkin(self, checkin_id: str, user_id: str) -> CheckIn:
        checkin = await self.checkin_repo.complete(checkin_id, user_id)
        if not checkin:
            raise NotFoundException("CheckIn", checkin_id)
        return checkin

    async def cancel_checkin(self, checkin_id: str, user_id: str) -> CheckIn:
        checkin = await self.checkin_repo.cancel(checkin_id, user_id)
        if not checkin:
            raise NotFoundException("CheckIn", checkin_id)
        return checkin
