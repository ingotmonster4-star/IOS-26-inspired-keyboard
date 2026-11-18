package com.example.ui.theme

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

private val VetroDarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF042235),
    primaryContainer = Color(0xFF0C4A6E),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = VetroCyanSecondary,
    onSecondary = Color(0xFF042F2E),
    background = VetroDarkBackground,
    onBackground = VetroDarkInk,
    surface = VetroDarkSurface,
    onSurface = VetroDarkInk,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = VetroDarkMuted,
)

private val VetroLightColorScheme = lightColorScheme(
    primary = VetroIcePrimary,
    onPrimary = VetroIceOnPrimary,
    primaryContainer = VetroIcePrimaryContainer,
    onPrimaryContainer = VetroIceOnPrimaryContainer,
    secondary = VetroCyanSecondary,
    onSecondary = Color.White,
    secondaryContainer = VetroCyanContainer,
    onSecondaryContainer = VetroOnCyanContainer,
    background = VetroSlateBackground,
    onBackground = VetroSlateInk,
    surface = VetroSlateSurface,
    onSurface = VetroSlateInk,
    surfaceVariant = Color(0xFFE6EFF8),
    onSurfaceVariant = VetroSlateMuted,
)

@Composable
fun VetroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> VetroDarkColorScheme
        else -> VetroLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VetroTypography,
        content = content,
    )
}
