package com.renaix.data.remote.api

import android.util.Log
import com.renaix.data.local.preferences.PreferencesManager
import com.renaix.data.remote.dto.request.RefreshTokenRequest
import com.renaix.data.remote.dto.response.ApiResponse
import com.renaix.data.remote.dto.response.RefreshTokenResponse
import com.renaix.util.Constants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

/**
 * Cliente HTTP Ktor configurado para la API de Renaix
 * Incluye:
 * - Serialización JSON
 * - Logging
 * - Timeout
 * - Interceptor de autenticación Bearer Token
 * - Retry automático
 */
object KtorClient {

    private const val TAG = "KtorClient"

    /**
     * Configuración de JSON para serialización
     */
    private val json = Json {
        prettyPrint = false  // Optimizado: sin formateo extra
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        explicitNulls = false  // No enviar campos null (evita borrado accidental en el backend)
    }

    /**
     * Crea el cliente HTTP base sin autenticación
     * Usado para login y registro
     */
    fun createPublicClient(): HttpClient {
        return HttpClient(Android) {
            // Serialización JSON
            install(ContentNegotiation) {
                json(json)
            }

            // Logging para debug
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d(TAG, message)
                    }
                }
                level = LogLevel.NONE  // Optimizado: desactivar logging en producción
            }

            // Timeouts
            install(HttpTimeout) {
                requestTimeoutMillis = Constants.NETWORK_TIMEOUT_SECONDS * 1000
                connectTimeoutMillis = Constants.NETWORK_TIMEOUT_SECONDS * 1000
                socketTimeoutMillis = Constants.NETWORK_TIMEOUT_SECONDS * 1000
            }

            // Request por defecto
            defaultRequest {
                url(Constants.API_BASE_URL)
                contentType(ContentType.Application.Json)
            }
        }
    }

    /**
     * Crea el cliente HTTP con autenticación Bearer Token
     * Incluye interceptor para añadir token automáticamente
     * y refresh token cuando expira
     */
    fun createAuthenticatedClient(preferencesManager: PreferencesManager): HttpClient {
        return HttpClient(Android) {
            // Serialización JSON
            install(ContentNegotiation) {
                json(json)
            }

            // Logging para debug
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d(TAG, message)
                    }
                }
                level = LogLevel.NONE  // Optimizado: desactivar logging en producción
            }

            // Timeouts
            install(HttpTimeout) {
                requestTimeoutMillis = Constants.NETWORK_TIMEOUT_SECONDS * 1000
                connectTimeoutMillis = Constants.NETWORK_TIMEOUT_SECONDS * 1000
                socketTimeoutMillis = Constants.NETWORK_TIMEOUT_SECONDS * 1000
            }

            // Request por defecto con token
            defaultRequest {
                url(Constants.API_BASE_URL)
                contentType(ContentType.Application.Json)

                // Añadir Bearer Token si existe
                preferencesManager.getAccessToken()?.let { token ->
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            // Retry con refresh token
            install(HttpRequestRetry) {
                maxRetries = 1
                retryIf { _, response ->
                    // Si recibimos 401, intentamos refresh token
                    response.status.value == 401
                }
                modifyRequest { request ->
                    // Intentar refresh token
                    val refreshToken = preferencesManager.getRefreshToken()
                    if (refreshToken != null) {
                        try {
                            val newToken = runBlocking { refreshAccessToken(refreshToken) }
                            if (newToken != null) {
                                preferencesManager.saveAccessToken(newToken)
                                request.headers.remove(HttpHeaders.Authorization)
                                request.header(HttpHeaders.Authorization, "Bearer $newToken")
                            } else {
                                // Refresh falló, limpiar sesión
                                preferencesManager.clearSession()
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error refreshing token", e)
                            preferencesManager.clearSession()
                        }
                    }
                }
            }
        }
    }

    /**
     * Intenta renovar el access token usando el refresh token
     */
    private suspend fun refreshAccessToken(refreshToken: String): String? {
        return try {
            val client = createPublicClient()
            val response = client.post(Constants.Endpoints.AUTH_REFRESH) {
                setBody(RefreshTokenRequest(refreshToken))
            }
            val apiResponse: ApiResponse<RefreshTokenResponse> = response.body()
            client.close()

            if (apiResponse.success) {
                apiResponse.data?.accessToken
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh token", e)
            null
        }
    }
}
