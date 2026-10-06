package com.victorbueno.app.ui.benchmark

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.victorbueno.app.data.models.EstadoTarea
import com.victorbueno.app.data.models.TareaRendimiento
import com.victorbueno.app.databinding.ElementoTareaRendimientoBinding

class AdaptadorRendimiento(
    private var tareas: MutableList<TareaRendimiento> = mutableListOf()
) : RecyclerView.Adapter<AdaptadorRendimiento.TareaViewHolder>() {

    fun actualizarTareas(nuevasTareas: List<TareaRendimiento>) {
        tareas = nuevasTareas.toMutableList()
        notifyDataSetChanged()
    }

    fun actualizarTareaIndividual(tareaActualizada: TareaRendimiento) {
        val indice = tareas.indexOfFirst { it.id == tareaActualizada.id }
        if (indice != -1) {
            tareas[indice] = tareaActualizada
            notifyItemChanged(indice)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val binding = ElementoTareaRendimientoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TareaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        holder.enlazar(tareas[position])
    }

    override fun getItemCount(): Int = tareas.size

    inner class TareaViewHolder(private val binding: ElementoTareaRendimientoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun enlazar(tarea: TareaRendimiento) {
            val tamanoTexto = if (tarea.tamanoKb > 0) " (${tarea.tamanoKb} KB)" else ""
            binding.tvTituloTarea.text = "${tarea.nombre}$tamanoTexto"
            binding.tvHiloTarea.text = if (tarea.nombreHilo.isNotEmpty()) "Hilo: ${tarea.nombreHilo}" else "En espera"

            if (tarea.mensajeResultado.isNotEmpty()) {
                binding.tvMensajeResultado.visibility = View.VISIBLE
                binding.tvMensajeResultado.text = tarea.mensajeResultado
            } else {
                binding.tvMensajeResultado.visibility = View.GONE
            }

            when (tarea.estado) {
                EstadoTarea.PENDIENTE -> {
                    binding.progressBarTarea.visibility = View.GONE
                    binding.tvEstadoTarea.text = "PENDIENTE"
                    binding.tvEstadoTarea.setTextColor(Color.GRAY)
                    binding.tvDuracionTarea.text = "- ms"
                }
                EstadoTarea.EJECUTANDO -> {
                    binding.progressBarTarea.visibility = View.VISIBLE
                    binding.tvEstadoTarea.text = "SUBIENDO..."
                    binding.tvEstadoTarea.setTextColor(Color.parseColor("#3B82F6"))
                    binding.tvDuracionTarea.text = "Procesando..."
                }
                EstadoTarea.COMPLETADO -> {
                    binding.progressBarTarea.visibility = View.GONE
                    binding.tvEstadoTarea.text = "EXITOSO"
                    binding.tvEstadoTarea.setTextColor(Color.parseColor("#10B981"))
                    binding.tvDuracionTarea.text = "${tarea.duracionMs} ms"
                }
                EstadoTarea.FALLIDO -> {
                    binding.progressBarTarea.visibility = View.GONE
                    binding.tvEstadoTarea.text = "FALLIDO"
                    binding.tvEstadoTarea.setTextColor(Color.RED)
                    binding.tvDuracionTarea.text = "${tarea.duracionMs} ms"
                }
            }
        }
    }
}
