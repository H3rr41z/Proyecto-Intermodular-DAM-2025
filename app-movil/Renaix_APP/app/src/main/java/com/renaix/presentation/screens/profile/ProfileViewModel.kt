package com.renaix.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renaix.domain.model.User
import com.renaix.domain.usecase.auth.LogoutUseCase
import com.renaix.domain.usecase.user.GetProfileUseCase
import com.renaix.presentation.common.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de perfil
 */
class ProfileViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<User>>(UiState.Loading)
    val state: StateFlow<UiState<User>> = _state.asStateFlow()

    private val _logoutState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val logoutState: StateFlow<UiState<Unit>> = _logoutState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = UiState.Loading

            getProfileUseCase()
                .onSuccess { user ->
                    _state.value = UiState.Success(user)
                }
                .onFailure { exception ->
                    _state.value = UiState.Error(exception.message ?: "Error al cargar perfil")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _logoutState.value = UiState.Loading

            logoutUseCase()
                .onSuccess {
                    _logoutState.value = UiState.Success(Unit)
                }
                .onFailure { exception ->
                    _logoutState.value = UiState.Error(exception.message ?: "Error al cerrar sesión")
                }
        }
    }
}
