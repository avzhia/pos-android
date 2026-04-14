package com.tiendanaturista.pos.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tiendanaturista.pos.R
import com.tiendanaturista.pos.data.Cliente

class ClientesAdapter(
    private val onClick: (Cliente) -> Unit
) : ListAdapter<Cliente, ClientesAdapter.VH>(DIFF) {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvAvatar: TextView = view.findViewById(R.id.tv_cl_avatar)
        val tvNombre: TextView = view.findViewById(R.id.tv_cl_nombre)
        val tvTel: TextView = view.findViewById(R.id.tv_cl_tel)
        val tvCompras: TextView = view.findViewById(R.id.tv_cl_compras)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_cliente, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = getItem(position)
        val ini = c.nombre.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()
        holder.tvAvatar.text = ini
        holder.tvNombre.text = c.nombre
        holder.tvTel.text = c.telefono.ifEmpty { "Sin teléfono" }
        holder.tvCompras.text = "${c.totalCompras} compras"
        holder.itemView.setOnClickListener { onClick(c) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Cliente>() {
            override fun areItemsTheSame(a: Cliente, b: Cliente) = a.id == b.id
            override fun areContentsTheSame(a: Cliente, b: Cliente) = a == b
        }
    }
}
