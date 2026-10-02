from datetime import datetime
from typing import Optional, List
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from app.models.location_sample import LocationSample
from app.utils.time import utc_now

class LocationRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def add_sample(
        self,
        incident_id: str,
        timestamp: datetime,
        latitude: float,
        longitude: float,
        accuracy: float,
        speed: Optional[float] = None,
        bearing: Optional[float] = None,
        provider: str = "FUSED"
    ) -> LocationSample:
        sample = LocationSample(
            incident_id=incident_id,
            timestamp=timestamp,
            latitude=latitude,
            longitude=longitude,
            accuracy=accuracy,
            speed=speed,
            bearing=bearing,
            provider=provider,
            server_received_at=utc_now()
        )
        self.session.add(sample)
        await self.session.flush()
        return sample

    async def add_batch(
        self,
        incident_id: str,
        samples: List[dict]
    ) -> int:
        now = utc_now()
        db_samples = [
            LocationSample(
                incident_id=incident_id,
                timestamp=s["timestamp"],
                latitude=s["latitude"],
                longitude=s["longitude"],
                accuracy=s["accuracy"],
                speed=s.get("speed"),
                bearing=s.get("bearing"),
                provider=s.get("provider", "FUSED"),
                server_received_at=now
            )
            for s in samples
        ]
        self.session.add_all(db_samples)
        await self.session.flush()
        return len(db_samples)

    async def get_latest(self, incident_id: str) -> Optional[LocationSample]:
        stmt = (
            select(LocationSample)
            .where(LocationSample.incident_id == incident_id)
            .order_by(LocationSample.timestamp.desc())
            .limit(1)
        )
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()

    async def get_history(
        self,
        incident_id: str,
        limit: int = 100,
        offset: int = 0
    ) -> List[LocationSample]:
        stmt = (
            select(LocationSample)
            .where(LocationSample.incident_id == incident_id)
            .order_by(LocationSample.timestamp.desc())
            .limit(limit)
            .offset(offset)
        )
        result = await self.session.execute(stmt)
        return list(result.scalars().all())
