package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val WaviqDarkColorScheme = darkColorScheme(
    primary = GlyphCrimson,
    onPrimary = SignalWhite,
    primaryContainer = GlyphCrimsonMuted,
    onPrimaryContainer = SignalWhite,
    secondary = BusGreen,
    onSecondary = ObsidianVoid,
    secondaryContainer = BusGreenMuted,
    onSecondaryContainer = BusGreen,
    tertiary = CyanScope,
    onTertiary = ObsidianVoid,
    background = ObsidianVoid,
    onBackground = SignalWhite,
    surface = HardwareSurface,
    onSurface = SignalWhite,
    surfaceVariant = ConsolePanel,
    onSurfaceVariant = TechSlate,
    outline = HairlineBorder,
    outlineVariant = MutedGrid,
    error = GlyphCrimson,
    onError = SignalWhite
)

private val WaviqShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun WaviqTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WaviqDarkColorScheme,
        typography = WaviqTypography,
        shapes = WaviqShapes,
        content = content
    )
}
