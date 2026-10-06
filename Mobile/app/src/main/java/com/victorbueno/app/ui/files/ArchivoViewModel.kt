package com.victorbueno.app.ui.files

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorbueno.app.data.models.ElementoArchivo
import com.victorbueno.app.data.repository.RepositorioArchivo
import com.victorbueno.app.utils.Resultado
import com.victorbueno.app.utils.UtilidadesImagen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ArchivoViewModel(private val repositorioArchivo: RepositorioArchivo) : ViewModel() {

    private val _estadoArchivos = MutableLiveData<Resultado<List<ElementoArchivo>>>()
    val estadoArchivos: LiveData<Resultado<List<ElementoArchivo>>> = _estadoArchivos

    private val _estadoOperacion = MutableLiveData<Resultado<String>?>()
    val estadoOperacion: LiveData<Resultado<String>?> = _estadoOperacion

    fun cargarArchivos() {
        _estadoArchivos.value = Resultado.Cargando
        viewModelScope.launch {
            val resultado = repositorioArchivo.listarArchivos()
            _estadoArchivos.postValue(resultado)
        }
    }

    fun subirArchivoDesdeUri(context: Context, uri: Uri) {
        _estadoOperacion.value = Resultado.Cargando
        viewModelScope.launch(Dispatchers.IO) {
            val archivo = UtilidadesImagen.obtenerArchivoDesdeUri(context, uri)
            if (archivo == null || !archivo.exists()) {
                _estadoOperacion.postValue(Resultado.Error("No se pudo leer el archivo seleccionado"))
                return@launch
            }
            subirArchivoFisico(archivo)
        }
    }

    fun subirMultiplesArchivos(context: Context, uris: List<Uri>) {
        _estadoOperacion.value = Resultado.Cargando
        viewModelScope.launch {
            val archivos = withContext(Dispatchers.IO) {
                UtilidadesImagen.obtenerArchivosDesdeUris(context, uris)
            }
            if (archivos.isEmpty()) {
                _estadoOperacion.postValue(Resultado.Error("No se pudieron leer las imágenes seleccionadas"))
                return@launch
            }

            val resultados = withContext(Dispatchers.IO) {
                coroutineScope {
                    archivos.map { archivo ->
                        async(Dispatchers.IO) {
                            repositorioArchivo.subirArchivo(archivo)
                        }
                    }.awaitAll()
                }
            }

            val exitos = resultados.count { it is Resultado.Exito }
            val fallos = resultados.size - exitos
            if (fallos == 0) {
                _estadoOperacion.postValue(Resultado.Exito("Se subieron $exitos archivo(s) concurrentemente"))
            } else {
                _estadoOperacion.postValue(Resultado.Exito("Subidos: $exitos, Fallidos: $fallos"))
            }
            cargarArchivos()
        }
    }

    fun subirArchivoFisico(archivo: File) {
        _estadoOperacion.postValue(Resultado.Cargando)
        viewModelScope.launch {
            val resultado = repositorioArchivo.subirArchivo(archivo)
            when (resultado) {
                is Resultado.Exito -> {
                    _estadoOperacion.postValue(Resultado.Exito("Archivo '${archivo.name}' subido correctamente"))
                    cargarArchivos()
                }
                is Resultado.Error -> {
                    _estadoOperacion.postValue(Resultado.Error(resultado.mensaje))
                }
                is Resultado.Cargando -> {}
            }
        }
    }

    fun eliminarArchivo(idArchivo: Int) {
        _estadoOperacion.value = Resultado.Cargando
        viewModelScope.launch {
            val resultado = repositorioArchivo.eliminarArchivo(idArchivo)
            when (resultado) {
                is Resultado.Exito -> {
                    _estadoOperacion.postValue(Resultado.Exito("Archivo eliminado correctamente"))
                    cargarArchivos()
                }
                is Resultado.Error -> {
                    _estadoOperacion.postValue(Resultado.Error(resultado.mensaje))
                }
                is Resultado.Cargando -> {}
            }
        }
    }

    fun limpiarEstadoOperacion() {
        _estadoOperacion.value = null
    }
}
