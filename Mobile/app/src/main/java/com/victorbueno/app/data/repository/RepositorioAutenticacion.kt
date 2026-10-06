package com.victorbueno.app.data.repository

import com.victorbueno.app.data.api.ServicioApi
import com.victorbueno.app.data.models.ActualizacionUsuario
import com.victorbueno.app.data.models.CreacionUsuario
import com.victorbueno.app.data.models.RespuestaAutenticacion
import com.victorbueno.app.data.models.SolicitudLogin
import com.victorbueno.app.data.models.Usuario
import com.victorbueno.app.utils.AdministradorSesion
import com.victorbueno.app.utils.Resultado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class RepositorioAutenticacion(
    private val servicioApi: ServicioApi,
    private val administradorSesion: AdministradorSesion
) {

    suspend fun iniciarSesion(email: String, clave: String): Resultado<RespuestaAutenticacion> = withContext(Dispatchers.IO) {
        try {
            val respuesta = servicioApi.iniciarSesion(SolicitudLogin(email = email, password = clave))
            if (respuesta.isSuccessful && respuesta.body() != null) {
                val datos = respuesta.body()!!
                administradorSesion.guardarToken(datos.accessToken)
                administradorSesion.guardarUsuario(datos.usuario)
                Resultado.Exito(datos)
            } else {
                val error = extraerMensajeError(respuesta.errorBody()?.string())
                Resultado.Error(error ?: "Credenciales incorrectas")
            }
        } catch (e: Exception) {
            Resultado.Error("Error de conexión: ${e.localizedMessage ?: e.message}", e)
        }
    }

    suspend fun registrar(solicitud: CreacionUsuario): Resultado<Usuario> = withContext(Dispatchers.IO) {
        try {
            val respuesta = servicioApi.registrarUsuario(solicitud)
            if (respuesta.isSuccessful && respuesta.body() != null) {
                Resultado.Exito(respuesta.body()!!)
            } else {
                val error = extraerMensajeError(respuesta.errorBody()?.string())
                Resultado.Error(error ?: "Error al registrar")
            }
        } catch (e: Exception) {
            Resultado.Error("Error de conexión: ${e.localizedMessage ?: e.message}", e)
        }
    }

    suspend fun obtenerPerfil(): Resultado<Usuario> = withContext(Dispatchers.IO) {
        try {
            val respuesta = servicioApi.obtenerPerfil()
            if (respuesta.isSuccessful && respuesta.body() != null) {
                val usuario = respuesta.body()!!
                administradorSesion.guardarUsuario(usuario)
                Resultado.Exito(usuario)
            } else {
                Resultado.Error("No se pudo obtener el perfil")
            }
        } catch (e: Exception) {
            Resultado.Error("Error de red: ${e.localizedMessage}", e)
        }
    }

    suspend fun actualizarPerfil(solicitud: ActualizacionUsuario): Resultado<Usuario> = withContext(Dispatchers.IO) {
        try {
            val respuesta = servicioApi.actualizarPerfil(solicitud)
            if (respuesta.isSuccessful && respuesta.body() != null) {
                val usuario = respuesta.body()!!
                administradorSesion.guardarUsuario(usuario)
                Resultado.Exito(usuario)
            } else {
                Resultado.Error("No se pudo actualizar el perfil")
            }
        } catch (e: Exception) {
            Resultado.Error("Error: ${e.localizedMessage}", e)
        }
    }

    fun cerrarSesion() {
        administradorSesion.cerrarSesion()
    }

    private fun extraerMensajeError(cuerpoError: String?): String? {
        if (cuerpoError.isNullOrBlank()) return null
        return try {
            val json = JSONObject(cuerpoError)
            json.optString("detail", json.optString("message", cuerpoError))
        } catch (e: Exception) {
            cuerpoError
        }
    }
}
