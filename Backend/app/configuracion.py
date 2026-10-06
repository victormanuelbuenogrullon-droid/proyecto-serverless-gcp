import os
from pydantic_settings import BaseSettings

class Configuracion(BaseSettings):
    NOMBRE_PROYECTO: str = "API UTESA Concurrencia"
    VERSION: str = "1.0.0"
    
    CLAVE_SECRETA_JWT: str = os.getenv("SECRET_KEY", "super_secret_jwt_key_utesa_concurrency_2026_victor_bueno")
    ALGORITMO: str = "HS256"
    MINUTOS_EXPIRACION_TOKEN: int = 60 * 24
    
    URL_BASE_DATOS: str = os.getenv("DATABASE_URL", "sqlite:///./app_database.db")
    DIRECTORIO_SUBIDAS: str = os.getenv("UPLOAD_DIR", "uploads")
    
    HOST: str = os.getenv("HOST", "0.0.0.0")
    PUERTO: int = int(os.getenv("PORT", "8000"))

    class Config:
        case_sensitive = True
        env_file = ".env"

configuracion = Configuracion()
