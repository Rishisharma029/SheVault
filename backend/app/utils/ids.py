import uuid

def generate_uuid() -> str:
    """Returns a standard UUID4 string."""
    return str(uuid.uuid4())

def generate_request_id() -> str:
    """Returns an X-Request-ID prefixed string."""
    return f"req_{uuid.uuid4().hex[:12]}"
