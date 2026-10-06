package com.victorbueno.app.ui.users

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.victorbueno.app.data.models.Usuario
import com.victorbueno.app.databinding.ActividadFormularioUsuarioBinding
import com.victorbueno.app.utils.AdministradorSesion
import com.victorbueno.app.utils.FabricaViewModel
import com.victorbueno.app.utils.Resultado
import com.victorbueno.app.utils.UtilidadesImagen
import java.io.File

class ActividadFormularioUsuario : AppCompatActivity() {

    private lateinit var binding: ActividadFormularioUsuarioBinding
    private val usuarioViewModel: UsuarioViewModel by viewModels { FabricaViewModel(this) }
    private var usuarioEnEdicion: Usuario? = null
    private var urlFotoSubida: String? = null

    private val selectorImagen = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val archivo: File? = UtilidadesImagen.obtenerArchivoDesdeUri(this, it)
            if (archivo != null) {
                Glide.with(this).load(archivo).circleCrop().into(binding.ivVistaPreviaAvatarFormulario)
                usuarioViewModel.subirFoto(archivo)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActividadFormularioUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usuarioEnEdicion = intent.getSerializableExtra("USUARIO_EDITAR") as? Usuario

        inicializarInterfaz()
        configurarEventos()
        observarViewModel()
    }

    private fun inicializarInterfaz() {
        if (usuarioEnEdicion != null) {
            binding.toolbarFormularioUsuario.title = "Editar Usuario"
            binding.etFormNombre.setText(usuarioEnEdicion!!.nombre)
            binding.etFormApellido.setText(usuarioEnEdicion!!.apellido)
            binding.etFormEmail.setText(usuarioEnEdicion!!.email)
            binding.btnGuardarUsuario.text = "Guardar Cambios"
            urlFotoSubida = usuarioEnEdicion!!.foto

            val urlBase = AdministradorSesion(this).obtenerUrlBase()
            val urlFoto = UtilidadesImagen.formatearUrlImagen(urlBase, usuarioEnEdicion!!.foto)
            if (!urlFoto.isNullOrBlank()) {
                Glide.with(this).load(urlFoto).circleCrop().into(binding.ivVistaPreviaAvatarFormulario)
            }
        } else {
            binding.toolbarFormularioUsuario.title = "Nuevo Usuario"
            binding.btnGuardarUsuario.text = "Crear Usuario"
        }

        binding.toolbarFormularioUsuario.setNavigationOnClickListener {
            finish()
        }
    }

    private fun configurarEventos() {
        binding.btnSeleccionarFoto.setOnClickListener {
            selectorImagen.launch("image/*")
        }

        binding.btnGuardarUsuario.setOnClickListener {
            val nombre = binding.etFormNombre.text.toString().trim()
            val apellido = binding.etFormApellido.text.toString().trim()
            val email = binding.etFormEmail.text.toString().trim()
            val clave = binding.etFormPassword.text.toString().trim()

            if (usuarioEnEdicion != null) {
                usuarioViewModel.actualizarUsuario(
                    id = usuarioEnEdicion!!.id,
                    nombre = nombre,
                    apellido = apellido,
                    email = email,
                    clave = if (clave.isEmpty()) null else clave,
                    foto = urlFotoSubida
                )
            } else {
                usuarioViewModel.crearUsuario(
                    nombre = nombre,
                    apellido = apellido,
                    email = email,
                    clave = clave,
                    foto = urlFotoSubida
                )
            }
        }
    }

    private fun observarViewModel() {
        usuarioViewModel.estadoSubida.observe(this) { estado ->
            when (estado) {
                is Resultado.Cargando -> {
                    binding.progressBarFormulario.visibility = View.VISIBLE
                    binding.btnSeleccionarFoto.isEnabled = false
                }
                is Resultado.Exito -> {
                    binding.progressBarFormulario.visibility = View.GONE
                    binding.btnSeleccionarFoto.isEnabled = true
                    urlFotoSubida = estado.datos.url
                    Toast.makeText(this, "Foto cargada", Toast.LENGTH_SHORT).show()
                }
                is Resultado.Error -> {
                    binding.progressBarFormulario.visibility = View.GONE
                    binding.btnSeleccionarFoto.isEnabled = true
                    Toast.makeText(this, "Error al subir foto: ${estado.mensaje}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        usuarioViewModel.estadoAccion.observe(this) { estado ->
            when (estado) {
                is Resultado.Cargando -> {
                    binding.progressBarFormulario.visibility = View.VISIBLE
                    binding.btnGuardarUsuario.isEnabled = false
                }
                is Resultado.Exito -> {
                    binding.progressBarFormulario.visibility = View.GONE
                    binding.btnGuardarUsuario.isEnabled = true
                    Toast.makeText(this, "Guardado exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Resultado.Error -> {
                    binding.progressBarFormulario.visibility = View.GONE
                    binding.btnGuardarUsuario.isEnabled = true
                    Toast.makeText(this, estado.mensaje, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
