package com.renaix.data.remote.dto.response

import kotlinx.serialization.Serializable

/**
 * Respuesta de categoría
 */
@Serializable
data class CategoryResponse(
    val id: Int,
    val name: String,
    val descripcion: String? = null,
    val imagen_url: String? = null,
    val producto_count: Int = 0
)
