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

private val DarkColorScheme = darkColorScheme(
    primary = RouteifyBlue400,
    onPrimary = NeutralGray900,
    primaryContainer = RouteifyBlue600,
    onPrimaryContainer = RouteifyBlue100,

    secondary = RouteifyGreen400,
    onSecondary = NeutralGray900,
    secondaryContainer = RouteifyGreen600,
    onSecondaryContainer = RouteifyGreen100,

    tertiary = RouteifyTeal,
    onTertiary = NeutralGray900,
    tertiaryContainer = RouteifyCyan,
    onTertiaryContainer = RouteifyBlue100,

    background = DarkBackground,
    onBackground = NeutralGray100,
    surface = DarkSurface,
    onSurface = NeutralGray100,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = NeutralGray300,

    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF5C1A1A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = RouteifyBlue500,
    onPrimary = Color.White,
    primaryContainer = RouteifyBlue100,
    onPrimaryContainer = RouteifyBlue600,

    secondary = RouteifyGreen500,
    onSecondary = Color.White,
    secondaryContainer = RouteifyGreen100,
    onSecondaryContainer = RouteifyGreen600,

    tertiary = RouteifyTeal,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCCF7F1),
    onTertiaryContainer = Color(0xFF00504A),

    background = NeutralGray50,
    onBackground = NeutralGray900,
    surface = Color.White,
    onSurface = NeutralGray900,
    surfaceVariant = NeutralGray100,
    onSurfaceVariant = NeutralGray600,

    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    outline = NeutralGray300,
    outlineVariant = NeutralGray200
)

@Composable
fun RouteifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to use custom theme
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}