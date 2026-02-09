package com.renaix.domain.usecase.auth

import com.renaix.domain.repository.AuthRepository

/**
 * Caso de uso para cerrar sesión
 */
class LogoutUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}
