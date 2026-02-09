package com.renaix.domain.model

/**
 * Modelo de dominio para Usuario
 */
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String? = null,
    val imageUrl: String? = null,
    val productosEnVenta: Int = 0,
    val productosVendidos: Int = 0,
    val valoracionPromedio: Double = 0.0
)

/**
 * Modelo de dominio para Usuario público (sin datos privados)
 */
data class PublicUser(
    val id: Int,
    val name: String,
    val imageUrl: String? = null,
    val valoracionPromedio: Double = 0.0
)

/**
 * Estadísticas del usuario
 */
data class UserStats(
    val productosEnVenta: Int = 0,
    val productosVendidos: Int = 0,
    val productosComprados: Int = 0,
    val valoracionPromedio: Double = 0.0,
    val totalComentarios: Int = 0,
    val totalDenunciasRealizadas: Int = 0
)

/**
 * Datos de autenticación
 */
data class AuthData(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)
