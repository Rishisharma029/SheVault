from datetime import datetime, timedelta, timezone
from typing import Optional, Dict, Any
import jwt
import bcrypt
from app.core.config import settings
from app.core.exceptions import UnauthorizedException

def hash_secret(secret: str) -> str:
    """Securely hash passwords or PINs using native bcrypt."""
    salt = bcrypt.gensalt()
    return bcrypt.hashpw(secret.encode("utf-8")[:72], salt).decode("utf-8")

def verify_secret(plain_secret: str, hashed_secret: str) -> bool:
    """Verify passwords or PINs using native bcrypt with constant-time comparison."""
    if not hashed_secret:
        return False
    try:
        return bcrypt.checkpw(plain_secret.encode("utf-8")[:72], hashed_secret.encode("utf-8"))
    except Exception:
        return False


def create_access_token(user_id: str, session_id: Optional[str] = None) -> str:
    now = datetime.now(timezone.utc)
    expire = now + timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode: Dict[str, Any] = {
        "sub": str(user_id),
        "token_type": "access",
        "iat": int(now.timestamp()),
        "exp": int(expire.timestamp()),
        "session_id": session_id or ""
    }
    return jwt.encode(to_encode, settings.JWT_SECRET, algorithm=settings.ALGORITHM)

def create_refresh_token(user_id: str, session_id: Optional[str] = None) -> str:
    now = datetime.now(timezone.utc)
    expire = now + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS)
    to_encode: Dict[str, Any] = {
        "sub": str(user_id),
        "token_type": "refresh",
        "iat": int(now.timestamp()),
        "exp": int(expire.timestamp()),
        "session_id": session_id or ""
    }
    return jwt.encode(to_encode, settings.JWT_REFRESH_SECRET, algorithm=settings.ALGORITHM)

def decode_token(token: str, is_refresh: bool = False) -> Dict[str, Any]:
    secret = settings.JWT_REFRESH_SECRET if is_refresh else settings.JWT_SECRET
    try:
        payload = jwt.decode(token, secret, algorithms=[settings.ALGORITHM])
        expected_type = "refresh" if is_refresh else "access"
        if payload.get("token_type") != expected_type:
            raise UnauthorizedException(f"Invalid token type; expected {expected_type}")
        return payload
    except jwt.ExpiredSignatureError:
        raise UnauthorizedException("Token has expired")
    except jwt.InvalidTokenError as e:
        raise UnauthorizedException(f"Invalid token: {str(e)}")
