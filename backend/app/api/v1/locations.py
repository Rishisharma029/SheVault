from typing import List
from fastapi import APIRouter, Depends, status
from sqlalchemy.ext.asyncio import AsyncSession
from app.core.database import get_db
from app.api.deps import get_current_user
from app.models.user import User
from app.services.location_service import LocationService
from app.services.incident_service import IncidentService
from app.schemas.location import (
    LocationSampleCreate,
    LocationBatchCreate,
    LocationSampleRead,
    DeviceStateCreate,
    DeviceStateRead
)
from app.schemas.common import APIResponse
from app.core.exceptions import NotFoundException
from app.utils.time import utc_now

router = APIRouter(prefix="/incidents/{incident_id}", tags=["Location & Telemetry"])

@router.post("/locations", response_model=APIResponse[LocationSampleRead], status_code=status.HTTP_201_CREATED)
async def upload_location(
    incident_id: str,
    req: LocationSampleCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    loc_service = LocationService(db)
    sample = await loc_service.ingest_location(
        incident_id=incident_id,
        user_id=current_user.id,
        timestamp=req.timestamp,
        latitude=req.latitude,
        longitude=req.longitude,
        accuracy=req.accuracy,
        speed=req.speed,
        bearing=req.bearing,
        provider=req.provider or "FUSED"
    )
    return APIResponse(success=True, data=LocationSampleRead.model_validate(sample))

@router.post("/locations/batch", response_model=APIResponse[int], status_code=status.HTTP_201_CREATED)
async def upload_location_batch(
    incident_id: str,
    req: LocationBatchCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    loc_service = LocationService(db)
    samples_data = [s.model_dump() for s in req.locations]
    count = await loc_service.ingest_batch(incident_id, current_user.id, samples_data)
    return APIResponse(success=True, data=count, message=f"Ingested {count} location samples")

@router.get("/locations/latest", response_model=APIResponse[LocationSampleRead])
async def get_latest_location(
    incident_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    loc_service = LocationService(db)
    sample = await loc_service.location_repo.get_latest(incident_id)
    if not sample:
        raise NotFoundException("LocationSample", f"latest for incident {incident_id}")
    return APIResponse(success=True, data=LocationSampleRead.model_validate(sample))

@router.post("/device-state", response_model=APIResponse[DeviceStateRead])
async def update_device_state(
    incident_id: str,
    req: DeviceStateCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    incident_service = IncidentService(db)
    incident = await incident_service.get_incident(incident_id, current_user.id)

    await incident_service.incident_repo.update_telemetry(
        incident_id=incident_id,
        battery=req.battery_percent,
        connectivity_state=req.connectivity
    )

    response_data = DeviceStateRead(
        battery_percent=req.battery_percent,
        is_charging=req.is_charging,
        network=req.network,
        connectivity=req.connectivity,
        movement_state=req.movement_state,
        updated_at=utc_now()
    )
    return APIResponse(success=True, data=response_data, message="Device state recorded")
