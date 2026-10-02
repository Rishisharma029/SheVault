import uuid
from datetime import datetime
from typing import List, Optional
from sqlalchemy import String, DateTime
from sqlalchemy.orm import Mapped, mapped_column, relationship
from app.core.database import Base
from app.utils.time import utc_now

class User(Base):
    __tablename__ = "users"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    name: Mapped[str] = mapped_column(String(100), nullable=False)
    phone: Mapped[str] = mapped_column(String(25), nullable=False, index=True)
    email: Mapped[str] = mapped_column(String(255), unique=True, index=True, nullable=False)
    password_hash: Mapped[str] = mapped_column(String(255), nullable=False)
    safe_pin_hash: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    duress_pin_hash: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    status: Mapped[str] = mapped_column(String(20), default="ACTIVE")
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now)
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now, onupdate=utc_now)

    # Relationships
    devices: Mapped[List["Device"]] = relationship("Device", back_populates="user", cascade="all, delete-orphan")
    trusted_contacts: Mapped[List["TrustedContact"]] = relationship("TrustedContact", back_populates="user", cascade="all, delete-orphan")
    incidents: Mapped[List["Incident"]] = relationship("Incident", back_populates="user")
    checkins: Mapped[List["CheckIn"]] = relationship("CheckIn", back_populates="user")
