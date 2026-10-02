from typing import List
from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from app.core.database import get_db
from app.api.deps import get_current_user
from app.models.user import User
from app.repositories.users import UserRepository
from app.schemas.user import DeviceRegisterRequest, DeviceRead
from app.schemas.common import APIResponse

router = APIRouter(prefix="/devices", tags=["Devices"])

@router.post("", response_model=APIResponse[DeviceRead])
async def register_device(
    req: DeviceRegisterRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    user_repo = UserRepository(db)
    device = await user_repo.register_device(
        user_id=current_user.id,
        device_uuid=req.device_uuid,
        platform=req.platform,
        app_version=req.app_version,
        os_version=req.os_version,
        push_token=req.push_token
    )
    return APIResponse(success=True, data=DeviceRead.model_validate(device), message="Device registered")

@router.get("", response_model=APIResponse[List[DeviceRead]])
async def list_devices(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    user_repo = UserRepository(db)
    devices = await user_repo.get_devices(current_user.id)
    return APIResponse(success=True, data=[DeviceRead.model_validate(d) for d in devices])
