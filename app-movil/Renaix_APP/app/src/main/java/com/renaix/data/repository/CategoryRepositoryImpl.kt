package com.renaix.data.repository

import com.renaix.data.local.database.RenaixDatabase
import com.renaix.data.mapper.toDomain
import com.renaix.data.remote.datasource.CategoryRemoteDataSource
import com.renaix.data.remote.datasource.NetworkResult
import com.renaix.domain.model.Category
import com.renaix.domain.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Implementación del repositorio de categorías
 */
class CategoryRepositoryImpl(
    private val remoteDataSource: CategoryRemoteDataSource,
    private val database: RenaixDatabase
) : CategoryRepository {

    override suspend fun getCategories(): Result<List<Category>> {
        return when (val result = remoteDataSource.getCategories()) {
            is NetworkResult.Success -> {
                val categories = result.data.map { it.toDomain() }
                // Guardar en caché
                withContext(Dispatchers.IO) {
                    categories.forEach { category ->
                        database.categoryQueries.insertCategory(
                            id = category.id.toLong(),
                            nombre = category.name,
                            descripcion = category.descripcion,
                            imagen_url = category.imagenUrl,
                            producto_count = category.productoCount.toLong()
                        )
                    }
                }
                Result.success(categories)
            }
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override fun getCachedCategories(): Flow<List<Category>> = flow {
        val cached = database.categoryQueries.selectAllCategories().executeAsList()
        val categories = cached.map {
            Category(
                id = it.id.toInt(),
                name = it.nombre,
                descripcion = it.descripcion,
                imagenUrl = it.imagen_url,
                productoCount = it.producto_count.toInt()
            )
        }
        emit(categories)
    }.flowOn(Dispatchers.IO)
}
