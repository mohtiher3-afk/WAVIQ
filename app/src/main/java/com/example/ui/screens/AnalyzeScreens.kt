package com.example.ui.screens

import com.example.ui.components.InteractiveSpectralCurve
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.audio.BiquadFilter
import com.example.audio.FilterType
import com.example.model.MusicGenre
import com.example.model.StartupCheckStatus
import com.example.model.StartupHealthState
import com.example.model.WaviqUiState
import com.example.ui.components.HardwarePanel
import com.example.ui.components.TelemetryStatTile
import com.example.ui.components.WaveformScrubBar
import com.example.ui.components.formatMs
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
import kotlin.math.ln
import kotlin.math.pow

@Composable
fun Screen01TrackInspector(
    state: WaviqUiState,
    onImportAudioClick: () -> Unit,
    onLoadReferenceClick: () -> Unit,
    onStartMicCaptureClick: () -> Unit,
    onStopMicCaptureClick: () -> Unit,
    onSeek: (Float) -> Unit,
    onGoToScope: () -> Unit,
    onRerunStartupGuard: () -> Unit = {},
    onDismissStartupBanner: () -> Unit = {}
) {
    val track = state.trackInfo
    val tel = state.telemetry
    val progress = if (tel.durationMs > 0L) (tel.positionMs.toFloat() / tel.durationMs).coerceIn(0f, 1f) else 0f
    val guard = state.startupGuard
    val showFallbackCard = !state.isStartupBannerDismissed &&
        (guard.healthState == StartupHealthState.DEGRADED_FALLBACK ||
            guard.healthState == StartupHealthState.PERMISSION_ADVISORY)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (showFallbackCard) {
            val isDegraded = guard.isDegraded
            val accent = if (isDegraded) GlyphCrimson else PeakAmber
            HardwarePanel(
                title = if (isDegraded) "STARTUP GUARD // FALLBACK MODE" else "STARTUP GUARD // HARDWARE ADVISORY",
                badge = guard.healthState.label,
                accentBorder = isDegraded,
                modifier = Modifier.testTag("startup_guard_fallback_card")
            ) {
                Text(
                    text = guard.fallbackSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SignalWhite
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(guard.tfliteCheck, guard.databaseCheck, guard.permissionCheck).forEach { check ->
                        val cColor = when (check.status) {
                            StartupCheckStatus.PASS -> BusGreen
                            StartupCheckStatus.ADVISORY -> PeakAmber
                            StartupCheckStatus.FALLBACK -> GlyphCrimson
                        }
                        TelemetryStatTile(
                            label = check.title,
                            value = check.status.label,
                            accentColor = cColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRerunStartupGuard,
                        border = BorderStroke(1.dp, accent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("startup_guard_rerun_button")
                    ) {
                        Icon(Icons.Filled.RestartAlt, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("RE-RUN AUDIT", style = MaterialTheme.typography.labelSmall, color = SignalWhite)
                    }
                    OutlinedButton(
                        onClick = onDismissStartupBanner,
                        border = BorderStroke(1.dp, HairlineBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("startup_guard_dismiss_button")
                    ) {
                        Text("DISMISS", style = MaterialTheme.typography.labelSmall, color = TechSlate)
                    }
                }
            }
        }

        HardwarePanel(
            title = "#01 // AUDIO TRACK INSPECTOR",
            badge = track.sourceLabel,
            accentBorder = true
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AudioFile,
                    contentDescription = "Active audio track",
                    tint = GlyphCrimson,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = SignalWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${track.sampleRate / 1000f} kHz  •  ${track.channels}CH STEREO  •  ${track.bitDepthLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TechSlate
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            WaveformScrubBar(
                peaks = track.waveformPeaks,
                progressFraction = progress,
                onSeek = onSeek
            )

            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatMs(tel.positionMs), style = MaterialTheme.typography.labelSmall, color = SignalWhite)
                Text("TAP WAVEFORM TO SCRUB", style = MaterialTheme.typography.labelSmall, color = TechSlate)
                Text(formatMs(tel.durationMs), style = MaterialTheme.typography.labelSmall, color = TechSlate)
            }

            if (state.isLoadingFile) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        color = GlyphCrimson,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("DECODING PCM STREAM...", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                }
            }

            state.fileErrorMessage?.let { err ->
                Spacer(Modifier.height(8.dp))
                Text(err, style = MaterialTheme.typography.bodyMedium, color = GlyphCrimson)
            }

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onImportAudioClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("import_audio_button")
                ) {
                    Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("IMPORT AUDIO", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onLoadReferenceClick,
                    border = BorderStroke(1.dp, HairlineBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("load_ref_button")
                ) {
                    Text("STUDIO REF", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                }
            }
        }

        val mic = state.micCapture
        HardwarePanel(
            title = "AUDIORECORD // LIVE MIC CAPTURE",
            badge = if (mic.isRecording) "REC ${formatMs(mic.durationMs)}" else "OPTIONAL INPUT",
            accentBorder = mic.isRecording
        ) {
            if (mic.isRecording) {
                LinearProgressIndicator(
                    progress = { (mic.inputLevel * 3.5f).coerceIn(0.05f, 1f) },
                    color = GlyphCrimson,
                    trackColor = ConsolePanel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onStopMicCaptureClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("stop_mic_capture_button")
                ) {
                    Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("STOP & LOAD MIC TAKE", style = MaterialTheme.typography.labelMedium)
                }
            } else {
                Text(
                    text = "Record live mono PCM directly into WAVIQ's 64-Bit DSP bus via AudioRecord.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TechSlate
                )
                mic.errorMessage?.let { msg ->
                    Spacer(Modifier.height(6.dp))
                    Text(msg, style = MaterialTheme.typography.labelSmall, color = PeakAmber)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onStartMicCaptureClick,
                    border = BorderStroke(1.dp, HairlineBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("start_mic_capture_button")
                ) {
                    Icon(Icons.Filled.Mic, contentDescription = null, tint = GlyphCrimson, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("CAPTURE LIVE MIC (AUDIORECORD)", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryStatTile(
                label = "INTEGRATED LUFS",
                value = String.format(Locale.US, "%.1f LUFS", track.integratedLufs),
                accentColor = BusGreen,
                modifier = Modifier.weight(1f)
            )
            TelemetryStatTile(
                label = "TRUE PEAK",
                value = String.format(Locale.US, "%.1f dBTP", track.truePeakDb),
                accentColor = if (track.truePeakDb > -0.5f) GlyphCrimson else SignalWhite,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryStatTile(
                label = "DYNAMIC RANGE",
                value = String.format(Locale.US, "%.1f dB", track.dynamicRangeDb),
                modifier = Modifier.weight(1f)
            )
            TelemetryStatTile(
                label = "STEREO WIDTH",
                value = "${track.stereoWidthPct}%",
                accentColor = CyanScope,
                modifier = Modifier.weight(1f)
            )
        }

        val diag = track.diagnostic
        val genreAccent = when (diag.detectedGenre) {
            MusicGenre.CLASSICAL, MusicGenre.ACOUSTIC -> BusGreen
            MusicGenre.POP, MusicGenre.ELECTRONIC, MusicGenre.TECHNO -> CyanScope
            MusicGenre.ROCK, MusicGenre.HIP_HOP, MusicGenre.TRAP -> PeakAmber
            MusicGenre.DRILL -> GlyphCrimson
        }

        HardwarePanel(
            title = "AUTO-AUDIT // SMART TRACK DIAGNOSTIC",
            badge = "AUTO-GENRE // ${diag.detectedGenre.title}",
            accentBorder = true,
            modifier = Modifier.testTag("smart_diagnostic_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryStatTile(
                    label = "DETECTED GENRE",
                    value = "${diag.detectedGenre.title} (${diag.genreConfidencePct}%)",
                    accentColor = genreAccent,
                    modifier = Modifier.weight(1.2f).testTag("smart_genre_tile")
                )
                TelemetryStatTile(
                    label = "AUTO HI-RES TARGET",
                    value = "${diag.detectedGenre.targetKhz}k • ${diag.detectedGenre.oversampling.factor}X ISP",
                    accentColor = BusGreen,
                    modifier = Modifier.weight(1f).testTag("smart_hires_target_tile")
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryStatTile(
                    label = "RECORDING TYPE",
                    value = diag.recordingTypeLabel,
                    accentColor = SignalWhite,
                    modifier = Modifier.weight(1.2f)
                )
                TelemetryStatTile(
                    label = "CLARITY NEED",
                    value = diag.clarityNeedLabel,
                    accentColor = when (diag.clarityNeedLabel) {
                        "HIGH" -> GlyphCrimson
                        "MODERATE" -> PeakAmber
                        else -> BusGreen
                    },
                    modifier = Modifier.weight(0.8f)
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryStatTile(
                    label = "HF BANDWIDTH",
                    value = String.format(Locale.US, "%.1f kHz (-%d%%)", diag.estimatedBandwidthKhz, diag.hfDeficitPct),
                    accentColor = CyanScope,
                    modifier = Modifier.weight(1.15f)
                )
                TelemetryStatTile(
                    label = "PEAK GUARD",
                    value = String.format(Locale.US, "%.1f dBTP (%dX)", state.deckParams.ceilingDb, state.oversamplingFactor.factor),
                    accentColor = BusGreen,
                    modifier = Modifier.weight(0.85f)
                )
            }
            Spacer(Modifier.height(10.dp))
            Surface(
                color = ConsolePanel,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, HairlineBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = diag.autoAppliedSummary,
                        style = MaterialTheme.typography.labelSmall,
                        color = BusGreen
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "RATIONALE: ${diag.genreRationale}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TechSlate
                    )
                }
            }
        }

        InteractiveSpectralCurve(
            genre = diag.detectedGenre,
            bandwidthKhz = diag.estimatedBandwidthKhz,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        Button(
            onClick = onGoToScope,
            colors = ButtonDefaults.buttonColors(containerColor = ConsolePanel, contentColor = SignalWhite),
            border = BorderStroke(1.dp, HairlineBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("go_to_scope_button")
        ) {
            Text("NEXT: #02 LIVE SCOPE ANALYZER", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun Screen02LiveScope(
    state: WaviqUiState,
    onTogglePlay: () -> Unit,
    onGoToEq: () -> Unit
) {
    val tel = state.telemetry
    // 0 = 64-BIN FFT, 1 = 3D VOLUMETRIC, 2 = WAVE SCOPE
    var scopeMode by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Nothing OS 8K / Hi-Res Master Telemetry Badges
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HiResBadge("8K / HI-RES MASTER", GlyphCrimson)
            HiResBadge("${state.hiResTargetKhz}KHZ HF RESTORE", BusGreen)
            HiResBadge(state.oversamplingFactor.label, CyanScope)
            HiResBadge(state.tfliteRuntimeMode, PeakAmber)
        }

        HardwarePanel(
            title = "#02 // LIVE SCOPE ANALYZER",
            badge = if (tel.isPlaying) "60HZ VOLUMETRIC" else "PAUSED — TAP PLAY",
            accentBorder = tel.isPlaying
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    onClick = { scopeMode = 0 },
                    color = if (scopeMode == 0) GlyphCrimson else ConsolePanel,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("scope_mode_fft")
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                        Text("64-BIN FFT", style = MaterialTheme.typography.labelSmall, color = SignalWhite)
                    }
                }
                Surface(
                    onClick = { scopeMode = 1 },
                    color = if (scopeMode == 1) GlyphCrimson else ConsolePanel,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("scope_mode_volumetric")
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                        Text("3D VOLUMETRIC", style = MaterialTheme.typography.labelSmall, color = SignalWhite)
                    }
                }
                Surface(
                    onClick = { scopeMode = 2 },
                    color = if (scopeMode == 2) GlyphCrimson else ConsolePanel,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("scope_mode_osc")
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                        Text("WAVE SCOPE", style = MaterialTheme.typography.labelSmall, color = SignalWhite)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF070708))
                    .border(1.dp, HairlineBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    for (g in 1..3) {
                        val y = size.height * (g / 4f)
                        drawLine(
                            color = Color(0xFF1F1F24),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                    }

                    when (scopeMode) {
                        0 -> {
                            val bins = tel.spectrumBins
                            val slot = size.width / bins.size.coerceAtLeast(1)
                            val barW = (slot * 0.72f).coerceAtLeast(2f)
                            bins.forEachIndexed { idx, v ->
                                val norm = v.coerceIn(0.03f, 1f)
                                val barH = norm * size.height
                                val color = when {
                                    norm > 0.85f -> GlyphCrimson
                                    norm > 0.62f -> PeakAmber
                                    else -> SignalWhite.copy(alpha = 0.85f)
                                }
                                drawRoundRect(
                                    color = color,
                                    topLeft = Offset(idx * slot, size.height - barH),
                                    size = Size(barW, barH),
                                    cornerRadius = CornerRadius(2f, 2f)
                                )
                            }
                        }
                        1 -> {
                            // 3D Volumetric Dual-Mirrored Spectrum with perspective depth shadow bars
                            val bins = tel.spectrumBins
                            val horizonY = size.height * 0.62f
                            val slot = size.width / bins.size.coerceAtLeast(1)
                            val barW = (slot * 0.68f).coerceAtLeast(2f)

                            // Horizon line
                            drawLine(
                                color = GlyphCrimson.copy(alpha = 0.45f),
                                start = Offset(0f, horizonY),
                                end = Offset(size.width, horizonY),
                                strokeWidth = 1.5f
                            )

                            bins.forEachIndexed { idx, v ->
                                val norm = v.coerceIn(0.04f, 1f)
                                val upH = norm * horizonY * 0.92f
                                val x = idx * slot

                                // Depth shadow layer offset for 3D volumetric illusion
                                drawRoundRect(
                                    color = GlyphCrimson.copy(alpha = 0.25f),
                                    topLeft = Offset(x + 3f, horizonY - upH - 4f),
                                    size = Size(barW, upH),
                                    cornerRadius = CornerRadius(2f, 2f)
                                )
                                // Primary upper volumetric crest
                                val crestColor = when {
                                    norm > 0.82f -> GlyphCrimson
                                    norm > 0.58f -> SignalWhite
                                    else -> CyanScope.copy(alpha = 0.85f)
                                }
                                drawRoundRect(
                                    color = crestColor,
                                    topLeft = Offset(x, horizonY - upH),
                                    size = Size(barW, upH),
                                    cornerRadius = CornerRadius(2f, 2f)
                                )
                                // Lower volumetric floor reflection
                                val downH = norm * (size.height - horizonY) * 0.88f
                                drawRoundRect(
                                    color = GlyphCrimson.copy(alpha = 0.42f),
                                    topLeft = Offset(x, horizonY + 2f),
                                    size = Size(barW, downH),
                                    cornerRadius = CornerRadius(2f, 2f)
                                )
                            }
                        }
                        else -> {
                            val samples = tel.scopeSamples
                            if (samples.size > 1) {
                                val path = Path()
                                val stepX = size.width / (samples.size - 1)
                                val midY = size.height / 2f
                                samples.forEachIndexed { i, s ->
                                    val x = i * stepX
                                    val y = midY - (s.coerceIn(-1f, 1f) * midY * 0.85f)
                                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                }
                                drawPath(
                                    path = path,
                                    color = GlyphCrimson,
                                    style = Stroke(width = 3f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("20Hz", style = MaterialTheme.typography.labelSmall, color = TechSlate)
                Text("250Hz", style = MaterialTheme.typography.labelSmall, color = TechSlate)
                Text("1kHz", style = MaterialTheme.typography.labelSmall, color = TechSlate)
                Text("6kHz", style = MaterialTheme.typography.labelSmall, color = TechSlate)
                Text("20kHz", style = MaterialTheme.typography.labelSmall, color = TechSlate)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryStatTile(
                label = "LIVE LUFS",
                value = String.format(Locale.US, "%.1f", tel.integratedLufs),
                accentColor = BusGreen,
                modifier = Modifier.weight(1f)
            )
            TelemetryStatTile(
                label = "CORRELATION",
                value = String.format(Locale.US, "%+.2f", tel.stereoCorrelation),
                accentColor = if (tel.stereoCorrelation < 0f) GlyphCrimson else CyanScope,
                modifier = Modifier.weight(1f)
            )
            TelemetryStatTile(
                label = "L / R VU",
                value = "${tel.leftVuDb.toInt()} / ${tel.rightVuDb.toInt()} dB",
                modifier = Modifier.weight(1.3f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryStatTile(
                label = "NEURAL GATE",
                value = String.format(Locale.US, "-%.0f%%", tel.neuralGateReductionPct),
                accentColor = if (tel.neuralGateReductionPct > 1f) BusGreen else TechSlate,
                modifier = Modifier
                    .weight(1f)
                    .testTag("scope_neural_gate_tile")
            )
            TelemetryStatTile(
                label = "DYN GAIN",
                value = String.format(Locale.US, "%+.1f dB", tel.neuralDynGainDb),
                accentColor = if (kotlin.math.abs(tel.neuralDynGainDb) > 0.1f) PeakAmber else SignalWhite,
                modifier = Modifier
                    .weight(1f)
                    .testTag("scope_neural_gain_tile")
            )
        }

        if (!tel.isPlaying) {
            Button(
                onClick = onTogglePlay,
                colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("scope_start_playback_button")
            ) {
                Text("START REAL-TIME AUDIO TAP", style = MaterialTheme.typography.labelLarge)
            }
        }

        Button(
            onClick = onGoToEq,
            colors = ButtonDefaults.buttonColors(containerColor = ConsolePanel, contentColor = SignalWhite),
            border = BorderStroke(1.dp, HairlineBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("go_to_eq_button")
        ) {
            Text("NEXT: #03 PARAMETRIC EQ & FILTERS", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun Screen03ParametricEq(
    state: WaviqUiState,
    onUpdateBand: (String, Float) -> Unit,
    onResetEq: () -> Unit,
    onGoToReports: () -> Unit
) {
    val bands = state.eqBands

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#03 // PARAMETRIC EQ & BIQUAD FILTERS",
            badge = "5-BAND ACTIVE",
            accentBorder = true
        ) {
            // Live calculated EQ frequency response curve
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF070708))
                    .border(1.dp, HairlineBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val midY = size.height / 2f
                    // 0 dB center line
                    drawLine(
                        color = MutedGrid,
                        start = Offset(0f, midY),
                        end = Offset(size.width, midY),
                        strokeWidth = 1.5f
                    )

                    val tempFilters = bands.mapIndexed { idx, band ->
                        BiquadFilter(48000f).apply {
                            val type = when (idx) {
                                0 -> FilterType.LOW_SHELF
                                4 -> FilterType.HIGH_SHELF
                                else -> FilterType.PEAKING
                            }
                            configure(type, band.frequencyHz, band.qFactor, band.gainDb, 48000f)
                        }
                    }

                    val points = 80
                    val path = Path()
                    val minLog = ln(20f)
                    val maxLog = ln(20000f)
                    for (i in 0 until points) {
                        val frac = i.toFloat() / (points - 1)
                        val freq = kotlin.math.exp(minLog + frac * (maxLog - minLog))
                        var totalDb = 0f
                        for (f in tempFilters) {
                            totalDb += f.magnitudeDbAt(freq)
                        }
                        val clampedDb = totalDb.coerceIn(-14f, 14f)
                        val x = frac * size.width
                        val y = midY - (clampedDb / 14f) * (size.height * 0.45f)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path = path, color = GlyphCrimson, style = Stroke(width = 3.5f))

                    // Band control nodes
                    bands.forEach { band ->
                        val frac = ((ln(band.frequencyHz) - minLog) / (maxLog - minLog)).coerceIn(0f, 1f)
                        val cx = frac * size.width
                        val cy = midY - (band.gainDb / 14f) * (size.height * 0.45f)
                        drawCircle(color = SignalWhite, radius = 7f, center = Offset(cx, cy))
                        drawCircle(color = GlyphCrimson, radius = 4f, center = Offset(cx, cy))
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("REAL-TIME BIQUAD RESPONSE (±12 dB)", style = MaterialTheme.typography.labelSmall, color = TechSlate)
                OutlinedButton(
                    onClick = onResetEq,
                    border = BorderStroke(1.dp, HairlineBorder),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("eq_reset_button")
                ) {
                    Icon(Icons.Filled.RestartAlt, contentDescription = null, tint = SignalWhite, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("FLAT", style = MaterialTheme.typography.labelSmall, color = SignalWhite)
                }
            }

            Spacer(Modifier.height(8.dp))

            bands.forEach { band ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${band.label} // ${band.frequencyHz.toInt()} Hz (Q ${band.qFactor})",
                            style = MaterialTheme.typography.labelMedium,
                            color = SignalWhite
                        )
                        Text(
                            text = String.format(Locale.US, "%+.1f dB", band.gainDb),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (band.gainDb == 0f) TechSlate else GlyphCrimson
                        )
                    }
                    Slider(
                        value = band.gainDb,
                        onValueChange = { onUpdateBand(band.id, it) },
                        valueRange = -12f..12f,
                        colors = SliderDefaults.colors(
                            thumbColor = SignalWhite,
                            activeTrackColor = GlyphCrimson,
                            inactiveTrackColor = HairlineBorder
                        ),
                        modifier = Modifier.testTag("eq_slider_${band.id}")
                    )
                }
            }
        }

        Button(
            onClick = onGoToReports,
            colors = ButtonDefaults.buttonColors(containerColor = ConsolePanel, contentColor = SignalWhite),
            border = BorderStroke(1.dp, HairlineBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("go_to_reports_button")
        ) {
            Text("NEXT: #04 ANALYSIS REPORTS", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun Screen04AnalysisReports(
    state: WaviqUiState,
    onGoToEnhanceEngine: () -> Unit
) {
    val track = state.trackInfo
    val lufsDelta = -14.0f - track.integratedLufs
    val headroomOk = track.truePeakDb <= -1.0f
    val dynamicHealthy = track.dynamicRangeDb >= 7.0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#04 // MASTERING AUDIT REPORT",
            badge = "AIR-GAPPED DIAGNOSTIC",
            accentBorder = true
        ) {
            AuditRow(
                label = "STREAMING TARGET (-14.0 LUFS)",
                detail = String.format(Locale.US, "Current %.1f LUFS (Delta %+.1f dB)", track.integratedLufs, lufsDelta),
                passed = kotlin.math.abs(lufsDelta) < 4f
            )
            Spacer(Modifier.height(10.dp))
            AuditRow(
                label = "TRUE-PEAK HEADROOM (<= -1.0 dBTP)",
                detail = String.format(Locale.US, "Measured %.1f dBTP inter-sample peak", track.truePeakDb),
                passed = headroomOk
            )
            Spacer(Modifier.height(10.dp))
            AuditRow(
                label = "CREST FACTOR / DYNAMIC RANGE",
                detail = String.format(Locale.US, "%.1f dB DR — ${if (dynamicHealthy) "Punch Preserved" else "Over-Compressed"}", track.dynamicRangeDb),
                passed = dynamicHealthy
            )
            Spacer(Modifier.height(10.dp))
            AuditRow(
                label = "STEREO PHASE & PROFILE (${track.diagnostic.recordingTypeLabel})",
                detail = "${track.stereoWidthPct}% width — ${track.diagnostic.clarityNeedLabel} restoration applied",
                passed = true
            )
            Spacer(Modifier.height(10.dp))
            AuditRow(
                label = "HIGH-FREQUENCY BANDWIDTH (${String.format(Locale.US, "%.1f", track.diagnostic.estimatedBandwidthKhz)} kHz)",
                detail = "${track.diagnostic.hfDeficitPct}% upper-band deficit — Hi-Res upsampler auto-calibrated",
                passed = track.diagnostic.hfDeficitPct < 50
            )
        }

        HardwarePanel(title = "RECOMMENDED DSP ACTION") {
            Text(
                text = if (lufsDelta > 1.5f) {
                    "Apply +${String.format(Locale.US, "%.1f", lufsDelta)} dB gain staging with True-Peak ceiling at -1.0 dBTP and subtle 12kHz air lift."
                } else {
                    "Track loudness is near streaming target. Engage Autonomous AI Engine (#05) for harmonic glue and stereo polish."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = SignalWhite
            )
        }

        Button(
            onClick = onGoToEnhanceEngine,
            colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("go_to_enhance_button")
        ) {
            Text("ENGAGE TAB 2: #05 AUTONOMOUS AI ENGINE", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun AuditRow(
    label: String,
    detail: String,
    passed: Boolean
) {
    Surface(
        color = ConsolePanel,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, HairlineBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (passed) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                contentDescription = if (passed) "Audit passed" else "Audit warning",
                tint = if (passed) BusGreen else PeakAmber,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                Text(detail, style = MaterialTheme.typography.bodyMedium, color = TechSlate)
            }
        }
    }
}

@Composable
fun HiResBadge(
    label: String,
    accent: Color
) {
    Surface(
        color = ConsolePanel,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, accent)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
