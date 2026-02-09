package com.renaix.domain.usecase.product

import com.renaix.domain.model.Product
import com.renaix.domain.repository.ProductRepository

/**
 * Caso de uso para búsqueda de productos
 */
class SearchProductsUseCase(private val productRepository: ProductRepository) {

    suspend operator fun invoke(
        query: String? = null,
        categoriaId: Int? = null,
        precioMin: Double? = null,
        precioMax: Double? = null,
        estadoProducto: String? = null,
        ubicacion: String? = null,
        orden: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): Result<List<Product>> {
        // Validar que haya al menos un criterio de búsqueda
        if (query.isNullOrBlank() && categoriaId == null && precioMin == null &&
            precioMax == null && estadoProducto == null && ubicacion == null) {
            // Sin filtros, obtener todos
            return productRepository.getProducts(page, limit)
        }

        return productRepository.searchProducts(
            query = query?.trim(),
            categoriaId = categoriaId,
            precioMin = precioMin,
            precioMax = precioMax,
            estadoProducto = estadoProducto,
            ubicacion = ubicacion?.trim(),
            orden = orden,
            page = page,
            limit = limit
        )
    }
}
