package com.renaix.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request para enviar un mensaje
 */
@Serializable
data class SendMessageRequest(
    @SerialName("receptor_id")
    val receptorId: Int,
    val texto: String,
    @SerialName("producto_id")
    val productoId: Int? = null
)
