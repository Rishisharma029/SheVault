import uuid
from datetime import datetime
from typing import Optional
from sqlalchemy import String, Text, DateTime, ForeignKey, Index
from sqlalchemy.orm import Mapped, mapped_column
from app.core.database import Base
from app.utils.time import utc_now

class AuditEvent(Base):
    __tablename__ = "audit_events"
    __table_args__ = (
        Index("ix_audit_events_user_action", "user_id", "action"),
    )

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id: Mapped[Optional[str]] = mapped_column(String(36), ForeignKey("users.id", ondelete="SET NULL"), nullable=True, index=True)
    actor_type: Mapped[str] = mapped_column(String(20), default="USER") # USER, SYSTEM, CONTACT, ADMIN
    action: Mapped[str] = mapped_column(String(50), nullable=False)     # LOGIN, PIN_CHANGED, DURESS_PIN_CHANGED, CONTACT_ADDED, CONTACT_REMOVED, INCIDENT_CREATED, INCIDENT_CANCELLED
    resource_type: Mapped[str] = mapped_column(String(50), nullable=False)
    resource_id: Mapped[Optional[str]] = mapped_column(String(36), nullable=True)
    timestamp: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now)
    metadata_json: Mapped[Optional[str]] = mapped_column(Text, nullable=True) # Sensitive values (PINs, raw passwords) must never be passed here
