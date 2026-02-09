package com.renaix.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request para crear un producto
 */
@Serializable
data class CreateProductRequest(
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    @SerialName("categoria_id")
    val categoriaId: Int,
    @SerialName("estado_producto")
    val estadoProducto: String,  // nuevo, como_nuevo, buen_estado, aceptable, para_reparar
    val antiguedad: String? = null,  // menos_1_mes, 1_6_meses, 6_12_meses, mas_1_ano
    val ubicacion: String? = null,
    @SerialName("etiqueta_ids")
    val etiquetaIds: List<Int>? = null,
    @SerialName("etiqueta_nombres")
    val etiquetaNombres: List<String>? = null
)

/**
 * Request para actualizar un producto
 */
@Serializable
data class UpdateProductRequest(
    val nombre: String? = null,
    val descripcion: String? = null,
    val precio: Double? = null,
    @SerialName("categoria_id")
    val categoriaId: Int? = null,
    @SerialName("estado_producto")
    val estadoProducto: String? = null,
    val antiguedad: String? = null,
    val ubicacion: String? = null,
    @SerialName("etiqueta_ids")
    val etiquetaIds: List<Int>? = null,
    @SerialName("etiqueta_nombres")
    val etiquetaNombres: List<String>? = null
)

/**
 * Request para añadir imagen a un producto
 */
@Serializable
data class AddProductImageRequest(
    val image: String,  // Base64
    @SerialName("es_principal")
    val esPrincipal: Boolean = false,
    val descripcion: String? = null
)

/**
 * Parámetros para búsqueda avanzada de productos
 */
data class ProductSearchParams(
    val query: String? = null,
    val categoriaId: Int? = null,
    val etiquetas: List<Int>? = null,
    val precioMin: Double? = null,
    val precioMax: Double? = null,
    val estadoProducto: String? = null,
    val ubicacion: String? = null,
    val orden: String? = null,  // precio_asc, precio_desc, fecha_desc, fecha_asc
    val page: Int = 1,
    val limit: Int = 20
)
