package com.victorbueno.app.utils

sealed class Resultado<out T> {
    data class Exito<out T>(val datos: T) : Resultado<T>()
    data class Error(val mensaje: String, val causa: Throwable? = null) : Resultado<Nothing>()
    object Cargando : Resultado<Nothing>()
}
