from datetime import datetime, timezone

def utc_now() -> datetime:
    """Returns current timezone-aware UTC datetime."""
    return datetime.now(timezone.utc)

def utc_now_iso() -> str:
    """Returns current UTC ISO 8601 string."""
    return utc_now().isoformat()
