package com.renaix.domain.usecase.user

import com.renaix.domain.model.PublicUser
import com.renaix.domain.repository.UserRepository

/**
 * Caso de uso para obtener el perfil público de un usuario
 */
class GetPublicProfileUseCase(
    private val userRepository: UserRepository
) {
    /**
     * Obtiene el perfil público de un usuario específico
     *
     * @param userId ID del usuario
     * @return Información pública del usuario
     */
    suspend operator fun invoke(userId: Int): Result<PublicUser> {
        return userRepository.getPublicProfile(userId)
    }
}
