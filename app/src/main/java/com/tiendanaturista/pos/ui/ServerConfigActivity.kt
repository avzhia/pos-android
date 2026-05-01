package com.tiendanaturista.pos.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tiendanaturista.pos.data.ApiService
import com.tiendanaturista.pos.databinding.ActivityServerConfigBinding
import kotlinx.coroutines.launch

class ServerConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServerConfigBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mostrar URL guardada si ya existe
        val urlGuardada = getServerUrl(this)
        if (urlGuardada.isNotEmpty()) {
            binding.etServerUrl.setText(urlGuardada)
        } else {
            binding.etServerUrl.setText("http://")
        }

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
                    saveServerUrl(this@ServerConfigActivity, finalUrl)
                    Toast.makeText(
                        this@ServerConfigActivity,
                        "✓ Conectado a ${resp.body()?.valor ?: "servidor"}",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@ServerConfigActivity, LoginActivity::class.java))
                    finish()
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ServerConfigActivity, "⚠ Servidor encontrado pero respondió con error (${resp.code()})", Toast.LENGTH_LONG).show()
                        binding.btnGuardar.isEnabled = true
                        binding.btnGuardar.text = "Guardar y conectar"
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ServerConfigActivity, "⚠ No se pudo conectar. Verifica la URL y que el servidor esté encendido.", Toast.LENGTH_LONG).show()
                    binding.btnGuardar.isEnabled = true
                    binding.btnGuardar.text = "Guardar y conectar"
                }
            }
        }
    }

    companion object {
        private const val PREFS = "pos_server_prefs"
        private const val KEY_URL = "server_url"

        fun getServerUrl(context: Context): String {
            return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_URL, "") ?: ""
        }

        fun saveServerUrl(context: Context, url: String) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_URL, url).apply()
        }

        fun clearServerUrl(context: Context) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().remove(KEY_URL).apply()
        }
    }
}
