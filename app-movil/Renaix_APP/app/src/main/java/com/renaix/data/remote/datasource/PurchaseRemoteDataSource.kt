package com.renaix.data.remote.datasource

import com.renaix.data.remote.api.RenaixApi
import com.renaix.data.remote.dto.request.CreatePurchaseRequest
import com.renaix.data.remote.dto.request.CreateRatingRequest
import com.renaix.data.remote.dto.response.*

/**
 * DataSource remoto para operaciones de compras
 */
class PurchaseRemoteDataSource(private val api: RenaixApi) {

    /**
     * Crea una compra
     */
    suspend fun createPurchase(
        productoId: Int,
        notas: String?
    ): NetworkResult<PurchaseResponse> {
        return try {
            val response = api.createPurchase(
                CreatePurchaseRequest(productoId, notas)
            )

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al crear compra",
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
     * Obtiene detalle de una compra
     */
    suspend fun getPurchaseDetail(purchaseId: Int): NetworkResult<PurchaseResponse> {
        return try {
            val response = api.getPurchaseDetail(purchaseId)

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Compra no encontrada",
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
     * Confirma una compra (vendedor)
     */
    suspend fun confirmPurchase(purchaseId: Int): NetworkResult<PurchaseStatusResponse> {
        return try {
            val response = api.confirmPurchase(purchaseId)

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al confirmar compra",
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
     * Completa una compra (comprador)
     */
    suspend fun completePurchase(purchaseId: Int): NetworkResult<PurchaseStatusResponse> {
        return try {
            val response = api.completePurchase(purchaseId)

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al completar compra",
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
     * Cancela una compra
     */
    suspend fun cancelPurchase(purchaseId: Int): NetworkResult<PurchaseStatusResponse> {
        return try {
            val response = api.cancelPurchase(purchaseId)

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al cancelar compra",
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
     * Valora una compra
     */
    suspend fun ratePurchase(
        purchaseId: Int,
        puntuacion: Int,
        comentario: String?
    ): NetworkResult<RatingResponse> {
        return try {
            val response = api.ratePurchase(
                purchaseId,
                CreateRatingRequest(puntuacion, comentario)
            )

            if (response.success && response.data != null) {
                NetworkResult.Success(response.data)
            } else {
                NetworkResult.Error(
                    message = response.error ?: "Error al valorar",
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
