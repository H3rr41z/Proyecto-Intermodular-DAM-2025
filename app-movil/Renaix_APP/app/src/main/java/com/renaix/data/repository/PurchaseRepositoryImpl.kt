package com.renaix.data.repository

import com.renaix.data.mapper.toDomain
import com.renaix.data.remote.datasource.NetworkResult
import com.renaix.data.remote.datasource.PurchaseRemoteDataSource
import com.renaix.domain.model.Purchase
import com.renaix.domain.model.Rating
import com.renaix.domain.repository.PurchaseRepository

/**
 * Implementación del repositorio de compras
 */
class PurchaseRepositoryImpl(
    private val remoteDataSource: PurchaseRemoteDataSource
) : PurchaseRepository {

    override suspend fun createPurchase(productoId: Int, notas: String?): Result<Purchase> {
        return when (val result = remoteDataSource.createPurchase(productoId, notas)) {
            is NetworkResult.Success -> Result.success(result.data.toDomain())
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun getPurchaseDetail(purchaseId: Int): Result<Purchase> {
        return when (val result = remoteDataSource.getPurchaseDetail(purchaseId)) {
            is NetworkResult.Success -> Result.success(result.data.toDomain())
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun confirmPurchase(purchaseId: Int): Result<Unit> {
        return when (val result = remoteDataSource.confirmPurchase(purchaseId)) {
            is NetworkResult.Success -> Result.success(Unit)
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun completePurchase(purchaseId: Int): Result<Unit> {
        return when (val result = remoteDataSource.completePurchase(purchaseId)) {
            is NetworkResult.Success -> Result.success(Unit)
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun cancelPurchase(purchaseId: Int): Result<Unit> {
        return when (val result = remoteDataSource.cancelPurchase(purchaseId)) {
            is NetworkResult.Success -> Result.success(Unit)
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun ratePurchase(
        purchaseId: Int,
        puntuacion: Int,
        comentario: String?
    ): Result<Rating> {
        return when (val result = remoteDataSource.ratePurchase(purchaseId, puntuacion, comentario)) {
            is NetworkResult.Success -> Result.success(result.data.toDomain())
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }
}
