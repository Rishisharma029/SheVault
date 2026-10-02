import uuid
from datetime import datetime
from typing import Optional
from sqlalchemy import String, Integer, Text, DateTime, ForeignKey, Index
from sqlalchemy.orm import Mapped, mapped_column, relationship
from app.core.database import Base
from app.utils.time import utc_now

class IncidentEvent(Base):
    __tablename__ = "incident_events"
    __table_args__ = (
        Index("ix_incident_events_seq", "incident_id", "sequence_number"),
    )

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    incident_id: Mapped[str] = mapped_column(String(36), ForeignKey("incidents.id", ondelete="CASCADE"), nullable=False, index=True)
    event_type: Mapped[str] = mapped_column(String(50), nullable=False)
    occurred_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now)
    sequence_number: Mapped[int] = mapped_column(Integer, nullable=False, default=1)
    payload_json: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now)

    incident: Mapped["Incident"] = relationship("Incident", back_populates="events")
