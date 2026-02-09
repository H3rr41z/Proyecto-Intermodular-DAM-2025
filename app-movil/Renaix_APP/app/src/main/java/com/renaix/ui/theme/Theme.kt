package com.renaix.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Esquema de colores para modo claro
 * Basado en la paleta morada del logo de Renaix
 */
private val LightColorScheme = lightColorScheme(
    // Colores primarios
    primary = Purple500,
    onPrimary = Color.White,
    primaryContainer = Purple100,
    onPrimaryContainer = Purple900,

    // Colores secundarios
    secondary = DeepPurple,
    onSecondary = Color.White,
    secondaryContainer = PurpleAccent,
    onSecondaryContainer = Purple900,

    // Colores terciarios
    tertiary = Pink500,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),

    // Colores de error
    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    // Colores de fondo y superficie
    background = BackgroundLight,
    onBackground = Grey900,
    surface = SurfaceLight,
    onSurface = Grey900,
    surfaceVariant = Grey100,
    onSurfaceVariant = Grey700,

    // Outline
    outline = Grey400,
    outlineVariant = Grey300,

    // Inverse
    inverseSurface = Grey900,
    inverseOnSurface = Grey100,
    inversePrimary = Purple300,

    // Surface tint
    surfaceTint = Purple500,

    // Scrim
    scrim = Color.Black.copy(alpha = 0.32f)
)

/**
 * Esquema de colores para modo oscuro
 */
private val DarkColorScheme = darkColorScheme(
    // Colores primarios
    primary = Purple300,
    onPrimary = Purple900,
    primaryContainer = Purple700,
    onPrimaryContainer = Purple100,

    // Colores secundarios
    secondary = PurpleAccent,
    onSecondary = Purple900,
    secondaryContainer = DeepPurple,
    onSecondaryContainer = Purple100,

    // Colores terciarios
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),

    // Colores de error
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    // Colores de fondo y superficie
    background = BackgroundDark,
    onBackground = Grey100,
    surface = SurfaceDark,
    onSurface = Grey100,
    surfaceVariant = Grey800,
    onSurfaceVariant = Grey300,

    // Outline
    outline = Grey600,
    outlineVariant = Grey700,

    // Inverse
    inverseSurface = Grey100,
    inverseOnSurface = Grey900,
    inversePrimary = Purple500,

    // Surface tint
    surfaceTint = Purple300,

    // Scrim
    scrim = Color.Black.copy(alpha = 0.32f)
)

/**
 * Tema principal de la aplicación Renaix
 *
 * @param darkTheme Indica si usar el tema oscuro
 * @param dynamicColor Indica si usar colores dinámicos (Android 12+)
 * @param content Contenido composable
 */
@Composable
fun RenaixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Deshabilitado para mantener el branding morado
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RenaixTypography,
        shapes = RenaixShapes,
        content = content
    )
}
