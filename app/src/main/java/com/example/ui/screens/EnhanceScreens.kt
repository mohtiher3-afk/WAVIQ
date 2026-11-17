package com.example.ui.screens

import com.example.ui.components.InteractiveSpectralCurve
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.EnhanceProfile
import com.example.model.MusicGenre
import com.example.model.TactileDeckParams
import com.example.model.WaviqUiState
import com.example.ui.components.HardwarePanel
import com.example.ui.components.TactileRotarySlider
import com.example.ui.components.TelemetryStatTile
import com.example.ui.theme.BusGreen
import com.example.ui.theme.ConsolePanel
import com.example.ui.theme.CyanScope
import com.example.ui.theme.GlyphCrimson
import com.example.ui.theme.GlyphCrimsonMuted
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.MutedGrid
import com.example.ui.theme.PeakAmber
import com.example.ui.theme.SignalWhite
import com.example.ui.theme.TechSlate
import java.util.Locale

@Composable
fun Screen05AutonomousEngine(
    state: WaviqUiState,
    onSelectProfile: (EnhanceProfile) -> Unit,
    onSelectGenre: (MusicGenre) -> Unit = {},
    onDispatchDeepProcessing: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val diag = state.trackInfo.diagnostic
    val activeGenre = state.selectedGenre

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#05 // SMART GENRE ADAPTIVE MASTERING",
            badge = if (state.isGenreAutoDetected && activeGenre == diag.detectedGenre) {
                "AUTO-DETECTED (${diag.genreConfidencePct}%)"
            } else {
                "MANUAL OVERRIDE"
            },
            accentBorder = true
        ) {
            Text(
                text = "Adaptive DSP presets tuned by real-time spectral FFT and transient crest analysis. Selecting a genre auto-calibrates EQ, Harmonic Drive, Hi-Res Target, and True-Peak limiting.",
                style = MaterialTheme.typography.bodyMedium,
                color = TechSlate
            )
            Spacer(Modifier.height(12.dp))

            MusicGenre.entries.forEach { genre ->
                val selected = genre == activeGenre
                val isDetected = genre == diag.detectedGenre
                val genreBorder = when (genre) {
                    MusicGenre.CLASSICAL, MusicGenre.ACOUSTIC -> BusGreen
                    MusicGenre.POP, MusicGenre.ELECTRONIC, MusicGenre.TECHNO -> CyanScope
                    MusicGenre.ROCK, MusicGenre.HIP_HOP, MusicGenre.TRAP -> PeakAmber
                    MusicGenre.DRILL -> GlyphCrimson
                }

                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectGenre(genre)
                    },
                    color = if (selected) GlyphCrimsonMuted else ConsolePanel,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = if (selected) 1.5.dp else 1.dp,
                        color = if (selected) GlyphCrimson else if (isDetected) genreBorder.copy(alpha = 0.7f) else HairlineBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("genre_card_${genre.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(genre.title, style = MaterialTheme.typography.labelLarge, color = SignalWhite)
                                if (isDetected) {
                                    Spacer(Modifier.width(8.dp))
                                    Surface(
                                        color = genreBorder.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(1.dp, genreBorder)
                                    ) {
                                        Text(
                                            text = "DETECTED",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = genreBorder,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(genre.subtitle, style = MaterialTheme.typography.bodyMedium, color = TechSlate)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${genre.targetKhz}k • ${genre.oversampling.factor}X",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) SignalWhite else BusGreen
                            )
                            Text(
                                text = "${genre.ceilingDb} dBTP • ${genre.targetLufs} LUFS",
                                style = MaterialTheme.typography.labelSmall,
                                color = TechSlate
                            )
                        }
                    }
                }
            }
        }

        InteractiveSpectralCurve(
            genre = activeGenre,
            bandwidthKhz = diag.estimatedBandwidthKhz,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        HardwarePanel(
            title = "TARGET STREAMING PROFILES",
            badge = "AIR-GAPPED NPU"
        ) {
            Text(
                text = "Manual override profiles for fine-tuning dynamic range, target LUFS, and brickwall saturation.",
                style = MaterialTheme.typography.bodyMedium,
                color = TechSlate
            )
            Spacer(Modifier.height(12.dp))

            EnhanceProfile.entries.forEach { profile ->
                val selected = profile == state.selectedProfile
                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectProfile(profile)
                    },
                    color = if (selected) GlyphCrimsonMuted else ConsolePanel,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (selected) GlyphCrimson else HairlineBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("profile_card_${profile.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(profile.title, style = MaterialTheme.typography.labelLarge, color = SignalWhite)
                            Spacer(Modifier.height(2.dp))
                            Text(profile.subtitle, style = MaterialTheme.typography.bodyMedium, color = TechSlate)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${profile.ceilingDb} dBTP",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) SignalWhite else TechSlate
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onDispatchDeepProcessing()
            },
            colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("run_deep_processing_button")
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("DISPATCH #06 DEEP 3.5S PROCESSING", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun Screen06DeepProcessing(
    state: WaviqUiState,
    onRunPass: () -> Unit,
    onGoToDeck: () -> Unit
) {
    val proc = state.processing
    val passes = listOf(
        "PASS 1 // SPECTRAL FFT AUDIT",
        "PASS 2 // HARMONIC TUBE CALIBRATION",
        "PASS 3 // MULTIBAND DYNAMICS ALIGN",
        "PASS 4 // TRUE-PEAK LIMITER SEAL"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#06 // DEEP 3.5S PROCESSING",
            badge = if (proc.isRunning) "NPU_DSP_ACTIVE" else if (proc.hasCompletedOnce) "LOCKED 100%" else "STANDBY",
            accentBorder = true
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(144.dp)) {
                    drawArc(
                        color = MutedGrid,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 12f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = if (proc.progress >= 0.99f) BusGreen else GlyphCrimson,
                        startAngle = -90f,
                        sweepAngle = 360f * proc.progress.coerceIn(0f, 1f),
                        useCenter = false,
                        style = Stroke(width = 12f, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(proc.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.displaySmall,
                        color = SignalWhite
                    )
                    Text(
                        text = state.selectedProfile.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = TechSlate
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = proc.currentStage,
                style = MaterialTheme.typography.labelLarge,
                color = if (proc.hasCompletedOnce && !proc.isRunning) BusGreen else GlyphCrimson
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { proc.progress.coerceIn(0f, 1f) },
                color = GlyphCrimson,
                trackColor = ConsolePanel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(Modifier.height(14.dp))
            passes.forEachIndexed { idx, label ->
                val done = proc.completedPasses > idx
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = if (done) BusGreen else MutedGrid,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (done) SignalWhite else TechSlate
                    )
                }
            }
        }

        if (!proc.isRunning) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onRunPass,
                    colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("rerun_35s_button")
                ) {
                    Text("RUN 3.5S PASS", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick = onGoToDeck,
                    colors = ButtonDefaults.buttonColors(containerColor = ConsolePanel, contentColor = SignalWhite),
                    border = BorderStroke(1.dp, HairlineBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("go_to_deck_button")
                ) {
                    Text("#07 TACTILE DECK", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun Screen07TactileDeck(
    state: WaviqUiState,
    onUpdateDeck: ((TactileDeckParams) -> TactileDeckParams) -> Unit,
    onGoToAb: () -> Unit
) {
    val d = state.deckParams
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HardwarePanel(
            title = "#07 // TACTILE MASTERING DECK",
            badge = "10MS HAPTIC DETENTS",
            accentBorder = true
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TactileRotarySlider(
                    label = "HARMONIC DRIVE",
                    value = d.drive,
                    valueDisplay = "${(d.drive * 100).toInt()}%",
                    onValueChange = { v -> onUpdateDeck { it.copy(drive = v) } },
                    testTag = "deck_drive_slider"
                )
                TactileRotarySlider(
                    label = "STEREO WIDTH (M/S)",
                    value = d.width,
                    valueDisplay = "${(d.width * 150).toInt()}%",
                    onValueChange = { v -> onUpdateDeck { it.copy(width = v) } },
                    testTag = "deck_width_slider"
                )
                TactileRotarySlider(
                    label = "TRANSIENT PUNCH",
                    value = d.punch,
                    valueDisplay = "${(d.punch * 100).toInt()}%",
                    onValueChange = { v -> onUpdateDeck { it.copy(punch = v) } },
                    testTag = "deck_punch_slider"
                )
                TactileRotarySlider(
                    label = "AIR EXCITER (10kHz)",
                    value = d.air,
                    valueDisplay = String.format(Locale.US, "%+.1f dB", (d.air - 0.35f) * 9f),
                    onValueChange = { v -> onUpdateDeck { it.copy(air = v) } },
                    testTag = "deck_air_slider"
                )
                TactileRotarySlider(
                    label = "LIMITER CEILING",
                    value = d.ceilingDb,
                    valueDisplay = String.format(Locale.US, "%.1f dBTP", d.ceilingDb),
                    onValueChange = { v -> onUpdateDeck { it.copy(ceilingDb = v) } },
                    valueRange = -3.0f..-0.1f,
                    testTag = "deck_ceiling_slider"
                )
            }
        }

        Button(
            onClick = onGoToAb,
            colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("go_to_ab_button")
        ) {
            Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("NEXT: #08 ORIGINAL VS ENHANCED A/B", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun Screen08AbCompare(
    state: WaviqUiState,
    onToggleAb: (Boolean) -> Unit,
    onGoToStudioPro: () -> Unit
) {
    val enhanced = state.abEnhancedEngaged
    val haptics = LocalHapticFeedback.current
    val tel = state.telemetry

    val cardBorder by animateColorAsState(
        if (enhanced) GlyphCrimson else HairlineBorder,
        label = "abBorder"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#08 // ORIGINAL VS ENHANCED A/B",
            badge = "15MS CONSTANT-POWER",
            accentBorder = enhanced
        ) {
            Text(
                text = "Flip seamlessly between raw bypassed input (A) and the full NPU/DSP mastering chain (B) with 15ms constant-power crossfade smoothing.",
                style = MaterialTheme.typography.bodyMedium,
                color = TechSlate
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleAb(false)
                    },
                    color = if (!enhanced) SignalWhite else ConsolePanel,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (!enhanced) SignalWhite else HairlineBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .testTag("ab_button_original")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "A // ORIGINAL",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (!enhanced) com.example.ui.theme.ObsidianVoid else SignalWhite
                        )
                        Text(
                            "DRY BYPASS",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (!enhanced) com.example.ui.theme.ObsidianVoid else TechSlate
                        )
                    }
                }

                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleAb(true)
                    },
                    color = if (enhanced) GlyphCrimson else ConsolePanel,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .testTag("ab_button_enhanced")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "B // ENHANCED",
                            style = MaterialTheme.typography.labelLarge,
                            color = SignalWhite
                        )
                        Text(
                            state.selectedProfile.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (enhanced) SignalWhite else TechSlate
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryStatTile(
                    label = "ACTIVE BUS",
                    value = if (enhanced) "ENHANCED WET" else "ORIGINAL DRY",
                    accentColor = if (enhanced) GlyphCrimson else SignalWhite,
                    modifier = Modifier.weight(1f)
                )
                TelemetryStatTile(
                    label = "REAL-TIME LUFS",
                    value = String.format(Locale.US, "%.1f LUFS", tel.integratedLufs),
                    accentColor = BusGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Button(
            onClick = onGoToStudioPro,
            colors = ButtonDefaults.buttonColors(containerColor = ConsolePanel, contentColor = SignalWhite),
            border = BorderStroke(1.dp, HairlineBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("go_to_studio_pro_button")
        ) {
            Icon(Icons.Filled.CompareArrows, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("NEXT TAB 3: #09 AI STUDIO PRO SUITE", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}
