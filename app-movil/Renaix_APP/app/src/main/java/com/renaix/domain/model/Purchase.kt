package com.renaix.domain.model

/**
 * Modelo de dominio para Compra
 */
data class Purchase(
    val id: Int,
    val producto: ProductSimple,
    val comprador: Owner,
    val vendedor: Owner,
    val precioFinal: Double,
    val estado: EstadoCompra,
    val fechaCompra: String? = null,
    val notas: String? = null,
    val compradorValoro: Boolean = false,
    val vendedorValoro: Boolean = false
)

/**
 * Producto simplificado para compras
 */
data class ProductSimple(
    val id: Int,
    val nombre: String
)

/**
 * Estado de la compra
 */
enum class EstadoCompra(val value: String, val displayName: String) {
    PENDIENTE("pendiente", "Pendiente"),
    CONFIRMADA("confirmada", "Confirmada"),
    COMPLETADA("completada", "Completada"),
    CANCELADA("cancelada", "Cancelada");

    companion object {
        fun fromString(value: String): EstadoCompra {
            return entries.find { it.value == value } ?: PENDIENTE
        }
    }
}
