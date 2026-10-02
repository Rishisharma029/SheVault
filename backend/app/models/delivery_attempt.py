import uuid
from datetime import datetime
from typing import Optional
from sqlalchemy import String, Text, DateTime, ForeignKey, Index
from sqlalchemy.orm import Mapped, mapped_column, relationship
from app.core.database import Base
from app.utils.time import utc_now

class DeliveryAttempt(Base):
    __tablename__ = "delivery_attempts"
    __table_args__ = (
        Index("ix_delivery_incident_status", "incident_id", "status"),
    )

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    incident_id: Mapped[str] = mapped_column(String(36), ForeignKey("incidents.id", ondelete="CASCADE"), nullable=False, index=True)
    recipient_id: Mapped[Optional[str]] = mapped_column(String(36), ForeignKey("trusted_contacts.id", ondelete="SET NULL"), nullable=True)
    
    channel: Mapped[str] = mapped_column(String(20), default="WEBSOCKET") # WEBSOCKET, PUSH, HTTP, SMS, EMAIL, OTHER
    status: Mapped[str] = mapped_column(String(20), default="QUEUED")     # QUEUED, SENDING, ACKNOWLEDGED, FAILED, RETRYING, UNKNOWN
    
    attempted_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now)
    acknowledged_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
    provider_message_id: Mapped[Optional[str]] = mapped_column(String(100), nullable=True)
    error_code: Mapped[Optional[str]] = mapped_column(String(50), nullable=True)
    error_message: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    metadata_json: Mapped[Optional[str]] = mapped_column(Text, nullable=True)

    incident: Mapped["Incident"] = relationship("Incident", back_populates="delivery_attempts")
