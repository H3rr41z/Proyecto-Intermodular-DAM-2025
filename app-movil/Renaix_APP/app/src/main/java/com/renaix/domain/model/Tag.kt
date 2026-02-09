package com.renaix.domain.model

/**
 * Modelo de dominio para Etiqueta
 */
data class Tag(
    val id: Int,
    val name: String,
    val productoCount: Int = 0,
    val color: Int = 0
)
