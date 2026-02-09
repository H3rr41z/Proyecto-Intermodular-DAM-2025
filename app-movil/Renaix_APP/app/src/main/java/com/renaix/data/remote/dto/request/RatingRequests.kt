package com.renaix.data.remote.dto.request

import kotlinx.serialization.Serializable

/**
 * Request para valorar una transacción
 */
@Serializable
data class CreateRatingRequest(
    val puntuacion: Int,  // 1-5
    val comentario: String? = null
)
