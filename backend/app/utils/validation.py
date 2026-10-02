from datetime import datetime, timezone, timedelta
from app.core.exceptions import ValidationException

def validate_coordinates(latitude: float, longitude: float, accuracy: float) -> None:
    if not (-90.0 <= latitude <= 90.0):
        raise ValidationException(f"Invalid latitude '{latitude}'. Must be between -90.0 and +90.0.")
    if not (-180.0 <= longitude <= 180.0):
        raise ValidationException(f"Invalid longitude '{longitude}'. Must be between -180.0 and +180.0.")
    if accuracy < 0.0:
        raise ValidationException(f"Invalid accuracy '{accuracy}'. Must be non-negative.")

def validate_timestamp(ts: datetime, max_future_minutes: int = 5, max_past_days: int = 7) -> None:
    now = datetime.now(timezone.utc)
    if ts.tzinfo is None:
        ts = ts.replace(tzinfo=timezone.utc)
    if ts > now + timedelta(minutes=max_future_minutes):
        raise ValidationException("Timestamp is too far in the future.")
    if ts < now - timedelta(days=max_past_days):
        raise ValidationException("Timestamp is too old to be accepted into active incident buffer.")

def validate_pin_format(pin: str) -> None:
    if not pin.isdigit() or len(pin) != 4:
        raise ValidationException("PIN must be exactly 4 numeric digits.")
