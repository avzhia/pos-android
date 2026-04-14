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
import java.text.NumberFormat
import java.util.Locale

class ProductoAdapter(
    private val onClick: (Producto) -> Unit
) : ListAdapter<Producto, ProductoAdapter.VH>(DIFF) {

    private val fmt = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcono: TextView = view.findViewById(R.id.tv_prod_icono)
        val tvNombre: TextView = view.findViewById(R.id.tv_prod_nombre)
        val tvPrecio: TextView = view.findViewById(R.id.tv_prod_precio)
        val tvStock: TextView = view.findViewById(R.id.tv_prod_stock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = getItem(position)
        val st = p.stockTotal

        holder.tvIcono.text = p.icono
        holder.tvNombre.text = p.nombre
        holder.tvPrecio.text = fmt.format(p.precio)

        when {
            st == 0 -> {
                holder.tvStock.text = "Agotado"
                holder.tvStock.setBackgroundResource(R.drawable.bg_stock_sin)
                holder.tvStock.setTextColor(holder.itemView.context.getColor(R.color.red_txt))
                holder.itemView.alpha = 0.4f
                holder.itemView.isEnabled = false
            }
            st <= p.stockMin -> {
                holder.tvStock.text = "$st uds"
                holder.tvStock.setBackgroundResource(R.drawable.bg_stock_bajo)
                holder.tvStock.setTextColor(holder.itemView.context.getColor(R.color.amber_txt))
                holder.itemView.alpha = 1f
                holder.itemView.isEnabled = true
            }
            else -> {
                holder.tvStock.text = "$st uds"
                holder.tvStock.setBackgroundResource(R.drawable.bg_stock_ok)
                holder.tvStock.setTextColor(holder.itemView.context.getColor(R.color.g2))
                holder.itemView.alpha = 1f
                holder.itemView.isEnabled = true
            }
        }

        holder.itemView.setOnClickListener { if (st > 0) onClick(p) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Producto>() {
            override fun areItemsTheSame(a: Producto, b: Producto) = a.id == b.id
            override fun areContentsTheSame(a: Producto, b: Producto) = a == b
        }
    }
}
