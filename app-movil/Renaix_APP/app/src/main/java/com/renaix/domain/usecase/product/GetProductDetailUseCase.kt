package com.renaix.domain.usecase.product

import com.renaix.domain.model.ProductDetail
import com.renaix.domain.repository.ProductRepository

/**
 * Caso de uso para obtener detalle de un producto
 */
class GetProductDetailUseCase(private val productRepository: ProductRepository) {

    suspend operator fun invoke(productId: Int): Result<ProductDetail> {
        if (productId <= 0) {
            return Result.failure(Exception("ID de producto inválido"))
        }
        return productRepository.getProductDetail(productId)
    }
}
