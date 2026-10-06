package com.victorbueno.app.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object UtilidadesImagen {

    fun obtenerArchivoDesdeUri(context: Context, uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val nombreArchivo = "foto_${System.currentTimeMillis()}.jpg"
            val archivoTemporal = File(context.cacheDir, nombreArchivo)
            
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(archivoTemporal)
            
            inputStream?.use { entrada ->
                outputStream.use { salida ->
                    entrada.copyTo(salida)
                }
            }
            archivoTemporal
        } catch (e: Exception) {
            null
        }
    }

    fun obtenerArchivosDesdeUris(context: Context, uris: List<Uri>): List<File> {
        val listaArchivos = mutableListOf<File>()
        val contentResolver = context.contentResolver
        uris.forEachIndexed { index, uri ->
            try {
                val nombreArchivo = "galeria_${System.currentTimeMillis()}_$index.jpg"
                val archivoTemporal = File(context.cacheDir, nombreArchivo)
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(archivoTemporal)
                inputStream?.use { entrada ->
                    outputStream.use { salida ->
                        entrada.copyTo(salida)
                    }
                }
                if (archivoTemporal.exists() && archivoTemporal.length() > 0) {
                    listaArchivos.add(archivoTemporal)
                }
            } catch (e: Exception) {
                // Omitir archivo con error de lectura
            }
        }
        return listaArchivos
    }

    fun formatearUrlImagen(urlBase: String, urlRelativa: String?): String? {
        if (urlRelativa.isNullOrBlank()) return null
        if (urlRelativa.startsWith("http://") || urlRelativa.startsWith("https://")) {
            return urlRelativa
        }
        val baseLimpia = if (urlBase.endsWith("/")) urlBase.dropLast(1) else urlBase
        val rutaLimpia = if (urlRelativa.startsWith("/")) urlRelativa else "/$urlRelativa"
        return "$baseLimpia$rutaLimpia"
    }
}
