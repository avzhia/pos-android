package com.tiendanaturista.pos.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tiendanaturista.pos.databinding.BottomsheetClienteDetalleBinding
import com.tiendanaturista.pos.ui.MainActivity
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ClienteDetalleBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetClienteDetalleBinding? = null
    private val binding get() = _binding!!
    private val fmt = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    companion object {
        fun newInstance(clienteId: Int) = ClienteDetalleBottomSheet().apply {
            arguments = Bundle().apply { putInt("cliente_id", clienteId) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomsheetClienteDetalleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val main = activity as? MainActivity ?: return
        val clienteId = arguments?.getInt("cliente_id") ?: return
        val cliente = main.clientes.find { it.id == clienteId } ?: return

        val ini = cliente.nombre.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
        binding.tvAvatar.text = ini.uppercase()
        binding.tvNombre.text = cliente.nombre
        binding.tvTelefono.text = cliente.telefono.ifEmpty { "Sin teléfono" }
        binding.tvCompras.text = cliente.totalCompras.toString()
        binding.tvGastado.text = fmt.format(cliente.totalGastado)
        binding.btnCerrar.setOnClickListener { dismiss() }

        cargarHistorial(main, clienteId)
    }

    private fun cargarHistorial(main: MainActivity, clienteId: Int) {
        lifecycleScope.launch {
            try {
                val resp = main.api.getVentasCliente(clienteId)
                if (resp.isSuccessful) {
                    val ventas = resp.body() ?: emptyList()
                    if (ventas.isEmpty()) {
                        binding.tvHistorialVacio.visibility = View.VISIBLE
                        return@launch
                    }
                    binding.tvHistorialVacio.visibility = View.GONE
                    val sb = StringBuilder()
                    ventas.take(10).forEach { v ->
                        val fecha = v.fecha.take(10)
                        sb.append("$fecha  —  ${fmt.format(v.total)}  (${v.formaPago})\n")
                    }
                    binding.tvHistorial.text = sb.toString().trimEnd()
                }
            } catch (e: Exception) {
                binding.tvHistorialVacio.text = "Error al cargar historial"
                binding.tvHistorialVacio.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
