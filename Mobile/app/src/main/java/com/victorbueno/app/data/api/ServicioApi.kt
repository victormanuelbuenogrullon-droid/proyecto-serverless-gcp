package com.victorbueno.app.data.api

import com.victorbueno.app.data.models.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ServicioApi {

    @POST("login")
    suspend fun iniciarSesion(@Body solicitud: SolicitudLogin): Response<RespuestaAutenticacion>

    @POST("register")
    suspend fun registrarUsuario(@Body solicitud: CreacionUsuario): Response<Usuario>

    @GET("profile")
    suspend fun obtenerPerfil(): Response<Usuario>

    @PUT("profile")
    suspend fun actualizarPerfil(@Body solicitud: ActualizacionUsuario): Response<Usuario>

    @GET("users")
    suspend fun obtenerUsuarios(
        @Query("skip") saltar: Int = 0,
        @Query("limit") limite: Int = 100
    ): Response<List<Usuario>>

    @GET("users/{id}")
    suspend fun obtenerUsuarioPorId(@Path("id") id: Int): Response<Usuario>

    @POST("users")
    suspend fun crearUsuario(@Body solicitud: CreacionUsuario): Response<Usuario>

    @PUT("users/{id}")
    suspend fun actualizarUsuario(
        @Path("id") id: Int,
        @Body solicitud: ActualizacionUsuario
    ): Response<Usuario>

    @DELETE("users/{id}")
    suspend fun eliminarUsuario(@Path("id") id: Int): Response<ResponseBody>

    @Multipart
    @POST("upload")
    suspend fun subirArchivo(
        @Part archivo: MultipartBody.Part
    ): Response<RespuestaSubida>

    @DELETE("upload/{id}")
    suspend fun eliminarArchivo(@Path("id") id: Int): Response<ResponseBody>

    @GET("upload/list")
    suspend fun listarArchivos(
        @Query("skip") saltar: Int = 0,
        @Query("limit") limite: Int = 100
    ): Response<List<ElementoArchivo>>

    @GET("stats")
    suspend fun obtenerEstadisticas(): Response<RespuestaEstadisticas>

    @GET("notifications")
    suspend fun obtenerNotificaciones(@Query("limit") limite: Int = 10): Response<List<ElementoNotificacion>>

    @POST("process/simulate-task")
    suspend fun simularTarea(
        @Query("task_id") idTarea: Int,
        @Query("delay_ms") retrasoMs: Int = 400
    ): Response<RespuestaSimulacion>
}
