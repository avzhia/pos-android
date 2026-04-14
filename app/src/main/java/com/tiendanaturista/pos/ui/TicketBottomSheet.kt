package com.tiendanaturista.pos.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tiendanaturista.pos.data.ItemVentaRequest
import com.tiendanaturista.pos.data.VentaRequest
import com.tiendanaturista.pos.databinding.BottomsheetTicketBinding
import com.tiendanaturista.pos.ui.adapters.TicketAdapter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class TicketBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetTicketBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TicketAdapter
    private val fmt = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomsheetTicketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val main = activity as? MainActivity ?: return

        adapter = TicketAdapter(
            onQtyChange = { _, _ ->
                main.updateFabBadge()
                renderTotal()
            },
            onRemove = { item ->
                main.ticket.remove(item)
                main.updateFabBadge()
                renderTotal()
                if (main.ticket.isEmpty()) dismiss()
            }
        )
        binding.rvItems.adapter = adapter
        binding.rvItems.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(context)

        adapter.submitList(main.ticket.toList())
        renderTotal()

        binding.btnLimpiar.setOnClickListener {
            main.ticket.clear()
            main.updateFabBadge()
            dismiss()
        }

        binding.btnCobrar.setOnClickListener {
            val sheet = CobroBottomSheet()
            sheet.show(parentFragmentManager, "cobro")
            dismiss()
        }
    }

    private fun renderTotal() {
        val main = activity as? MainActivity ?: return
        val total = main.ticket.sumOf { it.subtotal }
        binding.tvTotal.text = fmt.format(total)
        binding.btnCobrar.text = "💳 Cobrar ${fmt.format(total)}"
        binding.btnCobrar.isEnabled = main.ticket.isNotEmpty()
        adapter.submitList(main.ticket.toList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
