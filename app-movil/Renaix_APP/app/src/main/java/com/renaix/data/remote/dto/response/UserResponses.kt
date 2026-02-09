package com.renaix.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Respuesta completa del perfil de usuario
 */
@Serializable
data class UserProfileResponse(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("productos_en_venta")
    val productosEnVenta: Int = 0,
    @SerialName("productos_vendidos")
    val productosVendidos: Int = 0,
    @SerialName("valoracion_promedio")
    val valoracionPromedio: Double = 0.0
)

/**
 * Respuesta del perfil público de un usuario
 */
@Serializable
data class PublicUserResponse(
    val id: Int,
    val name: String,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("valoracion_promedio")
    val valoracionPromedio: Double = 0.0
)

/**
 * Estadísticas del usuario
 */
@Serializable
data class UserStatsResponse(
    @SerialName("productos_en_venta")
    val productosEnVenta: Int = 0,
    @SerialName("productos_vendidos")
    val productosVendidos: Int = 0,
    @SerialName("productos_comprados")
    val productosComprados: Int = 0,
    @SerialName("valoracion_promedio")
    val valoracionPromedio: Double = 0.0,
    @SerialName("total_comentarios")
    val totalComentarios: Int = 0,
    @SerialName("total_denuncias_realizadas")
    val totalDenunciasRealizadas: Int = 0
)

/**
 * Propietario de un producto (versión simplificada)
 */
@Serializable
data class OwnerResponse(
    val id: Int,
    val name: String,
    @SerialName("valoracion_promedio")
    val valoracionPromedio: Double? = null
)
