package com.victorbueno.app.ui.files

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.victorbueno.app.data.models.ElementoArchivo
import com.victorbueno.app.databinding.ElementoArchivoBinding
import com.victorbueno.app.utils.UtilidadesImagen

class AdaptadorArchivo(
    private val urlServidor: String,
    private val alEliminar: (ElementoArchivo) -> Unit,
    private val alHacerClic: (ElementoArchivo) -> Unit
) : RecyclerView.Adapter<AdaptadorArchivo.ArchivoViewHolder>() {

    private var archivos: List<ElementoArchivo> = emptyList()

    fun actualizarLista(nuevaLista: List<ElementoArchivo>) {
        archivos = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchivoViewHolder {
        val binding = ElementoArchivoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArchivoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArchivoViewHolder, position: Int) {
        holder.enlazar(archivos[position])
    }

    override fun getItemCount(): Int = archivos.size

    inner class ArchivoViewHolder(private val binding: ElementoArchivoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun enlazar(archivo: ElementoArchivo) {
            binding.tvNombreArchivo.text = archivo.filename
            
            val tamanoTexto = if (archivo.tamanoBytes != null && archivo.tamanoBytes > 0) {
                if (archivo.tamanoBytes >= 1024 * 1024) {
                    String.format("%.2f MB", archivo.tamanoBytes / (1024.0 * 1024.0))
                } else {
                    "${archivo.tamanoBytes / 1024} KB"
                }
            } else "Tamaño desconocido"

            val tipoTexto = archivo.tipoContenido ?: "archivo"
            binding.tvTamanoArchivo.text = "$tamanoTexto • $tipoTexto"
            binding.tvUrlArchivo.text = archivo.url

            val urlCompleta = UtilidadesImagen.formatearUrlImagen(urlServidor, archivo.url)
            if (!urlCompleta.isNullOrBlank()) {
                Glide.with(binding.root.context)
                    .load(urlCompleta)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(binding.ivThumbnailArchivo)
            } else {
                binding.ivThumbnailArchivo.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            binding.btnEliminarArchivo.setOnClickListener {
                alEliminar(archivo)
            }

            binding.root.setOnClickListener {
                alHacerClic(archivo)
            }
        }
    }
}
