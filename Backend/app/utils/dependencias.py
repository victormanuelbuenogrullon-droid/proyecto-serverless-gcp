from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.orm import Session
from app.base_datos import obtener_bd
from app.models.usuario import Usuario
from app.utils.seguridad import decodificar_token_acceso

seguridad_bearer = HTTPBearer()

def obtener_usuario_actual(
    credenciales: HTTPAuthorizationCredentials = Depends(seguridad_bearer),
    bd: Session = Depends(obtener_bd)
) -> Usuario:
    token = credenciales.credentials
    excepcion_autenticacion = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Token inválido o expirado",
        headers={"WWW-Authenticate": "Bearer"},
    )
    
    payload = decodificar_token_acceso(token)
    if payload is None:
        raise excepcion_autenticacion
        
    email: str = payload.get("sub")
    if email is None:
        raise excepcion_autenticacion
        
    usuario = bd.query(Usuario).filter(Usuario.email == email).first()
    if usuario is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Usuario no encontrado"
        )
    return usuario
