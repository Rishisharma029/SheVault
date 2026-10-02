from datetime import datetime
from typing import Optional, List, Any, Dict
from pydantic import BaseModel, Field

class IncidentLocationInput(BaseModel):
    latitude: float = Field(..., ge=-90.0, le=90.0)
    longitude: float = Field(..., ge=-180.0, le=180.0)
    accuracy: float = Field(..., ge=0.0)
    speed: Optional[float] = None
    bearing: Optional[float] = None

class IncidentCreateRequest(BaseModel):
    client_session_id: str = Field(..., min_length=1, max_length=100)
    device_id: Optional[str] = None
    activation_method: str = Field(default="HOLD") # HOLD, PANIC_GESTURE, CHECK_IN_ESCALATION, SYSTEM
    started_at: datetime
    battery: Optional[int] = Field(default=None, ge=0, le=100)
    location: Optional[IncidentLocationInput] = None
    connectivity_state: Optional[str] = "ONLINE"

class IncidentCreateResponse(BaseModel):
    incident_id: str
    status: str
    server_time: datetime
    accepted: bool = True

class IncidentCancelRequest(BaseModel):
    pin: str = Field(..., min_length=4, max_length=4)

class IncidentDisarmResponse(BaseModel):
    accepted: bool = True
    status: str = "SAFE_CANCELLED"
    server_time: datetime
    message: str = "Safety session terminated"

class IncidentEventCreate(BaseModel):
    event_type: str
    occurred_at: datetime
    payload: Optional[Dict[str, Any]] = None

class IncidentEventRead(BaseModel):
    id: str
    incident_id: str
    event_type: str
    occurred_at: datetime
    sequence_number: int
    payload_json: Optional[str] = None
    created_at: datetime

    model_config = {"from_attributes": True}


class IncidentRead(BaseModel):
    id: str
    user_id: str
    device_id: Optional[str] = None
    session_id: str
    activation_method: str
    status: str
    cancel_type: Optional[str] = None
    started_at: datetime
    grace_started_at: Optional[datetime] = None
    escalated_at: Optional[datetime] = None
    ended_at: Optional[datetime] = None
    initial_battery: Optional[int] = None
    last_battery: Optional[int] = None
    last_latitude: Optional[float] = None
    last_longitude: Optional[float] = None
    last_accuracy: Optional[float] = None
    connectivity_state: str
    created_at: datetime
    updated_at: datetime

    model_config = {"from_attributes": True}


class TelemetryPolicyResponse(BaseModel):
    location_interval_seconds: int = 15
    upload_interval_seconds: int = 10
    movement_mode: str = "ACTIVE"
    audio_enabled: bool = False
