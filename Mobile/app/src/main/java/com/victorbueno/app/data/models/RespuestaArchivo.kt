package com.victorbueno.app.data.models

import com.google.gson.annotations.SerializedName

data class RespuestaSubida(
    @SerializedName("id") val id: Int,
    @SerializedName("filename") val filename: String,
    @SerializedName("url") val url: String
)

data class ElementoArchivo(
    @SerializedName("id") val id: Int,
    @SerializedName("filename") val filename: String,
    @SerializedName("url") val url: String,
    @SerializedName("content_type") val tipoContenido: String? = null,
    @SerializedName("size_bytes") val tamanoBytes: Long? = null,
    @SerializedName("created_at") val creadoEn: String? = null
)
