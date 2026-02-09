package com.renaix.domain.usecase.user

import com.renaix.domain.model.User
import com.renaix.domain.repository.UserRepository

/**
 * Caso de uso para obtener el perfil del usuario
 */
class GetProfileUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(): Result<User> {
        return userRepository.getProfile()
    }
}
