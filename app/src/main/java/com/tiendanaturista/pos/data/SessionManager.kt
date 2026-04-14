package com.tiendanaturista.pos.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("pos_sesion", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun guardar(sesion: Sesion) {
        prefs.edit().putString("sesion", gson.toJson(sesion)).apply()
    }

    fun cargar(): Sesion? {
        val json = prefs.getString("sesion", null) ?: return null
        return try { gson.fromJson(json, Sesion::class.java) } catch (e: Exception) { null }
    }

    fun haySesion(): Boolean = cargar()?.cajero != null

    fun cerrar() { prefs.edit().remove("sesion").apply() }
}
