package com.renaix.data.remote.dto.request

import kotlinx.serialization.Serializable

/**
 * Request para crear una etiqueta
 */
@Serializable
data class CreateTagRequest(
    val nombre: String
)
