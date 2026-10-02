from typing import List
from fastapi import APIRouter, Depends, status
from sqlalchemy.ext.asyncio import AsyncSession
from app.core.database import get_db
from app.api.deps import get_current_user
from app.models.user import User
from app.services.incident_service import IncidentService
from app.schemas.incident import IncidentEventCreate, IncidentEventRead
from app.schemas.common import APIResponse

router = APIRouter(prefix="/incidents/{incident_id}/events", tags=["Incident Events"])

@router.post("", response_model=APIResponse[IncidentEventRead], status_code=status.HTTP_201_CREATED)
async def append_incident_event(
    incident_id: str,
    req: IncidentEventCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    service = IncidentService(db)
    incident = await service.get_incident(incident_id, current_user.id)

    event = await service.incident_repo.add_event(
        incident_id=incident_id,
        event_type=req.event_type,
        occurred_at=req.occurred_at,
        payload=req.payload
    )
    return APIResponse(success=True, data=IncidentEventRead.model_validate(event), message="Event recorded")

@router.get("", response_model=APIResponse[List[IncidentEventRead]])
async def list_incident_events(
    incident_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    service = IncidentService(db)
    await service.get_incident(incident_id, current_user.id)

    events = await service.incident_repo.get_events(incident_id)
    return APIResponse(success=True, data=[IncidentEventRead.model_validate(e) for e in events])
