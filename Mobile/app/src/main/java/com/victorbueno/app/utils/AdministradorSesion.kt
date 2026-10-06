package com.victorbueno.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.victorbueno.app.data.models.Usuario

class AdministradorSesion(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constantes.NOMBRE_PREFS,
        Context.MODE_PRIVATE
    )

    fun guardarToken(token: String) {
        prefs.edit().putString(Constantes.CLAVE_TOKEN_JWT, token).apply()
    }

    fun obtenerToken(): String? {
        return prefs.getString(Constantes.CLAVE_TOKEN_JWT, null)
    }

    fun guardarUsuario(usuario: Usuario) {
        prefs.edit()
            .putInt(Constantes.CLAVE_ID_USUARIO, usuario.id)
            .putString(Constantes.CLAVE_NOMBRE_USUARIO, usuario.nombre)
            .putString(Constantes.CLAVE_APELLIDO_USUARIO, usuario.apellido)
            .putString(Constantes.CLAVE_EMAIL_USUARIO, usuario.email)
            .putString(Constantes.CLAVE_FOTO_USUARIO, usuario.foto)
            .apply()
    }

    fun obtenerUsuario(): Usuario? {
        val id = prefs.getInt(Constantes.CLAVE_ID_USUARIO, -1)
        if (id == -1) return null

        val nombre = prefs.getString(Constantes.CLAVE_NOMBRE_USUARIO, "") ?: ""
        val apellido = prefs.getString(Constantes.CLAVE_APELLIDO_USUARIO, "") ?: ""
        val email = prefs.getString(Constantes.CLAVE_EMAIL_USUARIO, "") ?: ""
        val foto = prefs.getString(Constantes.CLAVE_FOTO_USUARIO, null)

        return Usuario(
            id = id,
            nombre = nombre,
            apellido = apellido,
            email = email,
            foto = foto
        )
    }

    fun estaAutenticado(): Boolean {
        return !obtenerToken().isNullOrBlank()
    }

    fun obtenerUrlBase(): String {
        return prefs.getString(Constantes.CLAVE_URL_PERSONALIZADA, Constantes.URL_BASE) ?: Constantes.URL_BASE
    }

    fun guardarUrlBase(url: String) {
        val urlFormateada = if (!url.endsWith("/")) "$url/" else url
        prefs.edit().putString(Constantes.CLAVE_URL_PERSONALIZADA, urlFormateada).apply()
        com.victorbueno.app.data.api.ClienteApi.reiniciarInstancia()
    }

    fun cerrarSesion() {
        prefs.edit()
            .remove(Constantes.CLAVE_TOKEN_JWT)
            .remove(Constantes.CLAVE_ID_USUARIO)
            .remove(Constantes.CLAVE_NOMBRE_USUARIO)
            .remove(Constantes.CLAVE_APELLIDO_USUARIO)
            .remove(Constantes.CLAVE_EMAIL_USUARIO)
            .remove(Constantes.CLAVE_FOTO_USUARIO)
            .apply()
    }
}
