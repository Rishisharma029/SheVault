from datetime import datetime
from typing import Optional, List
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, update
from app.models.checkin import CheckIn
from app.utils.time import utc_now

class CheckInRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def get_by_id(self, checkin_id: str, user_id: Optional[str] = None) -> Optional[CheckIn]:
        stmt = select(CheckIn).where(CheckIn.id == checkin_id)
        if user_id:
            stmt = stmt.where(CheckIn.user_id == user_id)
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()

    async def list_for_user(self, user_id: str) -> List[CheckIn]:
        stmt = select(CheckIn).where(CheckIn.user_id == user_id).order_by(CheckIn.created_at.desc())
        result = await self.session.execute(stmt)
        return list(result.scalars().all())

    async def create(
        self,
        user_id: str,
        title: str,
        expected_at: datetime,
        destination: Optional[str] = None,
        contact_id: Optional[str] = None
    ) -> CheckIn:
        checkin = CheckIn(
            user_id=user_id,
            contact_id=contact_id,
            title=title.strip(),
            destination=destination.strip() if destination else None,
            expected_at=expected_at,
            started_at=utc_now(),
            status="ACTIVE"
        )
        self.session.add(checkin)
        await self.session.flush()
        return checkin

    async def complete(self, checkin_id: str, user_id: str) -> Optional[CheckIn]:
        checkin = await self.get_by_id(checkin_id, user_id)
        if not checkin:
            return None

        checkin.status = "COMPLETED"
        checkin.completed_at = utc_now()
        await self.session.flush()
        return checkin

    async def cancel(self, checkin_id: str, user_id: str) -> Optional[CheckIn]:
        checkin = await self.get_by_id(checkin_id, user_id)
        if not checkin:
            return None

        checkin.status = "CANCELLED"
        checkin.completed_at = utc_now()
        await self.session.flush()
        return checkin

    async def get_overdue_active_checkins(self) -> List[CheckIn]:
        now = utc_now()
        stmt = select(CheckIn).where(CheckIn.status == "ACTIVE", CheckIn.expected_at < now)
        result = await self.session.execute(stmt)
        return list(result.scalars().all())
