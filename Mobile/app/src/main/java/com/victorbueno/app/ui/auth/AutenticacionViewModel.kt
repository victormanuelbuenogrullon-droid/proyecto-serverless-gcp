package com.victorbueno.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorbueno.app.data.models.CreacionUsuario
import com.victorbueno.app.data.models.RespuestaAutenticacion
import com.victorbueno.app.data.models.Usuario
import com.victorbueno.app.data.repository.RepositorioAutenticacion
import com.victorbueno.app.utils.Resultado
import kotlinx.coroutines.launch

class AutenticacionViewModel(private val repositorio: RepositorioAutenticacion) : ViewModel() {

    private val _estadoLogin = MutableLiveData<Resultado<RespuestaAutenticacion>>()
    val estadoLogin: LiveData<Resultado<RespuestaAutenticacion>> = _estadoLogin

    private val _estadoRegistro = MutableLiveData<Resultado<Usuario>>()
    val estadoRegistro: LiveData<Resultado<Usuario>> = _estadoRegistro

    fun iniciarSesion(email: String, clave: String) {
        if (email.isBlank() || clave.isBlank()) {
            _estadoLogin.value = Resultado.Error("Complete todos los campos.")
            return
        }
        _estadoLogin.value = Resultado.Cargando
        viewModelScope.launch {
            _estadoLogin.value = repositorio.iniciarSesion(email.trim(), clave)
        }
    }

    fun registrar(nombre: String, apellido: String, email: String, clave: String, foto: String? = null) {
        if (nombre.isBlank() || apellido.isBlank() || email.isBlank() || clave.isBlank()) {
            _estadoRegistro.value = Resultado.Error("Complete todos los campos obligatorios.")
            return
        }
        _estadoRegistro.value = Resultado.Cargando
        viewModelScope.launch {
            val solicitud = CreacionUsuario(
                nombre = nombre.trim(),
                apellido = apellido.trim(),
                email = email.trim(),
                password = clave,
                foto = foto
            )
            _estadoRegistro.value = repositorio.registrar(solicitud)
        }
    }
}
