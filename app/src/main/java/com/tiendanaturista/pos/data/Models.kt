package com.tiendanaturista.pos.data

import com.google.gson.annotations.SerializedName

// ── Auth / Sesión ──────────────────────────────────────────────────────────────

data class Sesion(
    val cajero: String,
    val cajeroId: Int,
    val tiendaId: Int,
    val tiendaNombre: String,
    val fondoInicial: Double,
    val apertura: String,
    val negocio: String = "TiendaNaturistaMX",
    val turnoId: Int? = null
)

// ── Turnos ─────────────────────────────────────────────────────────────────────

data class TurnoActivoResponse(
    val activo: Boolean,
    @SerializedName("turno_id") val turnoId: Int? = null,
    @SerializedName("fondo_inicial") val fondoInicial: Double? = null,
    @SerializedName("fecha_apertura") val fechaApertura: String? = null
)

data class AbrirTurnoRequest(
    @SerializedName("cajero_id") val cajeroId: Int,
    @SerializedName("tienda_id") val tiendaId: Int,
    @SerializedName("fondo_inicial") val fondoInicial: Double
)

data class TurnoResponse(
    val ok: Boolean,
    @SerializedName("turno_id") val turnoId: Int,
    @SerializedName("fondo_inicial") val fondoInicial: Double,
    @SerializedName("fecha_apertura") val fechaApertura: String
)

// ── Tiendas y Cajeros ──────────────────────────────────────────────────────────

data class Tienda(
    val id: Int,
    val nombre: String,
    val direccion: String = "",
    val activa: Boolean = true
)

data class Cajero(
    val id: Int,
    val nombre: String,
    @SerializedName("tienda_id") val tiendaId: Int,
    val activo: Boolean = true,
    @SerializedName("tiene_pin") val tienePin: Boolean = false
)

data class VerificarPinResponse(val ok: Boolean)

// ── Productos ──────────────────────────────────────────────────────────────────

data class Lote(
    val id: Int,
    @SerializedName("numero_lote") val numeroLote: String,
    val stock: Int,
    @SerializedName("costo_unitario") val costoUnitario: Double = 0.0,
    val caduca: Boolean = true,
    @SerializedName("fecha_caducidad") val fechaCaducidad: String? = null
)

data class Producto(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val icono: String = "🌿",
    val precio: Double,
    @SerializedName("stock_min") val stockMin: Int = 5,
    val activo: Boolean = true,
    @SerializedName("codigo_barras") val codigoBarras: String? = null,
    val lotes: List<Lote> = emptyList()
) {
    val stockTotal: Int get() = lotes.filter { it.numeroLote != "DEV" }.sumOf { it.stock }
}

// ── Clientes ───────────────────────────────────────────────────────────────────

data class Cliente(
    val id: Int,
    val nombre: String,
    val telefono: String = "",
    val email: String = "",
    @SerializedName("fecha_cumple") val fechaCumple: String? = null,
    @SerializedName("cliente_desde") val clienteDesde: String = "",
    val notas: String = "",
    val tipo: String = "regular",
    @SerializedName("total_compras") val totalCompras: Int = 0,
    @SerializedName("total_gastado") val totalGastado: Double = 0.0
)

data class VentaHistorial(
    val id: Int,
    val fecha: String,
    val total: Double,
    @SerializedName("forma_pago") val formaPago: String,
    val items: List<ItemHistorial> = emptyList()
)

data class ItemHistorial(
    val nombre: String,
    val cantidad: Int,
    val precio: Double
)

// ── Ventas ─────────────────────────────────────────────────────────────────────

data class ItemVentaRequest(
    @SerializedName("producto_id") val productoId: Int,
    @SerializedName("nombre_prod") val nombreProd: String,
    val cantidad: Int,
    @SerializedName("precio_unit") val precioUnit: Double,
    val subtotal: Double
)

data class VentaRequest(
    @SerializedName("cliente_id") val clienteId: Int = 1,
    @SerializedName("tienda_id") val tiendaId: Int,
    val cajero: String,
    @SerializedName("forma_pago") val formaPago: String,
    val total: Double,
    val notas: String = "Venta app móvil",
    val items: List<ItemVentaRequest>
)

// ── Config ─────────────────────────────────────────────────────────────────────

data class ConfigResponse(
    val clave: String,
    val valor: String
)

// ── Ticket (estado local) ──────────────────────────────────────────────────────

data class TicketItem(
    val producto: Producto,
    var cantidad: Int = 1
) {
    val subtotal: Double get() = producto.precio * cantidad
}
