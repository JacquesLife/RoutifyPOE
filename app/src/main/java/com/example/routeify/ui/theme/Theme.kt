/*
 * ============================================================================
 * MAIN THEME - Material 3 Theme Configuration
 * ============================================================================
 * 
 * Configures Material 3 design system with custom Routeify branding.
 * Handles light/dark mode switching and dynamic color support.
 * 
 * ============================================================================
 */

package com.example.routeify.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Define the dark and light color schemes
private val DarkColorScheme = darkColorScheme(
    primary = RouteifyBlue400,
    onPrimary = NeutralGray900,
    primaryContainer = RouteifyBlue600,
    onPrimaryContainer = RouteifyBlue100,

    // Secondary colors
    secondary = RouteifyGreen400,
    onSecondary = NeutralGray900,
    secondaryContainer = RouteifyGreen600,
    onSecondaryContainer = RouteifyGreen100,

    // Tertiary colors
    tertiary = RouteifyTeal,
    onTertiary = NeutralGray900,
    tertiaryContainer = RouteifyCyan,
    onTertiaryContainer = RouteifyBlue100,

    // Background and surface colors
    background = DarkBackground,
    onBackground = NeutralGray100,
    surface = DarkSurface,
    onSurface = NeutralGray100,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = NeutralGray300,

    // Error colors
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF5C1A1A),
    onErrorContainer = Color(0xFFFFDAD6)
)

// Light theme color scheme
private val LightColorScheme = lightColorScheme(
    primary = RouteifyBlue500,
    onPrimary = Color.White,
    primaryContainer = RouteifyBlue100,
    onPrimaryContainer = RouteifyBlue600,

    // Secondary colors
    secondary = RouteifyGreen500,
    onSecondary = Color.White,
    secondaryContainer = RouteifyGreen100,
    onSecondaryContainer = RouteifyGreen600,

    // Tertiary colors
    tertiary = RouteifyTeal,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCCF7F1),
    onTertiaryContainer = Color(0xFF00504A),

    // Background and surface colors
    background = NeutralGray50,
    onBackground = NeutralGray900,
    surface = Color.White,
    onSurface = NeutralGray900,
    surfaceVariant = NeutralGray100,
    onSurfaceVariant = NeutralGray600,

    // Error colors
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    outline = NeutralGray300,
    outlineVariant = NeutralGray200
)

// Main Theme Composable
@Composable
fun RouteifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to use custom theme
    content: @Composable () -> Unit
) {
    // Choose color scheme based on system settings and dynamic color preference
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Apply the selected color scheme and typography to MaterialTheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// --------------------------------------------------End of File----------------------------------------------------------------