from fastapi import APIRouter, Depends, status
from sqlalchemy.ext.asyncio import AsyncSession
from app.core.database import get_db
from app.services.auth_service import AuthService
from app.api.deps import get_current_user
from app.models.user import User
from app.schemas.auth import (
    RegisterRequest,
    LoginRequest,
    TokenResponse,
    RefreshTokenRequest,
    PINSetupRequest
)
from app.schemas.user import UserRead
from app.schemas.common import APIResponse
from app.core.config import settings

router = APIRouter(prefix="/auth", tags=["Authentication"])

@router.post("/register", response_model=APIResponse[TokenResponse], status_code=status.HTTP_201_CREATED)
async def register(req: RegisterRequest, db: AsyncSession = Depends(get_db)):
    auth_service = AuthService(db)
    user, access_token, refresh_token = await auth_service.register(
        name=req.name,
        phone=req.phone,
        email=req.email,
        password=req.password
    )
    return APIResponse(
        success=True,
        data=TokenResponse(
            access_token=access_token,
            refresh_token=refresh_token,
            token_type="Bearer",
            expires_in=settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60
        ),
        message="User registered successfully"
    )

@router.post("/login", response_model=APIResponse[TokenResponse])
async def login(req: LoginRequest, db: AsyncSession = Depends(get_db)):
    auth_service = AuthService(db)
    user, access_token, refresh_token = await auth_service.login(
        email=req.email,
        password=req.password
    )
    return APIResponse(
        success=True,
        data=TokenResponse(
            access_token=access_token,
            refresh_token=refresh_token,
            token_type="Bearer",
            expires_in=settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60
        ),
        message="Login successful"
    )

@router.post("/refresh", response_model=APIResponse[TokenResponse])
async def refresh_token(req: RefreshTokenRequest, db: AsyncSession = Depends(get_db)):
    auth_service = AuthService(db)
    new_access, new_refresh = await auth_service.refresh_tokens(req.refresh_token)
    return APIResponse(
        success=True,
        data=TokenResponse(
            access_token=new_access,
            refresh_token=new_refresh,
            token_type="Bearer",
            expires_in=settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60
        ),
        message="Token refreshed"
    )

@router.post("/logout", response_model=APIResponse[bool])
async def logout(current_user: User = Depends(get_current_user)):
    # In stateless JWT, client deletes tokens; Redis blocklist can be added if needed
    return APIResponse(success=True, data=True, message="Logged out successfully")

@router.get("/me", response_model=APIResponse[UserRead])
async def get_me(current_user: User = Depends(get_current_user)):
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

@router.post("/pins", response_model=APIResponse[bool])
async def setup_pins(
    req: PINSetupRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    auth_service = AuthService(db)
    await auth_service.setup_pins(current_user.id, req.safe_pin, req.duress_pin)
    return APIResponse(success=True, data=True, message="Safe and Duress PINs configured successfully")
