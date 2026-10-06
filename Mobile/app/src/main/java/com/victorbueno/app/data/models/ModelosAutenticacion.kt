package com.victorbueno.app.data.models

import com.google.gson.annotations.SerializedName

data class SolicitudLogin(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RespuestaAutenticacion(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("user") val usuario: Usuario
)
