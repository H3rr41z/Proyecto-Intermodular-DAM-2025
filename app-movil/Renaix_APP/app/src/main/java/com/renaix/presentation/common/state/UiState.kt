package com.renaix.presentation.common.state

/**
 * Estado genérico de UI para manejar Loading, Success y Error
 */
sealed class UiState<out T> {
    /**
     * Estado inicial/inactivo
     */
    object Idle : UiState<Nothing>()

    /**
     * Estado de carga
     */
    object Loading : UiState<Nothing>()

    /**
     * Estado de éxito con datos
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * Estado de error con mensaje
     */
    data class Error(val message: String) : UiState<Nothing>()

    /**
     * Verifica si está cargando
     */
    val isLoading: Boolean get() = this is Loading

    /**
     * Verifica si es éxito
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Verifica si es error
     */
    val isError: Boolean get() = this is Error

    /**
     * Obtiene los datos si es Success, o null
     */
    fun getOrNull(): T? = (this as? Success)?.data

    /**
     * Obtiene el mensaje de error si es Error, o null
     */
    fun errorOrNull(): String? = (this as? Error)?.message
}

/**
 * Estado para listas paginadas
 */
data class PaginatedState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = true,
    val endReached: Boolean = false
) {
    val isEmpty: Boolean get() = items.isEmpty() && !isLoading && error == null
    val showEmptyState: Boolean get() = isEmpty && !isLoading
}
