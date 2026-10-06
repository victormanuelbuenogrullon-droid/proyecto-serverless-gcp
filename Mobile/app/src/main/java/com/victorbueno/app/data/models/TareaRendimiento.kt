package com.victorbueno.app.data.models

import com.google.gson.annotations.SerializedName
import java.io.File

enum class EstadoTarea {
    PENDIENTE,
    EJECUTANDO,
    COMPLETADO,
    FALLIDO
}

data class TareaRendimiento(
    val id: Int,
    val nombre: String,
    val archivo: File? = null,
    val tamanoKb: Long = 0,
    var estado: EstadoTarea = EstadoTarea.PENDIENTE,
    var duracionMs: Long = 0,
    var nombreHilo: String = "",
    var progreso: Int = 0,
    var mensajeResultado: String = ""
)

data class RespuestaSimulacion(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val nombre: String,
    @SerializedName("processed_size") val tamanoProcesado: Int,
    @SerializedName("duration_ms") val duracionMs: Double,
    @SerializedName("status") val estado: String
)

data class ResultadoRendimiento(
    val modo: String,
    val tiempoTotalMs: Long,
    val cantidadTareas: Int,
    val tareas: List<TareaRendimiento>,
    val promedioTareaMs: Double,
    val factorAceleracion: Double? = null
)
