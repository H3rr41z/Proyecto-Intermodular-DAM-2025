package com.renaix.data.remote.dto.response

import kotlinx.serialization.Serializable

/**
 * Respuesta de comentario
 */
@Serializable
data class CommentResponse(
    val id: Int,
    val texto: String,
    val usuario: OwnerResponse,
    val fecha: String
)
