package com.renaix.data.remote.dto.request

import kotlinx.serialization.Serializable

/**
 * Request para crear un comentario en un producto
 */
@Serializable
data class CreateCommentRequest(
    val texto: String
)
