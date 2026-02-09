package com.renaix.presentation.screens.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renaix.domain.usecase.auth.RegisterUseCase
import com.renaix.presentation.common.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Registro
 */
class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError.asStateFlow()

    fun onNameChange(value: String) {
        _name.value = value
        _nameError.value = null
    }

    fun onEmailChange(value: String) {
        _email.value = value
        _emailError.value = null
    }

    fun onPhoneChange(value: String) {
        _phone.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
        _passwordError.value = null
    }

    fun onConfirmPasswordChange(value: String) {
        _confirmPassword.value = value
        _confirmPasswordError.value = null
    }

    fun register() {
        if (!validateInputs()) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            registerUseCase(
                name = _name.value,
                email = _email.value,
                password = _password.value,
                confirmPassword = _confirmPassword.value,
                phone = _phone.value.takeIf { it.isNotBlank() }
            )
                .onSuccess {
                    _uiState.value = UiState.Success(Unit)
                }
                .onFailure { exception ->
                    _uiState.value = UiState.Error(exception.message ?: "Error en el registro")
                }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (_name.value.isBlank()) {
            _nameError.value = "El nombre es requerido"
            isValid = false
        } else if (_name.value.length < 2) {
            _nameError.value = "Mínimo 2 caracteres"
            isValid = false
        }

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

        if (_confirmPassword.value != _password.value) {
            _confirmPasswordError.value = "Las contraseñas no coinciden"
            isValid = false
        }

        return isValid
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
