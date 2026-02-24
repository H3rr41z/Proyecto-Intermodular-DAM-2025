package com.renaix.data.repository

import com.renaix.data.mapper.toDomain
import com.renaix.data.remote.datasource.NetworkResult
import com.renaix.data.remote.datasource.RatingRemoteDataSource
import com.renaix.data.remote.dto.request.CreateRatingRequest
import com.renaix.domain.model.Rating
import com.renaix.domain.repository.RatingRepository

class RatingRepositoryImpl(
    private val remoteDataSource: RatingRemoteDataSource
) : RatingRepository {

    override suspend fun getMyRatings(): Result<List<Rating>> {
        return when (val result = remoteDataSource.getMyRatings()) {
            is NetworkResult.Success -> Result.success(result.data.map { it.toDomain() })
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun getUserRatings(userId: Int): Result<List<Rating>> {
        return when (val result = remoteDataSource.getUserRatings(userId)) {
            is NetworkResult.Success -> Result.success(result.data.map { it.toDomain() })
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun ratePurchase(purchaseId: Int, puntuacion: Int, comentario: String?): Result<Unit> {
        val request = CreateRatingRequest(puntuacion = puntuacion, comentario = comentario)
        return when (val result = remoteDataSource.ratePurchase(purchaseId, request)) {
            is NetworkResult.Success -> Result.success(Unit)
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }
}
