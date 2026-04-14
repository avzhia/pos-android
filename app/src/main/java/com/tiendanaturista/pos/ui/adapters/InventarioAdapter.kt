package com.tiendanaturista.pos.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tiendanaturista.pos.R
import com.tiendanaturista.pos.data.Producto

class InventarioAdapter : ListAdapter<Producto, InventarioAdapter.VH>(DIFF) {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcono: TextView = view.findViewById(R.id.tv_inv_icono)
        val tvNombre: TextView = view.findViewById(R.id.tv_inv_nombre)
        val tvCat: TextView = view.findViewById(R.id.tv_inv_cat)
        val tvStock: TextView = view.findViewById(R.id.tv_inv_stock)
        val tvStockLbl: TextView = view.findViewById(R.id.tv_inv_stock_lbl)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_inventario, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = getItem(position)
        val st = p.stockTotal
        holder.tvIcono.text = p.icono
        holder.tvNombre.text = p.nombre
        holder.tvCat.text = p.categoria
        holder.tvStock.text = st.toString()
        when {
            st == 0 -> holder.tvStock.setTextColor(holder.itemView.context.getColor(R.color.red_txt))
            st <= p.stockMin -> holder.tvStock.setTextColor(holder.itemView.context.getColor(R.color.amber_txt))
            else -> holder.tvStock.setTextColor(holder.itemView.context.getColor(R.color.g2))
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Producto>() {
            override fun areItemsTheSame(a: Producto, b: Producto) = a.id == b.id
            override fun areContentsTheSame(a: Producto, b: Producto) = a == b
        }
    }
}
