package com.renaix.data.repository

import com.renaix.data.local.preferences.PreferencesManager
import com.renaix.data.mapper.toDomain
import com.renaix.data.remote.datasource.AuthRemoteDataSource
import com.renaix.data.remote.datasource.NetworkResult
import com.renaix.domain.model.AuthData
import com.renaix.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementación del repositorio de autenticación
 */
class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
    private val preferencesManager: PreferencesManager
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> = preferencesManager.isLoggedIn

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        phone: String?
    ): Result<AuthData> {
        return when (val result = remoteDataSource.register(name, email, password, phone)) {
            is NetworkResult.Success -> {
                val authData = result.data.toDomain()
                // Guardar datos de sesión
                preferencesManager.saveAuthData(
                    accessToken = authData.accessToken,
                    refreshToken = authData.refreshToken,
                    userId = authData.user.id,
                    userName = authData.user.name,
                    userEmail = authData.user.email
                )
                Result.success(authData)
            }
            is NetworkResult.Error -> {
                Result.failure(Exception(result.message))
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<AuthData> {
        return when (val result = remoteDataSource.login(email, password)) {
            is NetworkResult.Success -> {
                val authData = result.data.toDomain()
                // Guardar datos de sesión
                preferencesManager.saveAuthData(
                    accessToken = authData.accessToken,
                    refreshToken = authData.refreshToken,
                    userId = authData.user.id,
                    userName = authData.user.name,
                    userEmail = authData.user.email
                )
                Result.success(authData)
            }
            is NetworkResult.Error -> {
                Result.failure(Exception(result.message))
            }
        }
    }

    override suspend fun logout(): Result<Unit> {
        // Intentar logout en servidor (no importa si falla)
        remoteDataSource.logout()
        // Limpiar datos locales
        preferencesManager.clearSession()
        return Result.success(Unit)
    }

    override suspend fun refreshToken(): Result<String> {
        val refreshToken = preferencesManager.getRefreshToken()
            ?: return Result.failure(Exception("No hay refresh token"))

        return when (val result = remoteDataSource.refreshToken(refreshToken)) {
            is NetworkResult.Success -> {
                preferencesManager.saveAccessToken(result.data.accessToken)
                Result.success(result.data.accessToken)
            }
            is NetworkResult.Error -> {
                // Si falla el refresh, limpiar sesión
                preferencesManager.clearSession()
                Result.failure(Exception(result.message))
            }
        }
    }

    override fun hasValidSession(): Boolean {
        return preferencesManager.hasValidSession()
    }

    override fun getCurrentUserId(): Int {
        return preferencesManager.getUserId()
    }
}
