from typing import Dict, Any
from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import delete
from app.core.database import get_db
from app.api.deps import get_current_user
from app.models.user import User
from app.models.incident import Incident
from app.repositories.contacts import ContactRepository
from app.repositories.incidents import IncidentRepository
from app.schemas.common import APIResponse
from app.core.config import settings
from app.core.exceptions import NotFoundException, ForbiddenException
from app.utils.time import utc_now

router = APIRouter(prefix="/privacy", tags=["Privacy & GDPR"])

@router.get("/export", response_model=APIResponse[Dict[str, Any]])
async def export_user_data(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    contact_repo = ContactRepository(db)
    incident_repo = IncidentRepository(db)

    contacts = await contact_repo.list_for_user(current_user.id)
    incidents = await incident_repo.list_for_user(current_user.id, limit=500)

    data = {
        "user": {
            "id": current_user.id,
            "name": current_user.name,
            "email": current_user.email,
            "phone": current_user.phone,
            "created_at": current_user.created_at.isoformat()
        },
        "trusted_contacts": [
            {
                "name": c.name,
                "relationship": c.relationship,
                "phone": c.phone,
                "priority": c.priority
            }
            for c in contacts
        ],
        "incidents": [
            {
                "id": i.id,
                "session_id": i.session_id,
                "status": i.status,
                "started_at": i.started_at.isoformat(),
                "ended_at": i.ended_at.isoformat() if i.ended_at else None
            }
            for i in incidents
        ]
    }
    return APIResponse(success=True, data=data, message="User data export generated")

@router.delete("/incidents/{incident_id}", response_model=APIResponse[bool])
async def delete_incident_data(
    incident_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    incident_repo = IncidentRepository(db)
    incident = await incident_repo.get_by_id(incident_id)
    if not incident:
        raise NotFoundException("Incident", incident_id)
    if incident.user_id != current_user.id:
        raise ForbiddenException("Unauthorized to delete this incident")

    stmt = delete(Incident).where(Incident.id == incident_id)
    await db.execute(stmt)
    return APIResponse(success=True, data=True, message="Incident record purged")

@router.delete("/account", response_model=APIResponse[bool])
async def delete_account(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    current_user.status = "DELETED"
    current_user.updated_at = utc_now()
    return APIResponse(success=True, data=True, message="Account scheduled for permanent deactivation")

@router.get("/retention", response_model=APIResponse[Dict[str, int]])
async def get_retention_policy():
    policy = {
        "location_retention_days": settings.RETENTION_LOCATION_DAYS,
        "delivery_log_retention_days": settings.RETENTION_DELIVERY_DAYS,
        "incident_retention_days": settings.RETENTION_INCIDENT_DAYS,
        "audit_retention_days": settings.RETENTION_AUDIT_DAYS
    }
    return APIResponse(success=True, data=policy)
