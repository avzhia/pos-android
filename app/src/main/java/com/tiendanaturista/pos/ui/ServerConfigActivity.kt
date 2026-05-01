package com.tiendanaturista.pos.ui

import android.content.Context
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

        // Verificar si ya hay URL guardada
        val urlGuardada = ServerPrefs.getUrl(this)
        if (urlGuardada.isNotEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityServerConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                    // Guardar URL antes de navegar
                    ServerPrefs.saveUrl(this@ServerConfigActivity, finalUrl)
                    
                    // Verificar que se guardó
                    val guardada = ServerPrefs.getUrl(this@ServerConfigActivity)
                    
                    runOnUiThread {
                        Toast.makeText(
                            this@ServerConfigActivity,
                            "✓ Conectado a ${resp.body()?.valor ?: "servidor"}",
                            Toast.LENGTH_LONG
                        ).show()
                        
                        if (guardada == finalUrl) {
                            val intent = Intent(this@ServerConfigActivity, LoginActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@ServerConfigActivity, "⚠ Error al guardar configuración", Toast.LENGTH_LONG).show()
                            binding.btnGuardar.isEnabled = true
                            binding.btnGuardar.text = "Guardar y conectar"
                        }
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
                        "⚠ No se pudo conectar: ${e.message}",
                        Toast.LENGTH_LONG).show()
                    binding.btnGuardar.isEnabled = true
                    binding.btnGuardar.text = "Guardar y conectar"
                }
            }
        }
    }
}
