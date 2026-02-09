package com.renaix.domain.repository

import com.renaix.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de productos
 */
interface ProductRepository {
    /**
     * Obtiene lista de productos
     */
    suspend fun getProducts(
        page: Int = 1,
        limit: Int = 20
    ): Result<List<Product>>

    /**
     * Obtiene detalle de un producto
     */
    suspend fun getProductDetail(productId: Int): Result<ProductDetail>

    /**
     * Busca productos
     */
    suspend fun searchProducts(
        query: String? = null,
        categoriaId: Int? = null,
        precioMin: Double? = null,
        precioMax: Double? = null,
        estadoProducto: String? = null,
        ubicacion: String? = null,
        orden: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): Result<List<Product>>

    /**
     * Crea un producto
     */
    suspend fun createProduct(
        nombre: String,
        descripcion: String,
        precio: Double,
        categoriaId: Int,
        estadoProducto: String,
        antiguedad: String? = null,
        ubicacion: String? = null,
        etiquetaIds: List<Int>? = null,
        etiquetaNombres: List<String>? = null
    ): Result<Int> // Retorna el ID del producto creado

    /**
     * Actualiza un producto
     */
    suspend fun updateProduct(
        productId: Int,
        nombre: String? = null,
        descripcion: String? = null,
        precio: Double? = null,
        categoriaId: Int? = null,
        estadoProducto: String? = null
    ): Result<ProductDetail>

    /**
     * Elimina un producto
     */
    suspend fun deleteProduct(productId: Int): Result<Unit>

    /**
     * Publica un producto
     */
    suspend fun publishProduct(productId: Int): Result<Unit>

    /**
     * Añade una imagen al producto
     */
    suspend fun addProductImage(
        productId: Int,
        imageBase64: String,
        esPrincipal: Boolean = false,
        descripcion: String? = null
    ): Result<ProductImage>

    /**
     * Elimina una imagen del producto
     */
    suspend fun deleteProductImage(productId: Int, imageId: Int): Result<Unit>

    /**
     * Obtiene productos del caché local
     */
    fun getCachedProducts(): Flow<List<Product>>

    /**
     * Verifica si un producto es favorito
     */
    suspend fun isFavorite(productId: Int): Boolean

    /**
     * Añade un producto a favoritos
     */
    suspend fun addToFavorites(productId: Int): Result<Unit>

    /**
     * Elimina un producto de favoritos
     */
    suspend fun removeFromFavorites(productId: Int): Result<Unit>

    /**
     * Obtiene productos favoritos
     */
    fun getFavorites(): Flow<List<Product>>
}
