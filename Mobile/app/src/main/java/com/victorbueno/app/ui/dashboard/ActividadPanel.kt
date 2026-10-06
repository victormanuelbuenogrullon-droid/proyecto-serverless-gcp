package com.victorbueno.app.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.victorbueno.app.data.models.DatosPanel
import com.victorbueno.app.databinding.ActividadPanelBinding
import com.victorbueno.app.ui.auth.ActividadLogin
import com.victorbueno.app.ui.benchmark.ActividadPruebaConcurrencia
import com.victorbueno.app.ui.profile.ActividadPerfil
import com.victorbueno.app.ui.users.ActividadListaUsuarios
import com.victorbueno.app.utils.AdministradorSesion
import com.victorbueno.app.utils.FabricaViewModel
import com.victorbueno.app.utils.Resultado
import com.victorbueno.app.utils.UtilidadesImagen

class ActividadPanel : AppCompatActivity() {

    private lateinit var binding: ActividadPanelBinding
    private val panelViewModel: PanelViewModel by viewModels { FabricaViewModel(this) }
    private lateinit var administradorSesion: AdministradorSesion
    private val adaptadorNotificacion = AdaptadorNotificacion()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActividadPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        administradorSesion = AdministradorSesion(this)

        configurarLista()
        configurarEventos()
        observarViewModel()

        panelViewModel.cargarPanel()
    }

    override fun onResume() {
        super.onResume()
        panelViewModel.cargarPanel()
    }

    private fun configurarLista() {
        binding.rvNotificacionesPanel.apply {
            layoutManager = LinearLayoutManager(this@ActividadPanel)
            adapter = adaptadorNotificacion
        }
    }

    private fun configurarEventos() {
        binding.swipeRefreshPanel.setOnRefreshListener {
            panelViewModel.cargarPanel()
        }

        binding.cardModuloUsuarios.setOnClickListener {
            startActivity(Intent(this, ActividadListaUsuarios::class.java))
        }

        binding.cardModuloArchivos.setOnClickListener {
            startActivity(Intent(this, com.victorbueno.app.ui.files.ActividadGestionArchivos::class.java))
        }

        binding.cardModuloRendimiento.setOnClickListener {
            startActivity(Intent(this, ActividadPruebaConcurrencia::class.java))
        }

        binding.cardModuloActividadReciente.setOnClickListener {
            startActivity(Intent(this, ActividadActividadReciente::class.java))
        }

        binding.cardModuloPerfil.setOnClickListener {
            startActivity(Intent(this, ActividadPerfil::class.java))
        }

        binding.btnCerrarSesion.setOnClickListener {
            mostrarDialogoCerrarSesion()
        }
    }

    private fun observarViewModel() {
        panelViewModel.estadoPanel.observe(this) { estado ->
            when (estado) {
                is Resultado.Cargando -> {
                    binding.swipeRefreshPanel.isRefreshing = true
                }
                is Resultado.Exito -> {
                    binding.swipeRefreshPanel.isRefreshing = false
                    mostrarDatos(estado.datos)
                }
                is Resultado.Error -> {
                    binding.swipeRefreshPanel.isRefreshing = false
                    Toast.makeText(this, estado.mensaje, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarDatos(datos: DatosPanel) {
        val usuario = datos.perfil ?: administradorSesion.obtenerUsuario()
        if (usuario != null) {
            binding.tvBienvenida.text = "Hola, ${usuario.nombre}"
            binding.tvEmailUsuario.text = usuario.email

            val urlFoto = UtilidadesImagen.formatearUrlImagen(administradorSesion.obtenerUrlBase(), usuario.foto)
            if (!urlFoto.isNullOrBlank()) {
                Glide.with(this)
                    .load(urlFoto)
                    .circleCrop()
                    .into(binding.ivAvatarPanel)
            }
        }

        if (datos.estadisticas != null) {
            binding.tvContadorUsuarios.text = datos.estadisticas.totalUsuarios.toString()
            binding.tvContadorArchivos.text = datos.estadisticas.totalArchivos.toString()
            binding.tvEstadoServidor.text = datos.estadisticas.estadoSistema
        }

        adaptadorNotificacion.actualizarLista(datos.notificaciones)
    }

    private fun mostrarDialogoCerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Deseas salir de la aplicación?")
            .setPositiveButton("Sí, salir") { _, _ ->
                panelViewModel.cerrarSesion()
                val intent = Intent(this, ActividadLogin::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
