package com.renaix.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request para crear una compra
 */
@Serializable
data class CreatePurchaseRequest(
    @SerialName("producto_id")
    val productoId: Int,
    val notas: String? = null
)
