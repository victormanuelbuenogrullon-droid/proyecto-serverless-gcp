# 📱 Aplicación Móvil Android Nativa (Kotlin + MVVM + Coroutines)

**Estudiante:** Victor Manuel Bueno Grullón  
**Matrícula:** 1177316  
**Tecnología Asignada:** Android Kotlin  
**Backend:** Python + FastAPI (Docker / Uvicorn)  

---

## 📌 Descripción del Proyecto

Aplicación móvil nativa para Android desarrollada en **Kotlin** bajo el patrón de diseño **MVVM (Model - View - ViewModel)**. Implementa autenticación robusta mediante **JWT (JSON Web Tokens)** con interceptores OkHttp, gestión completa de **CRUD de usuarios**, subida multipart de fotos y documentos al backend, y un módulo central de **Programación Concurrente y Paralela** utilizando **Kotlin Coroutines (`async` / `awaitAll`)** y **StateFlow / LiveData** en `Dispatchers.IO` para comparar empíricamente el rendimiento de ejecución frente a enfoques secuenciales tradicionales.

---

## 🏗️ Arquitectura del Sistema (MVVM)

```
        ┌────────────────────────────────┐
        │             VIEWS              │  (Activities, XML Layouts, Adapters)
        └───────────────┬────────────────┘
                        │ Observa LiveData / State
                        ▼
        ┌────────────────────────────────┐
        │           VIEWMODELS           │  (AuthViewModel, DashboardViewModel, UserViewModel, BenchmarkViewModel)
        └───────────────┬────────────────┘
                        │ viewModelScope / Coroutines
                        ▼
        ┌────────────────────────────────┐
        │          REPOSITORIES          │  (AuthRepository, UserRepository, DashboardRepository, BenchmarkRepository)
        └───────────────┬────────────────┘
                        │ withContext(Dispatchers.IO) / async / awaitAll
                        ▼
        ┌────────────────────────────────┐
        │            SERVICES            │  (Retrofit ApiService, OkHttp AuthInterceptor, Token Storage)
        └───────────────┬────────────────┘
                        │ HTTP / REST / Multipart (JWT Bearer)
                        ▼
        ┌────────────────────────────────┐
        │       API REST (FASTAPI)       │  (Endpoints Docker / Localhost)
        └────────────────────────────────┘
```

---

## 📂 Estructura de Carpetas

```
Mobile/app/src/main/
├── AndroidManifest.xml
├── java/com/victorbueno/app/
│   ├── data/
│   │   ├── api/             # Retrofit ApiService, ApiClient, AuthInterceptor
│   │   ├── models/          # User, AuthModels, DashboardModels, BenchmarkTask
│   │   └── repository/      # Repositorios con lógica de red y concurrencia
│   ├── ui/
│   │   ├── auth/            # LoginActivity, RegisterActivity, AuthViewModel
│   │   ├── dashboard/       # DashboardActivity, DashboardViewModel, NotificationAdapter
│   │   ├── users/           # UserListActivity, UserDetailActivity, UserFormActivity, UserAdapter, UserViewModel
│   │   ├── benchmark/       # ConcurrencyBenchmarkActivity, BenchmarkAdapter, BenchmarkViewModel
│   │   └── profile/         # ProfileActivity, ProfileViewModel
│   └── utils/               # SessionManager, Constants, Resource, ImageUtils, ViewModelFactory
└── res/
    ├── layout/              # Vistas XML diseñadas con Material 3
    ├── values/              # Strings, Colors, Themes
    └── xml/                 # Network Security Config y FileProvider Paths
```

---

## ⚡ Concurrencia y Paralelismo en Android Kotlin

### 1. Consumo Simultáneo de Servicios en Dashboard (Sección 6)
Al ingresar al Dashboard, en lugar de bloquear el hilo esperando secuencialmente cada petición:
```kotlin
// Consumo Concurrente con Coroutines
coroutineScope {
    val profileDeferred = async { apiService.getProfile() }
    val statsDeferred = async { apiService.getStats() }
    val usersDeferred = async { apiService.getUsers() }
    val notifsDeferred = async { apiService.getNotifications() }

    val profile = profileDeferred.await()
    val stats = statsDeferred.await()
    val users = usersDeferred.await()
    val notifs = notifsDeferred.await()
}
```

### 2. Laboratorio de Benchmark (Sección 7 y 8)
Permite al profesor y evaluador presionar en vivo:
1. **Botón Rojo (Secuencial):** Ejecuta tareas en bucle `for` midiendo tiempo total ($T_{sec} \approx 4.5\text{s}$ para 10 tareas).
2. **Botón Verde (Concurrente):** Despacha coroutines con `async(Dispatchers.IO)` y `awaitAll()` midiendo tiempo total ($T_{conc} \approx 0.6\text{s}$).
3. **Métricas en Pantalla:** Muestra el Speedup $S = \frac{T_{sec}}{T_{conc}}$ y el porcentaje de reducción de tiempo ($\approx 85\%$).
4. **Monitor de Hilos:** Muestra en tiempo real el hilo del pool de trabajo asignado (ej: `DefaultDispatcher-worker-1`, `DefaultDispatcher-worker-2`).

---

## 🚀 Cómo Ejecutar el Proyecto en Android Studio

1. **Abrir Android Studio:**
   - Seleccionar **File -> Open** y elegir la carpeta `Mobile`.
2. **Sincronizar Gradle:**
   - Dejar que Android Studio descargue las dependencias y construya el proyecto.
3. **Configuración de Conexión:**
   - Si se usa el **Emulador Oficial de Android Studio**, la app se conecta automáticamente a `http://10.0.2.2:8000/` (que es el alias de `localhost` de la PC).
   - Si se usa un **Dispositivo Físico**, en la pantalla de Login pulsar el botón `⚙️ Configurar IP Servidor Backend` e ingresar la IP de la máquina (ej: `http://192.168.1.15:8000/`).
4. **Ejecutar:**
   - Presionar el botón verde **Run (Shift + F10)**.

---

## 🔑 Credenciales para Demostración

- **Usuario 1 (Estudiante):** `victor.bueno@utesa.edu` / `Victor123*`
- **Usuario 2 (Profesor):** `profesor@utesa.edu` / `Admin123*`
