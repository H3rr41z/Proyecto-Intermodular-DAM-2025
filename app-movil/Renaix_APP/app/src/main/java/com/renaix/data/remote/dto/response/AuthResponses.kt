package com.renaix.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Respuesta de autenticación (login/register)
 */
@Serializable
data class AuthResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    val user: UserBasicResponse
)

/**
 * Información básica del usuario en respuesta de auth
 */
@Serializable
data class UserBasicResponse(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String? = null
)

/**
 * Respuesta de renovación de token
 */
@Serializable
data class RefreshTokenResponse(
    @SerialName("access_token")
    val accessToken: String
)
