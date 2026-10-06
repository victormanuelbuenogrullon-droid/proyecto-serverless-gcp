from datetime import datetime
from sqlalchemy import Column, Integer, String, DateTime
from sqlalchemy.orm import relationship
from app.base_datos import Base

class Usuario(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    nombre = Column(String(100), nullable=False)
    apellido = Column(String(100), nullable=False)
    email = Column(String(150), unique=True, index=True, nullable=False)
    password = Column(String(255), nullable=False)
    foto = Column(String(255), nullable=True, default=None)
    createdAt = Column(DateTime, default=datetime.utcnow)

    archivos = relationship("Archivo", back_populates="propietario", cascade="all, delete-orphan")
