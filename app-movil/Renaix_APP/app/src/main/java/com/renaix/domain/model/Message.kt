package com.renaix.domain.model

/**
 * Modelo de dominio para Mensaje
 */
data class Message(
    val id: Int,
    val texto: String,
    val emisor: Owner,
    val receptor: Owner,
    val leido: Boolean = false,
    val fecha: String,
    val hiloId: String? = null
)

/**
 * Modelo de dominio para Conversación
 */
data class Conversation(
    val hiloId: String,
    val participantes: List<Owner>,
    val ultimoMensaje: LastMessage? = null,
    val mensajes: List<Message> = emptyList()
) {
    /**
     * Obtiene el otro participante de la conversación
     */
    fun getOtherParticipant(currentUserId: Int): Owner? {
        return participantes.find { it.id != currentUserId }
    }
}

/**
 * Último mensaje de una conversación
 */
data class LastMessage(
    val texto: String,
    val fecha: String
)

/**
 * Mensajes no leídos
 */
data class UnreadMessages(
    val total: Int,
    val mensajes: List<Message>
)
