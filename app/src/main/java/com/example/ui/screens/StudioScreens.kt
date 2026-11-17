package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import com.example.model.GlyphPatternMode
import com.example.model.OversamplingFactor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.StemChannelState
import com.example.model.WaviqUiState
import com.example.ui.components.HardwarePanel
import com.example.ui.components.TelemetryStatTile
import com.example.ui.theme.BusGreen
import com.example.ui.theme.ConsolePanel
import com.example.ui.theme.CyanScope
import com.example.ui.theme.GlyphCrimson
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.MutedGrid
import com.example.ui.theme.PeakAmber
import com.example.ui.theme.SignalWhite
import com.example.ui.theme.TechSlate
import java.util.Locale

@Composable
fun Screen09StudioProSuite(
    state: WaviqUiState,
    onToggleDspHook: (String, Boolean) -> Unit,
    onSetOversampling: (OversamplingFactor) -> Unit,
    onToggleHiResKhz: () -> Unit,
    onJumpRoute: (Int) -> Unit
) {
    val d = state.deckParams
    val chainModules = listOf(
        Triple("01 // BIQUAD 5X EQ", "Sub ${state.eqBands.firstOrNull()?.gainDb ?: 0f} dB  •  Air ${state.eqBands.lastOrNull()?.gainDb ?: 0f} dB", 3),
        Triple("02 // HARMONIC DRIVE", "Drive ${(d.drive * 100).toInt()}%  •  Punch ${(d.punch * 100).toInt()}%", 7),
        Triple("03 // 4-WAY STEM MATRIX", "Vocals/Drums/Bass/Other Crossover", 10),
        Triple("04 // TRUE-PEAK LIMITER", "Ceiling ${d.ceilingDb} dBTP  •  ${state.oversamplingFactor.label}", 11)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HiResBadge("64-BIT DOUBLE DSP", GlyphCrimson)
            HiResBadge("${state.hiResTargetKhz}KHZ SBR + M/S", BusGreen)
            HiResBadge("3-BAND ${state.oversamplingFactor.label}", CyanScope)
            HiResBadge(state.tfliteRuntimeMode, PeakAmber)
        }

        HardwarePanel(
            title = "#09 // AI STUDIO PRO SUITE",
            badge = "64-BIT / 192KHZ",
            accentBorder = true
        ) {
            Text(
                text = "Unified 64-bit Double DSP console chaining parametric EQ, harmonic saturation, SBR HF reconstruction + M/S width, TFLite AI, stems, and 3-band True-Peak limiting.",
                style = MaterialTheme.typography.bodyMedium,
                color = TechSlate
            )
            Spacer(Modifier.height(12.dp))

            chainModules.forEach { (title, subtitle, targetRoute) ->
                Surface(
                    onClick = { onJumpRoute(targetRoute) },
                    color = ConsolePanel,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, HairlineBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("pro_chain_node_$targetRoute")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, style = MaterialTheme.typography.labelLarge, color = SignalWhite)
                            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TechSlate)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Open module", tint = GlyphCrimson)
                    }
                }
            }
        }

        HardwarePanel(
            title = "TRUE-PEAK OVERSAMPLING & HI-RES",
            badge = state.oversamplingFactor.label
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OversamplingFactor.entries.forEach { os ->
                    val selected = state.oversamplingFactor == os
                    Surface(
                        onClick = { onSetOversampling(os) },
                        color = if (selected) GlyphCrimson else ConsolePanel,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (selected) GlyphCrimson else HairlineBorder),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("oversampling_${os.factor}x_button")
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(10.dp)) {
                            Text("${os.factor}X ISP LIMITER", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                        }
                    }
                }
                Surface(
                    onClick = onToggleHiResKhz,
                    color = ConsolePanel,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BusGreen),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("hires_khz_toggle_button")
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(10.dp)) {
                        Text("${state.hiResTargetKhz}KHZ TARGET", style = MaterialTheme.typography.labelMedium, color = BusGreen)
                    }
                }
            }
        }

        val effLatencyMs = if (state.telemetry.isBypassed) 0f else state.telemetry.dspLatencyMs
        val budgetPct = ((effLatencyMs / 21.333f) * 100f).coerceIn(0f, 100f)
        val latencyColor = when {
            state.telemetry.isBypassed -> TechSlate
            effLatencyMs > 15.0f -> GlyphCrimson
            effLatencyMs > 8.0f -> PeakAmber
            else -> BusGreen
        }

        HardwarePanel(
            title = "AUDIOPROCESSOR // 64-BIT DOUBLE DSP HOOKS",
            badge = "${state.dspHooks.count { it.isEnabled }}/${state.dspHooks.size} ACTIVE • ${String.format(Locale.US, "%.2f ms", effLatencyMs)}"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryStatTile(
                    label = "CHAIN LATENCY",
                    value = String.format(Locale.US, "%.2f ms", effLatencyMs),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pro_chain_latency_tile"),
                    accentColor = latencyColor
                )
                TelemetryStatTile(
                    label = "BLOCK BUDGET (21.3ms)",
                    value = String.format(Locale.US, "%.1f%%", budgetPct),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pro_block_budget_tile"),
                    accentColor = CyanScope
                )
            }
            Spacer(Modifier.height(10.dp))

            state.dspHooks.forEach { hook ->
                val hookMs = if (state.telemetry.isBypassed || !hook.isEnabled) 0f else hook.latencyMs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(hook.name, style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                        Text(
                            text = "id: ${hook.id} • ${String.format(Locale.US, "%.2f ms", hookMs)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hook.isEnabled && !state.telemetry.isBypassed) TechSlate else MutedGrid
                        )
                    }
                    Switch(
                        checked = hook.isEnabled,
                        onCheckedChange = { onToggleDspHook(hook.id, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SignalWhite,
                            checkedTrackColor = GlyphCrimson
                        ),
                        modifier = Modifier.testTag("dsp_hook_switch_${hook.id}")
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryStatTile("LIVE LUFS", String.format(Locale.US, "%.1f", state.telemetry.integratedLufs), Modifier.weight(1f), BusGreen)
            TelemetryStatTile("TRUE PEAK", String.format(Locale.US, "%.1f dBTP", state.telemetry.truePeakDb), Modifier.weight(1f), GlyphCrimson)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryStatTile(
                label = "NEURAL GATE",
                value = String.format(Locale.US, "-%.0f%%", state.telemetry.neuralGateReductionPct),
                modifier = Modifier
                    .weight(1f)
                    .testTag("pro_neural_gate_tile"),
                accentColor = BusGreen
            )
            TelemetryStatTile(
                label = "DYN GAIN",
                value = String.format(Locale.US, "%+.1f dB", state.telemetry.neuralDynGainDb),
                modifier = Modifier
                    .weight(1f)
                    .testTag("pro_neural_gain_tile"),
                accentColor = PeakAmber
            )
        }

        HardwarePanel(
            title = "3-BAND TRUE-PEAK ISP LIMITER",
            badge = String.format(Locale.US, "%.1f dBTP CEILING", d.ceilingDb)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryStatTile(
                    label = "LOW <250HZ GR",
                    value = String.format(Locale.US, "-%.1f dB", state.telemetry.lowBandGrDb),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pro_band_gr_low"),
                    accentColor = GlyphCrimson
                )
                TelemetryStatTile(
                    label = "MID 250-4K GR",
                    value = String.format(Locale.US, "-%.1f dB", state.telemetry.midBandGrDb),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pro_band_gr_mid"),
                    accentColor = PeakAmber
                )
                TelemetryStatTile(
                    label = "HIGH >4K GR",
                    value = String.format(Locale.US, "-%.1f dB", state.telemetry.highBandGrDb),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pro_band_gr_high"),
                    accentColor = CyanScope
                )
            }
        }

        Button(
            onClick = { onJumpRoute(10) },
            colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("go_to_stems_button")
        ) {
            Text("NEXT: #10 4-WAY STEM SEPARATION", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun Screen10StemSeparation(
    state: WaviqUiState,
    onUpdateStem: (String, Float?, Boolean, Boolean) -> Unit,
    onResetStems: () -> Unit,
    onGoToBypass: () -> Unit
) {
    val stems = state.stemMixer.asList()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HardwarePanel(
            title = "#10 // 4-WAY STEM SEPARATION",
            badge = "STEM_MATRIX_4X",
            accentBorder = true
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Real-time 4-bus crossover isolation", style = MaterialTheme.typography.bodyMedium, color = TechSlate)
                OutlinedButton(
                    onClick = onResetStems,
                    border = BorderStroke(1.dp, HairlineBorder),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("stems_reset_button")
                ) {
                    Icon(Icons.Filled.RestartAlt, contentDescription = null, tint = SignalWhite, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("UNITY", style = MaterialTheme.typography.labelSmall, color = SignalWhite)
                }
            }

            Spacer(Modifier.height(10.dp))

            stems.forEach { stem ->
                StemRow(
                    stem = stem,
                    onLevelChange = { onUpdateStem(stem.id, it, false, false) },
                    onToggleMute = { onUpdateStem(stem.id, null, true, false) },
                    onToggleSolo = { onUpdateStem(stem.id, null, false, true) }
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        Button(
            onClick = onGoToBypass,
            colors = ButtonDefaults.buttonColors(containerColor = ConsolePanel, contentColor = SignalWhite),
            border = BorderStroke(1.dp, HairlineBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("go_to_bypass_button")
        ) {
            Text("NEXT: #11 ZERO-LATENCY BYPASS MATRIX", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun StemRow(
    stem: StemChannelState,
    onLevelChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onToggleSolo: () -> Unit
) {
    Surface(
        color = ConsolePanel,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (stem.soloed) GlyphCrimson else HairlineBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stem.label, style = MaterialTheme.typography.labelLarge, color = SignalWhite)
                    Text(stem.bandDescription, style = MaterialTheme.typography.labelSmall, color = TechSlate)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        onClick = onToggleSolo,
                        color = if (stem.soloed) PeakAmber else Color(0xFF28282C),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("stem_solo_${stem.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text("S", style = MaterialTheme.typography.labelMedium, color = if (stem.soloed) Color.Black else SignalWhite)
                        }
                    }
                    Surface(
                        onClick = onToggleMute,
                        color = if (stem.muted) GlyphCrimson else Color(0xFF28282C),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("stem_mute_${stem.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text("M", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                        }
                    }
                }
            }
            Slider(
                value = stem.level,
                onValueChange = onLevelChange,
                valueRange = 0f..1.5f,
                colors = SliderDefaults.colors(
                    thumbColor = SignalWhite,
                    activeTrackColor = if (stem.muted) MutedGrid else GlyphCrimson,
                    inactiveTrackColor = HairlineBorder
                ),
                modifier = Modifier.testTag("stem_slider_${stem.id}")
            )
        }
    }
}

@Composable
fun Screen11ZeroLatencyBypass(
    state: WaviqUiState,
    onToggleBypassEngaged: (Boolean) -> Unit,
    onGoToGlyph: () -> Unit
) {
    val engaged = state.bypassEngaged
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#11 // ZERO-LATENCY BYPASS VS ENGAGED",
            badge = if (engaged) "NPU_DSP_ACTIVE" else "BYPASS_RAW",
            accentBorder = engaged
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (engaged) "DSP MASTER BUS ENGAGED" else "TRUE HARDWARE BYPASS",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (engaged) BusGreen else GlyphCrimson
                    )
                    Text(
                        text = "15ms constant-power clickless relay switch",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TechSlate
                    )
                }
                Switch(
                    checked = engaged,
                    onCheckedChange = onToggleBypassEngaged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SignalWhite,
                        checkedTrackColor = BusGreen,
                        uncheckedThumbColor = SignalWhite,
                        uncheckedTrackColor = GlyphCrimson
                    ),
                    modifier = Modifier.testTag("zero_latency_bypass_switch")
                )
            }

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val busLatencyMs = if (!engaged || state.telemetry.isBypassed) 0f else state.telemetry.dspLatencyMs
                TelemetryStatTile(
                    label = "BUS LATENCY",
                    value = String.format(Locale.US, "%.2f ms", busLatencyMs),
                    modifier = Modifier.weight(1f).testTag("bypass_bus_latency_tile"),
                    accentColor = CyanScope
                )
                TelemetryStatTile("LIVE PEAK", String.format(Locale.US, "%.1f dBTP", state.telemetry.truePeakDb), Modifier.weight(1f))
            }
        }

        Button(
            onClick = onGoToGlyph,
            colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("go_to_glyph_button")
        ) {
            Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("NEXT: #12 GLYPH VU SYNC", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun Screen12GlyphVuSync(
    state: WaviqUiState,
    onSelectGlyphPattern: (GlyphPatternMode) -> Unit,
    onTogglePlay: () -> Unit,
    onGoToTranscode: () -> Unit
) {
    val tel = state.telemetry
    val frame = tel.glyphFrame

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#12 // NOTHING GLYPH VU SYNC",
            badge = if (tel.isPlaying) "${frame.pulsePatternLabel} • 60HZ" else "STANDBY",
            accentBorder = true
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GlyphPatternMode.entries.forEach { mode ->
                    val selected = state.glyphPatternMode == mode
                    Surface(
                        onClick = { onSelectGlyphPattern(mode) },
                        color = if (selected) GlyphCrimson else ConsolePanel,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, if (selected) GlyphCrimson else HairlineBorder),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("glyph_pattern_${mode.name.lowercase()}")
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                            Text(mode.label, style = MaterialTheme.typography.labelSmall, color = SignalWhite)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(276.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, HairlineBorder, RoundedCornerShape(14.dp))
                    .testTag("glyph_chassis_visualizer"),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_studio_console_bg),
                    contentDescription = "Nothing OS acoustic studio chassis background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0.32f)
                )

                // Rear chassis Nothing OS Glyph LED visualizer driven by real-time GlyphHardwareFrame
                Canvas(modifier = Modifier.fillMaxSize().padding(18.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val radius = minOf(size.width, size.height) * 0.34f

                    // ZONE A1: Top-left camera pill Glyph
                    val pillColor = if (frame.patternMode == GlyphPatternMode.GAIN_REDUCTION_CLAMP) {
                        GlyphCrimson
                    } else {
                        SignalWhite
                    }
                    drawRoundRect(
                        color = pillColor.copy(alpha = 0.18f + frame.cameraPillLevel * 0.82f),
                        topLeft = Offset(cx - radius * 1.18f, cy - radius * 1.12f),
                        size = Size(radius * 0.68f, 14f),
                        cornerRadius = CornerRadius(7f, 7f)
                    )

                    // ZONE A2: Top-right diagonal Glyph strip
                    val diagColor = if (frame.gainReductionDb > 0.4f) GlyphCrimson else SignalWhite
                    drawLine(
                        color = diagColor.copy(alpha = 0.18f + frame.diagonalStripLevel * 0.82f),
                        start = Offset(cx + radius * 0.65f, cy - radius * 1.15f),
                        end = Offset(cx + radius * 1.15f, cy - radius * 0.65f),
                        strokeWidth = 10f,
                        cap = StrokeCap.Round
                    )

                    // ZONE C1: 8-segment addressable wireless-coil C-Ring VU meter
                    val totalSweep = 256f
                    val startBase = 142f
                    val segCount = frame.centerRingSegments.size.coerceAtLeast(1)
                    val gapDeg = 5f
                    val segSweep = (totalSweep - gapDeg * (segCount - 1)) / segCount
                    frame.centerRingSegments.forEachIndexed { idx, segLvl ->
                        val segColor = when {
                            frame.patternMode == GlyphPatternMode.GAIN_REDUCTION_CLAMP && segLvl > 0.3f -> GlyphCrimson
                            idx >= segCount - 2 && segLvl > 0.6f -> GlyphCrimson
                            else -> SignalWhite
                        }
                        drawArc(
                            color = segColor.copy(alpha = 0.14f + segLvl * 0.86f),
                            startAngle = startBase + idx * (segSweep + gapDeg),
                            sweepAngle = segSweep,
                            useCenter = false,
                            topLeft = Offset(cx - radius, cy - radius),
                            size = Size(radius * 2f, radius * 2f),
                            style = Stroke(width = 11f + segLvl * 5f, cap = StrokeCap.Round)
                        )
                    }

                    // ZONE C2: Inner vocal / RMS center core dot
                    drawCircle(
                        color = GlyphCrimson.copy(alpha = 0.25f + frame.coreDotLevel * 0.75f),
                        radius = 12f + frame.coreDotLevel * 20f,
                        center = Offset(cx, cy)
                    )

                    // ZONE D1: 6-segment bottom USB-C exclamation bar (DSP Gain Reduction & Clamp)
                    val exclSegs = frame.usbExclamationSegments
                    val segH = 9f
                    val segGap = 4f
                    val baseBottom = size.height - 4f
                    exclSegs.forEachIndexed { idx, lvl ->
                        val yTop = baseBottom - (idx + 1) * (segH + segGap)
                        val barColor = if (lvl > 0.35f || frame.gainReductionDb > 0.2f) GlyphCrimson else CyanScope
                        drawRoundRect(
                            color = barColor.copy(alpha = 0.15f + lvl * 0.85f),
                            topLeft = Offset(cx - 6f, yTop),
                            size = Size(12f, segH),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryStatTile(
                    label = "RMS (0-100)",
                    value = "${frame.rmsPercent}%",
                    accentColor = SignalWhite,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("glyph_rms_tile")
                )
                TelemetryStatTile(
                    label = "GR (0-100)",
                    value = "${frame.gainReductionPercent}%",
                    accentColor = if (frame.gainReductionPercent > 15) GlyphCrimson else BusGreen,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("glyph_gr_tile")
                )
                TelemetryStatTile(
                    label = "AI GATE",
                    value = String.format(Locale.US, "-%.0f%%", tel.neuralGateReductionPct),
                    accentColor = PeakAmber,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("glyph_gate_tile")
                )
            }

            Spacer(Modifier.height(10.dp))
            val zoneLabels = listOf("A1", "A2", "C1", "C2", "D1")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                frame.pwm12BitChannels.forEachIndexed { idx, pwm ->
                    TelemetryStatTile(
                        label = "PWM ${zoneLabels.getOrElse(idx) { "Z$idx" }}",
                        value = "$pwm",
                        accentColor = if (pwm > 2800) GlyphCrimson else CyanScope,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (!tel.isPlaying) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onTogglePlay,
                    colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("glyph_play_button")
                ) {
                    Text("ANIMATE GLYPH WITH LIVE AUDIO", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Button(
            onClick = onGoToTranscode,
            colors = ButtonDefaults.buttonColors(containerColor = ConsolePanel, contentColor = SignalWhite),
            border = BorderStroke(1.dp, HairlineBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("go_to_transcode_button")
        ) {
            Text("NEXT TAB 4: #13 TRANSCODE & EXPORT", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}
