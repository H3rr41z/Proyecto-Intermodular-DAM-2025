package com.renaix.domain.usecase.product

import com.renaix.domain.model.Product
import com.renaix.domain.repository.ProductRepository

/**
 * Caso de uso para obtener lista de productos
 */
class GetProductsUseCase(private val productRepository: ProductRepository) {

    suspend operator fun invoke(page: Int = 1, limit: Int = 20): Result<List<Product>> {
        return productRepository.getProducts(page, limit)
    }
}
