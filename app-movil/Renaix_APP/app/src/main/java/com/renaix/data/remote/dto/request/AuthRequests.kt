package com.renaix.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request para registro de usuario
 */
@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null
)

/**
 * Request para login
 */
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Request para renovar token
 */
@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token")
    val refreshToken: String
)
