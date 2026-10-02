from typing import List, Optional
from fastapi import APIRouter, Depends, Header, status
from sqlalchemy.ext.asyncio import AsyncSession
from app.core.database import get_db
from app.api.deps import get_current_user
from app.models.user import User
from app.services.incident_service import IncidentService
from app.schemas.incident import (
    IncidentCreateRequest,
    IncidentCreateResponse,
    IncidentCancelRequest,
    IncidentDisarmResponse,
    IncidentRead,
    TelemetryPolicyResponse
)
from app.schemas.common import APIResponse
from app.utils.time import utc_now

router = APIRouter(prefix="/incidents", tags=["Incidents"])

@router.post("", response_model=APIResponse[IncidentCreateResponse], status_code=status.HTTP_201_CREATED)
async def create_incident(
    req: IncidentCreateRequest,
    idempotency_key: Optional[str] = Header(None, alias="Idempotency-Key"),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    service = IncidentService(db)
    session_id = idempotency_key or req.client_session_id

    loc = req.location
    incident, is_new = await service.create_incident(
        user_id=current_user.id,
        session_id=session_id,
        activation_method=req.activation_method,
        started_at=req.started_at,
        battery=req.battery,
        latitude=loc.latitude if loc else None,
        longitude=loc.longitude if loc else None,
        accuracy=loc.accuracy if loc else None,
        device_id=req.device_id,
        connectivity_state=req.connectivity_state or "ONLINE"
    )

    response_data = IncidentCreateResponse(
        incident_id=incident.id,
        status=incident.status,
        server_time=utc_now(),
        accepted=True
    )
    return APIResponse(
        success=True,
        data=response_data,
        message="Incident created" if is_new else "Incident re-accepted (idempotent)"
    )

@router.get("/{incident_id}", response_model=APIResponse[IncidentRead])
async def get_incident(
    incident_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    service = IncidentService(db)
    incident = await service.get_incident(incident_id, current_user.id)
    return APIResponse(success=True, data=IncidentRead.model_validate(incident))

@router.get("", response_model=APIResponse[List[IncidentRead]])
async def list_incidents(
    limit: int = 50,
    offset: int = 0,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    service = IncidentService(db)
    incidents = await service.incident_repo.list_for_user(current_user.id, limit=limit, offset=offset)
    return APIResponse(success=True, data=[IncidentRead.model_validate(i) for i in incidents])

@router.post("/{incident_id}/cancel", response_model=APIResponse[IncidentDisarmResponse])
async def cancel_incident(
    incident_id: str,
    req: IncidentCancelRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    service = IncidentService(db)
    effective_status, is_duress = await service.cancel_with_pin(incident_id, current_user.id, req.pin)

    # Note: Even if duress, client response is visually and structurally identical to safe cancellation
    return APIResponse(
        success=True,
        data=IncidentDisarmResponse(
            accepted=True,
            status="SAFE_CANCELLED",
            server_time=utc_now(),
            message="Safety session terminated"
        )
    )

@router.post("/{incident_id}/duress", response_model=APIResponse[IncidentDisarmResponse])
async def duress_incident(
    incident_id: str,
    req: IncidentCancelRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    service = IncidentService(db)
    effective_status, is_duress = await service.cancel_with_pin(incident_id, current_user.id, req.pin)

    # Invariant: Never return {"duress": true} to the device
    return APIResponse(
        success=True,
        data=IncidentDisarmResponse(
            accepted=True,
            status="SAFE_CANCELLED",
            server_time=utc_now(),
            message="Safety session terminated"
        )
    )

@router.post("/{incident_id}/escalate", response_model=APIResponse[IncidentRead])
async def escalate_incident(
    incident_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    service = IncidentService(db)
    incident = await service.escalate_incident(incident_id, current_user.id)
    return APIResponse(success=True, data=IncidentRead.model_validate(incident), message="Incident escalated")

@router.post("/{incident_id}/end", response_model=APIResponse[IncidentRead])
async def end_incident(
    incident_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    service = IncidentService(db)
    incident = await service.end_incident(incident_id, current_user.id)
    return APIResponse(success=True, data=IncidentRead.model_validate(incident), message="Incident ended")

@router.get("/{incident_id}/telemetry-policy", response_model=APIResponse[TelemetryPolicyResponse])
async def get_telemetry_policy(
    incident_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    service = IncidentService(db)
    incident = await service.get_incident(incident_id, current_user.id)

    # Dynamic policy based on incident status and battery
    is_critical = incident.status in ["ESCALATING", "ESCALATED", "DURESS_CANCELLED"]
    low_battery = (incident.last_battery or 100) < 20

    loc_interval = 5 if is_critical else (30 if low_battery else 15)
    upload_interval = 5 if is_critical else (20 if low_battery else 10)

    policy = TelemetryPolicyResponse(
        location_interval_seconds=loc_interval,
        upload_interval_seconds=upload_interval,
        movement_mode="HIGH_ACCURACY" if is_critical else "BALANCED",
        audio_enabled=False
    )
    return APIResponse(success=True, data=policy)
