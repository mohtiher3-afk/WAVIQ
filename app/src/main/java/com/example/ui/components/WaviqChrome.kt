package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AudioBusMode
import com.example.model.WaviqRouteSpec
import com.example.model.WaviqTab
import com.example.ui.theme.BusGreen
import com.example.ui.theme.BusGreenMuted
import com.example.ui.theme.ConsolePanel
import com.example.ui.theme.GlyphCrimson
import com.example.ui.theme.GlyphCrimsonGlow
import com.example.ui.theme.GlyphCrimsonMuted
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.HardwareSurface
import com.example.ui.theme.MutedGrid
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.PeakAmber
import com.example.ui.theme.SignalWhite
import com.example.ui.theme.TechSlate
import java.util.Locale

@Composable
fun StudioTopBar(
    activeRoute: WaviqRouteSpec,
    busModeOverride: AudioBusMode?,
    isPlaying: Boolean,
    dspLatencyMs: Float,
    isBypassed: Boolean,
    isDevMapOpen: Boolean,
    aiConfidencePct: Int = 92,
    onTogglePlay: () -> Unit,
    onToggleDevMap: () -> Unit
) {
    val activeBus = busModeOverride ?: activeRoute.busMode
    val effectiveBypassed = isBypassed || activeBus == AudioBusMode.BYPASS_RAW
    val effectiveLatencyMs = if (effectiveBypassed) 0f else dspLatencyMs
    val latencyColor = when {
        effectiveBypassed -> TechSlate
        effectiveLatencyMs > 15.0f -> GlyphCrimson
        effectiveLatencyMs > 8.0f -> PeakAmber
        else -> BusGreen
    }
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ObsidianVoid)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) GlyphCrimson else BusGreen)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "WAVIQ",
                            style = MaterialTheme.typography.titleLarge,
                            color = SignalWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = ConsolePanel,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, HairlineBorder)
                        ) {
                            Text(
                                text = if (effectiveBypassed) "BYPASS DRY" else "64-BIT OFFLINE",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (effectiveBypassed) TechSlate else BusGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = ConsolePanel,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, latencyColor.copy(alpha = 0.55f)),
                            modifier = Modifier.testTag("top_bus_latency_badge")
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.2f ms", effectiveLatencyMs),
                                style = MaterialTheme.typography.labelSmall,
                                color = latencyColor,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (effectiveBypassed) "BYPASS DRY BUS" else "AUTONOMOUS AUDIO INTELLIGENCE // ${activeBus.code}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TechSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onTogglePlay()
                    },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("top_transport_play_button")
                        .clip(CircleShape)
                        .background(if (isPlaying) GlyphCrimson else ConsolePanel)
                        .border(1.dp, if (isPlaying) GlyphCrimson else HairlineBorder, CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause audio playback" else "Play audio playback",
                        tint = SignalWhite
                    )
                }

                Spacer(Modifier.width(8.dp))

                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleDevMap()
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDevMapOpen) GlyphCrimson else ConsolePanel,
                    border = BorderStroke(1.dp, if (isDevMapOpen) GlyphCrimson else HairlineBorder),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("dev_map_toggle_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountTree,
                            contentDescription = "Open Developer Diagnostics",
                            tint = SignalWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isDevMapOpen) "BACK" else "DIAG",
                            style = MaterialTheme.typography.labelMedium,
                            color = SignalWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubScreenSelectorRail(
    routes: List<WaviqRouteSpec>,
    activeRouteId: Int,
    onSelectRoute: (Int) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ObsidianVoid)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        routes.forEach { route ->
            val selected = route.id == activeRouteId
            val bg by animateColorAsState(
                if (selected) GlyphCrimsonMuted else HardwareSurface,
                label = "pillBg"
            )
            val border by animateColorAsState(
                if (selected) GlyphCrimson else HairlineBorder,
                label = "pillBorder"
            )
            Surface(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSelectRoute(route.id)
                },
                shape = RoundedCornerShape(8.dp),
                color = bg,
                border = BorderStroke(1.dp, border),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("subroute_pill_${route.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = route.code,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) GlyphCrimson else TechSlate
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = route.shortTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) SignalWhite else TechSlate
                    )
                }
            }
        }
    }
}

@Composable
fun HardwarePanel(
    modifier: Modifier = Modifier,
    title: String? = null,
    badge: String? = null,
    accentBorder: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = HardwareSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (accentBorder) GlyphCrimson else HairlineBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (title != null || badge != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = SignalWhite
                        )
                    }
                    if (badge != null) {
                        Surface(
                            color = if (accentBorder) GlyphCrimsonMuted else ConsolePanel,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (accentBorder) GlyphCrimson else HairlineBorder)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (accentBorder) SignalWhite else TechSlate,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            content()
        }
    }
}

