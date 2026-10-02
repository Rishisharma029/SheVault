import asyncio
from datetime import datetime, timedelta, timezone
import logging
from sqlalchemy import delete
from app.core.database import AsyncSessionLocal
from app.core.config import settings
from app.models.location_sample import LocationSample
from app.models.delivery_attempt import DeliveryAttempt

logger = logging.getLogger(__name__)

async def run_cleanup_worker():
    """
    Background worker enforcing configured data retention policies.
    """
    logger.info("Starting data retention cleanup worker...")
    while True:
        try:
            now = datetime.now(timezone.utc)
            async with AsyncSessionLocal() as session:
                # 1. Clean location samples older than retention policy
                loc_cutoff = now - timedelta(days=settings.RETENTION_LOCATION_DAYS)
                stmt_loc = delete(LocationSample).where(LocationSample.timestamp < loc_cutoff)
                res_loc = await session.execute(stmt_loc)

                # 2. Clean delivery logs older than retention policy
                deliv_cutoff = now - timedelta(days=settings.RETENTION_DELIVERY_DAYS)
                stmt_deliv = delete(DeliveryAttempt).where(DeliveryAttempt.attempted_at < deliv_cutoff)
                res_deliv = await session.execute(stmt_deliv)

                await session.commit()
                if res_loc.rowcount > 0 or res_deliv.rowcount > 0:
                    logger.info(f"[Retention] Pruned {res_loc.rowcount} location samples and {res_deliv.rowcount} delivery logs.")

            # Run once every 6 hours
            await asyncio.sleep(21600)
        except asyncio.CancelledError:
            logger.info("Cleanup worker stopped.")
            break
        except Exception as e:
            logger.error(f"Error in cleanup worker loop: {e}")
            await asyncio.sleep(60.0)
