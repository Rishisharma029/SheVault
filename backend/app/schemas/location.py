from datetime import datetime
from typing import Optional, List
from pydantic import BaseModel, Field

class LocationSampleCreate(BaseModel):
    timestamp: datetime
    latitude: float = Field(..., ge=-90.0, le=90.0)
    longitude: float = Field(..., ge=-180.0, le=180.0)
    accuracy: float = Field(..., ge=0.0)
    speed: Optional[float] = Field(default=None, ge=0.0)
    bearing: Optional[float] = Field(default=None, ge=0.0, le=360.0)
    provider: Optional[str] = "FUSED"

class LocationBatchCreate(BaseModel):
    locations: List[LocationSampleCreate]

class LocationSampleRead(BaseModel):
    id: str
    incident_id: str
    timestamp: datetime
    latitude: float
    longitude: float
    accuracy: float
    speed: Optional[float] = None
    bearing: Optional[float] = None
    provider: str
    server_received_at: datetime

    model_config = {"from_attributes": True}


class DeviceStateCreate(BaseModel):
    battery_percent: int = Field(..., ge=0, le=100)
    is_charging: bool = False
    network: str = "CELLULAR"
    connectivity: str = "ONLINE"
    movement_state: str = "WALKING"

class DeviceStateRead(BaseModel):
    battery_percent: int
    is_charging: bool
    network: str
    connectivity: str
    movement_state: str
    updated_at: datetime
