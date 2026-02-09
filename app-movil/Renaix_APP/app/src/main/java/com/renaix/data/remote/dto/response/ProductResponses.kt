package com.renaix.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Respuesta de producto para listados
 */
@Serializable
data class ProductListResponse(
    val id: Int,
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,
    @SerialName("estado_venta")
    val estadoVenta: String,
    @SerialName("estado_producto")
    val estadoProducto: String,
    val ubicacion: String? = null,
    val propietario: OwnerResponse,
    val categoria: CategorySimpleResponse,
    val imagenes: List<ProductImageResponse> = emptyList()
)

/**
 * Respuesta de detalle de producto
 */
@Serializable
data class ProductDetailResponse(
    val id: Int,
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,
    @SerialName("estado_venta")
    val estadoVenta: String,
    @SerialName("estado_producto")
    val estadoProducto: String,
    val ubicacion: String? = null,
    val propietario: OwnerResponse,
    val categoria: CategorySimpleResponse,
    val etiquetas: List<TagSimpleResponse> = emptyList(),
    val imagenes: List<ProductImageResponse> = emptyList(),
    val comentarios: List<CommentResponse> = emptyList(),
    @SerialName("fecha_publicacion")
    val fechaPublicacion: String? = null
)

/**
 * Respuesta de imagen de producto
 */
@Serializable
data class ProductImageResponse(
    val id: Int,
    @SerialName("url_imagen")
    val urlImagen: String,
    @SerialName("es_principal")
    val esPrincipal: Boolean = false,
    val descripcion: String? = null
)

/**
 * Respuesta de producto creado/actualizado
 */
@Serializable
data class ProductCreateResponse(
    val id: Int,
    val nombre: String,
    val precio: Double,
    @SerialName("estado_venta")
    val estadoVenta: String,
    val imagenes: List<ProductImageResponse> = emptyList()
)

/**
 * Respuesta al publicar producto
 */
@Serializable
data class ProductPublishResponse(
    val id: Int,
    @SerialName("estado_venta")
    val estadoVenta: String,
    @SerialName("fecha_publicacion")
    val fechaPublicacion: String
)

/**
 * Categoría simplificada para respuestas de producto
 */
@Serializable
data class CategorySimpleResponse(
    val id: Int,
    val name: String
)

/**
 * Etiqueta simplificada para respuestas de producto
 */
@Serializable
data class TagSimpleResponse(
    val id: Int,
    val name: String
)
