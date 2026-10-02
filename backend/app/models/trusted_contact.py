import uuid
from datetime import datetime
from typing import Optional, List
from sqlalchemy import String, Boolean, DateTime, ForeignKey
from sqlalchemy.orm import Mapped, mapped_column, relationship as orm_relationship
from app.core.database import Base
from app.utils.time import utc_now

class TrustedContact(Base):
    __tablename__ = "trusted_contacts"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id: Mapped[str] = mapped_column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    name: Mapped[str] = mapped_column(String(100), nullable=False)
    relationship: Mapped[str] = mapped_column(String(50), nullable=False)
    phone: Mapped[str] = mapped_column(String(25), nullable=False)
    priority: Mapped[str] = mapped_column(String(20), default="PRIMARY") # PRIMARY, SECONDARY, TERTIARY
    is_active: Mapped[bool] = mapped_column(Boolean, default=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now)
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now, onupdate=utc_now)

    user: Mapped["User"] = orm_relationship("User", back_populates="trusted_contacts")
    permissions: Mapped[Optional["ContactPermission"]] = orm_relationship(
        "ContactPermission",
        back_populates="contact",

        uselist=False,
        cascade="all, delete-orphan"
    )

class ContactPermission(Base):
    __tablename__ = "contact_permissions"

    contact_id: Mapped[str] = mapped_column(String(36), ForeignKey("trusted_contacts.id", ondelete="CASCADE"), primary_key=True)
    incident_alerts: Mapped[bool] = mapped_column(Boolean, default=True)
    location: Mapped[bool] = mapped_column(Boolean, default=True)
    battery: Mapped[bool] = mapped_column(Boolean, default=True)
    evidence: Mapped[bool] = mapped_column(Boolean, default=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now)
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now, onupdate=utc_now)

    contact: Mapped["TrustedContact"] = orm_relationship("TrustedContact", back_populates="permissions")
