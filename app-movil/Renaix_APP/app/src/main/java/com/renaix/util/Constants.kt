package com.renaix.util

/**
 * Constantes globales de la aplicación Renaix
 */
object Constants {
    
    // ==================== API CONFIGURATION ====================
    
    /**
     * URL base del backend Odoo
     * Para emulador: 10.0.2.2 apunta a localhost del host
     * Para móvil físico: cambiar a la IP local (ej: 192.168.1.100)
     */
    private const val BASE_URL = "http://10.0.2.2:8069"
    
    /**
     * Versión de la API
     */
    private const val API_VERSION = "/api/v1"
    
    /**
     * URL completa de la API (con trailing slash para paths relativos)
     */
    const val API_BASE_URL = "$BASE_URL$API_VERSION/"
    
    // ==================== API ENDPOINTS ====================
    
    object Endpoints {
        // Auth
        const val AUTH_REGISTER = "auth/register"
        const val AUTH_LOGIN = "auth/login"
        const val AUTH_REFRESH = "auth/refresh"
        const val AUTH_LOGOUT = "auth/logout"

        // Users
        const val USERS_PROFILE = "usuarios/perfil"
        const val USERS_PUBLIC = "usuarios"

        // Products
        const val PRODUCTS = "productos"
        const val PRODUCTS_SEARCH = "productos/buscar"

        // Categories
        const val CATEGORIES = "categorias"

        // Tags
        const val TAGS = "etiquetas"
        const val TAGS_SEARCH = "etiquetas/buscar"

        // Purchases
        const val PURCHASES = "compras"

        // Comments
        const val COMMENTS = "comentarios"

        // Ratings
        const val RATINGS = "valoraciones"

        // Messages
        const val MESSAGES = "mensajes"
        const val CONVERSATIONS = "mensajes/conversaciones"

        // Reports
        const val REPORTS = "denuncias"
    }
    
    // ==================== SHARED PREFERENCES ====================
    
    const val ENCRYPTED_PREFS_NAME = "renaix_secure_prefs"
    
    object PrefsKeys {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val USER_ID = "user_id"
        const val USER_NAME = "user_name"
        const val USER_EMAIL = "user_email"
        const val IS_LOGGED_IN = "is_logged_in"
        const val DARK_MODE = "dark_mode"
    }
    
    // ==================== DATABASE ====================
    
    const val DATABASE_NAME = "renaix.db"
    const val DATABASE_VERSION = 1
    
    // ==================== PAGINATION ====================
    
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_PAGE_SIZE = 100
    
    // ==================== IMAGES ====================
    
    const val MAX_IMAGES_PER_PRODUCT = 10
    const val MAX_IMAGE_SIZE_MB = 5
    const val MAX_IMAGE_SIZE_BYTES = MAX_IMAGE_SIZE_MB * 1024 * 1024
    
    // ==================== VALIDATION ====================
    
    const val MIN_PASSWORD_LENGTH = 6
    const val MIN_NAME_LENGTH = 2
    const val MAX_NAME_LENGTH = 100
    const val MIN_DESCRIPTION_LENGTH = 10
    const val MAX_DESCRIPTION_LENGTH = 500
    const val MIN_COMMENT_LENGTH = 3
    const val MAX_COMMENT_LENGTH = 300
    
    // ==================== MAP ====================
    
    const val DEFAULT_ZOOM = 12f
    const val DEFAULT_LATITUDE = 39.4699 // Valencia
    const val DEFAULT_LONGITUDE = -0.3763
    
    // ==================== TIMEOUTS ====================
    
    const val NETWORK_TIMEOUT_SECONDS = 30L
    const val SPLASH_DELAY_MILLIS = 2000L
    
    // ==================== CACHE ====================
    
    const val CACHE_EXPIRATION_HOURS = 24
}
