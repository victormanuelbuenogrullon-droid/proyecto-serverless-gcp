package com.victorbueno.app.data.api

import android.content.Context
import com.victorbueno.app.utils.AdministradorSesion
import com.victorbueno.app.utils.Constantes
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ClienteApi {

    @Volatile
    private var instanciaServicio: ServicioApi? = null
    @Volatile
    private var urlBaseActual: String? = null

    fun obtenerServicio(context: Context): ServicioApi {
        val adminSesion = AdministradorSesion(context.applicationContext)
        val urlBase = adminSesion.obtenerUrlBase()

        if (instanciaServicio == null || urlBaseActual != urlBase) {
            synchronized(this) {
                if (instanciaServicio == null || urlBaseActual != urlBase) {
                    urlBaseActual = urlBase
                    instanciaServicio = crearRetrofit(adminSesion, urlBase).create(ServicioApi::class.java)
                }
            }
        }
        return instanciaServicio!!
    }

    fun reiniciarInstancia() {
        synchronized(this) {
            instanciaServicio = null
            urlBaseActual = null
        }
    }

    private fun crearRetrofit(adminSesion: AdministradorSesion, urlBase: String): Retrofit {
        val interceptorRegistro = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val clienteOkHttp = OkHttpClient.Builder()
            .addInterceptor(InterceptorAutenticacion(adminSesion))
            .addInterceptor(interceptorRegistro)
            .connectTimeout(Constantes.TIEMPO_ESPERA_SEGUNDOS, TimeUnit.SECONDS)
            .readTimeout(Constantes.TIEMPO_ESPERA_SEGUNDOS, TimeUnit.SECONDS)
            .writeTimeout(Constantes.TIEMPO_ESPERA_SEGUNDOS, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(urlBase)
            .client(clienteOkHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
