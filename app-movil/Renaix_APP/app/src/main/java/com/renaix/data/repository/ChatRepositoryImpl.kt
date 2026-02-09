package com.renaix.data.repository

import com.renaix.data.mapper.toDomain
import com.renaix.data.remote.datasource.ChatRemoteDataSource
import com.renaix.data.remote.datasource.NetworkResult
import com.renaix.domain.model.Conversation
import com.renaix.domain.model.Message
import com.renaix.domain.model.UnreadMessages
import com.renaix.domain.repository.ChatRepository

/**
 * Implementación del repositorio de chat
 */
class ChatRepositoryImpl(
    private val remoteDataSource: ChatRemoteDataSource
) : ChatRepository {

    override suspend fun getConversations(): Result<List<Conversation>> {
        return when (val result = remoteDataSource.getConversations()) {
            is NetworkResult.Success -> Result.success(result.data.map { it.toDomain() })
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun getConversation(userId: Int, productId: Int?): Result<List<Message>> {
        return when (val result = remoteDataSource.getConversation(userId, productId)) {
            is NetworkResult.Success -> Result.success(result.data.map { it.toDomain() })
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun getUnreadMessages(): Result<UnreadMessages> {
        return when (val result = remoteDataSource.getUnreadMessages()) {
            is NetworkResult.Success -> Result.success(result.data.toDomain())
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun getUnreadCount(): Result<Int> {
        return when (val result = remoteDataSource.getUnreadMessages()) {
            is NetworkResult.Success -> Result.success(result.data.total)
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun sendMessage(
        receptorId: Int,
        texto: String,
        productoId: Int?
    ): Result<Message> {
        return when (val result = remoteDataSource.sendMessage(receptorId, texto, productoId)) {
            is NetworkResult.Success -> Result.success(result.data.toDomain())
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun markAsRead(messageId: Int): Result<Unit> {
        return when (val result = remoteDataSource.markAsRead(messageId)) {
            is NetworkResult.Success -> Result.success(Unit)
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }
}
