import os
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from app.configuracion import configuracion
from app.base_datos import Base, motor, SesionLocal
from app.models.usuario import Usuario
from app.models.notificacion import Notificacion
from app.utils.seguridad import obtener_hash_password
from app.routers import autenticacion, usuarios, archivos, panel

Base.metadata.create_all(bind=motor)

def inicializar_datos():
    bd = SesionLocal()
    try:
        if bd.query(Usuario).count() == 0:
            victor = Usuario(
                nombre="Victor",
                apellido="Bueno",
                email="victor.bueno@utesa.edu",
                password=obtener_hash_password("Victor123*"),
                foto=None
            )
            admin = Usuario(
                nombre="Profesor",
                apellido="Evaluador",
                email="profesor@utesa.edu",
                password=obtener_hash_password("Admin123*"),
                foto=None
            )
            bd.add_all([victor, admin])

            notif1 = Notificacion(
                title="Sistema Inicializado",
                message="Servidor activo",
                type="info"
            )
            bd.add(notif1)
            bd.commit()
    except Exception:
        bd.rollback()
    finally:
        bd.close()

inicializar_datos()
os.makedirs(configuracion.DIRECTORIO_SUBIDAS, exist_ok=True)

app = FastAPI(
    title=configuracion.NOMBRE_PROYECTO,
    version=configuracion.VERSION,
    docs_url="/docs",
    redoc_url="/redoc"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.mount("/uploads", StaticFiles(directory=configuracion.DIRECTORIO_SUBIDAS), name="uploads")

app.include_router(autenticacion.enrutador)
app.include_router(usuarios.enrutador)
app.include_router(archivos.enrutador)
app.include_router(panel.enrutador)

@app.get("/")
def inicio():
    return {
        "estado": "online",
        "proyecto": configuracion.NOMBRE_PROYECTO,
        "estudiante": "Victor Manuel Bueno Grullon",
        "matricula": "1177316",
        "docs": "/docs"
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.principal:app", host=configuracion.HOST, port=configuracion.PUERTO, reload=True)
