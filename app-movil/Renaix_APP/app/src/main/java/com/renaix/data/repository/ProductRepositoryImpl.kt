package com.renaix.data.repository

import com.renaix.data.local.database.RenaixDatabase
import com.renaix.data.mapper.toDomain
import com.renaix.data.remote.datasource.NetworkResult
import com.renaix.data.remote.datasource.ProductRemoteDataSource
import com.renaix.data.remote.dto.request.ProductSearchParams
import com.renaix.data.remote.dto.request.UpdateProductRequest
import com.renaix.domain.model.*
import com.renaix.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Implementación del repositorio de productos
 */
class ProductRepositoryImpl(
    private val remoteDataSource: ProductRemoteDataSource,
    private val database: RenaixDatabase
) : ProductRepository {

    override suspend fun getProducts(page: Int, limit: Int): Result<List<Product>> {
        return when (val result = remoteDataSource.getProducts(page, limit)) {
            is NetworkResult.Success -> {
                val products = result.data.map { it.toDomain() }
                // Guardar en caché
                withContext(Dispatchers.IO) {
                    products.forEach { product ->
                        cacheProduct(product)
                    }
                }
                Result.success(products)
            }
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun getProductDetail(productId: Int): Result<ProductDetail> {
        return when (val result = remoteDataSource.getProductDetail(productId)) {
            is NetworkResult.Success -> Result.success(result.data.toDomain())
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun searchProducts(
        query: String?,
        categoriaId: Int?,
        precioMin: Double?,
        precioMax: Double?,
        estadoProducto: String?,
        ubicacion: String?,
        orden: String?,
        page: Int,
        limit: Int
    ): Result<List<Product>> {
        val params = ProductSearchParams(
            query = query,
            categoriaId = categoriaId,
            precioMin = precioMin,
            precioMax = precioMax,
            estadoProducto = estadoProducto,
            ubicacion = ubicacion,
            orden = orden,
            page = page,
            limit = limit
        )

        return when (val result = remoteDataSource.searchProducts(params)) {
            is NetworkResult.Success -> Result.success(result.data.map { it.toDomain() })
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun createProduct(
        nombre: String,
        descripcion: String,
        precio: Double,
        categoriaId: Int,
        estadoProducto: String,
        antiguedad: String?,
        ubicacion: String?,
        etiquetaIds: List<Int>?,
        etiquetaNombres: List<String>?
    ): Result<Int> {
        return when (val result = remoteDataSource.createProduct(
            nombre, descripcion, precio, categoriaId, estadoProducto,
            antiguedad, ubicacion, etiquetaIds, etiquetaNombres
        )) {
            is NetworkResult.Success -> Result.success(result.data.id)
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun updateProduct(
        productId: Int,
        nombre: String?,
        descripcion: String?,
        precio: Double?,
        categoriaId: Int?,
        estadoProducto: String?
    ): Result<ProductDetail> {
        val request = UpdateProductRequest(
            nombre = nombre,
            descripcion = descripcion,
            precio = precio,
            categoriaId = categoriaId,
            estadoProducto = estadoProducto
        )

        return when (val result = remoteDataSource.updateProduct(productId, request)) {
            is NetworkResult.Success -> Result.success(result.data.toDomain())
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun deleteProduct(productId: Int): Result<Unit> {
        return when (val result = remoteDataSource.deleteProduct(productId)) {
            is NetworkResult.Success -> {
                // Eliminar del caché
                withContext(Dispatchers.IO) {
                    database.productQueries.deleteProduct(productId.toLong())
                }
                Result.success(Unit)
            }
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun publishProduct(productId: Int): Result<Unit> {
        return when (val result = remoteDataSource.publishProduct(productId)) {
            is NetworkResult.Success -> Result.success(Unit)
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun addProductImage(
        productId: Int,
        imageBase64: String,
        esPrincipal: Boolean,
        descripcion: String?
    ): Result<ProductImage> {
        return when (val result = remoteDataSource.addProductImage(
            productId, imageBase64, esPrincipal, descripcion
        )) {
            is NetworkResult.Success -> Result.success(result.data.toDomain())
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun deleteProductImage(productId: Int, imageId: Int): Result<Unit> {
        return when (val result = remoteDataSource.deleteProductImage(productId, imageId)) {
            is NetworkResult.Success -> Result.success(Unit)
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override fun getCachedProducts(): Flow<List<Product>> = flow {
        val cachedProducts = database.productQueries.selectAllProducts().executeAsList()
        val products = cachedProducts.map { cached ->
            Product(
                id = cached.id.toInt(),
                nombre = cached.nombre,
                descripcion = cached.descripcion,
                precio = cached.precio,
                estadoVenta = EstadoVenta.fromString(cached.estado_venta),
                estadoProducto = EstadoProducto.fromString(cached.estado_producto),
                propietario = Owner(
                    id = cached.propietario_id.toInt(),
                    name = cached.propietario_nombre ?: ""
                ),
                categoria = Category(
                    id = cached.categoria_id?.toInt() ?: 0,
                    name = cached.categoria_nombre ?: ""
                ),
                imagenes = cached.imagen_principal?.let {
                    listOf(ProductImage(0, it, true))
                } ?: emptyList()
            )
        }
        emit(products)
    }.flowOn(Dispatchers.IO)

    override suspend fun isFavorite(productId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            database.favoriteQueries.isFavorite(productId.toLong()).executeAsOne() > 0
        }
    }

    override suspend fun addToFavorites(productId: Int): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                database.favoriteQueries.insertFavorite(
                    productId.toLong(),
                    System.currentTimeMillis()
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFromFavorites(productId: Int): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                database.favoriteQueries.deleteFavorite(productId.toLong())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getFavorites(): Flow<List<Product>> = flow {
        val favorites = database.favoriteQueries.selectAllFavorites().executeAsList()
        val products = favorites.map { cached ->
            Product(
                id = cached.id.toInt(),
                nombre = cached.nombre,
                descripcion = cached.descripcion,
                precio = cached.precio,
                estadoVenta = EstadoVenta.fromString(cached.estado_venta),
                estadoProducto = EstadoProducto.fromString(cached.estado_producto),
                propietario = Owner(
                    id = cached.propietario_id.toInt(),
                    name = cached.propietario_nombre ?: ""
                ),
                categoria = Category(
                    id = cached.categoria_id?.toInt() ?: 0,
                    name = cached.categoria_nombre ?: ""
                ),
                imagenes = cached.imagen_principal?.let {
                    listOf(ProductImage(0, it, true))
                } ?: emptyList()
            )
        }
        emit(products)
    }.flowOn(Dispatchers.IO)

    private fun cacheProduct(product: Product) {
        database.productQueries.insertProduct(
            id = product.id.toLong(),
            nombre = product.nombre,
            descripcion = product.descripcion,
            precio = product.precio,
            categoria_id = product.categoria.id.toLong(),
            categoria_nombre = product.categoria.name,
            estado_producto = product.estadoProducto.value,
            estado_venta = product.estadoVenta.value,
            imagen_principal = product.imagenPrincipal,
            propietario_id = product.propietario.id.toLong(),
            propietario_nombre = product.propietario.name,
            fecha_publicacion = null,
            fecha_actualizacion = System.currentTimeMillis()
        )
    }
}
