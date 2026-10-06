from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.base_datos import obtener_bd
from app.models.usuario import Usuario
from app.models.notificacion import Notificacion
from app.schemas.usuario import UsuarioCreacion, UsuarioRespuesta, UsuarioActualizacion
from app.schemas.token import Token, SolicitudLogin
from app.utils.seguridad import verificar_password, obtener_hash_password, crear_token_acceso
from app.utils.dependencias import obtener_usuario_actual

enrutador = APIRouter(tags=["Autenticación"])

@enrutador.post("/register", response_model=UsuarioRespuesta, status_code=status.HTTP_201_CREATED)
def registrar(datos: UsuarioCreacion, bd: Session = Depends(obtener_bd)):
    existente = bd.query(Usuario).filter(Usuario.email == datos.email).first()
    if existente:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="El correo ya está registrado."
        )
    
    nuevo_usuario = Usuario(
        nombre=datos.nombre,
        apellido=datos.apellido,
        email=datos.email,
        password=obtener_hash_password(datos.password),
        foto=datos.foto
    )
    bd.add(nuevo_usuario)
    bd.commit()
    bd.refresh(nuevo_usuario)

    notif = Notificacion(
        title="Usuario registrado",
        message=f"{nuevo_usuario.nombre} {nuevo_usuario.apellido}",
        type="success"
    )
    bd.add(notif)
    bd.commit()

    return nuevo_usuario

@enrutador.post("/login", response_model=Token)
def iniciar_sesion(solicitud: SolicitudLogin, bd: Session = Depends(obtener_bd)):
    usuario = bd.query(Usuario).filter(Usuario.email == solicitud.email).first()
    if not usuario or not verificar_password(solicitud.password, usuario.password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Correo o contraseña incorrectos."
        )
    
    token = crear_token_acceso(datos={"sub": usuario.email, "id": usuario.id})

    return Token(
        access_token=token,
        token_type="bearer",
        user=UsuarioRespuesta.model_validate(usuario)
    )

@enrutador.get("/profile", response_model=UsuarioRespuesta)
def obtener_perfil(usuario_actual: Usuario = Depends(obtener_usuario_actual)):
    return usuario_actual

@enrutador.put("/profile", response_model=UsuarioRespuesta)
def actualizar_perfil(
    datos: UsuarioActualizacion,
    usuario_actual: Usuario = Depends(obtener_usuario_actual),
    bd: Session = Depends(obtener_bd)
):
    if datos.nombre is not None:
        usuario_actual.nombre = datos.nombre
    if datos.apellido is not None:
        usuario_actual.apellido = datos.apellido
    if datos.email is not None and datos.email != usuario_actual.email:
        otro = bd.query(Usuario).filter(Usuario.email == datos.email).first()
        if otro and otro.id != usuario_actual.id:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="El correo ya se encuentra en uso."
            )
        usuario_actual.email = datos.email
    if datos.password is not None and datos.password.strip():
        usuario_actual.password = obtener_hash_password(datos.password)
    if datos.foto is not None:
        usuario_actual.foto = datos.foto

    bd.commit()
    bd.refresh(usuario_actual)
    return usuario_actual
