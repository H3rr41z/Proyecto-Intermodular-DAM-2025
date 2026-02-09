package com.renaix.data.remote.datasource

import com.renaix.data.remote.api.RenaixApi
import com.renaix.data.remote.dto.request.*
import com.renaix.data.remote.dto.response.*

/**
 * DataSource remoto para operaciones de productos
 */
class ProductRemoteDataSource(private val api: RenaixApi) {

    /**
     * Obtiene lista de productos
     */
    suspend fun getProducts(
        page: Int,
        limit: Int,
        estadoVenta: String = "disponible"
    ): NetworkResult<List<ProductListResponse>> {
        return try {
            val response = api.getProducts(page, limit, estadoVenta)

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
     * Obtiene detalle de un producto
     */
    suspend fun getProductDetail(productId: Int): NetworkResult<ProductDetailResponse> {
        return try {
            val response = api.getProductDetail(productId)

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Producto no encontrado",
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
     * Búsqueda avanzada de productos
     */
    suspend fun searchProducts(params: ProductSearchParams): NetworkResult<List<ProductListResponse>> {
        return try {
            val response = api.searchProducts(params)

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
                    message = response.error ?: "Error en búsqueda",
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
     * Crea un producto
     */
    suspend fun createProduct(
        nombre: String,
        descripcion: String,
        precio: Double,
        categoriaId: Int,
        estadoProducto: String,
        antiguedad: String?,
        ubicacion: String?,
        etiquetaIds: List<Int>?,
        etiquetaNombres: List<String>?
    ): NetworkResult<ProductCreateResponse> {
        return try {
            val response = api.createProduct(
                CreateProductRequest(
                    nombre = nombre,
                    descripcion = descripcion,
                    precio = precio,
                    categoriaId = categoriaId,
                    estadoProducto = estadoProducto,
                    antiguedad = antiguedad,
                    ubicacion = ubicacion,
                    etiquetaIds = etiquetaIds,
                    etiquetaNombres = etiquetaNombres
                )
            )

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al crear producto",
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
     * Actualiza un producto
     */
    suspend fun updateProduct(
        productId: Int,
        request: UpdateProductRequest
    ): NetworkResult<ProductDetailResponse> {
        return try {
            val response = api.updateProduct(productId, request)

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al actualizar producto",
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
     * Elimina un producto
     */
    suspend fun deleteProduct(productId: Int): NetworkResult<Unit> {
        return try {
            val response = api.deleteProduct(productId)

            if (response.success) {
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al eliminar producto",
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
     * Publica un producto
     */
    suspend fun publishProduct(productId: Int): NetworkResult<ProductPublishResponse> {
        return try {
            val response = api.publishProduct(productId)

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al publicar producto",
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
     * Añade imagen a un producto
     */
    suspend fun addProductImage(
        productId: Int,
        imageBase64: String,
        esPrincipal: Boolean,
        descripcion: String?
    ): NetworkResult<ProductImageResponse> {
        return try {
            val response = api.addProductImage(
                productId,
                AddProductImageRequest(imageBase64, esPrincipal, descripcion)
            )

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
     * Elimina imagen de un producto
     */
    suspend fun deleteProductImage(productId: Int, imageId: Int): NetworkResult<Unit> {
        return try {
            val response = api.deleteProductImage(productId, imageId)

            if (response.success) {
                NetworkResult.Success(Unit)
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
}
