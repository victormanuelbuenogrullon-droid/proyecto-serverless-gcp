from datetime import datetime
from typing import Optional
from pydantic import BaseModel, ConfigDict

class ArchivoRespuesta(BaseModel):
    id: int
    filename: str
    url: str
    content_type: Optional[str] = None
    size_bytes: Optional[int] = None
    created_at: Optional[datetime] = None

    model_config = ConfigDict(from_attributes=True)

class RespuestaSubidaExitosa(BaseModel):
    id: int
    filename: str
    url: str
