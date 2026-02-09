package com.renaix.presentation.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renaix.domain.model.Message
import com.renaix.domain.usecase.chat.GetConversationsUseCase
import com.renaix.presentation.common.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de lista de conversaciones
 *
 * Responsabilidades:
 * - Listar todas las conversaciones del usuario
 * - Mostrar mensajes no leídos
 * - Actualizar conversaciones en tiempo real
 */
class ConversationsViewModel(
    private val getConversationsUseCase: GetConversationsUseCase
) : ViewModel() {

    private val _conversations = MutableStateFlow<UiState<List<ConversationItem>>>(UiState.Idle)
    val conversations: StateFlow<UiState<List<ConversationItem>>> = _conversations.asStateFlow()

    init {
        loadConversations()
    }

    /**
     * Carga la lista de conversaciones
     */
    fun loadConversations() {
        viewModelScope.launch {
            _conversations.value = UiState.Loading

            getConversationsUseCase()
                .onSuccess { conversations ->
                    _conversations.value = UiState.Success(conversations)
                }
                .onFailure { error ->
                    _conversations.value = UiState.Error(
                        error.message ?: "Error al cargar conversaciones"
                    )
                }
        }
    }

    /**
     * Refresca las conversaciones (pull-to-refresh)
     */
    fun refreshConversations() {
        loadConversations()
    }

    /**
     * Marca una conversación como leída
     */
    fun markConversationAsRead(userId: Int) {
        viewModelScope.launch {
            // TODO: Implementar lógica para marcar como leída
            // Actualizar el estado local
            val currentState = _conversations.value
            if (currentState is UiState.Success) {
                val updatedConversations = currentState.data.map { conversation ->
                    if (conversation.userId == userId) {
                        conversation.copy(unreadCount = 0)
                    } else {
                        conversation
                    }
                }
                _conversations.value = UiState.Success(updatedConversations)
            }
        }
    }
}

/**
 * Representa un item de conversación en la lista
 */
data class ConversationItem(
    val userId: Int,
    val userName: String,
    val userAvatar: String? = null,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0,
    val productId: Int? = null,
    val productName: String? = null,
    val messages: List<Message> = emptyList()
)
