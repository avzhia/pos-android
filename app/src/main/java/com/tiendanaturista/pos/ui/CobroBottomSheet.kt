package com.tiendanaturista.pos.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tiendanaturista.pos.data.ItemVentaRequest
import com.tiendanaturista.pos.data.VentaRequest
import com.tiendanaturista.pos.databinding.BottomsheetCobroBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CobroBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetCobroBinding? = null
    private val binding get() = _binding!!
    private val fmt = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    private var formaPago = "Efectivo"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomsheetCobroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val main = activity as? MainActivity ?: return
        val total = main.ticket.sumOf { it.subtotal }

        binding.tvTitulo.text = "💳 Cobrar ${fmt.format(total)}"

        // Formas de pago
        val pagoViews = mapOf(
            "Efectivo" to binding.btnEfectivo,
            "Tarjeta" to binding.btnTarjeta,
            "Transfer." to binding.btnTransfer
        )
        pagoViews.forEach { (pago, btn) ->
            btn.setOnClickListener {
                formaPago = pago
                pagoViews.values.forEach { b -> b.isSelected = false }
                btn.isSelected = true
                binding.layoutRecibido.visibility =
                    if (pago == "Efectivo") View.VISIBLE else View.GONE
                binding.layoutCambio.visibility =
                    if (pago == "Efectivo") View.VISIBLE else View.GONE
                calcCambio(total)
            }
        }
        binding.btnEfectivo.isSelected = true

        // Cambio
        binding.etRecibido.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = calcCambio(total)
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnConfirmar.setOnClickListener { confirmarVenta(main, total) }
    }

    private fun calcCambio(total: Double) {
        val recibido = _binding?.etRecibido?.text.toString().toDoubleOrNull() ?: 0.0
        val cambio = recibido - total
        _binding?.tvCambio?.text = if (cambio >= 0) fmt.format(cambio) else "—"
    }

    private fun confirmarVenta(main: MainActivity, total: Double) {
        binding.btnConfirmar.isEnabled = false
        binding.btnConfirmar.text = "Verificando turno..."

        val sesion = main.session.cargar() ?: return

        lifecycleScope.launch {
            try {
                // Verificar que el turno sigue activo antes de registrar la venta
                val turnoResp = main.api.getTurnoActivo(
                    cajeroId = sesion.cajeroId,
                    tiendaId = sesion.tiendaId
                )

                if (!turnoResp.isSuccessful || turnoResp.body()?.activo != true) {
                    main.showToast("⚠ El turno fue cerrado. Inicia un nuevo turno para continuar.")
                    _binding?.btnConfirmar?.isEnabled = true
                    _binding?.btnConfirmar?.text = "✓ Confirmar venta"
                    return@launch
                }

                // Turno activo confirmado — registrar la venta
                _binding?.btnConfirmar?.text = "Procesando..."

                val resp = main.api.registrarVenta(VentaRequest(
                    tiendaId = sesion.tiendaId,
                    cajero = sesion.cajero,
                    formaPago = formaPago,
                    total = total,
                    items = main.ticket.map { item ->
                        ItemVentaRequest(
                            productoId = item.producto.id,
                            nombreProd = item.producto.nombre,
                            cantidad = item.cantidad,
                            precioUnit = item.producto.precio,
                            subtotal = item.subtotal
                        )
                    }
                ))
                if (resp.isSuccessful) {
                    main.ticket.clear()
                    main.updateFabBadge()
                    main.showToast("✓ Venta registrada")
                    dismiss()
                    main.recargarProductos()
                } else {
                    main.showToast("⚠ Error al registrar venta (${resp.code()})")
                    _binding?.btnConfirmar?.isEnabled = true
                    _binding?.btnConfirmar?.text = "✓ Confirmar venta"
                }
            } catch (e: Exception) {
                main.showToast("⚠ Error de conexión")
                _binding?.btnConfirmar?.isEnabled = true
                _binding?.btnConfirmar?.text = "✓ Confirmar venta"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
