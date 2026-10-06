import os
import uuid
from typing import List
from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, status
from sqlalchemy.orm import Session
from app.base_datos import obtener_bd
from app.configuracion import configuracion
from app.models.archivo import Archivo
from app.models.usuario import Usuario
from app.models.notificacion import Notificacion
from app.schemas.archivo import RespuestaSubidaExitosa, ArchivoRespuesta
from app.utils.dependencias import obtener_usuario_actual

enrutador = APIRouter(prefix="/upload", tags=["Archivos"])

EXTENSIONES_PERMITIDAS = {".jpg", ".jpeg", ".png", ".webp", ".gif", ".pdf", ".txt", ".doc", ".docx"}
TAMANO_MAXIMO = 10 * 1024 * 1024

def validar_extension(nombre_archivo: str) -> str:
    ext = os.path.splitext(nombre_archivo)[1].lower()
    if ext not in EXTENSIONES_PERMITIDAS:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Extensión no permitida ({ext})."
        )
    return ext

@enrutador.post("", response_model=RespuestaSubidaExitosa, status_code=status.HTTP_201_CREATED)
async def subir_archivo(
    file: UploadFile = File(...),
    bd: Session = Depends(obtener_bd),
    usuario_actual: Usuario = Depends(obtener_usuario_actual)
):
    if not file.filename:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Nombre de archivo inválido."
        )

    validar_extension(file.filename)
    os.makedirs(configuracion.DIRECTORIO_SUBIDAS, exist_ok=True)
    
    nombre_unico = f"{uuid.uuid4().hex[:12]}_{file.filename.replace(' ', '_')}"
    ruta_archivo = os.path.join(configuracion.DIRECTORIO_SUBIDAS, nombre_unico)

    contenido = await file.read()
    tamano_bytes = len(contenido)

    if tamano_bytes > TAMANO_MAXIMO:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="El archivo supera el límite de 10MB."
        )

    with open(ruta_archivo, "wb") as buffer:
        buffer.write(contenido)

    url_publica = f"/uploads/{nombre_unico}"

    db_archivo = Archivo(
        filename=nombre_unico,
        url=url_publica,
        content_type=file.content_type,
        size_bytes=tamano_bytes,
        user_id=usuario_actual.id
    )
    bd.add(db_archivo)
    bd.commit()
    bd.refresh(db_archivo)

    notif = Notificacion(
        title="Archivo subido",
        message=f"{nombre_unico}",
        type="info"
    )
    bd.add(notif)
    bd.commit()

    return RespuestaSubidaExitosa(
        id=db_archivo.id,
        filename=db_archivo.filename,
        url=db_archivo.url
    )

@enrutador.delete("/{id_archivo}", status_code=status.HTTP_200_OK)
def eliminar_archivo(
    id_archivo: int,
    bd: Session = Depends(obtener_bd),
    usuario_actual: Usuario = Depends(obtener_usuario_actual)
):
    db_archivo = bd.query(Archivo).filter(Archivo.id == id_archivo).first()
    if not db_archivo:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Archivo no encontrado."
        )

    ruta_fisica = os.path.join(configuracion.DIRECTORIO_SUBIDAS, db_archivo.filename)
    if os.path.exists(ruta_fisica):
        try:
            os.remove(ruta_fisica)
        except Exception:
            pass

    bd.delete(db_archivo)
    bd.commit()

    return {"message": "Archivo eliminado correctamente."}

@enrutador.get("/list", response_model=List[ArchivoRespuesta])
def listar_archivos(
    skip: int = 0,
    limit: int = 50,
    bd: Session = Depends(obtener_bd),
    usuario_actual: Usuario = Depends(obtener_usuario_actual)
):
    return bd.query(Archivo).offset(skip).limit(limit).all()
