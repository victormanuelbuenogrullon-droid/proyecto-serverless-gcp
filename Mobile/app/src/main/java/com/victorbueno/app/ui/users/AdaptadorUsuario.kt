package com.victorbueno.app.ui.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.victorbueno.app.data.models.Usuario
import com.victorbueno.app.databinding.ElementoUsuarioBinding
import com.victorbueno.app.utils.AdministradorSesion
import com.victorbueno.app.utils.UtilidadesImagen

class AdaptadorUsuario(
    private var usuarios: List<Usuario>,
    private val alHacerClic: (Usuario) -> Unit,
    private val alEditar: (Usuario) -> Unit,
    private val alEliminar: (Usuario) -> Unit
) : RecyclerView.Adapter<AdaptadorUsuario.UsuarioViewHolder>() {

    fun actualizarLista(nuevaLista: List<Usuario>) {
        usuarios = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val binding = ElementoUsuarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UsuarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        holder.enlazar(usuarios[position])
    }

    override fun getItemCount(): Int = usuarios.size

    inner class UsuarioViewHolder(private val binding: ElementoUsuarioBinding) : RecyclerView.ViewHolder(binding.root) {

        fun enlazar(usuario: Usuario) {
            val context = binding.root.context
            val urlBase = AdministradorSesion(context).obtenerUrlBase()

            binding.tvNombreUsuario.text = usuario.nombreCompleto
            binding.tvEmailUsuario.text = usuario.email
            binding.tvIdUsuario.text = "ID: #${usuario.id}"

            val urlFoto = UtilidadesImagen.formatearUrlImagen(urlBase, usuario.foto)
            if (!urlFoto.isNullOrBlank()) {
                Glide.with(context)
                    .load(urlFoto)
                    .circleCrop()
                    .into(binding.ivAvatarUsuario)
            } else {
                binding.ivAvatarUsuario.setImageResource(android.R.drawable.ic_menu_myplaces)
            }

            binding.root.setOnClickListener { alHacerClic(usuario) }
            binding.btnEditarUsuario.setOnClickListener { alEditar(usuario) }
            binding.btnEliminarUsuario.setOnClickListener { alEliminar(usuario) }
        }
    }
}
