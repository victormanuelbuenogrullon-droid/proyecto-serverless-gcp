package com.victorbueno.app.data.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Usuario(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("email") val email: String,
    @SerializedName("foto") val foto: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("password") val password: String? = null
) : Serializable {
    val nombreCompleto: String
        get() = "$nombre $apellido"
}

data class CreacionUsuario(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("foto") val foto: String? = null
)

data class ActualizacionUsuario(
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("apellido") val apellido: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("password") val password: String? = null,
    @SerializedName("foto") val foto: String? = null
)
