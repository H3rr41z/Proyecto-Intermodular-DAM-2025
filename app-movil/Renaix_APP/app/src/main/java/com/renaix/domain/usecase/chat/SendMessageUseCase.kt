package com.renaix.domain.usecase.chat

import com.renaix.domain.model.Message
import com.renaix.domain.repository.ChatRepository

/**
 * Caso de uso para enviar un mensaje
 */
class SendMessageUseCase(private val chatRepository: ChatRepository) {

    suspend operator fun invoke(
        receptorId: Int,
        texto: String,
        productoId: Int? = null
    ): Result<Message> {
        if (receptorId <= 0) {
            return Result.failure(Exception("Destinatario inválido"))
        }
        if (texto.isBlank()) {
            return Result.failure(Exception("El mensaje no puede estar vacío"))
        }
        if (texto.length > 2000) {
            return Result.failure(Exception("El mensaje no puede superar 2000 caracteres"))
        }

        return chatRepository.sendMessage(
            receptorId = receptorId,
            texto = texto.trim(),
            productoId = productoId
        )
    }
}
