import bcrypt
from datetime import datetime, timedelta, timezone
from typing import Optional
from jose import jwt, JWTError
from app.configuracion import configuracion

def verificar_password(clave_plana: str, clave_hash: str) -> bool:
    try:
        bytes_plano = clave_plana.encode('utf-8')[:72]
        bytes_hash = clave_hash.encode('utf-8')
        return bcrypt.checkpw(bytes_plano, bytes_hash)
    except Exception:
        return False

def obtener_hash_password(clave: str) -> str:
    bytes_clave = clave.encode('utf-8')[:72]
    sal = bcrypt.gensalt()
    hash_bytes = bcrypt.hashpw(bytes_clave, sal)
    return hash_bytes.decode('utf-8')

def crear_token_acceso(datos: dict, expiracion: Optional[timedelta] = None) -> str:
    a_codificar = datos.copy()
    if expiracion:
        vence = datetime.now(timezone.utc) + expiracion
    else:
        vence = datetime.now(timezone.utc) + timedelta(minutes=configuracion.MINUTOS_EXPIRACION_TOKEN)
    
    a_codificar.update({"exp": vence})
    return jwt.encode(a_codificar, configuracion.CLAVE_SECRETA_JWT, algorithm=configuracion.ALGORITMO)

def decodificar_token_acceso(token: str) -> Optional[dict]:
    try:
        return jwt.decode(token, configuracion.CLAVE_SECRETA_JWT, algorithms=[configuracion.ALGORITMO])
    except JWTError:
        return None
