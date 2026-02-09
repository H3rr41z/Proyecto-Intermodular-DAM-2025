package com.renaix.domain.usecase.auth

import com.renaix.domain.model.AuthData
import com.renaix.domain.repository.AuthRepository

/**
 * Caso de uso para registro de usuario
 */
class RegisterUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        phone: String?
    ): Result<AuthData> {
        // Validaciones
        if (name.isBlank()) {
            return Result.failure(Exception("El nombre es requerido"))
        }
        if (name.length < 2) {
            return Result.failure(Exception("El nombre debe tener al menos 2 caracteres"))
        }
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
        if (password != confirmPassword) {
            return Result.failure(Exception("Las contraseñas no coinciden"))
        }

        return authRepository.register(
            name = name.trim(),
            email = email.trim(),
            password = password,
            phone = phone?.takeIf { it.isNotBlank() }?.trim()
        )
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
