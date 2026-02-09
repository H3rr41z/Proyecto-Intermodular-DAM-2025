package com.renaix.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Formas personalizadas para la aplicación Renaix
 * Define las esquinas redondeadas para diferentes componentes
 */
val RenaixShapes = Shapes(
    // Para elementos muy pequeños (chips, badges)
    extraSmall = RoundedCornerShape(4.dp),

    // Para botones pequeños, campos de texto
    small = RoundedCornerShape(8.dp),

    // Para cards, dialogs
    medium = RoundedCornerShape(12.dp),

    // Para cards grandes, bottom sheets
    large = RoundedCornerShape(16.dp),

    // Para elementos muy grandes, modales
    extraLarge = RoundedCornerShape(24.dp)
)

/**
 * Esquinas personalizadas adicionales para casos específicos
 */
object CustomShapes {
    val BottomSheet = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    val ProductCard = RoundedCornerShape(16.dp)

    val ProductImage = RoundedCornerShape(12.dp)

    val Button = RoundedCornerShape(12.dp)

    val TextField = RoundedCornerShape(12.dp)

    val Chip = RoundedCornerShape(8.dp)

    val SearchBar = RoundedCornerShape(24.dp)

    val Avatar = RoundedCornerShape(50)

    val MessageBubble = RoundedCornerShape(16.dp)

    val FloatingActionButton = RoundedCornerShape(16.dp)
}
