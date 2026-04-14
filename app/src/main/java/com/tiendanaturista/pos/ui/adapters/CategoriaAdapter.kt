package com.tiendanaturista.pos.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tiendanaturista.pos.R

class CategoriaAdapter(
    private val onSelect: (String) -> Unit
) : ListAdapter<Pair<String, Boolean>, CategoriaAdapter.VH>(DIFF) {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tv_cat_nombre)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_categoria, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (nombre, activa) = getItem(position)
        holder.tvNombre.text = nombre
        holder.tvNombre.isSelected = activa
        if (activa) {
            holder.tvNombre.setBackgroundResource(R.drawable.bg_chip_active)
            holder.tvNombre.setTextColor(holder.itemView.context.getColor(R.color.white))
        } else {
            holder.tvNombre.setBackgroundResource(R.drawable.bg_chip_normal)
            holder.tvNombre.setTextColor(holder.itemView.context.getColor(R.color.txt2))
        }
        holder.tvNombre.setOnClickListener {
            onSelect(if (nombre == "Todos") "" else nombre)
            // Actualizar selección
            val updated = currentList.map { (n, _) -> Pair(n, n == nombre) }
            submitList(updated)
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Pair<String, Boolean>>() {
            override fun areItemsTheSame(a: Pair<String, Boolean>, b: Pair<String, Boolean>) = a.first == b.first
            override fun areContentsTheSame(a: Pair<String, Boolean>, b: Pair<String, Boolean>) = a == b
        }
    }
}
