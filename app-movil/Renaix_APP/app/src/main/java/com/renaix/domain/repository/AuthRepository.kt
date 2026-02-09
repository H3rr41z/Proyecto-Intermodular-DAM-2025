package com.renaix.domain.repository

import com.renaix.domain.model.AuthData
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de autenticación
 */
interface AuthRepository {
    /**
     * Estado de login observale
     */
    val isLoggedIn: Flow<Boolean>

    /**
     * Registra un nuevo usuario
     */
    suspend fun register(
        name: String,
        email: String,
        password: String,
        phone: String?
    ): Result<AuthData>

    /**
     * Inicia sesión
     */
    suspend fun login(email: String, password: String): Result<AuthData>

    /**
     * Cierra la sesión
     */
    suspend fun logout(): Result<Unit>

    /**
     * Renueva el token de acceso
     */
    suspend fun refreshToken(): Result<String>

    /**
     * Verifica si hay una sesión válida
     */
    fun hasValidSession(): Boolean

    /**
     * Obtiene el ID del usuario actual
     */
    fun getCurrentUserId(): Int
}
