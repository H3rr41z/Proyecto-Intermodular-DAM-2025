package com.renaix.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Respuesta de mensaje
 */
@Serializable
data class MessageResponse(
    val id: Int,
    val texto: String,
    val emisor: OwnerResponse,
    val receptor: OwnerResponse,
    val leido: Boolean = false,
    val fecha: String,
    @SerialName("hilo_id")
    val hiloId: String? = null
)

/**
 * Respuesta de conversación
 */
@Serializable
data class ConversationResponse(
    @SerialName("hilo_id")
    val hiloId: String,
    val participantes: List<OwnerResponse>,
    @SerialName("ultimo_mensaje")
    val ultimoMensaje: LastMessageResponse? = null,
    val mensajes: List<MessageResponse> = emptyList()
)

/**
 * Último mensaje en conversación
 */
@Serializable
data class LastMessageResponse(
    val texto: String,
    val fecha: String
)

/**
 * Respuesta de mensajes no leídos
 */
@Serializable
data class UnreadMessagesResponse(
    val total: Int,
    val mensajes: List<MessageResponse>
)
