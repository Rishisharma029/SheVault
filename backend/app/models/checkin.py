import uuid
from datetime import datetime
from typing import Optional
from sqlalchemy import String, DateTime, ForeignKey, Index
from sqlalchemy.orm import Mapped, mapped_column, relationship
from app.core.database import Base
from app.utils.time import utc_now

class CheckIn(Base):
    __tablename__ = "checkins"
    __table_args__ = (
        Index("ix_checkins_user_status", "user_id", "status"),
    )

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id: Mapped[str] = mapped_column(String(36), ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    contact_id: Mapped[Optional[str]] = mapped_column(String(36), ForeignKey("trusted_contacts.id", ondelete="SET NULL"), nullable=True)
    
    title: Mapped[str] = mapped_column(String(100), nullable=False)
    destination: Mapped[Optional[str]] = mapped_column(String(200), nullable=True)
    expected_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    started_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now)
    completed_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
    
    status: Mapped[str] = mapped_column(String(20), default="ACTIVE") # ACTIVE, COMPLETED, OVERDUE, CANCELLED
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now)

    user: Mapped["User"] = relationship("User", back_populates="checkins")
