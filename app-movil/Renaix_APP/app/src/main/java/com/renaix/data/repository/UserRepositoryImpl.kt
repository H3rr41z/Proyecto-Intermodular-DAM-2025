package com.renaix.data.repository

import com.renaix.data.mapper.toDomain
import com.renaix.data.remote.datasource.NetworkResult
import com.renaix.data.remote.datasource.UserRemoteDataSource
import com.renaix.domain.model.*
import com.renaix.domain.repository.UserRepository

/**
 * Implementación del repositorio de usuario
 */
class UserRepositoryImpl(
    private val remoteDataSource: UserRemoteDataSource
) : UserRepository {

    override suspend fun getProfile(): Result<User> {
        return when (val result = remoteDataSource.getProfile()) {
            is NetworkResult.Success -> Result.success(result.data.toDomain())
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun updateProfile(
        name: String?,
        phone: String?,
        image: String?
    ): Result<User> {
        return when (val result = remoteDataSource.updateProfile(name, phone, image)) {
            is NetworkResult.Success -> Result.success(result.data.toDomain())
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun uploadProfileImage(imageBase64: String): Result<User> {
        return when (val result = remoteDataSource.uploadProfileImage(imageBase64)) {
            is NetworkResult.Success -> Result.success(result.data.toDomain())
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun deleteProfileImage(): Result<User> {
        return when (val result = remoteDataSource.deleteProfileImage()) {
            is NetworkResult.Success -> Result.success(result.data.toDomain())
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> {
        return when (val result = remoteDataSource.changePassword(currentPassword, newPassword)) {
            is NetworkResult.Success -> Result.success(Unit)
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun getPublicProfile(userId: Int): Result<PublicUser> {
        return when (val result = remoteDataSource.getPublicProfile(userId)) {
            is NetworkResult.Success -> Result.success(result.data.toDomain())
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun getStats(): Result<UserStats> {
        return when (val result = remoteDataSource.getMyStats()) {
            is NetworkResult.Success -> Result.success(result.data.toDomain())
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun getMyProducts(page: Int, limit: Int): Result<List<Product>> {
        return when (val result = remoteDataSource.getMyProducts(page, limit)) {
            is NetworkResult.Success -> Result.success(result.data.map { it.toDomain() })
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun getMyPurchases(page: Int, limit: Int): Result<List<Purchase>> {
        return when (val result = remoteDataSource.getMyPurchases(page, limit)) {
            is NetworkResult.Success -> Result.success(result.data.map { it.toDomain() })
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }

    override suspend fun getMySales(page: Int, limit: Int): Result<List<Purchase>> {
        return when (val result = remoteDataSource.getMySales(page, limit)) {
            is NetworkResult.Success -> Result.success(result.data.map { it.toDomain() })
            is NetworkResult.Error -> Result.failure(Exception(result.message))
        }
    }
}
