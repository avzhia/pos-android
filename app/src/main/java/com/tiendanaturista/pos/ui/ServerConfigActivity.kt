package com.tiendanaturista.pos.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tiendanaturista.pos.data.ApiService
import com.tiendanaturista.pos.data.ServerPrefs
import com.tiendanaturista.pos.databinding.ActivityServerConfigBinding
import kotlinx.coroutines.launch

class ServerConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServerConfigBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si ya hay URL guardada, ir directo al login
        if (ServerPrefs.hasUrl(this)) {
            goLogin()
            return
        }

        binding = ActivityServerConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.etServerUrl.setText("http://")
        binding.btnGuardar.setOnClickListener { guardarYVerificar() }
    }

    private fun guardarYVerificar() {
        var url = binding.etServerUrl.text.toString().trim().trimEnd('/')
        if (url.isEmpty() || url == "http://" || url == "https://") {
            Toast.makeText(this, "⚠ Ingresa la URL del servidor", Toast.LENGTH_SHORT).show()
            return
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://$url"
        }

        binding.btnGuardar.isEnabled = false
        binding.btnGuardar.text = "Verificando conexión..."

        val finalUrl = url
        lifecycleScope.launch {
            try {
                val api = ApiService.create(finalUrl)
                val resp = api.getConfig("nombre_negocio")
                if (resp.isSuccessful) {
                    ServerPrefs.saveUrl(this@ServerConfigActivity, finalUrl)
                    runOnUiThread {
                        Toast.makeText(
                            this@ServerConfigActivity,
                            "✓ Conectado a ${resp.body()?.valor ?: "servidor"}",
                            Toast.LENGTH_SHORT
                        ).show()
                        goLogin()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ServerConfigActivity,
                            "⚠ Servidor respondió con error (${resp.code()})",
                            Toast.LENGTH_LONG).show()
                        binding.btnGuardar.isEnabled = true
                        binding.btnGuardar.text = "Guardar y conectar"
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ServerConfigActivity,
                        "⚠ No se pudo conectar. Verifica la URL y que el servidor esté encendido.",
                        Toast.LENGTH_LONG).show()
                    binding.btnGuardar.isEnabled = true
                    binding.btnGuardar.text = "Guardar y conectar"
                }
            }
        }
    }

    private fun goLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
