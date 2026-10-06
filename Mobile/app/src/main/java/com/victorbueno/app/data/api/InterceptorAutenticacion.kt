package com.victorbueno.app.data.api

import com.victorbueno.app.utils.AdministradorSesion
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

class InterceptorAutenticacion(private val administradorSesion: AdministradorSesion) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val peticionOriginal = chain.request()
        val token = administradorSesion.obtenerToken()
        val urlBase = administradorSesion.obtenerUrlBase()

        val constructor = peticionOriginal.newBuilder()

        val urlDestino = urlBase.toHttpUrlOrNull()
        if (urlDestino != null) {
            val nuevaUrl = peticionOriginal.url.newBuilder()
                .scheme(urlDestino.scheme)
                .host(urlDestino.host)
                .port(urlDestino.port)
                .build()
            constructor.url(nuevaUrl)
        }

        if (!token.isNullOrBlank()) {
            constructor.addHeader("Authorization", "Bearer $token")
        }

        constructor.addHeader("Accept", "application/json")

        return chain.proceed(constructor.build())
    }
}
