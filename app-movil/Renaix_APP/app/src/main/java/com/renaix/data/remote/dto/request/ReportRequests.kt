package com.renaix.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request para crear una denuncia
 */
@Serializable
data class CreateReportRequest(
    val tipo: String,  // producto, comentario, usuario
    val motivo: String,
    val categoria: String,  // contenido_inapropiado, spam, fraude, violencia, informacion_falsa, otro
    @SerialName("producto_id")
    val productoId: Int? = null,
    @SerialName("comentario_id")
    val comentarioId: Int? = null,
    @SerialName("usuario_reportado_id")
    val usuarioReportadoId: Int? = null
)
