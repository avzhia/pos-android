package com.tiendanaturista.pos.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.tiendanaturista.pos.data.Producto
import com.tiendanaturista.pos.databinding.FragmentVentasBinding
import com.tiendanaturista.pos.ui.MainActivity
import com.tiendanaturista.pos.ui.adapters.ProductoAdapter
import com.tiendanaturista.pos.ui.adapters.CategoriaAdapter

class VentasFragment : Fragment() {

    private var _binding: FragmentVentasBinding? = null
    private val binding get() = _binding!!

    private lateinit var prodAdapter: ProductoAdapter
    private lateinit var catAdapter: CategoriaAdapter
    private var catSel = ""

    private val scanLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents != null) {
            val main = activity as? MainActivity ?: return@registerForActivityResult
            val prod = main.productos.find { it.codigoBarras == result.contents }
            if (prod != null) main.agregarProd(prod)
            else main.showToast("⚠ Producto no encontrado")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVentasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Productos grid
        prodAdapter = ProductoAdapter { prod ->
            (activity as? MainActivity)?.agregarProd(prod)
        }
        binding.rvProductos.adapter = prodAdapter
        binding.rvProductos.layoutManager =
            androidx.recyclerview.widget.GridLayoutManager(context, 2)

        // Categorías
        catAdapter = CategoriaAdapter { cat ->
            catSel = cat
            filtrar()
        }
        binding.rvCategorias.adapter = catAdapter
        binding.rvCategorias.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)

        // Búsqueda
        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = filtrar()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Escáner
        binding.btnScan.setOnClickListener { iniciarScan() }

        onProductosLoaded()
    }

    fun onProductosLoaded() {
        val main = activity as? MainActivity ?: return
        if (_binding == null) return

        // Cargar categorías
        val cats = listOf("Todos") + main.productos.map { it.categoria }.distinct().sorted()
        catAdapter.submitList(cats.map { Pair(it, it == "Todos" || it == catSel) })

        filtrar()
    }

    fun onTicketChanged() {
        // nada por ahora
    }

    private fun filtrar() {
        val main = activity as? MainActivity ?: return
        val q = _binding?.etBuscar?.text.toString().lowercase()
        val lista = main.productos.filter { p ->
            (catSel.isEmpty() || catSel == "Todos" || p.categoria == catSel) &&
            (q.isEmpty() || p.nombre.lowercase().contains(q) || p.codigoBarras?.contains(q) == true)
        }
        prodAdapter.submitList(lista)
        _binding?.tvVacio?.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun iniciarScan() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 100)
            return
        }
        val opts = ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
            .setPrompt("Apunta al código de barras")
            .setCameraId(0)
            .setBeepEnabled(true)
            .setBarcodeImageEnabled(false)
            .setOrientationLocked(false)
        scanLauncher.launch(opts)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
