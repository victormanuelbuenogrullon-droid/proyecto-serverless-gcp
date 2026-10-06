package com.victorbueno.app.data.repository

import com.victorbueno.app.data.api.ServicioApi
import com.victorbueno.app.data.models.ActualizacionUsuario
import com.victorbueno.app.data.models.CreacionUsuario
import com.victorbueno.app.data.models.Usuario
import com.victorbueno.app.utils.Resultado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class RepositorioUsuario(private val servicioApi: ServicioApi) {

    suspend fun obtenerUsuarios(): Resultado<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            val respuesta = servicioApi.obtenerUsuarios()
            if (respuesta.isSuccessful && respuesta.body() != null) {
                Resultado.Exito(respuesta.body()!!)
            } else {
                Resultado.Error("Error al listar usuarios")
            }
        } catch (e: Exception) {
            Resultado.Error("Error de conexión: ${e.localizedMessage ?: e.message}", e)
        }
    }

    suspend fun obtenerUsuarioPorId(id: Int): Resultado<Usuario> = withContext(Dispatchers.IO) {
        try {
            val respuesta = servicioApi.obtenerUsuarioPorId(id)
            if (respuesta.isSuccessful && respuesta.body() != null) {
                Resultado.Exito(respuesta.body()!!)
            } else {
                Resultado.Error("Usuario no encontrado (ID: $id)")
            }
        } catch (e: Exception) {
            Resultado.Error("Error de conexión: ${e.localizedMessage}", e)
        }
    }

    suspend fun crearUsuario(solicitud: CreacionUsuario): Resultado<Usuario> = withContext(Dispatchers.IO) {
        try {
            val respuesta = servicioApi.crearUsuario(solicitud)
            if (respuesta.isSuccessful && respuesta.body() != null) {
                Resultado.Exito(respuesta.body()!!)
            } else {
                val error = extraerMensajeError(respuesta.errorBody()?.string())
                Resultado.Error(error ?: "Error al crear usuario")
            }
        } catch (e: Exception) {
            Resultado.Error("Error de conexión: ${e.localizedMessage}", e)
        }
    }

    suspend fun actualizarUsuario(id: Int, solicitud: ActualizacionUsuario): Resultado<Usuario> = withContext(Dispatchers.IO) {
        try {
            val respuesta = servicioApi.actualizarUsuario(id, solicitud)
            if (respuesta.isSuccessful && respuesta.body() != null) {
                Resultado.Exito(respuesta.body()!!)
            } else {
                val error = extraerMensajeError(respuesta.errorBody()?.string())
                Resultado.Error(error ?: "Error al actualizar usuario")
            }
        } catch (e: Exception) {
            Resultado.Error("Error de conexión: ${e.localizedMessage}", e)
        }
    }

    suspend fun eliminarUsuario(id: Int): Resultado<Boolean> = withContext(Dispatchers.IO) {
        try {
            val respuesta = servicioApi.eliminarUsuario(id)
            if (respuesta.isSuccessful) {
                Resultado.Exito(true)
            } else {
                Resultado.Error("Error al eliminar el usuario")
            }
        } catch (e: Exception) {
            Resultado.Error("Error de conexión: ${e.localizedMessage}", e)
        }
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
