package com.renaix.domain.model

/**
 * Modelo de dominio para Categoría
 */
data class Category(
    val id: Int,
    val name: String,
    val descripcion: String? = null,
    val imagenUrl: String? = null,
    val productoCount: Int = 0
)
