from datetime import datetime
from sqlalchemy import Column, Integer, String, DateTime
from app.base_datos import Base

class Notificacion(Base):
    __tablename__ = "notifications"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    title = Column(String(150), nullable=False)
    message = Column(String(500), nullable=False)
    type = Column(String(50), default="info")
    created_at = Column(DateTime, default=datetime.utcnow)
