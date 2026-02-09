package com.renaix.data.remote.datasource

/**
 * Wrapper para resultados de operaciones de red
 * Encapsula éxito, error y paginación
 */
sealed class NetworkResult<out T> {
    data class Success<T>(
        val data: T,
        val pagination: PaginationInfo? = null
    ) : NetworkResult<T>()

    data class Error(
        val message: String,
        val code: String? = null,
        val exception: Throwable? = null
    ) : NetworkResult<Nothing>()

    /**
     * Transforma el dato en caso de éxito
     */
    fun <R> map(transform: (T) -> R): NetworkResult<R> {
        return when (this) {
            is Success -> Success(transform(data), pagination)
            is Error -> this
        }
    }

    /**
     * Ejecuta una acción en caso de éxito
     */
    fun onSuccess(action: (T) -> Unit): NetworkResult<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Ejecuta una acción en caso de error
     */
    fun onError(action: (String, String?) -> Unit): NetworkResult<T> {
        if (this is Error) action(message, code)
        return this
    }

    /**
     * Obtiene el dato o null
     */
    fun getOrNull(): T? = (this as? Success)?.data

    /**
     * Obtiene el dato o lanza excepción
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception ?: RuntimeException(message)
    }

    /**
     * Verifica si es éxito
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Verifica si es error
     */
    val isError: Boolean get() = this is Error
}

/**
 * Información de paginación
 */
data class PaginationInfo(
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrev: Boolean
)
