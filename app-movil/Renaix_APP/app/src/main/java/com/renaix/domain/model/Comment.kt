package com.renaix.domain.model

/**
 * Modelo de dominio para Comentario
 */
data class Comment(
    val id: Int,
    val texto: String,
    val usuario: Owner,
    val fecha: String
)
