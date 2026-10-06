from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base
from app.configuracion import configuracion

if configuracion.URL_BASE_DATOS.startswith("sqlite"):
    motor = create_engine(
        configuracion.URL_BASE_DATOS,
        connect_args={"check_same_thread": False}
    )
else:
    motor = create_engine(configuracion.URL_BASE_DATOS, pool_pre_ping=True)

SesionLocal = sessionmaker(autocommit=False, autoflush=False, bind=motor)

Base = declarative_base()

def obtener_bd():
    bd = SesionLocal()
    try:
        yield bd
    finally:
        bd.close()
