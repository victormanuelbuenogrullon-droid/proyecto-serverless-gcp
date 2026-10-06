package com.victorbueno.app.utils

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.victorbueno.app.data.api.ClienteApi
import com.victorbueno.app.data.repository.*
import com.victorbueno.app.ui.auth.AutenticacionViewModel
import com.victorbueno.app.ui.benchmark.RendimientoViewModel
import com.victorbueno.app.ui.dashboard.PanelViewModel
import com.victorbueno.app.ui.files.ArchivoViewModel
import com.victorbueno.app.ui.profile.PerfilViewModel
import com.victorbueno.app.ui.users.UsuarioViewModel

class FabricaViewModel(private val context: Context) : ViewModelProvider.Factory {

    private val servicioApi = ClienteApi.obtenerServicio(context)
    private val adminSesion = AdministradorSesion(context)

    private val repositorioAuth = RepositorioAutenticacion(servicioApi, adminSesion)
    private val repositorioUsuario = RepositorioUsuario(servicioApi)
    private val repositorioArchivo = RepositorioArchivo(servicioApi)
    private val repositorioPanel = RepositorioPanel(servicioApi)
    private val repositorioRendimiento = RepositorioRendimiento(servicioApi)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AutenticacionViewModel::class.java) -> {
                AutenticacionViewModel(repositorioAuth) as T
            }
            modelClass.isAssignableFrom(PanelViewModel::class.java) -> {
                PanelViewModel(repositorioPanel, repositorioAuth) as T
            }
            modelClass.isAssignableFrom(UsuarioViewModel::class.java) -> {
                UsuarioViewModel(repositorioUsuario, repositorioArchivo) as T
            }
            modelClass.isAssignableFrom(RendimientoViewModel::class.java) -> {
                RendimientoViewModel(repositorioRendimiento) as T
            }
            modelClass.isAssignableFrom(PerfilViewModel::class.java) -> {
                PerfilViewModel(repositorioAuth, repositorioArchivo) as T
            }
            modelClass.isAssignableFrom(ArchivoViewModel::class.java) -> {
                ArchivoViewModel(repositorioArchivo) as T
            }
            else -> throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
        }
    }
}
