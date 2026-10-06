package com.victorbueno.app.ui.files

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.victorbueno.app.R
import com.victorbueno.app.data.models.ElementoArchivo
import com.victorbueno.app.databinding.ActividadGestionArchivosBinding
import com.victorbueno.app.utils.AdministradorSesion
import com.victorbueno.app.utils.FabricaViewModel
import com.victorbueno.app.utils.Resultado
import com.victorbueno.app.utils.UtilidadesImagen

class ActividadGestionArchivos : AppCompatActivity() {

    private lateinit var binding: ActividadGestionArchivosBinding
    private val archivoViewModel: ArchivoViewModel by viewModels { FabricaViewModel(this) }
    private lateinit var administradorSesion: AdministradorSesion
    private lateinit var adaptadorArchivo: AdaptadorArchivo

    private val selectorGaleria = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            Toast.makeText(this, "Subiendo ${uris.size} archivo(s) en paralelo...", Toast.LENGTH_SHORT).show()
            archivoViewModel.subirMultiplesArchivos(this, uris)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActividadGestionArchivosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        administradorSesion = AdministradorSesion(this)

        configurarToolbar()
        configurarLista()
        configurarEventos()
        observarViewModel()

        archivoViewModel.cargarArchivos()
    }

    private fun configurarToolbar() {
        binding.toolbarArchivos.setNavigationOnClickListener {
            finish()
        }
    }

    private fun configurarLista() {
        adaptadorArchivo = AdaptadorArchivo(
            urlServidor = administradorSesion.obtenerUrlBase(),
            alEliminar = { archivo ->
                mostrarDialogoEliminar(archivo)
            },
            alHacerClic = { archivo ->
                mostrarDetalleArchivo(archivo)
            }
        )

        binding.rvArchivos.apply {
            layoutManager = LinearLayoutManager(this@ActividadGestionArchivos)
            adapter = adaptadorArchivo
        }
    }

    private fun configurarEventos() {
        binding.swipeRefreshArchivos.setOnRefreshListener {
            archivoViewModel.cargarArchivos()
        }

        binding.btnSubirGaleria.setOnClickListener {
            selectorGaleria.launch("*/*")
        }

        binding.fabSubirArchivo.setOnClickListener {
            selectorGaleria.launch("*/*")
        }
    }

    private fun observarViewModel() {
        archivoViewModel.estadoArchivos.observe(this) { estado ->
            when (estado) {
                is Resultado.Cargando -> {
                    binding.progressBarArchivos.visibility = View.VISIBLE
                    binding.swipeRefreshArchivos.isRefreshing = true
                }
                is Resultado.Exito -> {
                    binding.progressBarArchivos.visibility = View.GONE
                    binding.swipeRefreshArchivos.isRefreshing = false
                    val archivos = estado.datos
                    adaptadorArchivo.actualizarLista(archivos)
                    binding.layoutListaVacia.visibility = if (archivos.isEmpty()) View.VISIBLE else View.GONE
                    binding.tvResumenArchivos.text = "Total de archivos en el servidor: ${archivos.size}"
                }
                is Resultado.Error -> {
                    binding.progressBarArchivos.visibility = View.GONE
                    binding.swipeRefreshArchivos.isRefreshing = false
                    Toast.makeText(this, estado.mensaje, Toast.LENGTH_LONG).show()
                }
            }
        }

        archivoViewModel.estadoOperacion.observe(this) { estado ->
            if (estado != null) {
                when (estado) {
                    is Resultado.Cargando -> {
                        binding.progressBarArchivos.visibility = View.VISIBLE
                    }
                    is Resultado.Exito -> {
                        binding.progressBarArchivos.visibility = View.GONE
                        Toast.makeText(this, estado.datos, Toast.LENGTH_SHORT).show()
                        archivoViewModel.limpiarEstadoOperacion()
                    }
                    is Resultado.Error -> {
                        binding.progressBarArchivos.visibility = View.GONE
                        Toast.makeText(this, estado.mensaje, Toast.LENGTH_LONG).show()
                        archivoViewModel.limpiarEstadoOperacion()
                    }
                }
            }
        }
    }

    private fun mostrarDialogoEliminar(archivo: ElementoArchivo) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_file_confirm_title))
            .setMessage(getString(R.string.delete_file_confirm_msg))
            .setPositiveButton(getString(R.string.btn_delete)) { _, _ ->
                archivoViewModel.eliminarArchivo(archivo.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDetalleArchivo(archivo: ElementoArchivo) {
        val vistaDialogo = layoutInflater.inflate(R.layout.dialogo_detalle_archivo, null)
        val ivVistaPrevia = vistaDialogo.findViewById<ImageView>(R.id.ivVistaPreviaDialogo)
        val tvNombre = vistaDialogo.findViewById<TextView>(R.id.tvNombreDialogo)
        val tvDetalles = vistaDialogo.findViewById<TextView>(R.id.tvDetallesDialogo)
        val tvRuta = vistaDialogo.findViewById<TextView>(R.id.tvRutaDialogo)

        tvNombre.text = archivo.filename
        
        val tamanoTexto = if (archivo.tamanoBytes != null && archivo.tamanoBytes > 0) {
            if (archivo.tamanoBytes >= 1024 * 1024) {
                String.format("%.2f MB", archivo.tamanoBytes / (1024.0 * 1024.0))
            } else {
                "${archivo.tamanoBytes / 1024} KB"
            }
        } else "Tamaño desconocido"

        tvDetalles.text = "ID: ${archivo.id} • $tamanoTexto • Tipo: ${archivo.tipoContenido ?: "N/A"}"
        tvRuta.text = "URL: ${archivo.url}"

        val urlCompleta = UtilidadesImagen.formatearUrlImagen(administradorSesion.obtenerUrlBase(), archivo.url)
        if (!urlCompleta.isNullOrBlank()) {
            Glide.with(this)
                .load(urlCompleta)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .fitCenter()
                .into(ivVistaPrevia)
        }

        AlertDialog.Builder(this)
            .setTitle("Detalle del Archivo")
            .setView(vistaDialogo)
            .setPositiveButton("Cerrar", null)
            .setNegativeButton("Eliminar") { _, _ ->
                mostrarDialogoEliminar(archivo)
            }
            .show()
    }
}
