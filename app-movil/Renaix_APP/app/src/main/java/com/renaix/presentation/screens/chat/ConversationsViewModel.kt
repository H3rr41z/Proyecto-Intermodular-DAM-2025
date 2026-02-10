package com.renaix.presentation.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renaix.domain.model.Conversation
import com.renaix.domain.usecase.chat.GetConversationsUseCase
import com.renaix.presentation.common.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de lista de conversaciones
 */
class ConversationsViewModel(
    private val getConversationsUseCase: GetConversationsUseCase
) : ViewModel() {

    private val _conversations = MutableStateFlow<UiState<List<Conversation>>>(UiState.Idle)
    val conversations: StateFlow<UiState<List<Conversation>>> = _conversations.asStateFlow()

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
     * Refresca las conversaciones
     */
    fun refreshConversations() {
        loadConversations()
    }
}
