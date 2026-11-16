package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val TacticalDarkColorScheme = darkColorScheme(
    primary = CrimsonPrimary,
    onPrimary = Color.White,
    primaryContainer = CrimsonContainer,
    onPrimaryContainer = OnCrimsonContainer,
    secondary = AmberSecondary,
    onSecondary = Color(0xFF1A1200),
    secondaryContainer = AmberContainer,
    onSecondaryContainer = OnAmberContainer,
    tertiary = RadarCyan,
    onTertiary = Color(0xFF001F24),
    tertiaryContainer = RadarCyanContainer,
    onTertiaryContainer = OnRadarCyanContainer,
    background = CommandObsidian,
    onBackground = TextPrimary,
    surface = CommandSurface,
    onSurface = TextPrimary,
    surfaceVariant = CommandSurfaceHigh,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = CommandSurface,
    surfaceContainerHigh = CommandSurfaceHigh,
    surfaceContainerHighest = CommandSurfaceHighest,
    outline = CommandBorder,
    outlineVariant = Color(0xFF222A3A),
    error = ErrorRed,
    onError = Color.White,
)

private val TacticalShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun Ra2LauncherTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = TacticalDarkColorScheme,
        typography = AppTypography,
        shapes = TacticalShapes,
        content = content,
    )
}
