package com.renaix.presentation.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renaix.domain.model.Message
import com.renaix.domain.usecase.chat.GetConversationsUseCase
import com.renaix.domain.usecase.chat.SendMessageUseCase
import com.renaix.presentation.common.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de chat individual
 *
 * Responsabilidades:
 * - Cargar mensajes de una conversación
 * - Enviar mensajes
 * - Actualizar en tiempo real (polling o WebSocket en el futuro)
 * - Marcar mensajes como leídos
 */
class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val getConversationsUseCase: GetConversationsUseCase
) : ViewModel() {

    private val _messages = MutableStateFlow<UiState<List<Message>>>(UiState.Idle)
    val messages: StateFlow<UiState<List<Message>>> = _messages.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    private val _sendMessageState = MutableStateFlow<SendMessageState>(SendMessageState.Idle)
    val sendMessageState: StateFlow<SendMessageState> = _sendMessageState.asStateFlow()

    private var currentUserId: Int = 0
    private var currentProductId: Int? = null

    /**
     * Inicializa el chat con un usuario específico
     */
    fun initChat(userId: Int, productId: Int? = null) {
        currentUserId = userId
        currentProductId = productId
        loadMessages()
    }

    /**
     * Carga los mensajes de la conversación
     */
    fun loadMessages() {
        viewModelScope.launch {
            _messages.value = UiState.Loading

            // TODO: Implementar un endpoint específico para obtener mensajes
            // Por ahora, usamos getConversationsUseCase como placeholder
            getConversationsUseCase()
                .onSuccess { conversations ->
                    // Filtrar la conversación actual
                    val conversation = conversations.find { conv ->
                        conv.participantes.any { it.id == currentUserId }
                    }
                    val messagesList = conversation?.mensajes ?: emptyList()
                    _messages.value = UiState.Success(messagesList)
                }
                .onFailure { error ->
                    _messages.value = UiState.Error(
                        error.message ?: "Error al cargar mensajes"
                    )
                }
        }
    }

    /**
     * Actualiza el texto del mensaje
     */
    fun updateMessageText(text: String) {
        _messageText.value = text
    }

    /**
     * Envía un mensaje
     */
    fun sendMessage() {
        val text = _messageText.value.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            _sendMessageState.value = SendMessageState.Sending

            sendMessageUseCase(
                receptorId = currentUserId,
                texto = text,
                productoId = currentProductId
            ).onSuccess { message ->
                _sendMessageState.value = SendMessageState.Success
                _messageText.value = ""

                // Añadir el mensaje a la lista local
                val currentMessages = (_messages.value as? UiState.Success)?.data ?: emptyList()
                _messages.value = UiState.Success(currentMessages + message)
            }.onFailure { error ->
                _sendMessageState.value = SendMessageState.Error(
                    error.message ?: "Error al enviar mensaje"
                )
            }
        }
    }

    /**
     * Resetea el estado de envío
     */
    fun resetSendMessageState() {
        _sendMessageState.value = SendMessageState.Idle
    }

    /**
     * Marca mensajes como leídos
     */
    fun markMessagesAsRead() {
        viewModelScope.launch {
            // TODO: Implementar endpoint para marcar como leído
        }
    }

    /**
     * Refresca los mensajes (pull-to-refresh)
     */
    fun refreshMessages() {
        loadMessages()
    }
}

/**
 * Estados del envío de mensajes
 */
sealed class SendMessageState {
    object Idle : SendMessageState()
    object Sending : SendMessageState()
    object Success : SendMessageState()
    data class Error(val message: String) : SendMessageState()
}
