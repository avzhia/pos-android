package com.tiendanaturista.pos.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tiendanaturista.pos.data.*
import com.tiendanaturista.pos.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch
import java.time.Instant

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager
    private val api = ApiService.create()

    private var tiendas: List<Tienda> = emptyList()
    private var cajeros: List<Cajero> = emptyList()
    private var tiendaSel: Tienda? = null
    private var cajeroSel: Cajero? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        // Si hay sesión activa, ir directo a MainActivity
        if (session.haySesion()) {
            goMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarDatos()
        setupListeners()
    }

    private fun cargarDatos() {
        lifecycleScope.launch {
            try {
                val cfgResp = api.getConfig("nombre_negocio")
                if (cfgResp.isSuccessful) {
                    binding.tvNegocio.text = "🌿 ${cfgResp.body()?.valor ?: "TiendaNaturistaMX"}"
                }

                val tResp = api.getTiendas()
                val cResp = api.getCajeros()

                if (tResp.isSuccessful && cResp.isSuccessful) {
                    tiendas = tResp.body()?.filter { it.activa } ?: emptyList()
                    cajeros = cResp.body() ?: emptyList()
                    renderTiendas()
                } else {
                    showError("⚠ Sin conexión al servidor")
                }
            } catch (e: Exception) {
                showError("⚠ Sin conexión al servidor")
            }
        }
    }

    private fun renderTiendas() {
        binding.llTiendas.removeAllViews()
        tiendas.forEach { tienda ->
            val chip = layoutInflater.inflate(
                com.tiendanaturista.pos.R.layout.item_login_opt,
                binding.llTiendas, false
            ) as android.widget.LinearLayout

            chip.findViewById<android.widget.TextView>(com.tiendanaturista.pos.R.id.tv_opt_nombre).text = tienda.nombre
            chip.findViewById<android.widget.TextView>(com.tiendanaturista.pos.R.id.tv_opt_sub).text =
                tienda.direccion.ifEmpty { "Sin dirección" }

            if (tiendaSel?.id == tienda.id) chip.setBackgroundResource(com.tiendanaturista.pos.R.drawable.bg_opt_selected)
            else chip.setBackgroundResource(com.tiendanaturista.pos.R.drawable.bg_opt_normal)

            chip.setOnClickListener {
                tiendaSel = tienda
                cajeroSel = null
                binding.layoutPin.visibility = View.GONE
                binding.etPin.text?.clear()
                renderTiendas()
                renderCajeros()
                checkBtn()
            }
            binding.llTiendas.addView(chip)
        }
    }

    private fun renderCajeros() {
        binding.llCajeros.removeAllViews()
        val filtrados = cajeros.filter { it.activo && it.tiendaId == tiendaSel?.id }
        filtrados.forEach { cajero ->
            val chip = layoutInflater.inflate(
                com.tiendanaturista.pos.R.layout.item_login_opt,
                binding.llCajeros, false
            ) as android.widget.LinearLayout

            chip.findViewById<android.widget.TextView>(com.tiendanaturista.pos.R.id.tv_opt_nombre).text = cajero.nombre
            chip.findViewById<android.widget.TextView>(com.tiendanaturista.pos.R.id.tv_opt_sub).text =
                if (cajero.tienePin) "🔒 Con PIN" else ""

            if (cajeroSel?.id == cajero.id) chip.setBackgroundResource(com.tiendanaturista.pos.R.drawable.bg_opt_selected)
            else chip.setBackgroundResource(com.tiendanaturista.pos.R.drawable.bg_opt_normal)

            chip.setOnClickListener {
                cajeroSel = cajero
                binding.layoutPin.visibility = if (cajero.tienePin) View.VISIBLE else View.GONE
                binding.etPin.text?.clear()
                renderCajeros()
                checkBtn()
            }
            binding.llCajeros.addView(chip)
        }
    }

    private fun checkBtn() {
        val fondoOk = binding.etFondo.text.toString().isNotEmpty()
        val listo = tiendaSel != null && cajeroSel != null && fondoOk
        binding.btnEntrar.isEnabled = listo
        binding.tvFondoWarn.visibility =
            if (cajeroSel != null && !fondoOk) View.VISIBLE else View.GONE
    }

    private fun setupListeners() {
        binding.etFondo.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) = checkBtn()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnEntrar.setOnClickListener { entrar() }
    }

    private fun entrar() {
        val fondo = binding.etFondo.text.toString().toDoubleOrNull() ?: 0.0
        val pin = binding.etPin.text.toString().trim()

        if (cajeroSel?.tienePin == true) {
            if (pin.isEmpty()) { Toast.makeText(this, "⚠ Ingresa tu PIN", Toast.LENGTH_SHORT).show(); return }
            lifecycleScope.launch {
                try {
                    val res = api.verificarPin(cajeroSel!!.id, pin)
                    if (res.isSuccessful && res.body()?.ok == true) guardarYEntrar(fondo)
                    else {
                        binding.tvPinError.visibility = View.VISIBLE
                        binding.etPin.text?.clear()
                    }
                } catch (e: Exception) {
                    binding.tvPinError.visibility = View.VISIBLE
                }
            }
        } else {
            guardarYEntrar(fondo)
        }
    }

    private fun guardarYEntrar(fondo: Double) {
        session.guardar(Sesion(
            cajero = cajeroSel!!.nombre,
            cajeroId = cajeroSel!!.id,
            tiendaId = tiendaSel!!.id,
            tiendaNombre = tiendaSel!!.nombre,
            fondoInicial = fondo,
            apertura = Instant.now().toString(),
            negocio = binding.tvNegocio.text.toString().replace("🌿 ", "")
        ))
        goMain()
    }

    private fun goMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showError(msg: String) {
        binding.llTiendas.removeAllViews()
        val tv = android.widget.TextView(this).apply {
            text = msg
            setTextColor(getColor(com.tiendanaturista.pos.R.color.red_txt))
            textSize = 13f
            setPadding(8, 8, 8, 8)
        }
        binding.llTiendas.addView(tv)
    }
}
