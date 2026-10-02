from typing import Optional, Any, Dict

class SheVaultException(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        status_code: int = 400,
        details: Optional[Dict[str, Any]] = None
    ):
        super().__init__(message)
        self.code = code
        self.message = message
        self.status_code = status_code
        self.details = details or {}

class NotFoundException(SheVaultException):
    def __init__(self, resource: str, identifier: str):
        super().__init__(
            code=f"{resource.upper()}_NOT_FOUND",
            message=f"{resource} with identifier '{identifier}' was not found.",
            status_code=404
        )

class UnauthorizedException(SheVaultException):
    def __init__(self, message: str = "Invalid or expired credentials"):
        super().__init__(
            code="UNAUTHORIZED",
            message=message,
            status_code=401
        )

class ForbiddenException(SheVaultException):
    def __init__(self, message: str = "Access forbidden to requested resource"):
        super().__init__(
            code="FORBIDDEN",
            message=message,
            status_code=403
        )

class InvalidStateTransitionException(SheVaultException):
    def __init__(self, current_state: str, attempted_state: str):
        super().__init__(
            code="INVALID_STATE_TRANSITION",
            message=f"Transition from '{current_state}' to '{attempted_state}' is not permitted by incident protocol.",
            status_code=409,
            details={"current_state": current_state, "attempted_state": attempted_state}
        )

class DuplicateResourceException(SheVaultException):
    def __init__(self, resource: str, field: str, value: str):
        super().__init__(
            code=f"DUPLICATE_{resource.upper()}",
            message=f"{resource} with {field} '{value}' already exists.",
            status_code=409
        )

class ValidationException(SheVaultException):
    def __init__(self, message: str, details: Optional[Dict[str, Any]] = None):
        super().__init__(
            code="VALIDATION_ERROR",
            message=message,
            status_code=422,
            details=details
        )

class RateLimitExceededException(SheVaultException):
    def __init__(self, message: str = "Rate limit exceeded. Please retry later."):
        super().__init__(
            code="RATE_LIMIT_EXCEEDED",
            message=message,
            status_code=429
        )
