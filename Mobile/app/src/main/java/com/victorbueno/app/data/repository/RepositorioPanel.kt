package com.victorbueno.app.data.repository

import com.victorbueno.app.data.api.ServicioApi
import com.victorbueno.app.data.models.*
import com.victorbueno.app.utils.Resultado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class RepositorioPanel(private val servicioApi: ServicioApi) {

    suspend fun obtenerDatosPanelConcurrente(): Resultado<DatosPanel> = withContext(Dispatchers.IO) {
        try {
            var perfil: Usuario? = null
            var estadisticas: RespuestaEstadisticas? = null
            var usuarios: List<Usuario> = emptyList()
            var notificaciones: List<ElementoNotificacion> = emptyList()

            val duracionMs = measureTimeMillis {
                coroutineScope {
                    val diferidoPerfil = async { servicioApi.obtenerPerfil() }
                    val diferidoStats = async { servicioApi.obtenerEstadisticas() }
                    val diferidoUsuarios = async { servicioApi.obtenerUsuarios() }
                    val diferidoNotifs = async { servicioApi.obtenerNotificaciones() }

                    val resPerfil = diferidoPerfil.await()
                    val resStats = diferidoStats.await()
                    val resUsuarios = diferidoUsuarios.await()
                    val resNotifs = diferidoNotifs.await()

                    if (resPerfil.isSuccessful) perfil = resPerfil.body()
                    if (resStats.isSuccessful) estadisticas = resStats.body()
                    if (resUsuarios.isSuccessful) usuarios = resUsuarios.body() ?: emptyList()
                    if (resNotifs.isSuccessful) notificaciones = resNotifs.body() ?: emptyList()
                }
            }

            Resultado.Exito(
                DatosPanel(
                    perfil = perfil,
                    estadisticas = estadisticas,
                    listaUsuarios = usuarios,
                    notificaciones = notificaciones,
                    duracionCargaMs = duracionMs
                )
            )
        } catch (e: Exception) {
            Resultado.Error("Error al cargar datos: ${e.localizedMessage}", e)
        }
    }

    suspend fun obtenerNotificaciones(): Resultado<List<ElementoNotificacion>> = withContext(Dispatchers.IO) {
        try {
            val respuesta = servicioApi.obtenerNotificaciones()
            if (respuesta.isSuccessful) {
                Resultado.Exito(respuesta.body() ?: emptyList())
            } else {
                Resultado.Error("Error al obtener notificaciones: ${respuesta.code()}")
            }
        } catch (e: Exception) {
            Resultado.Error("Error de red: ${e.localizedMessage}", e)
        }
    }
}
