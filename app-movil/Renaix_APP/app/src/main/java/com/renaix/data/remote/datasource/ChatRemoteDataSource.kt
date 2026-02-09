package com.renaix.data.remote.datasource

import com.renaix.data.remote.api.RenaixApi
import com.renaix.data.remote.dto.request.SendMessageRequest
import com.renaix.data.remote.dto.response.*

/**
 * DataSource remoto para operaciones de chat/mensajería
 */
class ChatRemoteDataSource(private val api: RenaixApi) {

    /**
     * Obtiene lista de conversaciones
     */
    suspend fun getConversations(): NetworkResult<List<ConversationResponse>> {
        return try {
            val response = api.getConversations()

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al obtener conversaciones",
                    code = response.code
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Error de conexión",
                exception = e
            )
        }
    }

    /**
     * Obtiene conversación con un usuario
     */
    suspend fun getConversation(
        userId: Int,
        productId: Int? = null
    ): NetworkResult<List<MessageResponse>> {
        return try {
            val response = api.getConversation(userId, productId)

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al obtener conversación",
                    code = response.code
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Error de conexión",
                exception = e
            )
        }
    }

    /**
     * Obtiene mensajes no leídos
     */
    suspend fun getUnreadMessages(): NetworkResult<UnreadMessagesResponse> {
        return try {
            val response = api.getUnreadMessages()

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al obtener mensajes",
                    code = response.code
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Error de conexión",
                exception = e
            )
        }
    }

    /**
     * Envía un mensaje
     */
    suspend fun sendMessage(
        receptorId: Int,
        texto: String,
        productoId: Int? = null
    ): NetworkResult<MessageResponse> {
        return try {
            val response = api.sendMessage(
                SendMessageRequest(receptorId, texto, productoId)
            )

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al enviar mensaje",
                    code = response.code
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Error de conexión",
                exception = e
            )
        }
    }

    /**
     * Marca un mensaje como leído
     */
    suspend fun markAsRead(messageId: Int): NetworkResult<Unit> {
        return try {
            val response = api.markMessageAsRead(messageId)

            if (response.success) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al marcar mensaje",
                    code = response.code
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Error de conexión",
                exception = e
            )
        }
    }
}
