package com.tiendanaturista.pos.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tiendanaturista.pos.R
import com.tiendanaturista.pos.data.*
import com.tiendanaturista.pos.databinding.ActivityMainBinding
import com.tiendanaturista.pos.ui.fragments.ClientesFragment
import com.tiendanaturista.pos.ui.fragments.InventarioFragment
import com.tiendanaturista.pos.ui.fragments.VentasFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var session: SessionManager
    lateinit var api: ApiService

    val ticket = mutableListOf<TicketItem>()
    var productos: List<Producto> = emptyList()
    var clientes: List<Cliente> = emptyList()

    private val ventasFragment = VentasFragment()
    private val inventarioFragment = InventarioFragment()
    private val clientesFragment = ClientesFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        api = ApiService.createFromPrefs(this)
        session = SessionManager(this)
        val sesion = session.cargar() ?: run { goLogin(); return }

        binding.tvNegocio.text = "🌿 ${sesion.negocio}"
        binding.tvCajero.text = "${sesion.cajero} · ${sesion.tiendaNombre}"

        setupFragments()
        setupNav()
        setupFab()
        setupSalir()

        // Cargar datos iniciales
        cargarDatos()
    }

    private fun setupFragments() {
        supportFragmentManager.beginTransaction()
            .add(binding.fragmentContainer.id, ventasFragment, "ventas")
            .add(binding.fragmentContainer.id, inventarioFragment, "inventario")
            .add(binding.fragmentContainer.id, clientesFragment, "clientes")
            .hide(inventarioFragment)
            .hide(clientesFragment)
            .commit()
    }

    private fun setupNav() {
        binding.btnNavVentas.setOnClickListener { showPanel("ventas") }
        binding.btnNavInventario.setOnClickListener { showPanel("inventario") }
        binding.btnNavClientes.setOnClickListener { showPanel("clientes") }
        updateNav("ventas")
    }

    fun showPanel(tag: String) {
        val ft = supportFragmentManager.beginTransaction()
        listOf(
            Pair("ventas", ventasFragment),
            Pair("inventario", inventarioFragment),
            Pair("clientes", clientesFragment)
        ).forEach { (t, f) ->
            if (t == tag) ft.show(f) else ft.hide(f)
        }
        ft.commit()
        updateNav(tag)

        // Ocultar FAB en paneles sin ticket
        binding.fabTicket.visibility = if (tag == "ventas") View.VISIBLE else View.GONE
        updateFabBadge()
    }

    private fun updateNav(active: String) {
        val btns = mapOf(
            "ventas" to binding.btnNavVentas,
            "inventario" to binding.btnNavInventario,
            "clientes" to binding.btnNavClientes
        )
        btns.forEach { (tag, btn) ->
            btn.isSelected = tag == active
            btn.setTextColor(
                if (tag == active) getColor(R.color.g2)
                else getColor(R.color.txt3)
            )
        }
    }

    private fun setupFab() {
        binding.fabTicketBtn.setOnClickListener {
            if (ticket.isEmpty()) return@setOnClickListener
            val dialog = TicketBottomSheet()
            dialog.show(supportFragmentManager, "ticket")
        }
        updateFabBadge()
    }

    fun updateFabBadge() {
        val total = ticket.sumOf { it.cantidad }
        binding.tvFabBadge.text = total.toString()
        binding.fabTicket.visibility =
            if (ticket.isNotEmpty() && isVentasVisible()) View.VISIBLE else View.GONE
    }

    private fun isVentasVisible(): Boolean =
        supportFragmentManager.findFragmentByTag("ventas")?.isHidden == false

    private fun setupSalir() {
        binding.btnSalir.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("¿Cerrar sesión?")
                .setMessage("El turno sigue abierto. Ciérralo desde el POS de escritorio.")
                .setPositiveButton("Salir") { _, _ -> session.cerrar(); goLogin() }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun cargarDatos() {
        lifecycleScope.launch {
            try {
                val pResp = api.getProductos()
                if (pResp.isSuccessful) {
                    productos = pResp.body() ?: emptyList()
                    // Esperar a que los fragmentos estén adjuntos al window
                    binding.root.post {
                        ventasFragment.onProductosLoaded()
                        inventarioFragment.onProductosLoaded()
                    }
                } else {
                    Toast.makeText(this@MainActivity,
                        "⚠ Error del servidor: ${pResp.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity,
                    "⚠ No se pudo conectar al servidor: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun recargarProductos() {
        lifecycleScope.launch {
            try {
                val pResp = api.getProductos()
                if (pResp.isSuccessful) {
                    productos = pResp.body() ?: emptyList()
                    binding.root.post {
                        ventasFragment.onProductosLoaded()
                        inventarioFragment.onProductosLoaded()
                    }
                }
            } catch (e: Exception) { /* silencioso, hay stock en memoria */ }
        }
    }

    fun agregarProd(prod: Producto) {
        val stockActual = productos.find { it.id == prod.id }?.stockTotal ?: 0
        val enTicket = ticket.find { it.producto.id == prod.id }?.cantidad ?: 0
        if (enTicket >= stockActual) {
            showToast("⚠ Sin stock disponible")
            return
        }
        val existing = ticket.find { it.producto.id == prod.id }
        if (existing != null) existing.cantidad++
        else ticket.add(TicketItem(prod))
        updateFabBadge()
        ventasFragment.onTicketChanged()
        showToast("✓ ${prod.nombre}")
    }

    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun goLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
