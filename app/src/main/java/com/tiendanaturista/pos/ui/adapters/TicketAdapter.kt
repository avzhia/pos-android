package com.tiendanaturista.pos.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tiendanaturista.pos.R
import com.tiendanaturista.pos.data.TicketItem
import java.text.NumberFormat
import java.util.Locale

class TicketAdapter(
    private val onQtyChange: (TicketItem, Int) -> Unit,
    private val onRemove: (TicketItem) -> Unit
) : ListAdapter<TicketItem, TicketAdapter.VH>(DIFF) {

    private val fmt = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvIcono: TextView = view.findViewById(R.id.tv_ticket_icono)
        val tvNombre: TextView = view.findViewById(R.id.tv_ticket_nombre)
        val tvPrecio: TextView = view.findViewById(R.id.tv_ticket_precio)
        val tvQty: TextView = view.findViewById(R.id.tv_ticket_qty)
        val btnMinus: TextView = view.findViewById(R.id.btn_ticket_minus)
        val btnPlus: TextView = view.findViewById(R.id.btn_ticket_plus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_ticket, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.tvIcono.text = item.producto.icono
        holder.tvNombre.text = item.producto.nombre
        holder.tvPrecio.text = "${fmt.format(item.producto.precio)} c/u"
        holder.tvQty.text = item.cantidad.toString()

        holder.btnPlus.setOnClickListener {
            item.cantidad++
            onQtyChange(item, item.cantidad)
            notifyItemChanged(holder.adapterPosition)
        }
        holder.btnMinus.setOnClickListener {
            if (item.cantidad > 1) {
                item.cantidad--
                onQtyChange(item, item.cantidad)
                notifyItemChanged(holder.adapterPosition)
            } else {
                onRemove(item)
            }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<TicketItem>() {
            override fun areItemsTheSame(a: TicketItem, b: TicketItem) = a.producto.id == b.producto.id
            override fun areContentsTheSame(a: TicketItem, b: TicketItem) = a.cantidad == b.cantidad
        }
    }
}
