package com.tiendanaturista.pos.data

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {

    // ── Config ──────────────────────────────────────────────────────────────
    @GET("api/admin/config/{clave}")
    suspend fun getConfig(@Path("clave") clave: String): Response<ConfigResponse>

    // ── Tiendas ─────────────────────────────────────────────────────────────
    @GET("api/tiendas")
    suspend fun getTiendas(): Response<List<Tienda>>

    // ── Cajeros ─────────────────────────────────────────────────────────────
    @GET("api/cajeros")
    suspend fun getCajeros(): Response<List<Cajero>>

    @GET("api/cajeros/verificar-pin")
    suspend fun verificarPin(
        @Query("cajero_id") cajeroId: Int,
        @Query("pin") pin: String
    ): Response<VerificarPinResponse>

    // ── Productos ────────────────────────────────────────────────────────────
    @GET("api/productos")
    suspend fun getProductos(): Response<List<Producto>>

    // ── Clientes ─────────────────────────────────────────────────────────────
    @GET("api/clientes")
    suspend fun getClientes(): Response<List<Cliente>>

    @GET("api/clientes/{id}/ventas")
    suspend fun getVentasCliente(@Path("id") id: Int): Response<List<VentaHistorial>>

    // ── Ventas ────────────────────────────────────────────────────────────────
    @POST("api/ventas")
    suspend fun registrarVenta(@Body venta: VentaRequest): Response<Any>

    // ── Turnos ────────────────────────────────────────────────────────────────
    @GET("api/turnos/activo")
    suspend fun getTurnoActivo(
        @Query("cajero_id") cajeroId: Int,
        @Query("tienda_id") tiendaId: Int
    ): Response<TurnoActivoResponse>

    @POST("api/turnos/abrir")
    suspend fun abrirTurno(@Body turno: AbrirTurnoRequest): Response<TurnoResponse>

    @POST("api/turnos/cerrar/{turno_id}")
    suspend fun cerrarTurno(@Path("turno_id") turnoId: Int): Response<Any>

    companion object {
        fun create(baseUrl: String): ApiService {
            val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            return Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }

        fun createFromPrefs(context: android.content.Context): ApiService {
            val url = ServerPrefs.getUrl(context)
            return create(url)
        }
    }
}
