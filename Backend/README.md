# 🚀 Backend REST API - Python + FastAPI (Concurrencia & JWT)

**Estudiante:** Victor Manuel Bueno Grullón  
**Matrícula:** 1177316  
**Tecnología Backend:** Python 3.11+ / FastAPI / SQLAlchemy / JWT / Docker  
**Tecnología Móvil Asignada:** Android Kotlin  

---

## 📌 Descripción del Proyecto

API REST modular y segura desarrollada con **FastAPI** y **SQLAlchemy**, diseñada para soportar autenticación JWT, operaciones CRUD de usuarios con hashing de contraseñas (Bcrypt), subida y almacenamiento de archivos multimedia/documentos, y servicios diseñados para el consumo simultáneo y benchmarking de concurrencia desde la aplicación móvil en Android Kotlin.

---

## 🛠️ Tecnologías y Librerías

- **Lenguaje:** Python 3.11+
- **Framework Web:** FastAPI
- **Servidor ASGI:** Uvicorn
- **ORM & BD:** SQLAlchemy (Soporte dual: SQLite para desarrollo rápido y PostgreSQL para producción en Docker)
- **Seguridad:** Python-Jose (JWT HS256) y Passlib / Bcrypt (Hashing de contraseñas)
- **Manejo de Archivos:** Python-Multipart, Aiofiles
- **Contenedores:** Docker & Docker Compose

---

## 📂 Estructura del Backend

```
Backend/
├── app/
│   ├── models/          # Modelos de Base de Datos (User, FileRecord, Notification)
│   ├── schemas/         # Esquemas de validación Pydantic (DTOs)
│   ├── routers/         # Controladores de Endpoints (Auth, Users, Uploads, Dashboard)
│   ├── utils/           # Utilidades de Seguridad (JWT, Bcrypt) y Dependencias
│   ├── config.py        # Configuración de variables de entorno
│   ├── database.py      # Conexión SQLAlchemy y generador de sesiones
│   └── main.py          # Punto de entrada de la aplicación FastAPI
├── uploads/             # Directorio de almacenamiento de archivos físicos
├── Dockerfile           # Imagen Docker para el API
├── docker-compose.yml   # Orquestación de contenedores (API + PostgreSQL)
├── requirements.txt     # Dependencias del proyecto
└── README.md
```

---

## 🚀 Instrucciones de Ejecución

### Opción 1: Ejecutar con Docker Compose (Recomendado para evaluación)

1. Abrir terminal en la carpeta `/Backend`.
2. Ejecutar el comando:
   ```bash
   docker compose up -d --build
   ```
3. La API estará disponible inmediatamente en:
   - **URL Base:** `http://localhost:8000`
   - **Documentación Interactiva (Swagger UI):** `http://localhost:8000/docs`
   - **Documentación ReDoc:** `http://localhost:8000/redoc`

4. Para detener los contenedores:
   ```bash
   docker compose down
   ```

---

### Opción 2: Ejecutar Localmente con Python

1. Crear y activar un entorno virtual:
   ```bash
   python -m venv venv
   # En Windows PowerShell:
   .\venv\Scripts\Activate.ps1
   # En Linux/Mac:
   source venv/bin/activate
   ```

2. Instalar dependencias:
   ```bash
   pip install -r requirements.txt
   ```

3. Iniciar el servidor:
   ```bash
   uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
   ```

---

## 🔑 Credenciales de Prueba Precargadas

La base de datos se inicializa automáticamente con los siguientes usuarios de prueba:

| Rol | Correo Electrónico | Contraseña |
|---|---|---|
| **Estudiante / Admin** | `victor.bueno@utesa.edu` | `Victor123*` |
| **Profesor / Evaluador** | `profesor@utesa.edu` | `Admin123*` |
| **Usuario Test 1** | `maria.santos@test.com` | `Maria123*` |
| **Usuario Test 2** | `carlos.rodriguez@test.com` | `Carlos123*` |

---

## 📡 Endpoints del API REST

### 1. Autenticación (`/auth` o directo)
- `POST /login` ➡️ Iniciar sesión (recibe `{ "email", "password" }`, retorna token JWT y datos de usuario).
- `POST /register` ➡️ Registrar nuevo usuario.
- `GET /profile` ➡️ Obtener perfil del usuario autenticado (*Requiere JWT*).
- `PUT /profile` ➡️ Actualizar perfil y foto de avatar (*Requiere JWT*).

### 2. CRUD de Usuarios (`/users`)
- `GET /users` ➡️ Listar todos los usuarios (*Requiere JWT*).
- `GET /users/{id}` ➡️ Consultar usuario por ID (*Requiere JWT*).
- `POST /users` ➡️ Crear un nuevo usuario con contraseña cifrada (*Requiere JWT*).
- `PUT /users/{id}` ➡️ Modificar datos de usuario (*Requiere JWT*).
- `DELETE /users/{id}` ➡️ Eliminar usuario (*Requiere JWT*).

### 3. Módulo de Archivos (`/upload`)
- `POST /upload` ➡️ Subida multipart de archivo (valida extensión, tamaño < 10MB, almacena en disco y registra en BD).
  - Retorno:
    ```json
    {
      "id": 15,
      "filename": "7a8b9c_foto.jpg",
      "url": "/uploads/7a8b9c_foto.jpg"
    }
    ```
- `DELETE /upload/{id}` ➡️ Eliminar archivo por ID (*Requiere JWT*).
- `GET /upload/list` ➡️ Listar archivos subidos (*Requiere JWT*).
- `GET /uploads/{filename}` ➡️ Acceso estático directo para carga en imágenes móviles.

### 4. Dashboard y Concurrencia
- `GET /stats` ➡️ Estadísticas del sistema (total usuarios, archivos, almacenamiento, estado).
- `GET /notifications` ➡️ Historial de eventos y alertas.
- `POST /process/simulate-task?task_id=1&delay_ms=400` ➡️ Tarea asíncrona para pruebas de estrés y comparación concurrente.

---

## 🔒 Formato de Autenticación JWT

Para consumir endpoints protegidos, enviar el encabezado HTTP:
```http
Authorization: Bearer <TU_TOKEN_JWT>
```
