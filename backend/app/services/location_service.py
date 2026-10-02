from datetime import datetime
from typing import Optional, List, Dict, Any
from sqlalchemy.ext.asyncio import AsyncSession
from app.repositories.locations import LocationRepository
from app.repositories.incidents import IncidentRepository
from app.utils.validation import validate_coordinates, validate_timestamp
from app.core.exceptions import NotFoundException, ForbiddenException
from app.core.redis import redis_manager, STREAM_NOTIFICATIONS
from app.models.location_sample import LocationSample
from app.utils.time import utc_now

class LocationService:
    def __init__(self, session: AsyncSession):
        self.session = session
        self.location_repo = LocationRepository(session)
        self.incident_repo = IncidentRepository(session)

    async def ingest_location(
        self,
        incident_id: str,
        user_id: str,
        timestamp: datetime,
        latitude: float,
        longitude: float,
        accuracy: float,
        speed: Optional[float] = None,
        bearing: Optional[float] = None,
        provider: str = "FUSED"
    ) -> LocationSample:
        validate_coordinates(latitude, longitude, accuracy)
        validate_timestamp(timestamp)

        incident = await self.incident_repo.get_by_id(incident_id)
        if not incident:
            raise NotFoundException("Incident", incident_id)
        if incident.user_id != user_id:
            raise ForbiddenException("Unauthorized to upload location for this incident")

        sample = await self.location_repo.add_sample(
            incident_id=incident_id,
            timestamp=timestamp,
            latitude=latitude,
            longitude=longitude,
            accuracy=accuracy,
            speed=speed,
            bearing=bearing,
            provider=provider
        )

        # Update latest coordinates on incident
        await self.incident_repo.update_telemetry(
            incident_id=incident_id,
            latitude=latitude,
            longitude=longitude,
            accuracy=accuracy
        )

        # Broadcast real-time location update
        await redis_manager.xadd(STREAM_NOTIFICATIONS, {
            "target_room": f"incident:{incident_id}",
            "event_type": "location.updated",
            "data": {
                "incident_id": incident_id,
                "latitude": latitude,
                "longitude": longitude,
                "accuracy": accuracy,
                "timestamp": timestamp.isoformat()
            }
        })

        return sample

    async def ingest_batch(
        self,
        incident_id: str,
        user_id: str,
        samples: List[Dict[str, Any]]
    ) -> int:
        incident = await self.incident_repo.get_by_id(incident_id)
        if not incident:
            raise NotFoundException("Incident", incident_id)
        if incident.user_id != user_id:
            raise ForbiddenException("Unauthorized to upload locations for this incident")

        valid_samples = []
        for s in samples:
            validate_coordinates(s["latitude"], s["longitude"], s["accuracy"])
            valid_samples.append(s)

        count = await self.location_repo.add_batch(incident_id, valid_samples)

        if valid_samples:
            latest = max(valid_samples, key=lambda x: x["timestamp"])
            await self.incident_repo.update_telemetry(
                incident_id=incident_id,
                latitude=latest["latitude"],
                longitude=latest["longitude"],
                accuracy=latest["accuracy"]
            )

        return count