@Composable
fun TelemetryStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accentColor: Color = SignalWhite
) {
    Surface(
        modifier = modifier,
        color = ConsolePanel,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, HairlineBorder)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TechSlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WaveformScrubBar(
    peaks: List<Float>,
    progressFraction: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val safePeaks = if (peaks.isEmpty()) List(64) { 0.25f } else peaks
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ConsolePanel)
            .border(1.dp, HairlineBorder, RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val frac = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    onSeek(frac)
                }
            }
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val count = safePeaks.size
            val slotW = size.width / count
            val barW = (slotW * 0.65f).coerceAtLeast(2f)
            val midY = size.height / 2f
            val playX = progressFraction.coerceIn(0f, 1f) * size.width

            for (i in 0 until count) {
                val x = i * slotW + (slotW - barW) / 2f
                val amp = safePeaks[i].coerceIn(0.06f, 1f)
                val h = amp * size.height * 0.9f
                val isPlayed = x <= playX
                drawRoundRect(
                    color = if (isPlayed) GlyphCrimson else MutedGrid,
                    topLeft = Offset(x, midY - h / 2f),
                    size = Size(barW, h),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }
            // Playhead needle
            drawLine(
                color = SignalWhite,
                start = Offset(playX, 0f),
                end = Offset(playX, size.height),
                strokeWidth = 2.5f
            )
        }
    }
}

@Composable
fun TactileRotarySlider(
    label: String,
    value: Float,
    valueDisplay: String,
    onValueChange: (Float) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    val norm = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start))
        .coerceIn(0f, 1f)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ConsolePanel)
            .border(1.dp, HairlineBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(28.dp)) {
                    drawArc(
                        color = MutedGrid,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = GlyphCrimson,
                        startAngle = 135f,
                        sweepAngle = 270f * norm,
                        useCenter = false,
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = SignalWhite
                )
            }
            Text(
                text = valueDisplay,
                style = MaterialTheme.typography.labelLarge,
                color = GlyphCrimson
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = SignalWhite,
                activeTrackColor = GlyphCrimson,
                inactiveTrackColor = HairlineBorder
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
fun WaviqBottomNavBar(
    activeTab: WaviqTab,
    onSelectTab: (WaviqTab) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HardwareSurface)
            .testTag("waviq_bottom_nav_bar")
    ) {
        HorizontalDivider(thickness = 1.dp, color = HairlineBorder)
        NavigationBar(
            containerColor = HardwareSurface,
            contentColor = SignalWhite,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets.navigationBars
        ) {
            WaviqTab.entries.forEach { tab ->
                val selected = tab == activeTab
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelectTab(tab)
                    },
                    icon = {
                        Icon(
                            imageVector = tabIcon(tab, selected),
                            contentDescription = tab.title
                        )
                    },
                    label = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SignalWhite,
                        selectedTextColor = SignalWhite,
                        indicatorColor = GlyphCrimson,
                        unselectedIconColor = TechSlate,
                        unselectedTextColor = TechSlate
                    ),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("bottom_tab_${tab.name.lowercase()}")
                )
            }
        }
    }
}

@Composable
fun WaviqLeftNavRail(
    activeTab: WaviqTab,
    onSelectTab: (WaviqTab) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .background(HardwareSurface)
            .testTag("waviq_left_nav_rail")
    ) {
        NavigationRail(
            containerColor = HardwareSurface,
            contentColor = SignalWhite
        ) {
            Spacer(Modifier.height(16.dp))
            WaviqTab.entries.forEach { tab ->
                val selected = tab == activeTab
                NavigationRailItem(
                    selected = selected,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelectTab(tab)
                    },
                    icon = {
                        Icon(
                            imageVector = tabIcon(tab, selected),
                            contentDescription = tab.title
                        )
                    },
                    label = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = SignalWhite,
                        selectedTextColor = SignalWhite,
                        indicatorColor = GlyphCrimson,
                        unselectedIconColor = TechSlate,
                        unselectedTextColor = TechSlate
                    ),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("rail_tab_${tab.name.lowercase()}")
                )
            }
        }
        VerticalDivider(thickness = 1.dp, color = HairlineBorder)
    }
}

private fun tabIcon(tab: WaviqTab, selected: Boolean) = when (tab) {
    WaviqTab.STUDIO -> if (selected) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome
    WaviqTab.LIBRARY -> if (selected) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic
}

fun formatMs(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val m = totalSec / 60L
    val s = totalSec % 60L
    return String.format(Locale.US, "%02d:%02d", m, s)
}
