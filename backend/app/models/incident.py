import uuid
from datetime import datetime
from typing import Optional, List
from sqlalchemy import String, Integer, Float, DateTime, ForeignKey, UniqueConstraint, Index
from sqlalchemy.orm import Mapped, mapped_column, relationship
from app.core.database import Base
from app.utils.time import utc_now

class Incident(Base):
    __tablename__ = "incidents"
    __table_args__ = (
        UniqueConstraint("user_id", "session_id", name="uq_user_session_id"),
        Index("ix_incidents_user_status", "user_id", "status"),
    )

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id: Mapped[str] = mapped_column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    device_id: Mapped[Optional[str]] = mapped_column(String(36), ForeignKey("devices.id", ondelete="SET NULL"), nullable=True)
    session_id: Mapped[str] = mapped_column(String(100), nullable=False)
    
    activation_method: Mapped[str] = mapped_column(String(30), default="HOLD") # HOLD, PANIC_GESTURE, CHECK_IN_ESCALATION, SYSTEM
    status: Mapped[str] = mapped_column(String(30), default="STARTING") 
    cancel_type: Mapped[Optional[str]] = mapped_column(String(30), nullable=True) # SAFE, DURESS, TIMEOUT, SYSTEM
    
    started_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now)
    grace_started_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
    escalated_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
    ended_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
    
    initial_battery: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    last_battery: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    
    last_latitude: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    last_longitude: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    last_accuracy: Mapped[Optional[float]] = mapped_column(Float, nullable=True)
    
    connectivity_state: Mapped[str] = mapped_column(String(20), default="ONLINE")
    
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now)
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now, onupdate=utc_now)

    user: Mapped["User"] = relationship("User", back_populates="incidents")
    events: Mapped[List["IncidentEvent"]] = relationship("IncidentEvent", back_populates="incident", cascade="all, delete-orphan", order_by="IncidentEvent.sequence_number")
    locations: Mapped[List["LocationSample"]] = relationship("LocationSample", back_populates="incident", cascade="all, delete-orphan", order_by="LocationSample.timestamp")
    delivery_attempts: Mapped[List["DeliveryAttempt"]] = relationship("DeliveryAttempt", back_populates="incident", cascade="all, delete-orphan")
