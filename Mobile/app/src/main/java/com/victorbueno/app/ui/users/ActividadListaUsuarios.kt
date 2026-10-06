package com.victorbueno.app.ui.users

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.victorbueno.app.data.models.Usuario
import com.victorbueno.app.databinding.ActividadListaUsuariosBinding
import com.victorbueno.app.utils.FabricaViewModel
import com.victorbueno.app.utils.Resultado

class ActividadListaUsuarios : AppCompatActivity() {

    private lateinit var binding: ActividadListaUsuariosBinding
    private val usuarioViewModel: UsuarioViewModel by viewModels { FabricaViewModel(this) }
    private lateinit var adaptadorUsuario: AdaptadorUsuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActividadListaUsuariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        configurarLista()
        configurarEventos()
        observarViewModel()

        usuarioViewModel.cargarUsuarios()
    }

    override fun onResume() {
        super.onResume()
        usuarioViewModel.cargarUsuarios()
    }

    private fun configurarToolbar() {
        binding.toolbarListaUsuarios.setNavigationOnClickListener {
            finish()
        }
    }

    private fun configurarLista() {
        adaptadorUsuario = AdaptadorUsuario(
            usuarios = emptyList(),
            alHacerClic = { usuario ->
                val intent = Intent(this, ActividadDetalleUsuario::class.java).apply {
                    putExtra("ID_USUARIO", usuario.id)
                }
                startActivity(intent)
            },
            alEditar = { usuario ->
                val intent = Intent(this, ActividadFormularioUsuario::class.java).apply {
                    putExtra("USUARIO_EDITAR", usuario)
                }
                startActivity(intent)
            },
            alEliminar = { usuario ->
                confirmarEliminacion(usuario)
            }
        )

        binding.rvListaUsuarios.apply {
            layoutManager = LinearLayoutManager(this@ActividadListaUsuarios)
            adapter = adaptadorUsuario
        }
    }

    private fun configurarEventos() {
        binding.swipeRefreshUsuarios.setOnRefreshListener {
            binding.etBuscarId.text?.clear()
            usuarioViewModel.cargarUsuarios()
        }

        binding.fabAgregarUsuario.setOnClickListener {
            startActivity(Intent(this, ActividadFormularioUsuario::class.java))
        }

        binding.btnBuscarPorId.setOnClickListener {
            ejecutarBusquedaPorId()
        }

        binding.etBuscarId.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                ejecutarBusquedaPorId()
                true
            } else {
                false
            }
        }

        binding.btnLimpiarBusqueda.setOnClickListener {
            binding.etBuscarId.text?.clear()
            binding.btnLimpiarBusqueda.visibility = View.GONE
            usuarioViewModel.cargarUsuarios()
        }
    }

    private fun ejecutarBusquedaPorId() {
        val textoId = binding.etBuscarId.text?.toString()?.trim()
        if (textoId.isNullOrEmpty()) {
            Toast.makeText(this, "Ingresa un ID de usuario", Toast.LENGTH_SHORT).show()
            return
        }

        val id = textoId.toIntOrNull()
        if (id == null) {
            Toast.makeText(this, "El ID debe ser numérico", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnLimpiarBusqueda.visibility = View.VISIBLE
        usuarioViewModel.cargarUsuarioPorId(id)
    }

    private fun observarViewModel() {
        usuarioViewModel.estadoLista.observe(this) { estado ->
            when (estado) {
                is Resultado.Cargando -> {
                    binding.progressBarUsuarios.visibility = View.VISIBLE
                    binding.tvUsuariosVacio.visibility = View.GONE
                }
                is Resultado.Exito -> {
                    binding.progressBarUsuarios.visibility = View.GONE
                    binding.swipeRefreshUsuarios.isRefreshing = false
                    adaptadorUsuario.actualizarLista(estado.datos)
                    binding.tvUsuariosVacio.visibility = if (estado.datos.isEmpty()) View.VISIBLE else View.GONE
                }
                is Resultado.Error -> {
                    binding.progressBarUsuarios.visibility = View.GONE
                    binding.swipeRefreshUsuarios.isRefreshing = false
                    Toast.makeText(this, estado.mensaje, Toast.LENGTH_LONG).show()
                }
            }
        }

        usuarioViewModel.estadoDetalle.observe(this) { estado ->
            when (estado) {
                is Resultado.Cargando -> {
                    binding.progressBarUsuarios.visibility = View.VISIBLE
                }
                is Resultado.Exito -> {
                    binding.progressBarUsuarios.visibility = View.GONE
                    adaptadorUsuario.actualizarLista(listOf(estado.datos))
                    binding.tvUsuariosVacio.visibility = View.GONE
                    Toast.makeText(this, "Usuario #${estado.datos.id} encontrado", Toast.LENGTH_SHORT).show()
                }
                is Resultado.Error -> {
                    binding.progressBarUsuarios.visibility = View.GONE
                    adaptadorUsuario.actualizarLista(emptyList())
                    binding.tvUsuariosVacio.visibility = View.VISIBLE
                    binding.tvUsuariosVacio.text = "No se encontró el usuario solicitado."
                    Toast.makeText(this, estado.mensaje, Toast.LENGTH_LONG).show()
                }
            }
        }

        usuarioViewModel.estadoEliminar.observe(this) { estado ->
            when (estado) {
                is Resultado.Cargando -> {
                    binding.progressBarUsuarios.visibility = View.VISIBLE
                }
                is Resultado.Exito -> {
                    binding.progressBarUsuarios.visibility = View.GONE
                    Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                    usuarioViewModel.cargarUsuarios()
                }
                is Resultado.Error -> {
                    binding.progressBarUsuarios.visibility = View.GONE
                    Toast.makeText(this, estado.mensaje, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun confirmarEliminacion(usuario: Usuario) {
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
