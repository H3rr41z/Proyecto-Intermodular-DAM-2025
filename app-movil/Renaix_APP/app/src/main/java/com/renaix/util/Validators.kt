package com.renaix.util

import com.renaix.util.Constants.MAX_COMMENT_LENGTH
import com.renaix.util.Constants.MAX_DESCRIPTION_LENGTH
import com.renaix.util.Constants.MAX_NAME_LENGTH
import com.renaix.util.Constants.MIN_COMMENT_LENGTH
import com.renaix.util.Constants.MIN_DESCRIPTION_LENGTH
import com.renaix.util.Constants.MIN_NAME_LENGTH
import com.renaix.util.Constants.MIN_PASSWORD_LENGTH

/**
 * Validaciones de formularios y datos
 */
object Validators {

    // ==================== Auth Validators ====================

    /**
     * Valida un email
     * @return Mensaje de error o null si es válido
     */
    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "El email es obligatorio"
            !email.isValidEmail() -> "Email inválido"
            else -> null
        }
    }

    /**
     * Valida una contraseña
     * @return Mensaje de error o null si es válida
     */
    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.length < MIN_PASSWORD_LENGTH ->
                "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres"
            !password.any { it.isDigit() } ->
                "La contraseña debe contener al menos un número"
            else -> null
        }
    }

    /**
     * Valida que dos contraseñas coincidan
     */
    fun validatePasswordMatch(password: String, confirmPassword: String): String? {
        return if (password != confirmPassword) {
            "Las contraseñas no coinciden"
        } else {
            null
        }
    }

    /**
     * Valida un nombre de usuario
     */
    fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "El nombre es obligatorio"
            name.length < MIN_NAME_LENGTH ->
                "El nombre debe tener al menos $MIN_NAME_LENGTH caracteres"
            name.length > MAX_NAME_LENGTH ->
                "El nombre no puede superar los $MAX_NAME_LENGTH caracteres"
            !name.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) ->
                "El nombre solo puede contener letras y espacios"
            else -> null
        }
    }

    /**
     * Valida un teléfono
     */
    fun validatePhone(phone: String): String? {
        if (phone.isBlank()) return null // Opcional

        return when {
            !phone.isValidSpanishPhone() ->
                "Teléfono inválido (formato: +34XXXXXXXXX o 6/7/9XXXXXXXX)"
            else -> null
        }
    }

    // ==================== Product Validators ====================

    /**
     * Valida el nombre de un producto
     */
    fun validateProductName(name: String): String? {
        return when {
            name.isBlank() -> "El nombre del producto es obligatorio"
            name.length < 3 -> "El nombre debe tener al menos 3 caracteres"
            name.length > MAX_NAME_LENGTH ->
                "El nombre no puede superar los $MAX_NAME_LENGTH caracteres"
            else -> null
        }
    }

    /**
     * Valida la descripción de un producto
     */
    fun validateProductDescription(description: String): String? {
        return when {
            description.isBlank() -> "La descripción es obligatoria"
            description.length < MIN_DESCRIPTION_LENGTH ->
                "La descripción debe tener al menos $MIN_DESCRIPTION_LENGTH caracteres"
            description.length > MAX_DESCRIPTION_LENGTH ->
                "La descripción no puede superar los $MAX_DESCRIPTION_LENGTH caracteres"
            else -> null
        }
    }

    /**
     * Valida el precio de un producto
     */
    fun validatePrice(priceString: String): String? {
        val price = priceString.toDoubleOrNull()

        return when {
            priceString.isBlank() -> "El precio es obligatorio"
            price == null -> "Precio inválido"
            price <= 0 -> "El precio debe ser mayor a 0"
            price > 1_000_000 -> "El precio no puede superar 1.000.000€"
            else -> null
        }
    }

    /**
     * Valida que se haya seleccionado una categoría
     */
    fun validateCategory(categoryId: Int?): String? {
        return if (categoryId == null || categoryId <= 0) {
            "Debes seleccionar una categoría"
        } else {
            null
        }
    }

    // ==================== Comment Validators ====================

    /**
     * Valida un comentario
     */
    fun validateComment(comment: String): String? {
        return when {
            comment.isBlank() -> "El comentario no puede estar vacío"
            comment.length < MIN_COMMENT_LENGTH ->
                "El comentario debe tener al menos $MIN_COMMENT_LENGTH caracteres"
            comment.length > MAX_COMMENT_LENGTH ->
                "El comentario no puede superar los $MAX_COMMENT_LENGTH caracteres"
            else -> null
        }
    }

    // ==================== Rating Validators ====================

    /**
     * Valida una valoración
     */
    fun validateRating(rating: Int): String? {
        return when {
            rating < 1 -> "La valoración mínima es 1 estrella"
            rating > 5 -> "La valoración máxima es 5 estrellas"
            else -> null
        }
    }

    // ==================== Search Validators ====================

    /**
     * Valida un término de búsqueda
     */
    fun validateSearchQuery(query: String): String? {
        return when {
            query.isBlank() -> "Introduce un término de búsqueda"
            query.length < 2 -> "El término debe tener al menos 2 caracteres"
            else -> null
        }
    }

    /**
     * Valida un rango de precios
     */
    fun validatePriceRange(minPrice: Double?, maxPrice: Double?): String? {
        if (minPrice == null || maxPrice == null) return null

        return when {
            minPrice < 0 -> "El precio mínimo no puede ser negativo"
            maxPrice < 0 -> "El precio máximo no puede ser negativo"
            minPrice > maxPrice -> "El precio mínimo no puede ser mayor al máximo"
            else -> null
        }
    }

    // ==================== Message Validators ====================

    /**
     * Valida un mensaje de chat
     */
    fun validateMessage(message: String): String? {
        return when {
            message.isBlank() -> "El mensaje no puede estar vacío"
            message.length > 1000 -> "El mensaje no puede superar los 1000 caracteres"
            else -> null
        }
    }

    // ==================== Report Validators ====================

    /**
     * Valida un motivo de denuncia
     */
    fun validateReportReason(reason: String): String? {
        return when {
            reason.isBlank() -> "Debes especificar un motivo"
            reason.length < 10 -> "El motivo debe tener al menos 10 caracteres"
            reason.length > 500 -> "El motivo no puede superar los 500 caracteres"
            else -> null
        }
    }
}
