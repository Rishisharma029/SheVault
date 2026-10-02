from typing import Optional
from pydantic import BaseModel, EmailStr, Field, field_validator

class RegisterRequest(BaseModel):
    name: str = Field(..., min_length=2, max_length=100)
    phone: str = Field(..., min_length=5, max_length=25)
    email: EmailStr
    password: str = Field(..., min_length=8, max_length=128)

class LoginRequest(BaseModel):
    email: EmailStr
    password: str

class TokenResponse(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "Bearer"
    expires_in: int

class RefreshTokenRequest(BaseModel):
    refresh_token: str

class PINSetupRequest(BaseModel):
    safe_pin: str = Field(..., min_length=4, max_length=4)
    duress_pin: str = Field(..., min_length=4, max_length=4)

    @field_validator("safe_pin", "duress_pin")
    @classmethod
    def validate_pin_digits(cls, v: str) -> str:
        if not v.isdigit() or len(v) != 4:
            raise ValueError("PIN must be exactly 4 numeric digits")
        return v

    @field_validator("duress_pin")
    @classmethod
    def validate_different_pins(cls, v: str, info) -> str:
        safe_pin = info.data.get("safe_pin")
        if safe_pin and v == safe_pin:
            raise ValueError("Duress PIN cannot be identical to Safe PIN")
        return v

class PINVerifyRequest(BaseModel):
    pin: str = Field(..., min_length=4, max_length=4)
