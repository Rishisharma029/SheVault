from typing import List
from fastapi import APIRouter, Depends, status
from sqlalchemy.ext.asyncio import AsyncSession
from app.core.database import get_db
from app.api.deps import get_current_user
from app.models.user import User
from app.services.contact_service import ContactService
from app.schemas.contact import ContactCreate, ContactUpdate, ContactRead, ContactPermissionsSchema
from app.schemas.common import APIResponse

router = APIRouter(prefix="/contacts", tags=["Trusted Contacts"])

@router.get("", response_model=APIResponse[List[ContactRead]])
async def list_contacts(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    service = ContactService(db)
    contacts = await service.list_contacts(current_user.id)
    return APIResponse(success=True, data=[ContactRead.model_validate(c) for c in contacts])

@router.post("", response_model=APIResponse[ContactRead], status_code=status.HTTP_201_CREATED)
async def create_contact(
    req: ContactCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    service = ContactService(db)
    perms = req.permissions
    contact = await service.add_contact(
        user_id=current_user.id,
        name=req.name,
        relationship=req.relationship,
        phone=req.phone,
        priority=req.priority,
        incident_alerts=perms.incident_alerts if perms else True,
        location=perms.location if perms else True,
        battery=perms.battery if perms else True,
        evidence=perms.evidence if perms else False
    )
    return APIResponse(success=True, data=ContactRead.model_validate(contact), message="Contact added")

@router.put("/{contact_id}", response_model=APIResponse[ContactRead])
async def update_contact(
    contact_id: str,
    req: ContactUpdate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    service = ContactService(db)
    contact = await service.update_contact(
        contact_id=contact_id,
        user_id=current_user.id,
        name=req.name,
        relationship=req.relationship,
        phone=req.phone,
        priority=req.priority,
        is_active=req.is_active
    )
    if req.permissions:
        await service.update_permissions(
            contact_id=contact_id,
            user_id=current_user.id,
            incident_alerts=req.permissions.incident_alerts,
            location=req.permissions.location,
            battery=req.permissions.battery,
            evidence=req.permissions.evidence
        )
    return APIResponse(success=True, data=ContactRead.model_validate(contact), message="Contact updated")

@router.put("/{contact_id}/permissions", response_model=APIResponse[ContactPermissionsSchema])
async def update_contact_permissions(
    contact_id: str,
    req: ContactPermissionsSchema,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    service = ContactService(db)
    perm = await service.update_permissions(
        contact_id=contact_id,
        user_id=current_user.id,
        incident_alerts=req.incident_alerts,
        location=req.location,
        battery=req.battery,
        evidence=req.evidence
    )
    return APIResponse(success=True, data=ContactPermissionsSchema.model_validate(perm), message="Permissions updated")

@router.delete("/{contact_id}", response_model=APIResponse[bool])
async def delete_contact(
    contact_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    service = ContactService(db)
    await service.remove_contact(contact_id, current_user.id)
    return APIResponse(success=True, data=True, message="Contact removed")
