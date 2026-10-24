package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Single Source of Truth for whether the active theme is Dark.
 * Consistent across all screens and components, respecting the user's
 * setting (LIGHT, DARK, or SYSTEM).
 */
val LocalIsDarkTheme = compositionLocalOf { false }

/**
 * Composable helper returning true if the app is currently displaying dark theme.
 */
@Composable
fun isAppDarkTheme(): Boolean = LocalIsDarkTheme.current

@Composable
fun MyApplicationTheme(
    themePreference: String = "SYSTEM",
    accentTheme: AccentTheme = AccentTheme.IIHT_BLUE,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val darkTheme = when (themePreference.uppercase()) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemInDark
    }

    val primaryLight = accentTheme.primary
    val primaryDark = accentTheme.darkPrimary

    val darkColors = darkColorScheme(
        primary = MockupDarkAccentCyan,
        onPrimary = Color(0xFF061329),
        primaryContainer = Color(0xFF1769FF).copy(alpha = 0.25f),
        onPrimaryContainer = Color(0xFFBAE6FD),
        secondary = MockupDarkAccentBlue,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFF10264A),
        onSecondaryContainer = Color(0xFFE2E8F0),
        tertiary = AmberAccent,
        onTertiary = Color(0xFF451A03),
        tertiaryContainer = Color(0xFF78350F).copy(alpha = 0.35f),
        onTertiaryContainer = Color(0xFFFDE68A),
        background = MockupDarkBg,
        onBackground = MockupDarkTextPrimary,
        surface = MockupDarkCard,
        onSurface = MockupDarkTextPrimary,
        surfaceVariant = MockupDarkCardSurface,
        onSurfaceVariant = MockupDarkTextSecondary,
        surfaceContainer = MockupDarkCard,
        surfaceContainerHigh = Color(0xFF162E56),
        surfaceContainerHighest = Color(0xFF1E3A66),
        surfaceContainerLow = MockupDarkCardSurface,
        surfaceContainerLowest = MockupDarkBg,
        outline = MockupDarkBorder,
        outlineVariant = Color(0xFF162E56),
        error = Color(0xFFF87171),
        onError = Color(0xFF450A0A),
        errorContainer = Color(0xFF7F1D1D).copy(alpha = 0.35f),
        onErrorContainer = Color(0xFFFECACA),
        scrim = Color(0x99000000)
    )

    val lightColors = lightColorScheme(
        primary = MockupLightAccentBlue,
        onPrimary = Color.White,
        primaryContainer = MockupLightSurfacePale,
        onPrimaryContainer = Color(0xFF102653),
        secondary = MockupLightAccentBlue,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFEFF6FF),
        onSecondaryContainer = Color(0xFF1769FF),
        tertiary = AmberAccent,
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFEF3C7),
        onTertiaryContainer = Color(0xFF92400E),
        background = MockupLightBg,
        onBackground = MockupLightTextPrimary,
        surface = MockupLightCard,
        onSurface = MockupLightTextPrimary,
        surfaceVariant = MockupLightSurfacePale,
        onSurfaceVariant = MockupLightTextSecondary,
        surfaceContainer = MockupLightCard,
        surfaceContainerHigh = Color(0xFFF0F6FF),
        surfaceContainerHighest = Color(0xFFE8F1FC),
        surfaceContainerLow = Color(0xFFFFFFFF),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        outline = MockupLightBorder,
        outlineVariant = Color(0xFFD8E6F5),
        error = Color(0xFFDC2626),
        onError = Color.White,
        errorContainer = Color(0xFFFEE2E2),
        onErrorContainer = Color(0xFF991B1B),
        scrim = Color(0x66000000)
    )

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColors
        else -> lightColors
    }

    CompositionLocalProvider(
        LocalIsDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
