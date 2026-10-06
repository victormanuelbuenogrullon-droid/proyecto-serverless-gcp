package com.victorbueno.app.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.victorbueno.app.data.models.ElementoNotificacion
import com.victorbueno.app.databinding.ElementoNotificacionBinding

class AdaptadorNotificacion(
    private var notificaciones: List<ElementoNotificacion> = emptyList()
) : RecyclerView.Adapter<AdaptadorNotificacion.NotificacionViewHolder>() {

    fun actualizarLista(nuevaLista: List<ElementoNotificacion>) {
        notificaciones = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacionViewHolder {
        val binding = ElementoNotificacionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificacionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificacionViewHolder, position: Int) {
        holder.enlazar(notificaciones[position])
    }

    override fun getItemCount(): Int = notificaciones.size

    inner class NotificacionViewHolder(private val binding: ElementoNotificacionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun enlazar(item: ElementoNotificacion) {
            binding.tvTituloNotif.text = item.titulo
            binding.tvMensajeNotif.text = item.mensaje
            binding.tvFechaNotif.text = item.fechaCreacion.replace("T", " ").take(19)
        }
    }
}
