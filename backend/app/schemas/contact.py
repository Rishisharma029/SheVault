from datetime import datetime
from typing import Optional
from pydantic import BaseModel, Field

class ContactPermissionsSchema(BaseModel):
    incident_alerts: bool = True
    location: bool = True
    battery: bool = True
    evidence: bool = False

    model_config = {"from_attributes": True}


class ContactCreate(BaseModel):
    name: str = Field(..., min_length=1, max_length=100)
    relationship: str = Field(..., min_length=1, max_length=50)
    phone: str = Field(..., min_length=5, max_length=25)
    priority: str = Field(default="PRIMARY")
    permissions: Optional[ContactPermissionsSchema] = None

class ContactUpdate(BaseModel):
    name: Optional[str] = None
    relationship: Optional[str] = None
    phone: Optional[str] = None
    priority: Optional[str] = None
    is_active: Optional[bool] = None
    permissions: Optional[ContactPermissionsSchema] = None

class ContactRead(BaseModel):
    id: str
    user_id: str
    name: str
    relationship: str
    phone: str
    priority: str
    is_active: bool
    permissions: Optional[ContactPermissionsSchema] = None
    created_at: datetime
    updated_at: datetime

    model_config = {"from_attributes": True}

