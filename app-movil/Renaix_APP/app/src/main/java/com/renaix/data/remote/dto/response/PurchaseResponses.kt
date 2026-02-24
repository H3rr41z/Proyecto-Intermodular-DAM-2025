package com.renaix.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Respuesta de compra
 */
@Serializable
data class PurchaseResponse(
    val id: Int,
    val producto: ProductSimpleResponse,
    val comprador: OwnerResponse,
    val vendedor: OwnerResponse,
    @SerialName("precio_final")
    val precioFinal: Double,
    val estado: String,  // pendiente, confirmada, completada, cancelada
    @SerialName("fecha_compra")
    val fechaCompra: String? = null,
    val notas: String? = null,
    @SerialName("comprador_valoro")
    val compradorValoro: Boolean = false,
    @SerialName("vendedor_valoro")
    val vendedorValoro: Boolean = false
)

/**
 * Respuesta simplificada de producto en compra
 */
@Serializable
data class ProductSimpleResponse(
    val id: Int,
    val nombre: String
)

/**
 * Respuesta de estado de compra (confirmar/completar/cancelar)
 */
@Serializable
data class PurchaseStatusResponse(
    val id: Int,
    val estado: String
)
