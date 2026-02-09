package com.renaix.data.remote.api

import com.renaix.data.remote.dto.request.*
import com.renaix.data.remote.dto.response.*
import com.renaix.util.Constants.Endpoints
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Interfaz de la API de Renaix
 * Contiene todas las llamadas a los endpoints del backend
 */
class RenaixApi(
    private val publicClient: HttpClient,
    private val authClient: HttpClient
) {

    // ==================== AUTH ====================

    /**
     * Registra un nuevo usuario
     */
    suspend fun register(request: RegisterRequest): ApiResponse<AuthResponse> {
        return publicClient.post(Endpoints.AUTH_REGISTER) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Inicia sesión
     */
    suspend fun login(request: LoginRequest): ApiResponse<AuthResponse> {
        return publicClient.post(Endpoints.AUTH_LOGIN) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Renueva el access token
     */
    suspend fun refreshToken(request: RefreshTokenRequest): ApiResponse<RefreshTokenResponse> {
        return publicClient.post(Endpoints.AUTH_REFRESH) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Cierra la sesión
     */
    suspend fun logout(): ApiResponse<Unit> {
        return authClient.post(Endpoints.AUTH_LOGOUT).body()
    }

    // ==================== USUARIOS ====================

    /**
     * Obtiene el perfil del usuario autenticado
     */
    suspend fun getProfile(): ApiResponse<UserProfileResponse> {
        return authClient.get(Endpoints.USERS_PROFILE).body()
    }

    /**
     * Actualiza el perfil del usuario
     */
    suspend fun updateProfile(request: UpdateProfileRequest): ApiResponse<UserProfileResponse> {
        return authClient.put(Endpoints.USERS_PROFILE) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Sube imagen de perfil
     */
    suspend fun uploadProfileImage(request: UploadProfileImageRequest): ApiResponse<UserProfileResponse> {
        return authClient.post("${Endpoints.USERS_PROFILE}/imagen") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Elimina imagen de perfil
     */
    suspend fun deleteProfileImage(): ApiResponse<UserProfileResponse> {
        return authClient.delete("${Endpoints.USERS_PROFILE}/imagen").body()
    }

    /**
     * Cambia la contraseña
     */
    suspend fun changePassword(request: ChangePasswordRequest): ApiResponse<Unit> {
        return authClient.put("${Endpoints.USERS_PROFILE}/password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Obtiene el perfil público de un usuario
     */
    suspend fun getPublicProfile(userId: Int): ApiResponse<PublicUserResponse> {
        return publicClient.get("${Endpoints.USERS_PUBLIC}/$userId").body()
    }

    /**
     * Obtiene los productos del usuario autenticado
     */
    suspend fun getMyProducts(page: Int = 1, limit: Int = 20): ApiResponse<List<ProductListResponse>> {
        return authClient.get("${Endpoints.USERS_PROFILE}/productos") {
            parameter("page", page)
            parameter("limit", limit)
        }.body()
    }

    /**
     * Obtiene las compras del usuario
     */
    suspend fun getMyPurchases(page: Int = 1, limit: Int = 20): ApiResponse<List<PurchaseResponse>> {
        return authClient.get("${Endpoints.USERS_PROFILE}/compras") {
            parameter("page", page)
            parameter("limit", limit)
        }.body()
    }

    /**
     * Obtiene las ventas del usuario
     */
    suspend fun getMySales(page: Int = 1, limit: Int = 20): ApiResponse<List<PurchaseResponse>> {
        return authClient.get("${Endpoints.USERS_PROFILE}/ventas") {
            parameter("page", page)
            parameter("limit", limit)
        }.body()
    }

    /**
     * Obtiene las valoraciones del usuario
     */
    suspend fun getMyRatings(): ApiResponse<List<RatingResponse>> {
        return authClient.get("${Endpoints.USERS_PROFILE}/valoraciones").body()
    }

    /**
     * Obtiene las estadísticas del usuario
     */
    suspend fun getMyStats(): ApiResponse<UserStatsResponse> {
        return authClient.get("${Endpoints.USERS_PROFILE}/estadisticas").body()
    }

    // ==================== PRODUCTOS ====================

    /**
     * Lista productos públicos
     */
    suspend fun getProducts(
        page: Int = 1,
        limit: Int = 20,
        estadoVenta: String = "disponible"
    ): ApiResponse<List<ProductListResponse>> {
        return publicClient.get(Endpoints.PRODUCTS) {
            parameter("page", page)
            parameter("limit", limit)
            parameter("estado_venta", estadoVenta)
        }.body()
    }

    /**
     * Obtiene detalle de un producto
     */
    suspend fun getProductDetail(productId: Int): ApiResponse<ProductDetailResponse> {
        return publicClient.get("${Endpoints.PRODUCTS}/$productId").body()
    }

    /**
     * Búsqueda avanzada de productos
     */
    suspend fun searchProducts(params: ProductSearchParams): ApiResponse<List<ProductListResponse>> {
        return publicClient.get(Endpoints.PRODUCTS_SEARCH) {
            params.query?.let { parameter("query", it) }
            params.categoriaId?.let { parameter("categoria_id", it) }
            params.etiquetas?.let { parameter("etiquetas", it.joinToString(",")) }
            params.precioMin?.let { parameter("precio_min", it) }
            params.precioMax?.let { parameter("precio_max", it) }
            params.estadoProducto?.let { parameter("estado_producto", it) }
            params.ubicacion?.let { parameter("ubicacion", it) }
            params.orden?.let { parameter("orden", it) }
            parameter("page", params.page)
            parameter("limit", params.limit)
        }.body()
    }

    /**
     * Crea un producto
     */
    suspend fun createProduct(request: CreateProductRequest): ApiResponse<ProductCreateResponse> {
        return authClient.post(Endpoints.PRODUCTS) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Actualiza un producto
     */
    suspend fun updateProduct(
        productId: Int,
        request: UpdateProductRequest
    ): ApiResponse<ProductDetailResponse> {
        return authClient.put("${Endpoints.PRODUCTS}/$productId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Elimina un producto
     */
    suspend fun deleteProduct(productId: Int): ApiResponse<Unit> {
        return authClient.delete("${Endpoints.PRODUCTS}/$productId").body()
    }

    /**
     * Publica un producto (borrador -> disponible)
     */
    suspend fun publishProduct(productId: Int): ApiResponse<ProductPublishResponse> {
        return authClient.post("${Endpoints.PRODUCTS}/$productId/publicar").body()
    }

    /**
     * Añade imagen a un producto
     */
    suspend fun addProductImage(
        productId: Int,
        request: AddProductImageRequest
    ): ApiResponse<ProductImageResponse> {
        return authClient.post("${Endpoints.PRODUCTS}/$productId/imagenes") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Elimina imagen de un producto
     */
    suspend fun deleteProductImage(productId: Int, imageId: Int): ApiResponse<Unit> {
        return authClient.delete("${Endpoints.PRODUCTS}/$productId/imagenes/$imageId").body()
    }

    // ==================== COMPRAS ====================

    /**
     * Crea una compra
     */
    suspend fun createPurchase(request: CreatePurchaseRequest): ApiResponse<PurchaseResponse> {
        return authClient.post(Endpoints.PURCHASES) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Obtiene detalle de una compra
     */
    suspend fun getPurchaseDetail(purchaseId: Int): ApiResponse<PurchaseResponse> {
        return authClient.get("${Endpoints.PURCHASES}/$purchaseId").body()
    }

    /**
     * Confirma una compra (vendedor)
     */
    suspend fun confirmPurchase(purchaseId: Int): ApiResponse<PurchaseStatusResponse> {
        return authClient.post("${Endpoints.PURCHASES}/$purchaseId/confirmar").body()
    }

    /**
     * Completa una compra (comprador)
     */
    suspend fun completePurchase(purchaseId: Int): ApiResponse<PurchaseStatusResponse> {
        return authClient.post("${Endpoints.PURCHASES}/$purchaseId/completar").body()
    }

    /**
     * Cancela una compra
     */
    suspend fun cancelPurchase(purchaseId: Int): ApiResponse<PurchaseStatusResponse> {
        return authClient.post("${Endpoints.PURCHASES}/$purchaseId/cancelar").body()
    }

    /**
     * Valora una compra
     */
    suspend fun ratePurchase(
        purchaseId: Int,
        request: CreateRatingRequest
    ): ApiResponse<RatingResponse> {
        return authClient.post("${Endpoints.PURCHASES}/$purchaseId/valorar") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // ==================== COMENTARIOS ====================

    /**
     * Lista comentarios de un producto
     */
    suspend fun getProductComments(productId: Int): ApiResponse<List<CommentResponse>> {
        return publicClient.get("${Endpoints.PRODUCTS}/$productId/comentarios").body()
    }

    /**
     * Crea un comentario en un producto
     */
    suspend fun createComment(
        productId: Int,
        request: CreateCommentRequest
    ): ApiResponse<CommentResponse> {
        return authClient.post("${Endpoints.PRODUCTS}/$productId/comentarios") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Elimina un comentario
     */
    suspend fun deleteComment(commentId: Int): ApiResponse<Unit> {
        return authClient.delete("${Endpoints.COMMENTS}/$commentId").body()
    }

    // ==================== VALORACIONES ====================

    /**
     * Obtiene valoraciones de un usuario
     */
    suspend fun getUserRatings(userId: Int): ApiResponse<List<RatingResponse>> {
        return authClient.get("${Endpoints.USERS_PUBLIC}/$userId/valoraciones").body()
    }

    // ==================== MENSAJES ====================

    /**
     * Lista conversaciones
     */
    suspend fun getConversations(): ApiResponse<List<ConversationResponse>> {
        return authClient.get(Endpoints.CONVERSATIONS).body()
    }

    /**
     * Obtiene conversación con un usuario
     */
    suspend fun getConversation(
        userId: Int,
        productId: Int? = null
    ): ApiResponse<List<MessageResponse>> {
        return authClient.get("${Endpoints.MESSAGES}/conversacion/$userId") {
            productId?.let { parameter("producto_id", it) }
        }.body()
    }

    /**
     * Obtiene mensajes no leídos
     */
    suspend fun getUnreadMessages(): ApiResponse<UnreadMessagesResponse> {
        return authClient.get("${Endpoints.MESSAGES}/no-leidos").body()
    }

    /**
     * Envía un mensaje
     */
    suspend fun sendMessage(request: SendMessageRequest): ApiResponse<MessageResponse> {
        return authClient.post(Endpoints.MESSAGES) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Marca un mensaje como leído
     */
    suspend fun markMessageAsRead(messageId: Int): ApiResponse<Unit> {
        return authClient.put("${Endpoints.MESSAGES}/$messageId/marcar-leido").body()
    }

    // ==================== DENUNCIAS ====================

    /**
     * Crea una denuncia
     */
    suspend fun createReport(request: CreateReportRequest): ApiResponse<ReportResponse> {
        return authClient.post(Endpoints.REPORTS) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Obtiene mis denuncias
     */
    suspend fun getMyReports(): ApiResponse<List<ReportResponse>> {
        return authClient.get("${Endpoints.REPORTS}/mis-denuncias").body()
    }

    // ==================== CATEGORÍAS ====================

    /**
     * Lista categorías
     */
    suspend fun getCategories(): ApiResponse<List<CategoryResponse>> {
        return publicClient.get(Endpoints.CATEGORIES).body()
    }

    // ==================== ETIQUETAS ====================

    /**
     * Crea una etiqueta
     */
    suspend fun createTag(request: CreateTagRequest): ApiResponse<TagResponse> {
        return authClient.post(Endpoints.TAGS) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Lista etiquetas populares
     */
    suspend fun getPopularTags(): ApiResponse<List<TagResponse>> {
        return publicClient.get(Endpoints.TAGS).body()
    }

    /**
     * Busca etiquetas
     */
    suspend fun searchTags(query: String): ApiResponse<List<TagResponse>> {
        return publicClient.get(Endpoints.TAGS_SEARCH) {
            parameter("q", query)
        }.body()
    }
}
