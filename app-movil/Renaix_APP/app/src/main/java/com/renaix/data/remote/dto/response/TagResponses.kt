package com.renaix.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Respuesta de etiqueta
 */
@Serializable
data class TagResponse(
    val id: Int,
    @SerialName("nombre")
    val name: String,
    @SerialName("producto_count")
    val productoCount: Int = 0,
    val color: Int = 0
)
