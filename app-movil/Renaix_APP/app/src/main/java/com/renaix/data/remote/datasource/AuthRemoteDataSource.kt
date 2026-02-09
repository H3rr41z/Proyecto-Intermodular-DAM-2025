package com.renaix.data.remote.datasource

import com.renaix.data.remote.api.RenaixApi
import com.renaix.data.remote.dto.request.LoginRequest
import com.renaix.data.remote.dto.request.RefreshTokenRequest
import com.renaix.data.remote.dto.request.RegisterRequest
import com.renaix.data.remote.dto.response.AuthResponse
import com.renaix.data.remote.dto.response.RefreshTokenResponse

/**
 * DataSource remoto para operaciones de autenticación
 */
class AuthRemoteDataSource(private val api: RenaixApi) {

    /**
     * Registra un nuevo usuario
     */
    suspend fun register(
        name: String,
        email: String,
        password: String,
        phone: String?
    ): NetworkResult<AuthResponse> {
        return try {
            val response = api.register(
                RegisterRequest(
                    name = name,
                    email = email,
                    password = password,
                    phone = phone
                )
            )

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error de registro",
                    code = response.code
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Error de conexión",
                exception = e
            )
        }
    }

    /**
     * Inicia sesión
     */
    suspend fun login(email: String, password: String): NetworkResult<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Credenciales inválidas",
                    code = response.code
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Error de conexión",
                exception = e
            )
        }
    }

    /**
     * Renueva el token de acceso
     */
    suspend fun refreshToken(refreshToken: String): NetworkResult<RefreshTokenResponse> {
        return try {
            val response = api.refreshToken(RefreshTokenRequest(refreshToken))

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Token inválido",
                    code = response.code
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Error de conexión",
                exception = e
            )
        }
    }

    /**
     * Cierra la sesión
     */
    suspend fun logout(): NetworkResult<Unit> {
        return try {
            val response = api.logout()

            if (response.success) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al cerrar sesión",
                    code = response.code
                )
            }
        } catch (e: Exception) {
            // Aunque falle la petición, consideramos logout exitoso localmente
            NetworkResult.Success(Unit)
        }
    }
}
