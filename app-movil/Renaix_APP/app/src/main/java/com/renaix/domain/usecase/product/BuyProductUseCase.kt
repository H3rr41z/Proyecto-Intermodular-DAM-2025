package com.renaix.domain.usecase.product

import com.renaix.domain.model.Purchase
import com.renaix.domain.repository.PurchaseRepository

/**
 * Caso de uso para comprar un producto
 */
class BuyProductUseCase(private val purchaseRepository: PurchaseRepository) {

    suspend operator fun invoke(
        productoId: Int,
        notas: String? = null
    ): Result<Purchase> {
        if (productoId <= 0) {
            return Result.failure(Exception("ID de producto inválido"))
        }

        return purchaseRepository.createPurchase(
            productoId = productoId,
            notas = notas?.trim()
        )
    }
}
