package com.androidengineers.agent_quickstart_android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
    darkColorScheme(
        primary = DarkPrimary,
        onPrimary = DarkPrimaryForeground,
        primaryContainer = DarkSidebar,
        onPrimaryContainer = DarkForeground,
        secondary = CyanLight,
        onSecondary = DarkForeground,
        secondaryContainer = DarkMuted,
        onSecondaryContainer = DarkForeground,
        tertiary = Violet,
        onTertiary = DarkForeground,
        tertiaryContainer = DarkMuted,
        onTertiaryContainer = DarkForeground,
        background = DarkBackground,
        onBackground = DarkForeground,
        surface = DarkCard,
        onSurface = DarkForeground,
        surfaceVariant = DarkMuted,
        onSurfaceVariant = DarkMutedForeground,
        surfaceContainer = DarkMuted,
        surfaceContainerHigh = DarkSidebar,
        surfaceContainerHighest = DarkBorder,
        outline = DarkBorder,
        outlineVariant = DarkBorder,
        error = DarkDestructive,
        onError = DarkForeground,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = WarmPrimary,
        onPrimary = WarmPrimaryForeground,
        primaryContainer = WarmMuted,
        onPrimaryContainer = WarmForeground,
        secondary = Blue,
        onSecondary = Color.White,
        secondaryContainer = WarmMuted,
        onSecondaryContainer = WarmForeground,
        tertiary = VioletDark,
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFF1E2FF),
        onTertiaryContainer = WarmForeground,
        background = WarmBackground,
        onBackground = WarmForeground,
        surface = WarmCard,
        onSurface = WarmForeground,
        surfaceVariant = WarmMuted,
        onSurfaceVariant = WarmMutedForeground,
        surfaceContainer = WarmCard,
        surfaceContainerHigh = Color(0xFFF8F6F5),
        surfaceContainerHighest = Color(0xFFF0EDEB),
        outline = WarmBorder,
        outlineVariant = WarmBorder,
        error = WarmDestructive,
        onError = Color.White,
    )

@Composable
fun DroidBuddyTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
