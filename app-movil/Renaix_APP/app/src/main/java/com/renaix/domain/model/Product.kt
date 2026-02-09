package com.renaix.domain.model

/**
 * Modelo de dominio para Producto (versión lista)
 */
data class Product(
    val id: Int,
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,
    val estadoVenta: EstadoVenta,
    val estadoProducto: EstadoProducto,
    val ubicacion: String? = null,
    val propietario: Owner,
    val categoria: Category,
    val imagenes: List<ProductImage> = emptyList(),
    val fechaPublicacion: String? = null
) {
    /**
     * Obtiene la imagen principal o la primera
     */
    val imagenPrincipal: String?
        get() = imagenes.firstOrNull { it.esPrincipal }?.urlImagen
            ?: imagenes.firstOrNull()?.urlImagen
}

/**
 * Modelo de dominio para Producto con detalle completo
 */
data class ProductDetail(
    val id: Int,
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,
    val estadoVenta: EstadoVenta,
    val estadoProducto: EstadoProducto,
    val ubicacion: String? = null,
    val propietario: Owner,
    val categoria: Category,
    val etiquetas: List<Tag> = emptyList(),
    val imagenes: List<ProductImage> = emptyList(),
    val comentarios: List<Comment> = emptyList(),
    val fechaPublicacion: String? = null
) {
    val imagenPrincipal: String?
        get() = imagenes.firstOrNull { it.esPrincipal }?.urlImagen
            ?: imagenes.firstOrNull()?.urlImagen
}

/**
 * Imagen de producto
 */
data class ProductImage(
    val id: Int,
    val urlImagen: String,
    val esPrincipal: Boolean = false,
    val descripcion: String? = null
)

/**
 * Propietario de un producto
 */
data class Owner(
    val id: Int,
    val name: String,
    val valoracionPromedio: Double? = null
)

/**
 * Estado de venta del producto
 */
enum class EstadoVenta(val value: String, val displayName: String) {
    BORRADOR("borrador", "Borrador"),
    DISPONIBLE("disponible", "Disponible"),
    RESERVADO("reservado", "Reservado"),
    VENDIDO("vendido", "Vendido"),
    ELIMINADO("eliminado", "Eliminado");

    companion object {
        fun fromString(value: String): EstadoVenta {
            return entries.find { it.value == value } ?: DISPONIBLE
        }
    }
}

/**
 * Estado físico del producto
 */
enum class EstadoProducto(val value: String, val displayName: String) {
    NUEVO("nuevo", "Nuevo"),
    COMO_NUEVO("como_nuevo", "Como nuevo"),
    BUEN_ESTADO("buen_estado", "Buen estado"),
    ACEPTABLE("aceptable", "Aceptable"),
    PARA_REPARAR("para_reparar", "Para reparar");

    companion object {
        fun fromString(value: String): EstadoProducto {
            return entries.find { it.value == value } ?: BUEN_ESTADO
        }
    }
}
