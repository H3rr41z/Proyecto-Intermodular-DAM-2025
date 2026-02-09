package com.renaix.domain.repository

import com.renaix.domain.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de categorías
 */
interface CategoryRepository {
    /**
     * Obtiene todas las categorías
     */
    suspend fun getCategories(): Result<List<Category>>

    /**
     * Obtiene categorías del caché
     */
    fun getCachedCategories(): Flow<List<Category>>
}
