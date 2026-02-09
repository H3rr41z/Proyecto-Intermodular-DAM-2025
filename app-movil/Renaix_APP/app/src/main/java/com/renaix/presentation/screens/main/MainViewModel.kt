package com.renaix.presentation.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renaix.domain.usecase.auth.LogoutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla Main
 *
 * Responsabilidades:
 * - Gestionar el estado de la bottom navigation
 * - Manejar el logout
 * - Coordinar la navegación entre tabs
 */
class MainViewModel(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(MainTab.Products)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Idle)
    val logoutState: StateFlow<LogoutState> = _logoutState.asStateFlow()

    /**
     * Cambia el tab seleccionado
     */
    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
    }

    /**
     * Cierra la sesión del usuario
     */
    fun logout() {
        viewModelScope.launch {
            _logoutState.value = LogoutState.Loading

            logoutUseCase()
                .onSuccess {
                    _logoutState.value = LogoutState.Success
                }
                .onFailure { error ->
                    // Incluso si falla, consideramos logout exitoso
                    // porque los datos locales se limpian
                    _logoutState.value = LogoutState.Success
                }
        }
    }

    /**
     * Resetea el estado de logout
     */
    fun resetLogoutState() {
        _logoutState.value = LogoutState.Idle
    }
}

/**
 * Tabs disponibles en la navegación principal
 */
enum class MainTab {
    Products,
    Search,
    Map,
    Chat,
    Profile
}

/**
 * Estados del proceso de logout
 */
sealed class LogoutState {
    object Idle : LogoutState()
    object Loading : LogoutState()
    object Success : LogoutState()
}
