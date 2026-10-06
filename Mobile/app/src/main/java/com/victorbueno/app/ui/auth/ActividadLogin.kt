package com.victorbueno.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.victorbueno.app.databinding.ActividadLoginBinding
import com.victorbueno.app.ui.dashboard.ActividadPanel
import com.victorbueno.app.utils.AdministradorSesion
import com.victorbueno.app.utils.FabricaViewModel
import com.victorbueno.app.utils.Resultado

class ActividadLogin : AppCompatActivity() {

    private lateinit var binding: ActividadLoginBinding
    private val authViewModel: AutenticacionViewModel by viewModels { FabricaViewModel(this) }
    private lateinit var administradorSesion: AdministradorSesion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActividadLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        administradorSesion = AdministradorSesion(this)

        if (administradorSesion.estaAutenticado()) {
            irAlPanel()
            return
        }

        configurarEventos()
        observarViewModel()
    }

    private fun configurarEventos() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            authViewModel.iniciarSesion(email, password)
        }

        binding.btnIrRegistro.setOnClickListener {
            startActivity(Intent(this, ActividadRegistro::class.java))
        }

        binding.tvConfigurarServidor.setOnClickListener {
            mostrarDialogoConfiguracion()
        }
    }

    private fun observarViewModel() {
        authViewModel.estadoLogin.observe(this) { estado ->
            when (estado) {
                is Resultado.Cargando -> {
                    binding.progressBarLogin.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                }
                is Resultado.Exito -> {
                    binding.progressBarLogin.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "Bienvenido, ${estado.datos.usuario.nombre}", Toast.LENGTH_SHORT).show()
                    irAlPanel()
                }
                is Resultado.Error -> {
                    binding.progressBarLogin.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, estado.mensaje, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarDialogoConfiguracion() {
        val input = EditText(this).apply {
            setText(administradorSesion.obtenerUrlBase())
            setSelection(text.length)
        }

        AlertDialog.Builder(this)
            .setTitle("Servidor Backend")
            .setMessage("URL base del API:")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevaUrl = input.text.toString().trim()
                if (nuevaUrl.isNotEmpty()) {
                    administradorSesion.guardarUrlBase(nuevaUrl)
                    Toast.makeText(this, "Servidor: $nuevaUrl", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun irAlPanel() {
        val intent = Intent(this, ActividadPanel::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
