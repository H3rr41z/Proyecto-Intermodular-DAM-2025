package com.renaix.presentation.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renaix.domain.usecase.auth.CheckSessionUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Splash
 *
 * Responsabilidades:
 * - Verificar si existe una sesión válida
 * - Decidir si navegar a Login o Main
 * - Mostrar el logo durante un tiempo mínimo
 */
class SplashViewModel(
    private val checkSessionUseCase: CheckSessionUseCase
) : ViewModel() {

    private val _navigationEvent = MutableStateFlow<NavigationEvent>(NavigationEvent.None)
    val navigationEvent: StateFlow<NavigationEvent> = _navigationEvent.asStateFlow()

    init {
        checkSession()
    }

    /**
     * Verifica si hay una sesión válida
     * Espera un mínimo de 2 segundos para mostrar el splash
     */
    private fun checkSession() {
        viewModelScope.launch {
            // Delay mínimo para mostrar el splash
            delay(2000)

            // Verificar sesión
            val hasValidSession = checkSessionUseCase()

            // Navegar según el resultado
            _navigationEvent.value = if (hasValidSession) {
                NavigationEvent.NavigateToMain
            } else {
                NavigationEvent.NavigateToLogin
            }
        }
    }

    /**
     * Resetea el evento de navegación después de ser consumido
     */
    fun onNavigationHandled() {
        _navigationEvent.value = NavigationEvent.None
    }
}

/**
 * Eventos de navegación desde el Splash
 */
sealed class NavigationEvent {
    object None : NavigationEvent()
    object NavigateToLogin : NavigationEvent()
    object NavigateToMain : NavigationEvent()
}
