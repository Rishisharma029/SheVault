from typing import List
from fastapi import APIRouter, Depends, status
from sqlalchemy.ext.asyncio import AsyncSession
from app.core.database import get_db
from app.api.deps import get_current_user
from app.models.user import User
from app.services.checkin_service import CheckInService
from app.schemas.checkin import CheckInCreate, CheckInRead
from app.schemas.common import APIResponse

router = APIRouter(prefix="/checkins", tags=["Safety Check-Ins"])

@router.post("", response_model=APIResponse[CheckInRead], status_code=status.HTTP_201_CREATED)
async def create_checkin(
    req: CheckInCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    service = CheckInService(db)
    checkin = await service.create_checkin(
        user_id=current_user.id,
        title=req.title,
        expected_at=req.expected_at,
        destination=req.destination,
        contact_id=req.contact_id
    )
    return APIResponse(success=True, data=CheckInRead.model_validate(checkin), message="Check-in created")

@router.get("", response_model=APIResponse[List[CheckInRead]])
async def list_checkins(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    service = CheckInService(db)
    checkins = await service.list_checkins(current_user.id)
    return APIResponse(success=True, data=[CheckInRead.model_validate(c) for c in checkins])

@router.post("/{checkin_id}/complete", response_model=APIResponse[CheckInRead])
async def complete_checkin(
    checkin_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    service = CheckInService(db)
    checkin = await service.complete_checkin(checkin_id, current_user.id)
    return APIResponse(success=True, data=CheckInRead.model_validate(checkin), message="Check-in completed")

@router.post("/{checkin_id}/cancel", response_model=APIResponse[CheckInRead])
async def cancel_checkin(
    checkin_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    service = CheckInService(db)
    checkin = await service.cancel_checkin(checkin_id, current_user.id)
    return APIResponse(success=True, data=CheckInRead.model_validate(checkin), message="Check-in cancelled")
