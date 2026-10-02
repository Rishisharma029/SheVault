from typing import List, Optional
from sqlalchemy.ext.asyncio import AsyncSession
from app.repositories.contacts import ContactRepository
from app.models.trusted_contact import TrustedContact, ContactPermission
from app.core.exceptions import NotFoundException, ForbiddenException

class ContactService:
    def __init__(self, session: AsyncSession):
        self.session = session
        self.contact_repo = ContactRepository(session)

    async def list_contacts(self, user_id: str) -> List[TrustedContact]:
        return await self.contact_repo.list_for_user(user_id)

    async def add_contact(
        self,
        user_id: str,
        name: str,
        relationship: str,
        phone: str,
        priority: str = "PRIMARY",
        incident_alerts: bool = True,
        location: bool = True,
        battery: bool = True,
        evidence: bool = False
    ) -> TrustedContact:
        return await self.contact_repo.create(
            user_id=user_id,
            name=name,
            relationship=relationship,
            phone=phone,
            priority=priority,
            incident_alerts=incident_alerts,
            location=location,
            battery=battery,
            evidence=evidence
        )

    async def update_contact(
        self,
        contact_id: str,
        user_id: str,
        name: Optional[str] = None,
        relationship: Optional[str] = None,
        phone: Optional[str] = None,
        priority: Optional[str] = None,
        is_active: Optional[bool] = None
    ) -> TrustedContact:
        contact = await self.contact_repo.update(
            contact_id=contact_id,
            user_id=user_id,
            name=name,
            relationship=relationship,
            phone=phone,
            priority=priority,
            is_active=is_active
        )
        if not contact:
            raise NotFoundException("TrustedContact", contact_id)
        return contact

    async def update_permissions(
        self,
        contact_id: str,
        user_id: str,
        incident_alerts: Optional[bool] = None,
        location: Optional[bool] = None,
        battery: Optional[bool] = None,
        evidence: Optional[bool] = None
    ) -> ContactPermission:
        contact = await self.contact_repo.get_by_id(contact_id, user_id)
        if not contact:
            raise NotFoundException("TrustedContact", contact_id)

        perm = await self.contact_repo.update_permissions(
            contact_id=contact_id,
            incident_alerts=incident_alerts,
            location=location,
            battery=battery,
            evidence=evidence
        )
        return perm

    async def remove_contact(self, contact_id: str, user_id: str) -> bool:
        success = await self.contact_repo.delete(contact_id, user_id)
        if not success:
            raise NotFoundException("TrustedContact", contact_id)
        return True
