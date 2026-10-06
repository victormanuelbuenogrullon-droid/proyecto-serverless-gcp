package com.victorbueno.app.data.repository

import com.victorbueno.app.data.api.ServicioApi
import com.victorbueno.app.data.models.ElementoArchivo
import com.victorbueno.app.data.models.RespuestaSubida
import com.victorbueno.app.utils.Resultado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class RepositorioArchivo(private val servicioApi: ServicioApi) {

    suspend fun listarArchivos(saltar: Int = 0, limite: Int = 100): Resultado<List<ElementoArchivo>> = withContext(Dispatchers.IO) {
        try {
            val respuesta = servicioApi.listarArchivos(saltar, limite)
            if (respuesta.isSuccessful && respuesta.body() != null) {
                Resultado.Exito(respuesta.body()!!)
            } else {
                Resultado.Error("Error al listar archivos: ${respuesta.code()} ${respuesta.message()}")
            }
        } catch (e: Exception) {
            Resultado.Error("Error de conexión: ${e.localizedMessage}", e)
        }
    }

    suspend fun subirArchivo(archivo: File): Resultado<RespuestaSubida> = withContext(Dispatchers.IO) {
        try {
            val cuerpoArchivo = archivo.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val parteMultipart = MultipartBody.Part.createFormData("file", archivo.name, cuerpoArchivo)

            val respuesta = servicioApi.subirArchivo(parteMultipart)
            if (respuesta.isSuccessful && respuesta.body() != null) {
                Resultado.Exito(respuesta.body()!!)
            } else {
                Resultado.Error("Error al subir archivo: ${respuesta.message()}")
            }
        } catch (e: Exception) {
            Resultado.Error("Error en la subida: ${e.localizedMessage}", e)
        }
    }

    suspend fun eliminarArchivo(idArchivo: Int): Resultado<Boolean> = withContext(Dispatchers.IO) {
        try {
            val respuesta = servicioApi.eliminarArchivo(idArchivo)
            if (respuesta.isSuccessful) {
                Resultado.Exito(true)
            } else {
                Resultado.Error("Error al eliminar archivo")
            }
        } catch (e: Exception) {
            Resultado.Error("Error: ${e.localizedMessage}", e)
        }
    }
}
