package com.renaix.domain.usecase.chat

import com.renaix.domain.model.Message
import com.renaix.domain.repository.ChatRepository

/**
 * Caso de uso para obtener mensajes de una conversación
 */
class GetMessagesUseCase(
    private val chatRepository: ChatRepository
) {
    /**
     * Obtiene los mensajes de una conversación con un usuario
     *
     * @param userId ID del otro usuario en la conversación
     * @param productId ID del producto relacionado (opcional)
     * @return Lista de mensajes de la conversación
     */
    suspend operator fun invoke(
        userId: Int,
        productId: Int? = null
    ): Result<List<Message>> {
        return chatRepository.getConversation(userId, productId)
    }
}
