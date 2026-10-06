package com.victorbueno.app.ui.benchmark

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.victorbueno.app.data.models.ResultadoRendimiento
import com.victorbueno.app.databinding.ActividadPruebaConcurrenciaBinding
import com.victorbueno.app.utils.FabricaViewModel
import java.util.Locale

class ActividadPruebaConcurrencia : AppCompatActivity() {

    private lateinit var binding: ActividadPruebaConcurrenciaBinding
    private val rendimientoViewModel: RendimientoViewModel by viewModels { FabricaViewModel(this) }
    private val adaptadorRendimiento = AdaptadorRendimiento()

    private val selectorGaleria = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            rendimientoViewModel.cargarImagenesDesdeUris(this, uris)
            binding.tvEstadoLote.text = "Lote preparado: ${uris.size} imágenes seleccionadas de tu galería"
        } else {
            Toast.makeText(this, "No se seleccionaron imágenes", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActividadPruebaConcurrenciaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        configurarLista()
        configurarEventos()
        observarViewModel()
    }

    private fun configurarToolbar() {
        binding.toolbarRendimiento.setNavigationOnClickListener {
            finish()
        }
    }

    private fun configurarLista() {
        binding.rvTareasRendimiento.apply {
            layoutManager = LinearLayoutManager(this@ActividadPruebaConcurrencia)
            adapter = adaptadorRendimiento
        }
    }

    private fun configurarEventos() {
        binding.btnSeleccionarGaleria.setOnClickListener {
            selectorGaleria.launch("image/*")
        }

        binding.btnEjecutarSecuencial.setOnClickListener {
            val lista = rendimientoViewModel.archivosSeleccionados.value
            if (lista.isNullOrEmpty()) {
                Toast.makeText(this, "Primero selecciona imágenes de tu galería", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            rendimientoViewModel.ejecutarSecuencial()
        }

        binding.btnEjecutarConcurrente.setOnClickListener {
            val lista = rendimientoViewModel.archivosSeleccionados.value
            if (lista.isNullOrEmpty()) {
                Toast.makeText(this, "Primero selecciona imágenes de tu galería", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            rendimientoViewModel.ejecutarConcurrente()
        }
    }

    private fun observarViewModel() {
        rendimientoViewModel.ejecutando.observe(this) { ejecutando ->
            binding.btnEjecutarSecuencial.isEnabled = !ejecutando
            binding.btnEjecutarConcurrente.isEnabled = !ejecutando
            binding.btnSeleccionarGaleria.isEnabled = !ejecutando
        }

        rendimientoViewModel.listaTareas.observe(this) { tareas ->
            adaptadorRendimiento.actualizarTareas(tareas)
        }

        rendimientoViewModel.actualizacionTarea.observe(this) { tarea ->
            adaptadorRendimiento.actualizarTareaIndividual(tarea)
        }

        rendimientoViewModel.resultadoSecuencial.observe(this) { resultado ->
            if (resultado != null) {
                val segundos = resultado.tiempoTotalMs / 1000.0
                binding.tvResultadoSecuencial.text = String.format(Locale.US, "%.2f s (%d ms)", segundos, resultado.tiempoTotalMs)
                actualizarAceleracion(resultado, rendimientoViewModel.resultadoConcurrente.value)
            }
        }

        rendimientoViewModel.resultadoConcurrente.observe(this) { resultado ->
            if (resultado != null) {
                val segundos = resultado.tiempoTotalMs / 1000.0
                binding.tvResultadoConcurrente.text = String.format(Locale.US, "%.2f s (%d ms)", segundos, resultado.tiempoTotalMs)
                actualizarAceleracion(rendimientoViewModel.resultadoSecuencial.value, resultado)
            }
        }
    }

    private fun actualizarAceleracion(sec: ResultadoRendimiento?, conc: ResultadoRendimiento?) {
        if (sec != null && conc != null && conc.tiempoTotalMs > 0) {
            val factor = sec.tiempoTotalMs.toDouble() / conc.tiempoTotalMs.toDouble()
            val porcentaje = if (sec.tiempoTotalMs > conc.tiempoTotalMs) {
                ((sec.tiempoTotalMs - conc.tiempoTotalMs).toDouble() / sec.tiempoTotalMs.toDouble()) * 100.0
            } else 0.0
            binding.tvResultadoAceleracion.text = String.format(Locale.US, "%.2fx más rápido (~%.0f%% menos tiempo)", factor, porcentaje)
        }
    }
}
