package com.renaix.domain.usecase.chat

import com.renaix.domain.model.Conversation
import com.renaix.domain.repository.ChatRepository

/**
 * Caso de uso para obtener conversaciones
 */
class GetConversationsUseCase(private val chatRepository: ChatRepository) {

    suspend operator fun invoke(): Result<List<Conversation>> {
        return chatRepository.getConversations()
    }
}
