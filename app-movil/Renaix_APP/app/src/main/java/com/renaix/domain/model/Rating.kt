package com.renaix.domain.model

/**
 * Modelo de dominio para Valoración
 */
data class Rating(
    val id: Int,
    val puntuacion: Int,
    val comentario: String? = null,
    val tipoValoracion: TipoValoracion,
    val valorador: Owner? = null,
    val fecha: String
)

/**
 * Tipo de valoración
 */
enum class TipoValoracion(val value: String, val displayName: String) {
    COMPRADOR_A_VENDEDOR("comprador_a_vendedor", "Comprador a Vendedor"),
    VENDEDOR_A_COMPRADOR("vendedor_a_comprador", "Vendedor a Comprador");

    companion object {
        fun fromString(value: String): TipoValoracion {
            return entries.find { it.value == value } ?: COMPRADOR_A_VENDEDOR
        }
    }
}
