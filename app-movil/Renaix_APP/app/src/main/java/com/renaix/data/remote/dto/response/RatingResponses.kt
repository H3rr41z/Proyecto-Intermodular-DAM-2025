package com.renaix.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Respuesta de valoración
 */
@Serializable
data class RatingResponse(
    val id: Int,
    val puntuacion: Int,
    val comentario: String? = null,
    @SerialName("tipo_valoracion")
    val tipoValoracion: String,  // comprador_a_vendedor, vendedor_a_comprador
    val valorador: OwnerResponse? = null,
    val fecha: String
)
