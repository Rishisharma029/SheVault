from datetime import datetime
from typing import Optional, List
from pydantic import BaseModel, EmailStr

class UserRead(BaseModel):
    id: str
    name: str
    phone: str
    email: EmailStr
    status: str
    has_safe_pin: bool
    has_duress_pin: bool
    created_at: datetime
    updated_at: datetime

    model_config = {"from_attributes": True}


class UserUpdate(BaseModel):
    name: Optional[str] = None
    phone: Optional[str] = None

class DeviceRegisterRequest(BaseModel):
    device_uuid: str
    platform: str = "ANDROID"
    app_version: str = "1.0.0"
    os_version: str = "Android 14"
    push_token: Optional[str] = None

class DeviceRead(BaseModel):
    id: str
    user_id: str
    device_uuid: str
    platform: str
    app_version: str
    os_version: str
    last_seen_at: datetime
    created_at: datetime

    model_config = {"from_attributes": True}

