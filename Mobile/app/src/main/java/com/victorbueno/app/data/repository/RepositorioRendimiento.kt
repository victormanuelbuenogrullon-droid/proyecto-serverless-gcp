package com.victorbueno.app.data.repository

import com.victorbueno.app.data.api.ServicioApi
import com.victorbueno.app.data.models.EstadoTarea
import com.victorbueno.app.data.models.ResultadoRendimiento
import com.victorbueno.app.data.models.TareaRendimiento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.system.measureTimeMillis

class RepositorioRendimiento(private val servicioApi: ServicioApi) {

    private suspend fun subirArchivoIndividual(archivo: File): Pair<Boolean, String> {
        return try {
            val cuerpo = archivo.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val parte = MultipartBody.Part.createFormData("file", archivo.name, cuerpo)
            val res = servicioApi.subirArchivo(parte)
            if (res.isSuccessful && res.body() != null) {
                Pair(true, "HTTP ${res.code()} - ID: ${res.body()?.id}")
            } else {
                Pair(false, "HTTP ${res.code()}: ${res.message()}")
            }
        } catch (e: Exception) {
            Pair(false, "Error: ${e.localizedMessage ?: "Fallo de conexión"}")
        }
    }

    suspend fun ejecutarSecuencial(
        archivos: List<File>,
        alActualizarTarea: suspend (TareaRendimiento) -> Unit
    ): ResultadoRendimiento = withContext(Dispatchers.IO) {
        val tareas = archivos.mapIndexed { index, archivo ->
            TareaRendimiento(
                id = index + 1,
                nombre = archivo.name,
                archivo = archivo,
                tamanoKb = archivo.length() / 1024
            )
        }

        val tiempoTotal = measureTimeMillis {
            for (tarea in tareas) {
                tarea.estado = EstadoTarea.EJECUTANDO
                tarea.nombreHilo = Thread.currentThread().name
                alActualizarTarea(tarea.copy())

                val duracionTarea = measureTimeMillis {
                    val (exito, mensaje) = if (tarea.archivo != null && tarea.archivo.exists()) {
                        subirArchivoIndividual(tarea.archivo)
                    } else {
                        Pair(false, "Archivo no disponible")
                    }
                    tarea.estado = if (exito) EstadoTarea.COMPLETADO else EstadoTarea.FALLIDO
                    tarea.mensajeResultado = mensaje
                }
                tarea.duracionMs = duracionTarea
                tarea.progreso = 100
                alActualizarTarea(tarea.copy())
            }
        }

        ResultadoRendimiento(
            modo = "Secuencial",
            tiempoTotalMs = tiempoTotal,
            cantidadTareas = tareas.size,
            tareas = tareas,
            promedioTareaMs = if (tareas.isNotEmpty()) tiempoTotal.toDouble() / tareas.size else 0.0
        )
    }

    suspend fun ejecutarConcurrente(
        archivos: List<File>,
        tiempoBaseSecuencialMs: Long? = null,
        alActualizarTarea: suspend (TareaRendimiento) -> Unit
    ): ResultadoRendimiento = withContext(Dispatchers.IO) {
        val tareas = archivos.mapIndexed { index, archivo ->
            TareaRendimiento(
                id = index + 1,
                nombre = archivo.name,
                archivo = archivo,
                tamanoKb = archivo.length() / 1024
            )
        }

        val tiempoTotal = measureTimeMillis {
            coroutineScope {
                val diferidos = tareas.map { tarea ->
                    async(Dispatchers.IO) {
                        tarea.estado = EstadoTarea.EJECUTANDO
                        tarea.nombreHilo = Thread.currentThread().name
                        alActualizarTarea(tarea.copy())

                        val duracionTarea = measureTimeMillis {
                            val (exito, mensaje) = if (tarea.archivo != null && tarea.archivo.exists()) {
                                subirArchivoIndividual(tarea.archivo)
                            } else {
                                Pair(false, "Archivo no disponible")
                            }
                            tarea.estado = if (exito) EstadoTarea.COMPLETADO else EstadoTarea.FALLIDO
                            tarea.mensajeResultado = mensaje
                        }
                        tarea.duracionMs = duracionTarea
                        tarea.progreso = 100
                        alActualizarTarea(tarea.copy())
                        tarea
                    }
                }
                diferidos.awaitAll()
            }
        }

        val aceleracion = if (tiempoBaseSecuencialMs != null && tiempoTotal > 0) {
            tiempoBaseSecuencialMs.toDouble() / tiempoTotal.toDouble()
        } else null

        ResultadoRendimiento(
            modo = "Concurrente",
            tiempoTotalMs = tiempoTotal,
            cantidadTareas = tareas.size,
            tareas = tareas,
            promedioTareaMs = if (tareas.isNotEmpty()) tiempoTotal.toDouble() / tareas.size else 0.0,
            factorAceleracion = aceleracion
        )
    }
}
