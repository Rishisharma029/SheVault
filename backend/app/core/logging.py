import logging
import sys
import contextvars
from typing import Optional

# Context variable to hold request_id for the current task/request
request_id_ctx: contextvars.ContextVar[Optional[str]] = contextvars.ContextVar("request_id", default=None)

class RequestIDFilter(logging.Filter):
    def filter(self, record: logging.LogRecord) -> bool:
        record.request_id = request_id_ctx.get() or "no-req-id"
        return True

def setup_logging():
    log_format = "%(asctime)s [%(levelname)s] [req:%(request_id)s] %(name)s: %(message)s"
    formatter = logging.Formatter(log_format)

    handler = logging.StreamHandler(sys.stdout)
    handler.setFormatter(formatter)
    handler.addFilter(RequestIDFilter())

    root_logger = logging.getLogger()
    root_logger.setLevel(logging.INFO)
    root_logger.handlers = [handler]

    # Silence overly verbose third-party loggers
    logging.getLogger("uvicorn.access").setLevel(logging.WARNING)

def get_logger(name: str) -> logging.Logger:
    return logging.getLogger(name)
