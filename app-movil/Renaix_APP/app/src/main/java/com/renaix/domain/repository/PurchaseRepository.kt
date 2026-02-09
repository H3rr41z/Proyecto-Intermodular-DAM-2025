package com.renaix.domain.repository

import com.renaix.domain.model.Purchase
import com.renaix.domain.model.Rating

/**
 * Repositorio de compras
 */
interface PurchaseRepository {
    /**
     * Crea una compra
     */
    suspend fun createPurchase(productoId: Int, notas: String? = null): Result<Purchase>

    /**
     * Obtiene detalle de una compra
     */
    suspend fun getPurchaseDetail(purchaseId: Int): Result<Purchase>

    /**
     * Confirma una compra (vendedor)
     */
    suspend fun confirmPurchase(purchaseId: Int): Result<Unit>

    /**
     * Completa una compra (comprador)
     */
    suspend fun completePurchase(purchaseId: Int): Result<Unit>

    /**
     * Cancela una compra
     */
    suspend fun cancelPurchase(purchaseId: Int): Result<Unit>

    /**
     * Valora una compra
     */
    suspend fun ratePurchase(
        purchaseId: Int,
        puntuacion: Int,
        comentario: String? = null
    ): Result<Rating>
}
