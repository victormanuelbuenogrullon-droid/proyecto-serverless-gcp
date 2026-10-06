# 📡 Documentación de Endpoints del API REST (Python + FastAPI)

**Estudiante:** Victor Manuel Bueno Grullón  
**Matrícula:** 1177316  
**Tecnología Backend:** Python + FastAPI + SQLAlchemy + PostgreSQL / SQLite  

---

## 🔐 Seguridad y Autenticación JWT

Todos los endpoints protegidos requieren el encabezado estándar HTTP de autorización:
```http
Authorization: Bearer <TOKEN_JWT>
```

Los tokens generados tienen una validez de 24 horas y están firmados utilizando el algoritmo **HS256** con la clave secreta del backend. Las contraseñas se almacenan cifradas con **Bcrypt**.

---

## 📋 Catálogo Completo de Endpoints

### 1. Autenticación y Perfil

#### `POST /login`
- **Descripción:** Valida credenciales de acceso y genera el token de sesión JWT.
- **Acceso:** Público.
- **Request Body:**
  ```json
  {
    "email": "victor.bueno@utesa.edu",
    "password": "Victor123*"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
    "token_type": "bearer",
    "user": {
      "id": 1,
      "nombre": "Victor",
      "apellido": "Bueno",
      "email": "victor.bueno@utesa.edu",
      "foto": null,
      "createdAt": "2026-09-28T10:00:00"
    }
  }
  ```

#### `POST /register`
- **Descripción:** Registra un nuevo usuario en la base de datos con contraseña cifrada (Bcrypt).
- **Acceso:** Público.
- **Request Body:**
  ```json
  {
    "nombre": "Juan",
    "apellido": "Perez",
    "email": "juan.perez@test.com",
    "password": "Password123*",
    "foto": null
  }
  ```
- **Response (201 Created):** Retorna el objeto del usuario creado.

#### `GET /profile`
- **Descripción:** Retorna la información completa del usuario actualmente autenticado.
- **Acceso:** Protegido (Requiere JWT).
- **Response (200 OK):** Objeto de usuario.

#### `PUT /profile`
- **Descripción:** Actualiza los datos del usuario autenticado (nombre, apellido, email, contraseña o foto).
- **Acceso:** Protegido (Requiere JWT).

---

### 2. Módulo CRUD de Usuarios (`/users`)

#### `GET /users`
- **Descripción:** Obtiene el listado de todos los usuarios registrados en el sistema.
- **Acceso:** Protegido (Requiere JWT).
- **Query Params:** `skip` (int, default: 0), `limit` (int, default: 100).
- **Response (200 OK):** Array de objetos de usuario.

#### `GET /users/{id}`
- **Descripción:** Obtiene los datos detallados de un usuario específico mediante su ID.
- **Acceso:** Protegido (Requiere JWT).
- **Response (200 OK):** Objeto de usuario con `id`, `nombre`, `apellido`, `email`, `foto`, `createdAt`.
- **Response (404 Not Found):** Si el ID no existe.

#### `POST /users`
- **Descripción:** Crea un nuevo usuario en el sistema.
- **Acceso:** Protegido (Requiere JWT).
- **Request Body:** `{ "nombre", "apellido", "email", "password", "foto" }`
- **Response (201 Created):** Objeto del usuario creado.

#### `PUT /users/{id}`
- **Descripción:** Actualiza los datos de un usuario por su identificador.
- **Acceso:** Protegido (Requiere JWT).
- **Response (200 OK):** Objeto del usuario modificado.

#### `DELETE /users/{id}`
- **Descripción:** Elimina permanentemente a un usuario del sistema.
- **Acceso:** Protegido (Requiere JWT).
- **Response (200 OK):** `{ "message": "Usuario con ID {id} eliminado exitosamente." }`

---

### 3. Módulo de Subida de Archivos (`/upload`)

#### `POST /upload`
- **Descripción:** Recibe un archivo binario mediante `multipart/form-data`, valida tamaño (< 10MB) y extensión permitida (.jpg, .png, .pdf, etc.), lo almacena en el directorio físico `/uploads` con nombre único UUID y registra la referencia en la base de datos.
- **Acceso:** Protegido (Requiere JWT).
- **Form Data:** `file: <archivo_binario>`
- **Response (201 Created):**
  ```json
  {
    "id": 15,
    "filename": "7f8b9a12c_foto_usuario_15.jpg",
    "url": "/uploads/7f8b9a12c_foto_usuario_15.jpg"
  }
  ```

#### `DELETE /upload/{id}`
- **Descripción:** Elimina el archivo del almacenamiento físico en disco y de la base de datos.
- **Acceso:** Protegido (Requiere JWT).

#### `GET /uploads/{filename}`
- **Descripción:** Endpoint de archivos estáticos para visualizar o descargar las imágenes y documentos desde la app móvil.
- **Acceso:** Público.

---

### 4. Módulo de Dashboard y Concurrencia

#### `GET /stats`
- **Descripción:** Retorna métricas globales para el dashboard (total de usuarios, archivos, almacenamiento y estado).
- **Acceso:** Protegido (Requiere JWT).
- **Response (200 OK):**
  ```json
  {
    "total_users": 5,
    "total_files": 12,
    "storage_used_bytes": 3456000,
    "system_status": "ONLINE - HEALTHY",
    "active_sessions": 1,
    "server_timestamp": "2026-09-28T10:45:00"
  }
  ```

#### `GET /notifications`
- **Descripción:** Retorna el historial de eventos del sistema para el feed de actividad reciente.
- **Acceso:** Protegido (Requiere JWT).

#### `POST /process/simulate-task`
- **Descripción:** Endpoint diseñado para la simulación de procesamiento asíncrono y benchmarking de concurrencia en clase.
- **Acceso:** Protegido (Requiere JWT).
- **Query Params:** `task_id` (int), `delay_ms` (int).
