package com.renaix.presentation.screens.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renaix.domain.usecase.auth.LoginUseCase
import com.renaix.presentation.common.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Login
 */
class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    fun onEmailChange(value: String) {
        _email.value = value
        _emailError.value = null
    }

    fun onPasswordChange(value: String) {
        _password.value = value
        _passwordError.value = null
    }

    fun login() {
        if (!validateInputs()) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            loginUseCase(_email.value, _password.value)
                .onSuccess {
                    _uiState.value = UiState.Success(Unit)
                }
                .onFailure { exception ->
                    _uiState.value = UiState.Error(exception.message ?: "Error al iniciar sesión")
                }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (_email.value.isBlank()) {
            _emailError.value = "El email es requerido"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_email.value).matches()) {
            _emailError.value = "Email inválido"
            isValid = false
        }

        if (_password.value.isBlank()) {
            _passwordError.value = "La contraseña es requerida"
            isValid = false
        } else if (_password.value.length < 6) {
            _passwordError.value = "Mínimo 6 caracteres"
            isValid = false
        }

        return isValid
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
