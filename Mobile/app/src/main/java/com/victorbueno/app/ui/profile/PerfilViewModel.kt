package com.victorbueno.app.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorbueno.app.data.models.ActualizacionUsuario
import com.victorbueno.app.data.models.RespuestaSubida
import com.victorbueno.app.data.models.Usuario
import com.victorbueno.app.data.repository.RepositorioArchivo
import com.victorbueno.app.data.repository.RepositorioAutenticacion
import com.victorbueno.app.utils.Resultado
import kotlinx.coroutines.launch
import java.io.File

class PerfilViewModel(
    private val repositorioAuth: RepositorioAutenticacion,
    private val repositorioArchivo: RepositorioArchivo
) : ViewModel() {

    private val _estadoPerfil = MutableLiveData<Resultado<Usuario>>()
    val estadoPerfil: LiveData<Resultado<Usuario>> = _estadoPerfil

    private val _estadoActualizacion = MutableLiveData<Resultado<Usuario>>()
    val estadoActualizacion: LiveData<Resultado<Usuario>> = _estadoActualizacion

    private val _estadoSubida = MutableLiveData<Resultado<RespuestaSubida>>()
    val estadoSubida: LiveData<Resultado<RespuestaSubida>> = _estadoSubida

    fun cargarPerfil() {
        _estadoPerfil.value = Resultado.Cargando
        viewModelScope.launch {
            _estadoPerfil.value = repositorioAuth.obtenerPerfil()
        }
    }

    fun actualizarPerfil(nombre: String, apellido: String, email: String, clave: String? = null, foto: String? = null) {
        _estadoActualizacion.value = Resultado.Cargando
        viewModelScope.launch {
            val solicitud = ActualizacionUsuario(
                nombre = nombre.trim(),
                apellido = apellido.trim(),
                email = email.trim(),
                password = if (clave.isNullOrBlank()) null else clave,
                foto = foto
            )
            _estadoActualizacion.value = repositorioAuth.actualizarPerfil(solicitud)
        }
    }

    fun subirAvatar(archivo: File) {
        _estadoSubida.value = Resultado.Cargando
        viewModelScope.launch {
            _estadoSubida.value = repositorioArchivo.subirArchivo(archivo)
        }
    }
}
