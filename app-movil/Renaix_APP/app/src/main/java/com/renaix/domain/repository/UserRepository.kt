package com.renaix.domain.repository

import com.renaix.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de usuario
 */
interface UserRepository {
    /**
     * Obtiene el perfil del usuario autenticado
     */
    suspend fun getProfile(): Result<User>

    /**
     * Actualiza el perfil del usuario
     */
    suspend fun updateProfile(
        name: String? = null,
        phone: String? = null,
        image: String? = null
    ): Result<User>

    /**
     * Sube una imagen de perfil
     */
    suspend fun uploadProfileImage(imageBase64: String): Result<User>

    /**
     * Elimina la imagen de perfil
     */
    suspend fun deleteProfileImage(): Result<User>

    /**
     * Cambia la contraseña
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>

    /**
     * Obtiene el perfil público de un usuario
     */
    suspend fun getPublicProfile(userId: Int): Result<PublicUser>

    /**
     * Obtiene las estadísticas del usuario
     */
    suspend fun getStats(): Result<UserStats>

    /**
     * Obtiene los productos del usuario
     */
    suspend fun getMyProducts(page: Int = 1, limit: Int = 20): Result<List<Product>>

    /**
     * Obtiene las compras del usuario
     */
    suspend fun getMyPurchases(page: Int = 1, limit: Int = 20): Result<List<Purchase>>

    /**
     * Obtiene las ventas del usuario
     */
    suspend fun getMySales(page: Int = 1, limit: Int = 20): Result<List<Purchase>>
}
