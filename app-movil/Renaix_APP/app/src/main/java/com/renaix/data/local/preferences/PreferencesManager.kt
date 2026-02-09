package com.renaix.data.local.preferences

import com.renaix.util.Constants.PrefsKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor de preferencias de la aplicación
 * Maneja tokens, sesión de usuario y configuración
 */
class PreferencesManager(
    private val securePreferences: SecurePreferences
) {
    private val _isLoggedIn = MutableStateFlow(getIsLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // ==================== ACCESS TOKEN ====================

    /**
     * Guarda el access token
     */
    fun saveAccessToken(token: String) {
        securePreferences.putString(PrefsKeys.ACCESS_TOKEN, token)
    }

    /**
     * Obtiene el access token
     */
    fun getAccessToken(): String? {
        return securePreferences.getString(PrefsKeys.ACCESS_TOKEN)
    }

    /**
     * Elimina el access token
     */
    fun clearAccessToken() {
        securePreferences.remove(PrefsKeys.ACCESS_TOKEN)
    }

    // ==================== REFRESH TOKEN ====================

    /**
     * Guarda el refresh token
     */
    fun saveRefreshToken(token: String) {
        securePreferences.putString(PrefsKeys.REFRESH_TOKEN, token)
    }

    /**
     * Obtiene el refresh token
     */
    fun getRefreshToken(): String? {
        return securePreferences.getString(PrefsKeys.REFRESH_TOKEN)
    }

    /**
     * Elimina el refresh token
     */
    fun clearRefreshToken() {
        securePreferences.remove(PrefsKeys.REFRESH_TOKEN)
    }

    // ==================== USER DATA ====================

    /**
     * Guarda el ID del usuario
     */
    fun saveUserId(userId: Int) {
        securePreferences.putInt(PrefsKeys.USER_ID, userId)
    }

    /**
     * Obtiene el ID del usuario
     */
    fun getUserId(): Int {
        return securePreferences.getInt(PrefsKeys.USER_ID, -1)
    }

    /**
     * Guarda el nombre del usuario
     */
    fun saveUserName(name: String) {
        securePreferences.putString(PrefsKeys.USER_NAME, name)
    }

    /**
     * Obtiene el nombre del usuario
     */
    fun getUserName(): String? {
        return securePreferences.getString(PrefsKeys.USER_NAME)
    }

    /**
     * Guarda el email del usuario
     */
    fun saveUserEmail(email: String) {
        securePreferences.putString(PrefsKeys.USER_EMAIL, email)
    }

    /**
     * Obtiene el email del usuario
     */
    fun getUserEmail(): String? {
        return securePreferences.getString(PrefsKeys.USER_EMAIL)
    }

    // ==================== SESSION ====================

    /**
     * Guarda el estado de login
     */
    fun setLoggedIn(isLoggedIn: Boolean) {
        securePreferences.putBoolean(PrefsKeys.IS_LOGGED_IN, isLoggedIn)
        _isLoggedIn.value = isLoggedIn
    }

    /**
     * Obtiene el estado de login
     */
    private fun getIsLoggedIn(): Boolean {
        return securePreferences.getBoolean(PrefsKeys.IS_LOGGED_IN, false)
    }

    /**
     * Verifica si el usuario está logueado y tiene tokens válidos
     */
    fun hasValidSession(): Boolean {
        return getIsLoggedIn() && !getAccessToken().isNullOrEmpty()
    }

    /**
     * Guarda todos los datos de autenticación
     */
    fun saveAuthData(
        accessToken: String,
        refreshToken: String,
        userId: Int,
        userName: String,
        userEmail: String
    ) {
        saveAccessToken(accessToken)
        saveRefreshToken(refreshToken)
        saveUserId(userId)
        saveUserName(userName)
        saveUserEmail(userEmail)
        setLoggedIn(true)
    }

    /**
     * Limpia todos los datos de sesión (logout)
     */
    fun clearSession() {
        clearAccessToken()
        clearRefreshToken()
        securePreferences.remove(PrefsKeys.USER_ID)
        securePreferences.remove(PrefsKeys.USER_NAME)
        securePreferences.remove(PrefsKeys.USER_EMAIL)
        setLoggedIn(false)
    }

    /**
     * Limpia todas las preferencias
     */
    fun clearAll() {
        securePreferences.clear()
        _isLoggedIn.value = false
    }
}
