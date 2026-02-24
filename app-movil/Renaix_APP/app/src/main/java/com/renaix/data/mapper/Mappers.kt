package com.renaix.data.mapper

import com.renaix.data.remote.dto.response.*
import com.renaix.domain.model.*

// ==================== AUTH MAPPERS ====================

fun AuthResponse.toDomain(): AuthData {
    return AuthData(
        accessToken = accessToken,
        refreshToken = refreshToken,
        user = user.toDomain()
    )
}

fun UserBasicResponse.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        phone = phone
    )
}

// ==================== USER MAPPERS ====================

fun UserProfileResponse.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        phone = phone,
        imageUrl = imageUrl,
        productosEnVenta = productosEnVenta,
        productosVendidos = productosVendidos,
        valoracionPromedio = valoracionPromedio
    )
}

fun PublicUserResponse.toDomain(): PublicUser {
    return PublicUser(
        id = id,
        name = name,
        imageUrl = imageUrl,
        valoracionPromedio = valoracionPromedio
    )
}

fun UserStatsResponse.toDomain(): UserStats {
    return UserStats(
        productosEnVenta = productosEnVenta,
        productosVendidos = productosVendidos,
        productosComprados = productosComprados,
        valoracionPromedio = valoracionPromedio,
        totalComentarios = totalComentarios,
        totalDenunciasRealizadas = totalDenunciasRealizadas
    )
}

fun OwnerResponse.toDomain(): Owner {
    return Owner(
        id = id,
        name = name,
        valoracionPromedio = valoracionPromedio
    )
}

// ==================== PRODUCT MAPPERS ====================

fun ProductListResponse.toDomain(): Product {
    return Product(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        precio = precio,
        estadoVenta = EstadoVenta.fromString(estadoVenta),
        estadoProducto = EstadoProducto.fromString(estadoProducto),
        ubicacion = ubicacion,
        propietario = propietario.toDomain(),
        categoria = categoria.toDomain(),
        imagenes = imagenes.map { it.toDomain() }
    )
}

fun ProductDetailResponse.toDomain(): ProductDetail {
    return ProductDetail(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        precio = precio,
        estadoVenta = EstadoVenta.fromString(estadoVenta),
        estadoProducto = EstadoProducto.fromString(estadoProducto),
        ubicacion = ubicacion,
        propietario = propietario.toDomain(),
        categoria = categoria.toDomain(),
        etiquetas = etiquetas.map { it.toDomain() },
        imagenes = imagenes.map { it.toDomain() },
        comentarios = comentarios.map { it.toDomain() },
        fechaPublicacion = fechaPublicacion
    )
}

fun ProductImageResponse.toDomain(): ProductImage {
    return ProductImage(
        id = id,
        urlImagen = urlImagen,
        esPrincipal = esPrincipal,
        descripcion = descripcion
    )
}

// ==================== CATEGORY MAPPERS ====================

fun CategoryResponse.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        descripcion = descripcion,
        imagenUrl = imagenUrl,
        productoCount = productoCount
    )
}

fun CategorySimpleResponse.toDomain(): Category {
    return Category(
        id = id,
        name = name
    )
}

// ==================== TAG MAPPERS ====================

fun TagResponse.toDomain(): Tag {
    return Tag(
        id = id,
        name = name,
        productoCount = productoCount,
        color = color
    )
}

fun TagSimpleResponse.toDomain(): Tag {
    return Tag(
        id = id,
        name = name
    )
}

// ==================== PURCHASE MAPPERS ====================

fun PurchaseResponse.toDomain(): Purchase {
    return Purchase(
        id = id,
        producto = producto.toDomain(),
        comprador = comprador.toDomain(),
        vendedor = vendedor.toDomain(),
        precioFinal = precioFinal,
        estado = EstadoCompra.fromString(estado),
        fechaCompra = fechaCompra,
        notas = notas,
        compradorValoro = compradorValoro,
        vendedorValoro = vendedorValoro
    )
}

fun ProductSimpleResponse.toDomain(): ProductSimple {
    return ProductSimple(
        id = id,
        nombre = nombre
    )
}

// ==================== COMMENT MAPPERS ====================

fun CommentResponse.toDomain(): Comment {
    return Comment(
        id = id,
        texto = texto,
        usuario = usuario.toDomain(),
        fecha = fecha
    )
}

// ==================== RATING MAPPERS ====================

fun RatingResponse.toDomain(): Rating {
    return Rating(
        id = id,
        puntuacion = puntuacion,
        comentario = comentario,
        tipoValoracion = TipoValoracion.fromString(tipoValoracion),
        valorador = valorador?.toDomain(),
        fecha = fecha
    )
}

// ==================== MESSAGE MAPPERS ====================

fun MessageResponse.toDomain(): Message {
    return Message(
        id = id,
        texto = texto,
        emisor = emisor.toDomain(),
        receptor = receptor.toDomain(),
        leido = leido,
        fecha = fecha,
        hiloId = hiloId,
        messageType = MessageType.fromString(messageType),
        offerData = offerData?.toDomain()
    )
}

fun OfferDataResponse.toDomain(): OfferData {
    return OfferData(
        productId = productId ?: 0,
        productName = productName,
        originalPrice = originalPrice,
        offeredPrice = offeredPrice
    )
}

fun ConversationResponse.toDomain(): Conversation {
    return Conversation(
        hiloId = hiloId,
        participantes = participantes.map { it.toDomain() },
        ultimoMensaje = ultimoMensaje?.toDomain(),
        mensajes = mensajes.map { it.toDomain() }
    )
}

fun LastMessageResponse.toDomain(): LastMessage {
    return LastMessage(
        texto = texto,
        fecha = fecha
    )
}

fun UnreadMessagesResponse.toDomain(): UnreadMessages {
    return UnreadMessages(
        total = total,
        mensajes = mensajes.map { it.toDomain() }
    )
}

// ==================== REPORT MAPPERS ====================

fun ReportResponse.toDomain(): Report {
    return Report(
        id = id,
        tipo = TipoDenuncia.fromString(tipo),
        motivo = motivo,
        categoria = CategoriaDenuncia.fromString(categoria),
        estado = EstadoDenuncia.fromString(estado),
        fechaDenuncia = fechaDenuncia
    )
}
