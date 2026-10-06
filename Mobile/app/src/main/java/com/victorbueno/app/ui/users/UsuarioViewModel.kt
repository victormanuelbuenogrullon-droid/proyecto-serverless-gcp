package com.victorbueno.app.ui.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorbueno.app.data.models.ActualizacionUsuario
import com.victorbueno.app.data.models.CreacionUsuario
import com.victorbueno.app.data.models.RespuestaSubida
import com.victorbueno.app.data.models.Usuario
import com.victorbueno.app.data.repository.RepositorioArchivo
import com.victorbueno.app.data.repository.RepositorioUsuario
import com.victorbueno.app.utils.Resultado
import kotlinx.coroutines.launch
import java.io.File

class UsuarioViewModel(
    private val repositorioUsuario: RepositorioUsuario,
    private val repositorioArchivo: RepositorioArchivo
) : ViewModel() {

    private val _estadoLista = MutableLiveData<Resultado<List<Usuario>>>()
    val estadoLista: LiveData<Resultado<List<Usuario>>> = _estadoLista

    private val _estadoDetalle = MutableLiveData<Resultado<Usuario>>()
    val estadoDetalle: LiveData<Resultado<Usuario>> = _estadoDetalle

    private val _estadoAccion = MutableLiveData<Resultado<Usuario>>()
    val estadoAccion: LiveData<Resultado<Usuario>> = _estadoAccion

    private val _estadoEliminar = MutableLiveData<Resultado<Boolean>>()
    val estadoEliminar: LiveData<Resultado<Boolean>> = _estadoEliminar

    private val _estadoSubida = MutableLiveData<Resultado<RespuestaSubida>>()
    val estadoSubida: LiveData<Resultado<RespuestaSubida>> = _estadoSubida

    fun cargarUsuarios() {
        _estadoLista.value = Resultado.Cargando
        viewModelScope.launch {
            _estadoLista.value = repositorioUsuario.obtenerUsuarios()
        }
    }

    fun cargarUsuarioPorId(id: Int) {
        _estadoDetalle.value = Resultado.Cargando
        viewModelScope.launch {
            _estadoDetalle.value = repositorioUsuario.obtenerUsuarioPorId(id)
        }
    }

    fun crearUsuario(nombre: String, apellido: String, email: String, clave: String, foto: String? = null) {
        if (nombre.isBlank() || apellido.isBlank() || email.isBlank() || clave.isBlank()) {
            _estadoAccion.value = Resultado.Error("Complete todos los campos obligatorios.")
            return
        }
        _estadoAccion.value = Resultado.Cargando
        viewModelScope.launch {
            val solicitud = CreacionUsuario(nombre.trim(), apellido.trim(), email.trim(), clave, foto)
            _estadoAccion.value = repositorioUsuario.crearUsuario(solicitud)
        }
    }

    fun actualizarUsuario(id: Int, nombre: String, apellido: String, email: String, clave: String? = null, foto: String? = null) {
        _estadoAccion.value = Resultado.Cargando
        viewModelScope.launch {
            val solicitud = ActualizacionUsuario(
                nombre = nombre.trim(),
                apellido = apellido.trim(),
                email = email.trim(),
                password = if (clave.isNullOrBlank()) null else clave,
                foto = foto
            )
            _estadoAccion.value = repositorioUsuario.actualizarUsuario(id, solicitud)
        }
    }

    fun eliminarUsuario(id: Int) {
        _estadoEliminar.value = Resultado.Cargando
        viewModelScope.launch {
            _estadoEliminar.value = repositorioUsuario.eliminarUsuario(id)
        }
    }

    fun subirFoto(archivo: File) {
        _estadoSubida.value = Resultado.Cargando
        viewModelScope.launch {
            _estadoSubida.value = repositorioArchivo.subirArchivo(archivo)
        }
    }
}
