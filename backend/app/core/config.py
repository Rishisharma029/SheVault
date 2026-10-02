from pydantic_settings import BaseSettings
from typing import Optional

class Settings(BaseSettings):
    PROJECT_NAME: str = "SheVault Safety API"
    VERSION: str = "1.0.0"
    API_V1_STR: str = "/api/v1"
    DEBUG: bool = False

    # Security
    JWT_SECRET: str = "shevault-super-secret-jwt-signing-key-change-in-production-2026"
    JWT_REFRESH_SECRET: str = "shevault-super-secret-refresh-key-change-in-production-2026"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60  # 1 hour
    REFRESH_TOKEN_EXPIRE_DAYS: int = 30    # 30 days

    # Database: Default to async SQLite for local/testing, PostgreSQL for production
    DATABASE_URL: str = "sqlite+aiosqlite:///./shevault.db"
    
    # Redis configuration
    REDIS_URL: str = "redis://localhost:6379/0"
    REDIS_ENABLED: bool = True

    # Rate Limiting
    RATE_LIMIT_LOGIN: str = "10/minute"
    RATE_LIMIT_REGISTER: str = "5/minute"
    RATE_LIMIT_PIN: str = "5/minute"
    RATE_LIMIT_INCIDENT_CREATE: str = "30/minute"
    
    # Data Retention (in days)
    RETENTION_LOCATION_DAYS: int = 7
    RETENTION_DELIVERY_DAYS: int = 30
    RETENTION_INCIDENT_DAYS: int = 365
    RETENTION_AUDIT_DAYS: int = 730

    model_config = {
        "env_file": ".env",
        "extra": "ignore"
    }

settings = Settings()
