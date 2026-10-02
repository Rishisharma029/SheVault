from typing import Dict, Any
from fastapi import APIRouter, status, Response
from app.core.database import check_database_health
from app.core.redis import redis_manager

router = APIRouter(tags=["Health"])

@router.get("/health")
async def general_health():
    return {"status": "ok"}

@router.get("/health/live")
async def liveness_probe():
    return {"status": "alive"}

@router.get("/health/ready")
async def readiness_probe(response: Response) -> Dict[str, Any]:
    db_ok = await check_database_health()
    redis_ok = await redis_manager.health_check()

    # If DB is not available, service cannot serve traffic
    is_ready = db_ok
    if not is_ready:
        response.status_code = status.HTTP_503_SERVICE_UNAVAILABLE

    return {
        "status": "ready" if is_ready else "degraded",
        "database": "ok" if db_ok else "unreachable",
        "redis": "ok" if redis_ok else "in_memory_fallback"
    }
