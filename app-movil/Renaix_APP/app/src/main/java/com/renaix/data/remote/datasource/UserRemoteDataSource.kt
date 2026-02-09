package com.renaix.data.remote.datasource

import com.renaix.data.remote.api.RenaixApi
import com.renaix.data.remote.dto.request.ChangePasswordRequest
import com.renaix.data.remote.dto.request.UpdateProfileRequest
import com.renaix.data.remote.dto.request.UploadProfileImageRequest
import com.renaix.data.remote.dto.response.*

/**
 * DataSource remoto para operaciones de usuario
 */
class UserRemoteDataSource(private val api: RenaixApi) {

    /**
     * Obtiene el perfil del usuario autenticado
     */
    suspend fun getProfile(): NetworkResult<UserProfileResponse> {
        return try {
            val response = api.getProfile()

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al obtener perfil",
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
     * Actualiza el perfil
     */
    suspend fun updateProfile(
        name: String?,
        phone: String?,
        image: String?
    ): NetworkResult<UserProfileResponse> {
        return try {
            val response = api.updateProfile(
                UpdateProfileRequest(name = name, phone = phone, image = image)
            )

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al actualizar perfil",
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
     * Sube imagen de perfil
     */
    suspend fun uploadProfileImage(imageBase64: String): NetworkResult<UserProfileResponse> {
        return try {
            val response = api.uploadProfileImage(UploadProfileImageRequest(imageBase64))

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al subir imagen",
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
     * Elimina la imagen de perfil
     */
    suspend fun deleteProfileImage(): NetworkResult<UserProfileResponse> {
        return try {
            val response = api.deleteProfileImage()

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al eliminar imagen",
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
     * Cambia la contraseña
     */
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): NetworkResult<Unit> {
        return try {
            val response = api.changePassword(
                ChangePasswordRequest(currentPassword, newPassword)
            )

            if (response.success) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al cambiar contraseña",
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
     * Obtiene perfil público de un usuario
     */
    suspend fun getPublicProfile(userId: Int): NetworkResult<PublicUserResponse> {
        return try {
            val response = api.getPublicProfile(userId)

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Usuario no encontrado",
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
     * Obtiene estadísticas del usuario
     */
    suspend fun getMyStats(): NetworkResult<UserStatsResponse> {
        return try {
            val response = api.getMyStats()

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al obtener estadísticas",
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
     * Obtiene mis productos
     */
    suspend fun getMyProducts(page: Int, limit: Int): NetworkResult<List<ProductListResponse>> {
        return try {
            val response = api.getMyProducts(page, limit)

            if (response.success && response.data != null) {
                val pagination = response.pagination?.let {
                    PaginationInfo(
                        total = it.total,
                        page = it.page,
                        limit = it.limit,
                        totalPages = it.total_pages,
                        hasNext = it.has_next,
                        hasPrev = it.has_prev
                    )
                }
                NetworkResult.Success(response.data, pagination)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al obtener productos",
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
     * Obtiene mis compras
     */
    suspend fun getMyPurchases(page: Int, limit: Int): NetworkResult<List<PurchaseResponse>> {
        return try {
            val response = api.getMyPurchases(page, limit)

            if (response.success && response.data != null) {
                val pagination = response.pagination?.let {
                    PaginationInfo(
                        total = it.total,
                        page = it.page,
                        limit = it.limit,
                        totalPages = it.total_pages,
                        hasNext = it.has_next,
                        hasPrev = it.has_prev
                    )
                }
                NetworkResult.Success(response.data, pagination)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al obtener compras",
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
     * Obtiene mis ventas
     */
    suspend fun getMySales(page: Int, limit: Int): NetworkResult<List<PurchaseResponse>> {
        return try {
            val response = api.getMySales(page, limit)

            if (response.success && response.data != null) {
                val pagination = response.pagination?.let {
                    PaginationInfo(
                        total = it.total,
                        page = it.page,
                        limit = it.limit,
                        totalPages = it.total_pages,
                        hasNext = it.has_next,
                        hasPrev = it.has_prev
                    )
                }
                NetworkResult.Success(response.data, pagination)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al obtener ventas",
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
}
