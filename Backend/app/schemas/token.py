from typing import Optional
from pydantic import BaseModel, EmailStr
from app.schemas.usuario import UsuarioRespuesta

class Token(BaseModel):
    access_token: str
    token_type: str
    user: UsuarioRespuesta

class DatosToken(BaseModel):
    email: Optional[str] = None
    user_id: Optional[int] = None

class SolicitudLogin(BaseModel):
    email: EmailStr
    password: str
