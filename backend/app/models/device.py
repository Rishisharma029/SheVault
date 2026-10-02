import uuid
from datetime import datetime
from typing import Optional
from sqlalchemy import String, DateTime, ForeignKey
from sqlalchemy.orm import Mapped, mapped_column, relationship
from app.core.database import Base
from app.utils.time import utc_now

class Device(Base):
    __tablename__ = "devices"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id: Mapped[str] = mapped_column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    device_uuid: Mapped[str] = mapped_column(String(100), nullable=False, index=True)
    platform: Mapped[str] = mapped_column(String(20), default="ANDROID")
    app_version: Mapped[str] = mapped_column(String(20), default="1.0.0")
    os_version: Mapped[str] = mapped_column(String(50), default="Android 14")
    push_token: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    last_seen_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now)

    user: Mapped["User"] = relationship("User", back_populates="devices")
