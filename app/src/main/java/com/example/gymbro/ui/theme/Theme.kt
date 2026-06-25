package com.example.gymbro.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider

val LocalIsDarkTheme = compositionLocalOf { false }

// ── Light Brutalist ────────────────────────────────────────────────
private val LightBrutalistScheme = lightColorScheme(
    primary             = BrutalistYellow,
    onPrimary           = BrutalistLightText,
    secondary           = BrutalistPink,
    onSecondary         = BrutalistLightText,
    tertiary            = BrutalistBlue,
    onTertiary          = White,
    background          = BrutalistLightBackground,
    onBackground        = BrutalistLightText,
    surface             = BrutalistLightSurface,
    onSurface           = BrutalistLightText,
    surfaceVariant      = BrutalistLightCard,
    onSurfaceVariant    = BrutalistLightText,
    outline             = BrutalistLightBorder,
    outlineVariant      = BrutalistLightBorder,
    error               = BrutalistRed,
    onError             = White,
)

// ── Dark Brutalist (AMOLED) ────────────────────────────────────────
private val DarkBrutalistScheme = darkColorScheme(
    primary             = BrutalistYellow,
    onPrimary           = BrutalistDarkText,
    secondary           = BrutalistPink,
    onSecondary         = BrutalistDarkText,
    tertiary            = BrutalistBlue,
    onTertiary          = White,
    background          = BrutalistDarkBackground,
    onBackground        = BrutalistDarkText,
    surface             = BrutalistDarkSurface,
    onSurface           = BrutalistDarkText,
    surfaceVariant      = BrutalistDarkCard,
    onSurfaceVariant    = BrutalistDarkText,
    outline             = BrutalistDarkBorder,
    outlineVariant      = BrutalistDarkBorder,
    error               = BrutalistRed,
    onError             = White,
)

@Composable
fun GymBroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkBrutalistScheme else LightBrutalistScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
