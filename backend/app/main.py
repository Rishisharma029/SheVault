import asyncio
import time
from contextlib import asynccontextmanager
from typing import AsyncGenerator
from fastapi import FastAPI, Request, status
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from fastapi.exceptions import RequestValidationError
from starlette.exceptions import HTTPException as StarletteHTTPException

from app.core.config import settings
from app.core.logging import setup_logging, get_logger, request_id_ctx
from app.core.redis import redis_manager
from app.core.exceptions import SheVaultException
from app.utils.ids import generate_request_id

from app.api.v1.router import api_router
from app.api.v1.health import router as health_router
from app.websocket.incident_socket import router as ws_router

from app.workers.incident_worker import run_incident_worker
from app.workers.delivery_worker import run_delivery_worker
from app.workers.cleanup_worker import run_cleanup_worker

setup_logging()
logger = get_logger("app.main")

@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncGenerator[None, None]:
    logger.info("Initializing SheVault Safety Backend...")
    # Connect to Redis / in-memory fallback
    await redis_manager.connect()

    # Ensure tables exist
    from app.core.database import engine, Base
    import app.models  # noqa
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    logger.info("Database schemas verified.")

    # Launch background stream workers
    incident_task = asyncio.create_task(run_incident_worker())
    delivery_task = asyncio.create_task(run_delivery_worker())
    cleanup_task = asyncio.create_task(run_cleanup_worker())

    yield

    logger.info("Shutting down background workers...")
    incident_task.cancel()
    delivery_task.cancel()
    cleanup_task.cancel()
    await redis_manager.disconnect()
    logger.info("SheVault shutdown complete.")

app = FastAPI(
    title=settings.PROJECT_NAME,
    version=settings.VERSION,
    lifespan=lifespan,
    docs_url="/docs",
    redoc_url="/redoc"
)

# CORS Configuration (supporting React guardian dashboard & mobile clients)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
    expose_headers=["X-Request-ID"]
)

# Request ID & Logging Middleware
@app.middleware("http")
async def request_id_middleware(request: Request, call_next):
    req_id = request.headers.get("X-Request-ID") or generate_request_id()
    token = request_id_ctx.set(req_id)
    
    start_time = time.time()
    try:
        response = await call_next(request)
        duration_ms = (time.time() - start_time) * 1000
        response.headers["X-Request-ID"] = req_id
        logger.info(
            f"{request.method} {request.url.path} -> {response.status_code} ({duration_ms:.1f}ms)"
        )
        return response
    finally:
        request_id_ctx.reset(token)

# Standardized Error Handling
@app.exception_handler(SheVaultException)
async def shevault_exception_handler(request: Request, exc: SheVaultException):
    req_id = request_id_ctx.get() or "no-req-id"
    logger.warning(f"Business logic exception [{exc.code}]: {exc.message}")
    return JSONResponse(
        status_code=exc.status_code,
        headers={"X-Request-ID": req_id},
        content={
            "error": {
                "code": exc.code,
                "message": exc.message,
                "request_id": req_id,
                "details": exc.details
            }
        }
    )

from fastapi.encoders import jsonable_encoder

@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    req_id = request_id_ctx.get() or "no-req-id"
    logger.warning(f"Validation error: {exc.errors()}")
    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        headers={"X-Request-ID": req_id},
        content={
            "error": {
                "code": "VALIDATION_ERROR",
                "message": "The request body or parameters failed schema validation.",
                "request_id": req_id,
                "details": jsonable_encoder(exc.errors())
            }
        }
    )


@app.exception_handler(StarletteHTTPException)
async def http_exception_handler(request: Request, exc: StarletteHTTPException):
    req_id = request_id_ctx.get() or "no-req-id"
    return JSONResponse(
        status_code=exc.status_code,
        headers={"X-Request-ID": req_id},
        content={
            "error": {
                "code": f"HTTP_{exc.status_code}",
                "message": exc.detail,
                "request_id": req_id
            }
        }
    )

@app.exception_handler(Exception)
async def generic_exception_handler(request: Request, exc: Exception):
    req_id = request_id_ctx.get() or "no-req-id"
    logger.error(f"Unhandled server error: {exc}", exc_info=True)
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        headers={"X-Request-ID": req_id},
        content={
            "error": {
                "code": "INTERNAL_SERVER_ERROR",
                "message": "An unexpected server error occurred. Our safety reliability team has been notified.",
                "request_id": req_id
            }
        }
    )

# Mount Routes
app.include_router(health_router)
app.include_router(api_router, prefix=settings.API_V1_STR)
app.include_router(ws_router, prefix=settings.API_V1_STR)
