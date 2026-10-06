import time
import asyncio
from datetime import datetime
from typing import List
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import func
from app.base_datos import obtener_bd
from app.models.usuario import Usuario
from app.models.archivo import Archivo
from app.models.notificacion import Notificacion
from app.schemas.panel import (
    EstadisticasRespuesta,
    NotificacionRespuesta,
    ElementoTareaSimulada
)
from app.utils.dependencias import obtener_usuario_actual

enrutador = APIRouter(tags=["Panel"])

@enrutador.get("/stats", response_model=EstadisticasRespuesta)
def obtener_estadisticas(
    bd: Session = Depends(obtener_bd),
    usuario_actual: Usuario = Depends(obtener_usuario_actual)
):
    total_usuarios = bd.query(Usuario).count()
    total_archivos = bd.query(Archivo).count()
    almacenamiento = bd.query(func.sum(Archivo.size_bytes)).scalar() or 0

    return EstadisticasRespuesta(
        total_users=total_usuarios,
        total_files=total_archivos,
        storage_used_bytes=int(almacenamiento),
        system_status="ONLINE",
        active_sessions=1,
        server_timestamp=datetime.utcnow()
    )

@enrutador.get("/notifications", response_model=List[NotificacionRespuesta])
def obtener_notificaciones(
    limit: int = 10,
    bd: Session = Depends(obtener_bd),
    usuario_actual: Usuario = Depends(obtener_usuario_actual)
):
    return bd.query(Notificacion).order_by(Notificacion.created_at.desc()).limit(limit).all()

@enrutador.post("/process/simulate-task", response_model=ElementoTareaSimulada)
async def simular_tarea(
    task_id: int,
    delay_ms: int = 400,
    usuario_actual: Usuario = Depends(obtener_usuario_actual)
):
    inicio = time.time()
    await asyncio.sleep(delay_ms / 1000.0)
    duracion = (time.time() - inicio) * 1000.0
    return ElementoTareaSimulada(
        id=task_id,
        name=f"Tarea_{task_id}",
        processed_size=1024 * (task_id + 1),
        duration_ms=round(duracion, 2),
        status="COMPLETED"
    )
