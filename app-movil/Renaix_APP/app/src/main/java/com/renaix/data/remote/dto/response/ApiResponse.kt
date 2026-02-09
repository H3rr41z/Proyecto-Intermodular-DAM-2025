package com.renaix.data.remote.dto.response

import kotlinx.serialization.Serializable

/**
 * Wrapper genérico para todas las respuestas de la API
 * Formato estándar de respuesta de la API de Renaix
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val error: String? = null,
    val code: String? = null,
    val pagination: PaginationResponse? = null
)

/**
 * Información de paginación
 */
@Serializable
data class PaginationResponse(
    val total: Int,
    val page: Int,
    val limit: Int,
    val total_pages: Int,
    val has_next: Boolean,
    val has_prev: Boolean
)
