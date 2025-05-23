package com.example.bookkeeper.ui.theme

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

// Define original colors
val CoralOrange = Color(0xFFFF8C5A) // The color used for buttons in the wireframe
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val LightGray = Color(0xFFF5F5F5)
val DarkGray = Color(0xFF333333)

// 2025 Updated Color Palette
val VibrantCoral = Color(0xFFFF5C5A)
val DeepIndigo = Color(0xFF3D4B96)
val PearlWhite = Color(0xFFF9F8F5)
val CharcoalBlack = Color(0xFF1A1A1A)
val StormGray = Color(0xFF4A4A4A)
val LightSilver = Color(0xFFECECEC)
val SoftLavender = Color(0xFFE6E0F0)

// Define the light color scheme
private val LightColorScheme = lightColorScheme(
    primary = DeepIndigo,
    onPrimary = White,
    primaryContainer = SoftLavender,
    onPrimaryContainer = DeepIndigo,

    secondary = StormGray,
    onSecondary = White,
    secondaryContainer = LightSilver,
    onSecondaryContainer = StormGray,

    tertiary = VibrantCoral,
    onTertiary = White,

    background = White,
    onBackground = CharcoalBlack,

    surface = White,
    onSurface = CharcoalBlack,
    surfaceVariant = LightSilver,
    onSurfaceVariant = StormGray
)

// Define the dark color scheme
private val DarkColorScheme = darkColorScheme(
    primary = SoftLavender,
    onPrimary = DeepIndigo,
    primaryContainer = Color(0xFF232C66),
    onPrimaryContainer = SoftLavender,

    secondary = LightSilver,
    onSecondary = StormGray,
    secondaryContainer = Color(0xFF2F3030),
    onSecondaryContainer = LightSilver,

    tertiary = VibrantCoral,
    onTertiary = White,

    background = CharcoalBlack,
    onBackground = White,

    surface = Color(0xFF121212),
    onSurface = Color(0xFFE2E2E2),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFCACACA)
)

@Composable
fun BookKeeperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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
        typography = AppTypography,
        content = content
    )
}