from datetime import datetime
from typing import Optional
from pydantic import BaseModel, Field

class CheckInCreate(BaseModel):
    title: str = Field(..., min_length=1, max_length=100)
    destination: Optional[str] = None
    expected_at: datetime
    contact_id: Optional[str] = None

class CheckInUpdate(BaseModel):
    status: Optional[str] = None # ACTIVE, COMPLETED, OVERDUE, CANCELLED
    completed_at: Optional[datetime] = None

class CheckInRead(BaseModel):
    id: str
    user_id: str
    contact_id: Optional[str] = None
    title: str
    destination: Optional[str] = None
    expected_at: datetime
    started_at: datetime
    completed_at: Optional[datetime] = None
    status: str
    created_at: datetime

    model_config = {"from_attributes": True}

