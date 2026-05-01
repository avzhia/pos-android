package com.tiendanaturista.pos.data

import android.content.Context

object ServerPrefs {
    private const val PREFS = "pos_server_prefs"
    private const val KEY_URL = "server_url"

    fun getUrl(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_URL, "") ?: ""

    fun saveUrl(context: Context, url: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_URL, url).commit() // commit() en lugar de apply() — síncrono
    }

    fun clear(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(KEY_URL).commit()
}
