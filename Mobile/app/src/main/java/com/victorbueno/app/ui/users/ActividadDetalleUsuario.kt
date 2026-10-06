package com.victorbueno.app.ui.users

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.victorbueno.app.data.models.Usuario
import com.victorbueno.app.databinding.ActividadDetalleUsuarioBinding
import com.victorbueno.app.utils.AdministradorSesion
import com.victorbueno.app.utils.FabricaViewModel
import com.victorbueno.app.utils.Resultado
import com.victorbueno.app.utils.UtilidadesImagen

class ActividadDetalleUsuario : AppCompatActivity() {

    private lateinit var binding: ActividadDetalleUsuarioBinding
    private val usuarioViewModel: UsuarioViewModel by viewModels { FabricaViewModel(this) }
    private var usuarioActual: Usuario? = null
    private var idUsuario: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActividadDetalleUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idUsuario = intent.getIntExtra("ID_USUARIO", -1)
        if (idUsuario == -1) {
            finish()
            return
        }

        configurarToolbar()
        configurarEventos()
        observarViewModel()

        usuarioViewModel.cargarUsuarioPorId(idUsuario)
    }

    override fun onResume() {
        super.onResume()
        if (idUsuario != -1) usuarioViewModel.cargarUsuarioPorId(idUsuario)
    }

    private fun configurarToolbar() {
        binding.toolbarDetalle.setNavigationOnClickListener {
            finish()
        }
    }

    private fun configurarEventos() {
        binding.btnEditarDetalle.setOnClickListener {
            usuarioActual?.let { usuario ->
                val intent = Intent(this, ActividadFormularioUsuario::class.java).apply {
                    putExtra("USUARIO_EDITAR", usuario)
                }
                startActivity(intent)
            }
        }

        binding.btnEliminarDetalle.setOnClickListener {
            usuarioActual?.let { usuario ->
                AlertDialog.Builder(this)
                    .setTitle("Eliminar Usuario")
                    .setMessage("¿Deseas eliminar a ${usuario.nombreCompleto}?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        usuarioViewModel.eliminarUsuario(usuario.id)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    private fun observarViewModel() {
        usuarioViewModel.estadoDetalle.observe(this) { estado ->
            when (estado) {
                is Resultado.Cargando -> {}
                is Resultado.Exito -> {
                    usuarioActual = estado.datos
                    mostrarDetalle(estado.datos)
                }
                is Resultado.Error -> {
                    Toast.makeText(this, estado.mensaje, Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }

        usuarioViewModel.estadoEliminar.observe(this) { estado ->
            when (estado) {
                is Resultado.Exito -> {
                    Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Resultado.Error -> {
                    Toast.makeText(this, estado.mensaje, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun mostrarDetalle(usuario: Usuario) {
        val urlBase = AdministradorSesion(this).obtenerUrlBase()

        binding.tvNombreCompletoDetalle.text = usuario.nombreCompleto
        binding.tvEmailDetalle.text = usuario.email
        binding.tvIdDetalle.text = "#${usuario.id}"
        binding.tvFechaCreacionDetalle.text = usuario.createdAt?.replace("T", " ")?.take(19) ?: "N/A"
        binding.tvReferenciaFotoDetalle.text = usuario.foto ?: "Sin foto"

        val urlFoto = UtilidadesImagen.formatearUrlImagen(urlBase, usuario.foto)
        if (!urlFoto.isNullOrBlank()) {
            Glide.with(this)
                .load(urlFoto)
                .circleCrop()
                .into(binding.ivAvatarDetalle)
        }
    }
}
