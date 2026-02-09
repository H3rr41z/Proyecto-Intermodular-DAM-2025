package com.renaix.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.renaix.util.Constants

/**
 * Wrapper para EncryptedSharedPreferences
 * Proporciona almacenamiento seguro para datos sensibles como tokens
 */
class SecurePreferences(context: Context) {

    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        Constants.ENCRYPTED_PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Guarda un valor String
     */
    fun putString(key: String, value: String?) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    /**
     * Obtiene un valor String
     */
    fun getString(key: String, defaultValue: String? = null): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    /**
     * Guarda un valor Int
     */
    fun putInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    /**
     * Obtiene un valor Int
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    /**
     * Guarda un valor Long
     */
    fun putLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }

    /**
     * Obtiene un valor Long
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }

    /**
     * Guarda un valor Boolean
     */
    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    /**
     * Obtiene un valor Boolean
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    /**
     * Elimina una clave específica
     */
    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    /**
     * Limpia todas las preferencias
     */
    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    /**
     * Verifica si una clave existe
     */
    fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }
}
