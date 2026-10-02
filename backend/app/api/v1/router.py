from fastapi import APIRouter
from app.api.v1.auth import router as auth_router
from app.api.v1.users import router as users_router
from app.api.v1.devices import router as devices_router
from app.api.v1.contacts import router as contacts_router
from app.api.v1.incidents import router as incidents_router
from app.api.v1.incident_events import router as events_router
from app.api.v1.locations import router as locations_router
from app.api.v1.checkins import router as checkins_router
from app.api.v1.privacy import router as privacy_router

api_router = APIRouter()

api_router.include_router(auth_router)
api_router.include_router(users_router)
api_router.include_router(devices_router)
api_router.include_router(contacts_router)
api_router.include_router(incidents_router)
api_router.include_router(events_router)
api_router.include_router(locations_router)
api_router.include_router(checkins_router)
api_router.include_router(privacy_router)
