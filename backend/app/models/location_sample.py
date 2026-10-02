import uuid
from datetime import datetime
from typing import Optional
from sqlalchemy import String, Float, DateTime, ForeignKey, Index
from sqlalchemy.orm import Mapped, mapped_column, relationship
from app.core.database import Base
from app.utils.time import utc_now

class LocationSample(Base):
    __tablename__ = "location_samples"
    __table_args__ = (
        Index("ix_location_incident_timestamp", "incident_id", "timestamp"),
    )

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    incident_id: Mapped[str] = mapped_column(String(36), ForeignKey("incidents.id", ondelete="CASCADE"), nullable=False, index=True)
    timestamp: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    latitude: Mapped[float] = mapped_column(Float, nullable=False)
    longitude: Mapped[float] = mapped_column(Float, nullable=False)
    accuracy: Mapped[float] = mapped_column(Float, nullable=False)
    speed: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    bearing: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    provider: Mapped[str] = mapped_column(String(20), default="FUSED")
    server_received_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now)

    incident: Mapped["Incident"] = relationship("Incident", back_populates="locations")
