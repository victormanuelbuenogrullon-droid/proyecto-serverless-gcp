# ⚡ Concurrencia y Paralelismo en Android con Kotlin Coroutines

**Estudiante:** Victor Manuel Bueno Grullón  
**Matrícula:** 1177316  
**Tecnología Asignada:** Android Kotlin  

---

## 1. Fundamentos Teóricos: Concurrencia vs Paralelismo

- **Concurrencia:** Trata de **manejar muchas cosas a la vez**. Es la composición de procesos que se ejecutan independientemente y pueden intercalarse o cooperar en un único procesador o en múltiples núcleos.
- **Paralelismo:** Trata de **hacer muchas cosas a la vez**. Es la ejecución física y simultánea de múltiples cálculos en diferentes núcleos de CPU de forma paralela en el mismo instante temporal.

En los dispositivos móviles modernos, donde las operaciones de entrada/salida de red (I/O) y el procesamiento de imágenes o archivos representan los cuellos de botella principales, el uso de concurrencia y paralelismo es indispensable para evitar el bloqueo del hilo principal de interfaz de usuario (`Main Thread / UI Thread`) y garantizar una alta tasa de respuesta (60-120 FPS).

---

## 2. Mecanismo Implementado: Kotlin Coroutines & Flow

En Android Kotlin, el mecanismo moderno y estándar de la industria son las **Coroutines (Rutinas de Suspensión)**.

### ¿Por qué Coroutines frente a Threads tradicionales de Java?
1. **Ligereza (Lightweight):** Un hilo del sistema operativo (`Thread`) consume aproximadamente 1MB de pila de memoria. Podemos tener millones de coroutines concurrentes sin saturar la memoria del dispositivo, ya que comparten un pool de hilos de trabajo gestionados dinámicamente.
2. **No bloqueantes (Non-blocking):** Cuando una función suspendida (`suspend fun`) espera la respuesta de un endpoint HTTP, el hilo subyacente no se queda inactivo esperando; queda libre para ejecutar otras coroutines.
3. **Cancelación Estructurada (Structured Concurrency):** Mediante `viewModelScope` y `coroutineScope`, si el usuario abandona la pantalla, todas las tareas hijas en ejecución se cancelan automáticamente, previniendo fugas de memoria (*memory leaks*).

```
         ┌─────────────────────────────────────────────────────────┐
         │             COROUTINE DISPATCHER (IO)                   │
         └───────────────────────────┬─────────────────────────────┘
                                     │
         ┌───────────────────────────┼───────────────────────────┐
         ▼                           ▼                           ▼
┌──────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│ Thread Worker 1  │       │ Thread Worker 2  │       │ Thread Worker N  │
│ [Coroutine A]    │       │ [Coroutine B]    │       │ [Coroutine C]    │
└──────────────────┘       └──────────────────┘       └──────────────────┘
```

---

## 3. Implementación de los Dos Procesos del Proyecto

### Proceso A: Consumo Simultáneo de Servicios al Cargar el Dashboard

En lugar de ejecutar peticiones secuenciales:
```kotlin
// INEFICIENTE: Secuencial (Tiempo Total = Suma de todos los tiempos)
val profile = apiService.getProfile()        // Espera ~400ms
val stats = apiService.getStats()            // Espera ~350ms
val users = apiService.getUsers()            // Espera ~450ms
val notifs = apiService.getNotifications()   // Espera ~300ms
// Total ≈ 1,500 ms (1.5 segundos)
```

Implementamos el consumo concurrente con `async` y `await`:
```kotlin
// ÓPTIMO: Concurrente (Tiempo Total ≈ Max(tiempo_individual) + overhead)
coroutineScope {
    val d1 = async(Dispatchers.IO) { apiService.getProfile() }
    val d2 = async(Dispatchers.IO) { apiService.getStats() }
    val d3 = async(Dispatchers.IO) { apiService.getUsers() }
    val d4 = async(Dispatchers.IO) { apiService.getNotifications() }

    val profile = d1.await()
    val stats = d2.await()
    val users = d3.await()
    val notifs = d4.await()
}
// Total ≈ 460 ms (Aceleración de 3.2x)
```

---

### Proceso B: Laboratorio de Benchmark y Procesamiento por Lotes

Se implementaron dos variantes del mismo caso de uso en `BenchmarkRepository.kt`:

#### 1. Versión Secuencial
```kotlin
for (task in tasks) {
    task.state = TaskState.RUNNING
    val response = apiService.simulateTask(task.id, delayMs = 450)
    task.state = TaskState.COMPLETED
}
```

#### 2. Versión Concurrente / Paralela
```kotlin
coroutineScope {
    val deferredJobs = tasks.map { task ->
        async(Dispatchers.IO) {
            task.state = TaskState.RUNNING
            task.threadName = Thread.currentThread().name
            val response = apiService.simulateTask(task.id, delayMs = 450)
            task.state = TaskState.COMPLETED
            task
        }
    }
    deferredJobs.awaitAll()
}
```
