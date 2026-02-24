package com.renaix.domain.repository

import com.renaix.domain.model.Rating

interface RatingRepository {
    suspend fun getMyRatings(): Result<List<Rating>>
    suspend fun getUserRatings(userId: Int): Result<List<Rating>>
    suspend fun ratePurchase(purchaseId: Int, puntuacion: Int, comentario: String?): Result<Unit>
}
