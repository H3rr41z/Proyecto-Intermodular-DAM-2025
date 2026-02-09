package com.renaix.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request para actualizar perfil de usuario
 */
@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val phone: String? = null,
    val image: String? = null  // Base64
)

/**
 * Request para subir imagen de perfil
 */
@Serializable
data class UploadProfileImageRequest(
    val image: String  // Base64 format: "data:image/jpeg;base64,..."
)

/**
 * Request para cambiar contraseña
 */
@Serializable
data class ChangePasswordRequest(
    @SerialName("password_actual")
    val currentPassword: String,
    @SerialName("password_nueva")
    val newPassword: String
)
