package com.victorbueno.app.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.victorbueno.app.data.models.Usuario
import com.victorbueno.app.databinding.ActividadPerfilBinding
import com.victorbueno.app.utils.AdministradorSesion
import com.victorbueno.app.utils.FabricaViewModel
import com.victorbueno.app.utils.Resultado
import com.victorbueno.app.utils.UtilidadesImagen
import java.io.File

class ActividadPerfil : AppCompatActivity() {

    private lateinit var binding: ActividadPerfilBinding
    private val perfilViewModel: PerfilViewModel by viewModels { FabricaViewModel(this) }
    private lateinit var administradorSesion: AdministradorSesion
    private var urlFotoActual: String? = null

    private val selectorAvatar = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val archivo: File? = UtilidadesImagen.obtenerArchivoDesdeUri(this, it)
            if (archivo != null) {
                Glide.with(this).load(archivo).circleCrop().into(binding.ivAvatarPerfil)
                perfilViewModel.subirAvatar(archivo)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActividadPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        administradorSesion = AdministradorSesion(this)

        configurarToolbar()
        configurarEventos()
        observarViewModel()

        perfilViewModel.cargarPerfil()
    }

    private fun configurarToolbar() {
        binding.toolbarPerfil.setNavigationOnClickListener {
            finish()
        }
    }

    private fun configurarEventos() {
        binding.btnCambiarFotoPerfil.setOnClickListener {
            selectorAvatar.launch("image/*")
        }

        binding.btnGuardarPerfil.setOnClickListener {
            val nombre = binding.etPerfilNombre.text.toString().trim()
            val apellido = binding.etPerfilApellido.text.toString().trim()
            val email = binding.etPerfilEmail.text.toString().trim()
            val clave = binding.etPerfilPassword.text.toString().trim()

            if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Complete los campos obligatorios.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            perfilViewModel.actualizarPerfil(
                nombre = nombre,
                apellido = apellido,
                email = email,
                clave = if (clave.isEmpty()) null else clave,
                foto = urlFotoActual
            )
        }
    }

    private fun observarViewModel() {
        perfilViewModel.estadoPerfil.observe(this) { estado ->
            when (estado) {
                is Resultado.Cargando -> {
                    binding.progressBarPerfil.visibility = View.VISIBLE
                }
                is Resultado.Exito -> {
                    binding.progressBarPerfil.visibility = View.GONE
                    mostrarDatosUsuario(estado.datos)
                }
                is Resultado.Error -> {
                    binding.progressBarPerfil.visibility = View.GONE
                    val local = administradorSesion.obtenerUsuario()
                    if (local != null) mostrarDatosUsuario(local)
                }
            }
        }

        perfilViewModel.estadoSubida.observe(this) { estado ->
            when (estado) {
                is Resultado.Cargando -> {
                    binding.progressBarPerfil.visibility = View.VISIBLE
                    binding.btnCambiarFotoPerfil.isEnabled = false
                }
                is Resultado.Exito -> {
                    binding.progressBarPerfil.visibility = View.GONE
                    binding.btnCambiarFotoPerfil.isEnabled = true
                    urlFotoActual = estado.datos.url
                    Toast.makeText(this, "Foto actualizada", Toast.LENGTH_SHORT).show()
                }
                is Resultado.Error -> {
                    binding.progressBarPerfil.visibility = View.GONE
                    binding.btnCambiarFotoPerfil.isEnabled = true
                    Toast.makeText(this, "Error: ${estado.mensaje}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        perfilViewModel.estadoActualizacion.observe(this) { estado ->
            when (estado) {
                is Resultado.Cargando -> {
                    binding.progressBarPerfil.visibility = View.VISIBLE
                    binding.btnGuardarPerfil.isEnabled = false
                }
                is Resultado.Exito -> {
                    binding.progressBarPerfil.visibility = View.GONE
                    binding.btnGuardarPerfil.isEnabled = true
                    administradorSesion.guardarUsuario(estado.datos)
                    Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Resultado.Error -> {
                    binding.progressBarPerfil.visibility = View.GONE
                    binding.btnGuardarPerfil.isEnabled = true
                    Toast.makeText(this, estado.mensaje, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarDatosUsuario(usuario: Usuario) {
        binding.etPerfilNombre.setText(usuario.nombre)
        binding.etPerfilApellido.setText(usuario.apellido)
        binding.etPerfilEmail.setText(usuario.email)
        urlFotoActual = usuario.foto

        val urlBase = administradorSesion.obtenerUrlBase()
        val urlFoto = UtilidadesImagen.formatearUrlImagen(urlBase, usuario.foto)
        if (!urlFoto.isNullOrBlank()) {
            Glide.with(this)
                .load(urlFoto)
                .circleCrop()
                .into(binding.ivAvatarPerfil)
        }
    }
}
