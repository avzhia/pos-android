package com.tiendanaturista.pos.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tiendanaturista.pos.databinding.FragmentInventarioBinding
import com.tiendanaturista.pos.ui.MainActivity
import com.tiendanaturista.pos.ui.adapters.InventarioAdapter

class InventarioFragment : Fragment() {

    private var _binding: FragmentInventarioBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: InventarioAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInventarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = InventarioAdapter()
        binding.rvInventario.adapter = adapter
        binding.rvInventario.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(context)

        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = filtrar(s.toString())
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        onProductosLoaded()
    }

    fun onProductosLoaded() {
        filtrar("")
    }

    private fun filtrar(q: String) {
        val main = activity as? MainActivity ?: return
        if (_binding == null) return
        val lista = if (q.isEmpty()) main.productos
        else main.productos.filter { it.nombre.lowercase().contains(q.lowercase()) }
        adapter.submitList(lista)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
