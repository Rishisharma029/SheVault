from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from app.core.database import get_db
from app.api.deps import get_current_user
from app.models.user import User
from app.schemas.user import UserRead, UserUpdate
from app.schemas.common import APIResponse
from app.utils.time import utc_now

router = APIRouter(prefix="/users", tags=["Users"])

@router.get("/me", response_model=APIResponse[UserRead])
async def get_user_profile(current_user: User = Depends(get_current_user)):
    user_read = UserRead(
        id=current_user.id,
        name=current_user.name,
        phone=current_user.phone,
        email=current_user.email,
        status=current_user.status,
        has_safe_pin=bool(current_user.safe_pin_hash),
        has_duress_pin=bool(current_user.duress_pin_hash),
        created_at=current_user.created_at,
        updated_at=current_user.updated_at
    )
    return APIResponse(success=True, data=user_read)

@router.put("/me", response_model=APIResponse[UserRead])
async def update_user_profile(
    req: UserUpdate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    if req.name:
        current_user.name = req.name.strip()
    if req.phone:
        current_user.phone = req.phone.strip()
    current_user.updated_at = utc_now()
    await db.flush()

    user_read = UserRead(
        id=current_user.id,
        name=current_user.name,
        phone=current_user.phone,
        email=current_user.email,
        status=current_user.status,
        has_safe_pin=bool(current_user.safe_pin_hash),
        has_duress_pin=bool(current_user.duress_pin_hash),
        created_at=current_user.created_at,
        updated_at=current_user.updated_at
    )
    return APIResponse(success=True, data=user_read, message="Profile updated")
