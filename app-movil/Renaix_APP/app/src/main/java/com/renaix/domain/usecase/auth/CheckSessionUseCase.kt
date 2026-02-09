package com.renaix.domain.usecase.auth

import com.renaix.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

/**
 * Caso de uso para verificar sesión
 */
class CheckSessionUseCase(private val authRepository: AuthRepository) {

    /**
     * Verifica si hay una sesión válida
     */
    operator fun invoke(): Boolean {
        return authRepository.hasValidSession()
    }

    /**
     * Observa el estado de login
     */
    fun observeLoginState(): Flow<Boolean> {
        return authRepository.isLoggedIn
    }
}
