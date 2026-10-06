package com.victorbueno.app.ui.dashboard

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.victorbueno.app.data.models.ElementoNotificacion
import com.victorbueno.app.databinding.ActividadActividadRecienteBinding
import com.victorbueno.app.utils.FabricaViewModel
import com.victorbueno.app.utils.Resultado

class ActividadActividadReciente : AppCompatActivity() {

    private lateinit var binding: ActividadActividadRecienteBinding
    private val panelViewModel: PanelViewModel by viewModels { FabricaViewModel(this) }
    private val adaptadorNotificacion = AdaptadorNotificacion()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActividadActividadRecienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        configurarLista()
        configurarEventos()
        observarViewModel()

        panelViewModel.cargarNotificaciones()
    }

    private fun configurarToolbar() {
        binding.toolbarActividadReciente.setNavigationOnClickListener {
            finish()
        }
    }

    private fun configurarLista() {
        binding.rvListaActividadReciente.apply {
            layoutManager = LinearLayoutManager(this@ActividadActividadReciente)
            adapter = adaptadorNotificacion
        }
    }

    private fun configurarEventos() {
        binding.swipeRefreshActividadReciente.setOnRefreshListener {
            panelViewModel.cargarNotificaciones()
        }
    }

    private fun observarViewModel() {
        panelViewModel.estadoNotificaciones.observe(this) { estado ->
            when (estado) {
                is Resultado.Cargando -> {
                    binding.progressBarActividadReciente.visibility = View.VISIBLE
                    binding.tvActividadVacia.visibility = View.GONE
                }
                is Resultado.Exito -> {
                    binding.progressBarActividadReciente.visibility = View.GONE
                    binding.swipeRefreshActividadReciente.isRefreshing = false
                    adaptadorNotificacion.actualizarLista(estado.datos)
                    binding.tvActividadVacia.visibility = if (estado.datos.isEmpty()) View.VISIBLE else View.GONE
                }
                is Resultado.Error -> {
                    binding.progressBarActividadReciente.visibility = View.GONE
                    binding.swipeRefreshActividadReciente.isRefreshing = false
                    Toast.makeText(this, estado.mensaje, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
