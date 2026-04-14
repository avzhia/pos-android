package com.tiendanaturista.pos.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.tiendanaturista.pos.databinding.FragmentClientesBinding
import com.tiendanaturista.pos.ui.MainActivity
import com.tiendanaturista.pos.ui.adapters.ClientesAdapter
import com.tiendanaturista.pos.ui.ClienteDetalleBottomSheet
import kotlinx.coroutines.launch

class ClientesFragment : Fragment() {

    private var _binding: FragmentClientesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ClientesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentClientesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ClientesAdapter { cliente ->
            val sheet = ClienteDetalleBottomSheet.newInstance(cliente.id)
            sheet.show(parentFragmentManager, "detalle_cliente")
        }
        binding.rvClientes.adapter = adapter
        binding.rvClientes.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(context)

        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = filtrar(s.toString())
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        cargarClientes()
    }

    private fun cargarClientes() {
        val main = activity as? MainActivity ?: return
        if (main.clientes.isEmpty()) {
            lifecycleScope.launch {
                try {
                    val resp = main.api.getClientes()
                    if (resp.isSuccessful) {
                        main.clientes = resp.body()?.filter { it.tipo != "general" } ?: emptyList()
                        filtrar("")
                    }
                } catch (e: Exception) {}
            }
        } else filtrar("")
    }

    private fun filtrar(q: String) {
        val main = activity as? MainActivity ?: return
        if (_binding == null) return
        val lista = if (q.isEmpty()) main.clientes
        else main.clientes.filter {
            it.nombre.lowercase().contains(q.lowercase()) ||
            it.telefono.contains(q)
        }
        adapter.submitList(lista)
        binding.tvVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
