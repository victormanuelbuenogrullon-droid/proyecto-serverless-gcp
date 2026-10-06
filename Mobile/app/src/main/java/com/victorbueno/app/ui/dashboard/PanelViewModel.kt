package com.victorbueno.app.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorbueno.app.data.models.DatosPanel
import com.victorbueno.app.data.models.ElementoNotificacion
import com.victorbueno.app.data.repository.RepositorioAutenticacion
import com.victorbueno.app.data.repository.RepositorioPanel
import com.victorbueno.app.utils.Resultado
import kotlinx.coroutines.launch

class PanelViewModel(
    private val repositorioPanel: RepositorioPanel,
    private val repositorioAuth: RepositorioAutenticacion
) : ViewModel() {

    private val _estadoPanel = MutableLiveData<Resultado<DatosPanel>>()
    val estadoPanel: LiveData<Resultado<DatosPanel>> = _estadoPanel

    private val _estadoNotificaciones = MutableLiveData<Resultado<List<ElementoNotificacion>>>()
    val estadoNotificaciones: LiveData<Resultado<List<ElementoNotificacion>>> = _estadoNotificaciones

    fun cargarPanel() {
        _estadoPanel.value = Resultado.Cargando
        viewModelScope.launch {
            _estadoPanel.value = repositorioPanel.obtenerDatosPanelConcurrente()
        }
    }

    fun cargarNotificaciones() {
        _estadoNotificaciones.value = Resultado.Cargando
        viewModelScope.launch {
            _estadoNotificaciones.value = repositorioPanel.obtenerNotificaciones()
        }
    }

    fun cerrarSesion() {
        repositorioAuth.cerrarSesion()
    }
}
