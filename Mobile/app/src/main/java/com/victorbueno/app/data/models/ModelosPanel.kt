package com.victorbueno.app.data.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RespuestaEstadisticas(
    @SerializedName("total_users") val totalUsuarios: Int,
    @SerializedName("total_files") val totalArchivos: Int,
    @SerializedName("storage_used_bytes") val almacenamientoBytes: Long,
    @SerializedName("system_status") val estadoSistema: String,
    @SerializedName("active_sessions") val sesionesActivas: Int,
    @SerializedName("server_timestamp") val fechaServidor: String
) : Serializable

data class ElementoNotificacion(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val titulo: String,
    @SerializedName("message") val mensaje: String,
    @SerializedName("type") val tipo: String,
    @SerializedName("created_at") val fechaCreacion: String
) : Serializable

data class DatosPanel(
    val perfil: Usuario?,
    val estadisticas: RespuestaEstadisticas?,
    val listaUsuarios: List<Usuario>,
    val notificaciones: List<ElementoNotificacion>,
    val duracionCargaMs: Long
)
