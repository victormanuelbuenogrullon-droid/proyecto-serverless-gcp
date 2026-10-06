from datetime import datetime
from typing import Optional
from pydantic import BaseModel, EmailStr, ConfigDict

class UsuarioBase(BaseModel):
    nombre: str
    apellido: str
    email: EmailStr
    foto: Optional[str] = None

class UsuarioCreacion(UsuarioBase):
    password: str

class UsuarioActualizacion(BaseModel):
    nombre: Optional[str] = None
    apellido: Optional[str] = None
    email: Optional[EmailStr] = None
    password: Optional[str] = None
    foto: Optional[str] = None

class UsuarioRespuesta(UsuarioBase):
    id: int
    createdAt: datetime

    model_config = ConfigDict(from_attributes=True)
