from typing import Optional, List
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, update, delete
from sqlalchemy.orm import selectinload
from app.models.trusted_contact import TrustedContact, ContactPermission
from app.utils.time import utc_now

class ContactRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def get_by_id(self, contact_id: str, user_id: Optional[str] = None) -> Optional[TrustedContact]:
        stmt = select(TrustedContact).options(selectinload(TrustedContact.permissions)).where(TrustedContact.id == contact_id)
        if user_id:
            stmt = stmt.where(TrustedContact.user_id == user_id)
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()

    async def list_for_user(self, user_id: str) -> List[TrustedContact]:
        stmt = (
            select(TrustedContact)
            .options(selectinload(TrustedContact.permissions))
            .where(TrustedContact.user_id == user_id)
            .order_by(TrustedContact.created_at.asc())
        )
        result = await self.session.execute(stmt)
        return list(result.scalars().all())

    async def create(
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
        contact = TrustedContact(
            user_id=user_id,
            name=name.strip(),
            relationship=relationship.strip(),
            phone=phone.strip(),
            priority=priority,
            is_active=True
        )
        self.session.add(contact)
        await self.session.flush()

        permission = ContactPermission(
            contact_id=contact.id,
            incident_alerts=incident_alerts,
            location=location,
            battery=battery,
            evidence=evidence
        )
        self.session.add(permission)
        await self.session.flush()

        contact.permissions = permission
        return contact

    async def update(
        self,
        contact_id: str,
        user_id: str,
        name: Optional[str] = None,
        relationship: Optional[str] = None,
        phone: Optional[str] = None,
        priority: Optional[str] = None,
        is_active: Optional[bool] = None
    ) -> Optional[TrustedContact]:
        contact = await self.get_by_id(contact_id, user_id)
        if not contact:
            return None

        if name is not None:
            contact.name = name.strip()
        if relationship is not None:
            contact.relationship = relationship.strip()
        if phone is not None:
            contact.phone = phone.strip()
        if priority is not None:
            contact.priority = priority
        if is_active is not None:
            contact.is_active = is_active
        contact.updated_at = utc_now()

        await self.session.flush()
        return contact

    async def update_permissions(
        self,
        contact_id: str,
        incident_alerts: Optional[bool] = None,
        location: Optional[bool] = None,
        battery: Optional[bool] = None,
        evidence: Optional[bool] = None
    ) -> Optional[ContactPermission]:
        stmt = select(ContactPermission).where(ContactPermission.contact_id == contact_id)
        result = await self.session.execute(stmt)
        perm = result.scalar_one_or_none()
        if not perm:
            return None

        if incident_alerts is not None:
            perm.incident_alerts = incident_alerts
        if location is not None:
            perm.location = location
        if battery is not None:
            perm.battery = battery
        if evidence is not None:
            perm.evidence = evidence
        perm.updated_at = utc_now()

        await self.session.flush()
        return perm

    async def delete(self, contact_id: str, user_id: str) -> bool:
        stmt = delete(TrustedContact).where(TrustedContact.id == contact_id, TrustedContact.user_id == user_id)
        result = await self.session.execute(stmt)
        return result.rowcount > 0

    async def get_active_emergency_contacts(self, user_id: str) -> List[TrustedContact]:
        stmt = (
            select(TrustedContact)
            .join(ContactPermission, ContactPermission.contact_id == TrustedContact.id)
            .options(selectinload(TrustedContact.permissions))
            .where(
                TrustedContact.user_id == user_id,
                TrustedContact.is_active == True,
                ContactPermission.incident_alerts == True
            )
            .order_by(TrustedContact.priority.asc())
        )
        result = await self.session.execute(stmt)
        return list(result.scalars().all())
