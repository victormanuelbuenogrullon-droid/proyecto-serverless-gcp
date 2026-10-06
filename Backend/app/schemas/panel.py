from datetime import datetime
from typing import List
from pydantic import BaseModel, ConfigDict
from app.schemas.usuario import UsuarioRespuesta

class EstadisticasRespuesta(BaseModel):
    total_users: int
    total_files: int
    storage_used_bytes: int
    system_status: str
    active_sessions: int
    server_timestamp: datetime

class NotificacionRespuesta(BaseModel):
    id: int
    title: str
    message: str
    type: str
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)

class ElementoTareaSimulada(BaseModel):
    id: int
    name: str
    processed_size: int
    duration_ms: float
    status: str
