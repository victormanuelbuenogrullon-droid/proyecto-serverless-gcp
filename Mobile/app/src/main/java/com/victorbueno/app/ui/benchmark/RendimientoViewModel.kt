package com.victorbueno.app.ui.benchmark

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorbueno.app.data.models.ResultadoRendimiento
import com.victorbueno.app.data.models.TareaRendimiento
import com.victorbueno.app.data.repository.RepositorioRendimiento
import com.victorbueno.app.utils.UtilidadesImagen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class RendimientoViewModel(private val repositorio: RepositorioRendimiento) : ViewModel() {

    private val _ejecutando = MutableLiveData<Boolean>(false)
    val ejecutando: LiveData<Boolean> = _ejecutando

    private val _archivosSeleccionados = MutableLiveData<List<File>>(emptyList())
    val archivosSeleccionados: LiveData<List<File>> = _archivosSeleccionados

    private val _actualizacionTarea = MutableLiveData<TareaRendimiento>()
    val actualizacionTarea: LiveData<TareaRendimiento> = _actualizacionTarea

    private val _resultadoSecuencial = MutableLiveData<ResultadoRendimiento?>()
    val resultadoSecuencial: LiveData<ResultadoRendimiento?> = _resultadoSecuencial

    private val _resultadoConcurrente = MutableLiveData<ResultadoRendimiento?>()
    val resultadoConcurrente: LiveData<ResultadoRendimiento?> = _resultadoConcurrente

    private val _listaTareas = MutableLiveData<List<TareaRendimiento>>()
    val listaTareas: LiveData<List<TareaRendimiento>> = _listaTareas

    fun establecerArchivos(archivos: List<File>) {
        _archivosSeleccionados.value = archivos
        val tareasIniciales = archivos.mapIndexed { index, archivo ->
            TareaRendimiento(
                id = index + 1,
                nombre = archivo.name,
                archivo = archivo,
                tamanoKb = archivo.length() / 1024
            )
        }
        _listaTareas.value = tareasIniciales
    }

    fun cargarImagenesDesdeUris(context: Context, uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val archivos = UtilidadesImagen.obtenerArchivosDesdeUris(context, uris)
            withContext(Dispatchers.Main) {
                establecerArchivos(archivos)
            }
        }
    }

    fun ejecutarSecuencial() {
        val archivos = _archivosSeleccionados.value
        if (archivos.isNullOrEmpty() || _ejecutando.value == true) return
        _ejecutando.value = true

        val tareasIniciales = archivos.mapIndexed { index, archivo ->
            TareaRendimiento(
                id = index + 1,
                nombre = archivo.name,
                archivo = archivo,
                tamanoKb = archivo.length() / 1024
            )
        }
        _listaTareas.value = tareasIniciales

        viewModelScope.launch {
            val resultado = repositorio.ejecutarSecuencial(archivos) { tareaActualizada ->
                withContext(Dispatchers.Main) {
                    _actualizacionTarea.value = tareaActualizada
                }
            }
            withContext(Dispatchers.Main) {
                _listaTareas.value = resultado.tareas
                _resultadoSecuencial.value = resultado
                _ejecutando.value = false
            }
        }
    }

    fun ejecutarConcurrente() {
        val archivos = _archivosSeleccionados.value
        if (archivos.isNullOrEmpty() || _ejecutando.value == true) return
        _ejecutando.value = true

        val tareasIniciales = archivos.mapIndexed { index, archivo ->
            TareaRendimiento(
                id = index + 1,
                nombre = archivo.name,
                archivo = archivo,
                tamanoKb = archivo.length() / 1024
            )
        }
        _listaTareas.value = tareasIniciales

        viewModelScope.launch {
            val tiempoSecuencialMs = _resultadoSecuencial.value?.tiempoTotalMs
            val resultado = repositorio.ejecutarConcurrente(archivos, tiempoSecuencialMs) { tareaActualizada ->
                withContext(Dispatchers.Main) {
                    _actualizacionTarea.value = tareaActualizada
                }
            }
            withContext(Dispatchers.Main) {
                _listaTareas.value = resultado.tareas
                _resultadoConcurrente.value = resultado
                _ejecutando.value = false
            }
        }
    }
}
