package com.renaix.domain.usecase.auth

import com.renaix.domain.model.AuthData
import com.renaix.domain.repository.AuthRepository

/**
 * Caso de uso para iniciar sesión
 */
class LoginUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(email: String, password: String): Result<AuthData> {
        // Validaciones
        if (email.isBlank()) {
            return Result.failure(Exception("El email es requerido"))
        }
        if (!isValidEmail(email)) {
            return Result.failure(Exception("El email no es válido"))
        }
        if (password.isBlank()) {
            return Result.failure(Exception("La contraseña es requerida"))
        }
        if (password.length < 6) {
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        }

        return authRepository.login(email.trim(), password)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
