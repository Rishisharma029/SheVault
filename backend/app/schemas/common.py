from typing import Generic, TypeVar, Optional, Any, Dict
from pydantic import BaseModel, Field

T = TypeVar("T")

class ErrorDetail(BaseModel):
    code: str
    message: str
    request_id: str
    details: Optional[Dict[str, Any]] = None

class ErrorResponse(BaseModel):
    error: ErrorDetail

class APIResponse(BaseModel, Generic[T]):
    success: bool = True
    data: Optional[T] = None
    message: Optional[str] = None
    request_id: Optional[str] = None

class PaginationMeta(BaseModel):
    total: int
    page: int
    page_size: int
    total_pages: int
