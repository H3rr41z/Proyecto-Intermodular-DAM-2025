package com.renaix.data.remote.datasource

import com.renaix.data.remote.api.RenaixApi
import com.renaix.data.remote.dto.response.CategoryResponse

/**
 * DataSource remoto para operaciones de categorías
 */
class CategoryRemoteDataSource(private val api: RenaixApi) {

    /**
     * Obtiene todas las categorías
     */
    suspend fun getCategories(): NetworkResult<List<CategoryResponse>> {
        return try {
            val response = api.getCategories()

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al obtener categorías",
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
