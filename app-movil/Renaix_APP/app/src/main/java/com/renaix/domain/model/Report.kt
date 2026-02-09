package com.renaix.domain.model

/**
 * Modelo de dominio para Denuncia
 */
data class Report(
    val id: Int,
    val tipo: TipoDenuncia,
    val motivo: String,
    val categoria: CategoriaDenuncia,
    val estado: EstadoDenuncia,
    val fechaDenuncia: String
)

/**
 * Tipo de denuncia
 */
enum class TipoDenuncia(val value: String, val displayName: String) {
    PRODUCTO("producto", "Producto"),
    COMENTARIO("comentario", "Comentario"),
    USUARIO("usuario", "Usuario");

    companion object {
        fun fromString(value: String): TipoDenuncia {
            return entries.find { it.value == value } ?: PRODUCTO
        }
    }
}

/**
 * Categoría de denuncia
 */
enum class CategoriaDenuncia(val value: String, val displayName: String) {
    CONTENIDO_INAPROPIADO("contenido_inapropiado", "Contenido inapropiado"),
    SPAM("spam", "Spam"),
    FRAUDE("fraude", "Fraude"),
    VIOLENCIA("violencia", "Violencia"),
    INFORMACION_FALSA("informacion_falsa", "Información falsa"),
    OTRO("otro", "Otro");

    companion object {
        fun fromString(value: String): CategoriaDenuncia {
            return entries.find { it.value == value } ?: OTRO
        }
    }
}

/**
 * Estado de la denuncia
 */
enum class EstadoDenuncia(val value: String, val displayName: String) {
    PENDIENTE("pendiente", "Pendiente"),
    EN_REVISION("en_revision", "En revisión"),
    RESUELTA("resuelta", "Resuelta"),
    RECHAZADA("rechazada", "Rechazada");

    companion object {
        fun fromString(value: String): EstadoDenuncia {
            return entries.find { it.value == value } ?: PENDIENTE
        }
    }
}
