package com.renaix.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Respuesta de denuncia
 */
@Serializable
data class ReportResponse(
    val id: Int,
    val tipo: String,  // producto, comentario, usuario
    val motivo: String,
    val categoria: String,
    val estado: String,  // pendiente, en_revision, resuelta, rechazada
    @SerialName("fecha_denuncia")
    val fechaDenuncia: String
)
