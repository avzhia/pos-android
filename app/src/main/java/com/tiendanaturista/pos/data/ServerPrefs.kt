package com.tiendanaturista.pos.data

import android.content.Context

object ServerPrefs {
    private const val PREFS = "pos_server_prefs"
    private const val KEY_URL = "server_url"
    private const val DEFAULT_URL = "http://192.168.3.210:8001"

    fun getUrl(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_URL, DEFAULT_URL) ?: DEFAULT_URL

    fun saveUrl(context: Context, url: String) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_URL, url).apply()

    fun hasUrl(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .contains(KEY_URL)

    fun clear(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(KEY_URL).apply()
}
