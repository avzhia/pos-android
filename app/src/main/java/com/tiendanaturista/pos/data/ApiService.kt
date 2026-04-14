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

    companion object {
        private const val BASE_URL = "https://pos.minube.icu/"

        fun create(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
