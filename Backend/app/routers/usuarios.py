from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.base_datos import obtener_bd
from app.models.usuario import Usuario
from app.models.notificacion import Notificacion
from app.schemas.usuario import UsuarioCreacion, UsuarioRespuesta, UsuarioActualizacion
from app.utils.seguridad import obtener_hash_password
from app.utils.dependencias import obtener_usuario_actual

enrutador = APIRouter(prefix="/users", tags=["Usuarios"])

@enrutador.get("", response_model=List[UsuarioRespuesta])
def listar_usuarios(
    skip: int = 0,
    limit: int = 100,
    bd: Session = Depends(obtener_bd),
    usuario_actual: Usuario = Depends(obtener_usuario_actual)
):
    return bd.query(Usuario).offset(skip).limit(limit).all()

@enrutador.get("/{id_usuario}", response_model=UsuarioRespuesta)
def obtener_usuario(
    id_usuario: int,
    bd: Session = Depends(obtener_bd),
    usuario_actual: Usuario = Depends(obtener_usuario_actual)
):
    usuario = bd.query(Usuario).filter(Usuario.id == id_usuario).first()
    if not usuario:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Usuario #{id_usuario} no encontrado."
        )
    return usuario

@enrutador.post("", response_model=UsuarioRespuesta, status_code=status.HTTP_201_CREATED)
def crear_usuario(
    datos: UsuarioCreacion,
    bd: Session = Depends(obtener_bd),
    usuario_actual: Usuario = Depends(obtener_usuario_actual)
):
    existente = bd.query(Usuario).filter(Usuario.email == datos.email).first()
    if existente:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="El correo ya está registrado."
        )
    
    nuevo = Usuario(
        nombre=datos.nombre,
        apellido=datos.apellido,
        email=datos.email,
        password=obtener_hash_password(datos.password),
        foto=datos.foto
    )
    bd.add(nuevo)
    bd.commit()
    bd.refresh(nuevo)

    notif = Notificacion(
        title="Usuario creado",
        message=f"{nuevo.nombre} {nuevo.apellido}",
        type="info"
    )
    bd.add(notif)
    bd.commit()

    return nuevo

@enrutador.put("/{id_usuario}", response_model=UsuarioRespuesta)
def actualizar_usuario(
    id_usuario: int,
    datos: UsuarioActualizacion,
    bd: Session = Depends(obtener_bd),
    usuario_actual: Usuario = Depends(obtener_usuario_actual)
):
    usuario = bd.query(Usuario).filter(Usuario.id == id_usuario).first()
    if not usuario:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Usuario no encontrado."
        )
    
    if datos.nombre is not None:
        usuario.nombre = datos.nombre
    if datos.apellido is not None:
        usuario.apellido = datos.apellido
    if datos.email is not None and datos.email != usuario.email:
        otro = bd.query(Usuario).filter(Usuario.email == datos.email).first()
        if otro and otro.id != id_usuario:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="El correo ya está en uso."
            )
        usuario.email = datos.email
    if datos.password is not None and datos.password.strip():
        usuario.password = obtener_hash_password(datos.password)
    if datos.foto is not None:
        usuario.foto = datos.foto

    bd.commit()
    bd.refresh(usuario)
    return usuario

@enrutador.delete("/{id_usuario}", status_code=status.HTTP_200_OK)
def eliminar_usuario(
    id_usuario: int,
    bd: Session = Depends(obtener_bd),
    usuario_actual: Usuario = Depends(obtener_usuario_actual)
):
    usuario = bd.query(Usuario).filter(Usuario.id == id_usuario).first()
    if not usuario:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Usuario no encontrado."
        )
    
    bd.delete(usuario)
    bd.commit()

    notif = Notificacion(
        title="Usuario eliminado",
        message=f"ID #{id_usuario}",
        type="warning"
    )
    bd.add(notif)
    bd.commit()

    return {"message": "Usuario eliminado exitosamente."}
