package com.victorbueno.app.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.victorbueno.app.databinding.ActividadRegistroBinding
import com.victorbueno.app.utils.FabricaViewModel
import com.victorbueno.app.utils.Resultado

class ActividadRegistro : AppCompatActivity() {

    private lateinit var binding: ActividadRegistroBinding
    private val authViewModel: AutenticacionViewModel by viewModels { FabricaViewModel(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActividadRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarEventos()
        observarViewModel()
    }

    private fun configurarEventos() {
        binding.btnRealizarRegistro.setOnClickListener {
            val nombre = binding.etRegNombre.text.toString().trim()
            val apellido = binding.etRegApellido.text.toString().trim()
            val email = binding.etRegEmail.text.toString().trim()
            val pass = binding.etRegPassword.text.toString().trim()

            authViewModel.registrar(nombre, apellido, email, pass)
        }

        binding.btnVolverLogin.setOnClickListener {
            finish()
        }
    }

    private fun observarViewModel() {
        authViewModel.estadoRegistro.observe(this) { estado ->
            when (estado) {
                is Resultado.Cargando -> {
                    binding.progressBarRegistro.visibility = View.VISIBLE
                    binding.btnRealizarRegistro.isEnabled = false
                }
                is Resultado.Exito -> {
                    binding.progressBarRegistro.visibility = View.GONE
                    binding.btnRealizarRegistro.isEnabled = true
                    Toast.makeText(this, "Registro completado con éxito", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Resultado.Error -> {
                    binding.progressBarRegistro.visibility = View.GONE
                    binding.btnRealizarRegistro.isEnabled = true
                    Toast.makeText(this, estado.mensaje, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
