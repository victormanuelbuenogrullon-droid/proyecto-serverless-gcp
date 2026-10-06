# Aplicación Móvil Nativa (Android Kotlin) + API REST (FastAPI)

**Estudiante:** Victor Manuel Bueno Grullón  
**Matrícula:** 1177316  
**Tecnología Móvil:** Android Kotlin  
**Tecnología Backend:** Python + FastAPI  
**Asignatura:** Desarrollo Móvil / Programación Concurrente y Paralela  
**Institución:** Universidad Tecnológica de Santiago (UTESA)  

---

## Estructura del Proyecto

```
├── Backend/                    # API REST en Python FastAPI + Docker + PostgreSQL/SQLite
│   ├── app/
│   │   ├── models/             # Modelos de base de datos (usuario.py, archivo.py, notificacion.py)
│   │   ├── schemas/            # Esquemas de datos (usuario.py, token.py, archivo.py, panel.py)
│   │   ├── routers/            # Enrutadores (autenticacion.py, usuarios.py, archivos.py, panel.py)
│   │   ├── utils/              # Utilidades de seguridad y dependencias
│   │   ├── configuracion.py    # Variables de entorno y configuración
│   │   ├── base_datos.py       # Conexión SQLAlchemy
│   │   └── principal.py        # Punto de entrada de FastAPI
│   ├── uploads/                # Almacenamiento de archivos físicos
│   ├── Dockerfile              # Dockerfile para la API
│   ├── docker-compose.yml      # Docker Compose para API y PostgreSQL
│   ├── requirements.txt        # Dependencias de Python
│   └── README.md
│
├── Mobile/                     # Aplicación Android Nativa en Kotlin (Gradle)
│   ├── app/
│   │   ├── src/main/java/com/victorbueno/app/
│   │   │   ├── data/           # ServicioApi, ClienteApi, Modelos, Repositorios
│   │   │   ├── ui/             # Actividades, ViewModels y Adaptadores
│   │   │   └── utils/          # AdministradorSesion, Constantes, Resultado, UtilidadesImagen
│   │   └── src/main/res/       # Vistas XML en español y recursos
│   ├── build.gradle.kts
│   └── README.md
│
├── Documentacion/
│   ├── 01_Arquitectura_MVVM.md
│   ├── 02_Rutas_API_REST.md
│   ├── 03_Concurrencia_y_Paralelismo.md
│   ├── 04_Comparacion_Tiempos_Rendimiento.md
│   └── 05_Guia_Demostracion_Clase.md
│
└── README.md
```

---

## Ejecución del Backend

```bash
cd Backend
# Con Docker:
docker compose up -d

# Con Python local:
pip install -r requirements.txt
python -m uvicorn app.principal:app --reload --host 0.0.0.0 --port 8000
```
Documentación interactiva Swagger: [http://localhost:8000/docs](http://localhost:8000/docs)

---

## Ejecución de la App Móvil

1. Abrir la carpeta `Mobile` en Android Studio.
2. Sincronizar Gradle y ejecutar en emulador o dispositivo físico.

### Credenciales de Prueba:
- **Usuario:** `victor.bueno@utesa.edu`
- **Contraseña:** `Victor123*`
