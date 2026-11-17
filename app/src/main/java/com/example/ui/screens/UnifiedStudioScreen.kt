package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.audio.BiquadFilter
import com.example.audio.FilterType
import com.example.audio.LocalAiMasteringAgent
import com.example.audio.OnDeviceModelRegistry
import com.example.model.EnhanceProfile
import com.example.model.GlyphPatternMode
import com.example.model.MusicGenre
import com.example.model.OversamplingFactor
import com.example.model.ProTuningTab
import com.example.model.StartupCheckStatus
import com.example.model.StartupHealthState
import com.example.model.TactileDeckParams
import com.example.model.WaviqUiState
import com.example.ui.components.AnalysisTelemetrySection
import com.example.ui.components.DecisionExplainabilitySection
import com.example.ui.components.HardwarePanel
import com.example.ui.components.InteractiveSpectralCurve
import com.example.ui.components.TactileRotarySlider
import com.example.ui.components.TelemetryStatTile
import com.example.ui.components.WaveformScrubBar
import com.example.ui.components.formatMs
import com.example.ui.theme.BusGreen
import com.example.ui.theme.ConsolePanel
import com.example.ui.theme.CyanScope
import com.example.ui.theme.GlyphCrimson
import com.example.ui.theme.GlyphCrimsonMuted
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.HardwareSurface
import com.example.ui.theme.MutedGrid
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.PeakAmber
import com.example.ui.theme.SignalWhite
import com.example.ui.theme.TechSlate
import java.util.Locale
import kotlin.math.ln

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedStudioScreen(
    state: WaviqUiState,
    onImportAudioClick: () -> Unit,
    onLoadReferenceClick: () -> Unit,
    onStartMicCaptureClick: () -> Unit,
    onStopMicCaptureClick: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleAb: (Boolean) -> Unit,
    onRunAutoMasterAi: () -> Unit,
    onUpdateAiInput: (String) -> Unit,
    onSubmitAiCommand: (String) -> Unit,
    onSetProSheetOpen: (Boolean) -> Unit,
    onSelectProTuningTab: (ProTuningTab) -> Unit,
    onUpdateBand: (String, Float) -> Unit,
    onResetEq: () -> Unit,
    onUpdateDeck: ((TactileDeckParams) -> TactileDeckParams) -> Unit,
    onSelectProfile: (EnhanceProfile) -> Unit,
    onSelectGenre: (MusicGenre) -> Unit,
    onUpdateStem: (String, Float?, Boolean, Boolean) -> Unit,
    onResetStems: () -> Unit,
    onToggleDspHook: (String, Boolean) -> Unit,
    onSetOversampling: (OversamplingFactor) -> Unit,
    onToggleHiResKhz: () -> Unit,
    onSelectGlyphPattern: (GlyphPatternMode) -> Unit,
    onApplyMasteringPreset: (String) -> Unit,
    onRerunStartupGuard: () -> Unit,
    onDismissStartupBanner: () -> Unit,
    onCancelProcessing: () -> Unit = {},
    onClearFileError: () -> Unit = {},
    scrollState: ScrollState = rememberScrollState()
) {
    val track = state.trackInfo
    val tel = state.telemetry
    val diag = track.diagnostic
    val haptics = LocalHapticFeedback.current
    val progress = if (tel.durationMs > 0L) (tel.positionMs.toFloat() / tel.durationMs).coerceIn(0f, 1f) else 0f

    val guard = state.startupGuard
    val showFallbackCard = !state.isStartupBannerDismissed &&
        (guard.healthState == StartupHealthState.DEGRADED_FALLBACK ||
            guard.healthState == StartupHealthState.PERMISSION_ADVISORY)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Optional StartupGuard Fallback / Advisory Card
        if (showFallbackCard) {
            val isDegraded = guard.isDegraded
            val accent = if (isDegraded) GlyphCrimson else PeakAmber
            HardwarePanel(
                title = if (isDegraded) "STARTUP GUARD // FALLBACK MODE" else "STARTUP GUARD // ADVISORY",
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
                Spacer(Modifier.height(12.dp))
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

        // 2. Hero Audio Source & Transport Card
        HardwarePanel(
            title = "ACTIVE STUDIO SOURCE",
            badge = track.sourceLabel,
            accentBorder = true
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
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${track.sampleRate / 1000f} kHz  •  ${track.bitDepthLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TechSlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))

                // Quick A/B Master Comparison Toggle
                val enhanced = state.abEnhancedEngaged
                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleAb(!enhanced)
                    },
                    color = if (enhanced) GlyphCrimsonMuted else ConsolePanel,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (enhanced) GlyphCrimson else HairlineBorder),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("ab_toggle_chip")
                ) {
                    Text(
                        text = if (enhanced) "AI: ON (B)" else "RAW (A)",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (enhanced) SignalWhite else TechSlate,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            if (track.durationMs <= 0L || track.waveformPeaks.isEmpty()) {
                Surface(
                    color = ConsolePanel,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, HairlineBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studio_empty_state_banner")
                ) {
                    Text(
                        text = "No active audio stream loaded. Import a file, load bundled studio reference, or record a mic take below.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TechSlate,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

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
                Text("${state.hiResTargetKhz}kHz // ${state.oversamplingFactor.factor}X ISP", style = MaterialTheme.typography.labelSmall, color = CyanScope)
                Text(formatMs(tel.durationMs), style = MaterialTheme.typography.labelSmall, color = TechSlate)
            }

            if (state.isLoadingFile) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = ConsolePanel,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CyanScope.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studio_analyzing_banner")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = GlyphCrimson,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "DECODING & MEASURING BS.1770 / SPECTRAL METRICS...",
                            style = MaterialTheme.typography.labelMedium,
                            color = SignalWhite
                        )
                    }
                }
            }

            if (track.isDurationTruncated) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "TRACK TRUNCATED AT DEVICE SAFE MEMORY LIMIT",
                    style = MaterialTheme.typography.labelSmall,
                    color = PeakAmber
                )
            }

            state.fileErrorMessage?.let { err ->
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = ConsolePanel,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GlyphCrimson),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studio_file_error_banner")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp)
                    ) {
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodyMedium,
                            color = GlyphCrimson,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onClearFileError,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("dismiss_file_error_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Dismiss audio error",
                                tint = SignalWhite
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = state.micCapture.isRecording) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    LinearProgressIndicator(
                        progress = { (state.micCapture.inputLevel * 3.5f).coerceIn(0.05f, 1f) },
                        color = GlyphCrimson,
                        trackColor = ConsolePanel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(Modifier.height(8.dp))
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
                        Text("STOP & LOAD MIC TAKE (${formatMs(state.micCapture.durationMs)})", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            state.micCapture.errorMessage?.let { msg ->
                if (!state.micCapture.isRecording) {
                    Spacer(Modifier.height(8.dp))
                    Text(msg, style = MaterialTheme.typography.labelSmall, color = PeakAmber)
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onImportAudioClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .minimumInteractiveComponentSize()
                        .testTag("import_audio_button")
                ) {
                    Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("IMPORT", style = MaterialTheme.typography.labelMedium, maxLines = 1)
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
                    Text("REF", style = MaterialTheme.typography.labelMedium, color = SignalWhite, maxLines = 1)
                }

                OutlinedButton(
                    onClick = {
                        if (state.micCapture.isRecording) onStopMicCaptureClick() else onStartMicCaptureClick()
                    },
                    border = BorderStroke(1.dp, if (state.micCapture.isRecording) GlyphCrimson else HairlineBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(0.9f)
                        .minimumInteractiveComponentSize()
                        .testTag("mic_record_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Capture microphone",
                        tint = if (state.micCapture.isRecording) GlyphCrimson else SignalWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("MIC", style = MaterialTheme.typography.labelMedium, color = SignalWhite, maxLines = 1)
                }
            }
        }

        // 3. Local AI Mastering Hub (Primary Intelligent Engine)
        val genreAccent = when (state.selectedGenre) {
            MusicGenre.CLASSICAL, MusicGenre.ACOUSTIC -> BusGreen
            MusicGenre.POP, MusicGenre.ELECTRONIC, MusicGenre.TECHNO -> CyanScope
            MusicGenre.ROCK, MusicGenre.HIP_HOP, MusicGenre.TRAP -> PeakAmber
            MusicGenre.DRILL -> GlyphCrimson
        }

        HardwarePanel(
            title = "AUTONOMOUS AUDIO ENGINE",
            badge = "${if (state.isGenreAutoDetected) "HEURISTIC" else "MANUAL"} // ${state.selectedGenre.title}",
            accentBorder = true,
            modifier = Modifier.testTag("smart_diagnostic_card")
        ) {
            // Active Decision / Guidance Summary
            Surface(
                color = ConsolePanel,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    1.dp,
                    if (state.lastCommandUnsupported) PeakAmber else genreAccent.copy(alpha = 0.45f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (state.lastCommandUnsupported) "COMMAND NOT APPLIED" else diag.recordingTypeLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (state.lastCommandUnsupported) PeakAmber else genreAccent
                        )
                        Text(
                            text = state.tfliteRuntimeMode,
                            style = MaterialTheme.typography.labelSmall,
                            color = PeakAmber
                        )
                    }
                    Text(
                        text = state.aiAppliedSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (state.lastCommandUnsupported) PeakAmber else SignalWhite
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Primary One-Tap Autonomous Mastering Button
            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRunAutoMasterAi()
                },
                enabled = !state.processing.isRunning,
                colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("auto_master_ai_button")
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (state.processing.isRunning) state.processing.currentStage else "APPLY RECOMMENDATIONS (AUTO-CALIBRATE)",
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            AnimatedVisibility(visible = state.processing.isRunning) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { state.processing.progress.coerceIn(0f, 1f) },
                        color = BusGreen,
                        trackColor = ConsolePanel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                    OutlinedButton(
                        onClick = onCancelProcessing,
                        border = BorderStroke(1.dp, PeakAmber),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("cancel_processing_button")
                    ) {
                        Text("CANCEL CALIBRATION", style = MaterialTheme.typography.labelMedium, color = PeakAmber)
                    }
                }
            }

            if (!state.processing.isRunning && (state.processing.isCancelled || state.processing.errorMessage != null)) {
                Spacer(Modifier.height(8.dp))
                val isError = state.processing.errorMessage != null
                val statusAccent = if (isError) GlyphCrimson else PeakAmber
                Surface(
                    color = ConsolePanel,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, statusAccent.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studio_processing_status_banner")
                ) {
                    Text(
                        text = state.processing.errorMessage
                            ?: "CALIBRATION CANCELLED — PREVIOUS MASTER SETTINGS PRESERVED",
                        style = MaterialTheme.typography.labelSmall,
                        color = statusAccent,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Mastering Preset Buttons (Warm Tape, Transparent Punch, Broadcast)
            Text(
                text = "MASTERING PRESETS (EXCITER & LIMITER)",
                style = MaterialTheme.typography.labelSmall,
                color = TechSlate
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("Warm Tape", "8X • -1.5dB", "mastering_preset_warm_tape"),
                    Triple("Transparent Punch", "4X • -1.0dB", "mastering_preset_transparent_punch"),
                    Triple("Broadcast", "4X • -2.0dB", "mastering_preset_broadcast")
                ).forEach { (presetName, badge, tag) ->
                    OutlinedButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onApplyMasteringPreset(presetName)
                        },
                        border = BorderStroke(1.dp, HairlineBorder),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = ConsolePanel),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag(tag)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(presetName, style = MaterialTheme.typography.labelSmall, color = SignalWhite, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(badge, style = MaterialTheme.typography.labelSmall, color = CyanScope, maxLines = 1)
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Offline Natural-Language Command Console (AR / EN Offline Intent)
            Text(
                text = "MASTERING COMMAND CONSOLE (AR / EN)",
                style = MaterialTheme.typography.labelSmall,
                color = TechSlate
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.aiCommandInput,
                    onValueChange = onUpdateAiInput,
                    placeholder = {
                        Text(
                            text = "مثال: باص أقوى، وضوح الغناء، 192kHz لمعان",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TechSlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        onSubmitAiCommand(state.aiCommandInput)
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlyphCrimson,
                        unfocusedBorderColor = HairlineBorder,
                        focusedContainerColor = ConsolePanel,
                        unfocusedContainerColor = ConsolePanel,
                        focusedTextColor = SignalWhite,
                        unfocusedTextColor = SignalWhite,
                        cursorColor = GlyphCrimson
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("local_ai_prompt_input")
                )

                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSubmitAiCommand(state.aiCommandInput)
                    },
                    color = GlyphCrimson,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .size(52.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("local_ai_apply_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Apply AI sound command",
                            tint = SignalWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Quick Bilingual AI Intent Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LocalAiMasteringAgent.quickPresets.forEach { preset ->
                    Surface(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSubmitAiCommand(preset.prompt)
                        },
                        color = ConsolePanel,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, HairlineBorder),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("ai_preset_${preset.id}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = preset.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = SignalWhite
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Concise Acoustic Telemetry Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryStatTile(
                    label = "INTEGRATED",
                    value = String.format(Locale.US, "%.1f LUFS", track.integratedLufs),
                    accentColor = BusGreen,
                    modifier = Modifier.weight(1f)
                )
                TelemetryStatTile(
                    label = "PEAK GUARD",
                    value = String.format(Locale.US, "%.1f dBTP", state.deckParams.ceilingDb),
                    accentColor = GlyphCrimson,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryStatTile(
                    label = "HF EXCITER",
                    value = String.format(Locale.US, "%.1fkHz -> %dk", diag.estimatedBandwidthKhz, state.hiResTargetKhz),
                    accentColor = CyanScope,
                    modifier = Modifier.weight(1.1f)
                )
                TelemetryStatTile(
                    label = "3-BAND ISP",
                    value = "${state.oversamplingFactor.factor}X // ${track.stereoWidthPct}% W",
                    accentColor = PeakAmber,
                    modifier = Modifier.weight(0.9f)
                )
            }

            Spacer(Modifier.height(10.dp))
            AnalysisTelemetrySection(
                report = diag.analysisReport,
                accentColor = genreAccent
            )

            Spacer(Modifier.height(10.dp))
            DecisionExplainabilitySection(
                classificationSourceLabel = state.classificationSourceLabel,
                targetStandard = state.decisionTargetStandard,
                safetyWarnings = state.decisionSafetyWarnings,
                stages = state.decisionStages
            )
        }

        // 4. Interactive Spectral Response Curve
        InteractiveSpectralCurve(
            genre = state.selectedGenre,
            bandwidthKhz = diag.estimatedBandwidthKhz
        )

        // 5. Optional Pro Manual Tuning Drawer Button
        OutlinedButton(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onSetProSheetOpen(true)
            },
            border = BorderStroke(1.dp, HairlineBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("open_pro_sheet_button")
        ) {
            Icon(Icons.Filled.Tune, contentDescription = null, tint = GlyphCrimson, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = "PRO MANUAL TUNING (EQ • DECK • BAND MIXER • DIAGNOSTICS)",
                style = MaterialTheme.typography.labelMedium,
                color = SignalWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    // Optional Collapsible Pro Manual Tuning Sheet
    if (state.isProSheetOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { onSetProSheetOpen(false) },
            sheetState = sheetState,
            containerColor = HardwareSurface,
            contentColor = SignalWhite,
            modifier = Modifier.testTag("pro_manual_tuning_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("PRO MANUAL TUNING", style = MaterialTheme.typography.titleMedium, color = SignalWhite)
                        Text("64-BIT DOUBLE PRECISION OVERRIDE", style = MaterialTheme.typography.labelSmall, color = TechSlate)
                    }
                    IconButton(
                        onClick = { onSetProSheetOpen(false) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("close_pro_sheet_button")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close manual tuning sheet", tint = SignalWhite)
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProTuningTab.entries.forEach { tab ->
                        val selected = state.proTuningTab == tab
                        Surface(
                            onClick = { onSelectProTuningTab(tab) },
                            color = if (selected) GlyphCrimson else ConsolePanel,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (selected) GlyphCrimson else HairlineBorder),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("pro_tab_${tab.name.lowercase()}")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                Text(tab.label, style = MaterialTheme.typography.labelSmall, color = SignalWhite)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 460.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (state.proTuningTab) {
                        ProTuningTab.EQ -> ProSheetEqContent(state, onUpdateBand, onResetEq)
                        ProTuningTab.DECK -> ProSheetDeckContent(state, onUpdateDeck, onSelectProfile, onSelectGenre)
                        ProTuningTab.STEMS -> ProSheetStemsContent(state, onUpdateStem, onResetStems)
                        ProTuningTab.ENGINE -> ProSheetEngineContent(
                            state = state,
                            onToggleDspHook = onToggleDspHook,
                            onSetOversampling = onSetOversampling,
                            onToggleHiResKhz = onToggleHiResKhz,
                            onSelectGlyphPattern = onSelectGlyphPattern
                        )
                        ProTuningTab.DIAGNOSTICS -> ProSheetDiagnosticsContent(state)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProSheetEqContent(
    state: WaviqUiState,
    onUpdateBand: (String, Float) -> Unit,
    onResetEq: () -> Unit
) {
    val bands = state.eqBands
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ObsidianVoid)
            .border(1.dp, HairlineBorder, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val midY = size.height / 2f
            drawLine(color = MutedGrid, start = Offset(0f, midY), end = Offset(size.width, midY), strokeWidth = 1.5f)
            val filters = bands.mapIndexed { idx, band ->
                BiquadFilter(48000.0).apply {
                    val type = when (idx) {
                        0 -> FilterType.LOW_SHELF
                        4 -> FilterType.HIGH_SHELF
                        else -> FilterType.PEAKING
                    }
                    configure(type, band.frequencyHz, band.qFactor, band.gainDb, 48000f)
                }
            }
            val minLog = ln(20f)
            val maxLog = ln(20000f)
            val path = Path()
            for (i in 0 until 72) {
                val frac = i / 71f
                val freq = kotlin.math.exp(minLog + frac * (maxLog - minLog))
                var totalDb = 0f
                for (f in filters) totalDb += f.magnitudeDbAt(freq)
                val x = frac * size.width
                val y = midY - (totalDb.coerceIn(-14f, 14f) / 14f) * (size.height * 0.44f)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path = path, color = GlyphCrimson, style = Stroke(width = 3f))
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("5-BAND 64-BIT BIQUAD EQ", style = MaterialTheme.typography.labelSmall, color = TechSlate)
        OutlinedButton(
            onClick = onResetEq,
            border = BorderStroke(1.dp, HairlineBorder),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("eq_reset_button")
        ) {
            Text("FLAT", style = MaterialTheme.typography.labelSmall, color = SignalWhite)
        }
    }

    bands.forEach { band ->
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${band.label} // ${band.frequencyHz.toInt()} Hz", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                Text(String.format(Locale.US, "%+.1f dB", band.gainDb), style = MaterialTheme.typography.labelMedium, color = GlyphCrimson)
            }
            Slider(
                value = band.gainDb,
                onValueChange = { onUpdateBand(band.id, it) },
                valueRange = -12f..12f,
                colors = SliderDefaults.colors(thumbColor = SignalWhite, activeTrackColor = GlyphCrimson, inactiveTrackColor = HairlineBorder),
                modifier = Modifier.testTag("eq_slider_${band.id}")
            )
        }
    }
}

@Composable
private fun ProSheetDeckContent(
    state: WaviqUiState,
    onUpdateDeck: ((TactileDeckParams) -> TactileDeckParams) -> Unit,
    onSelectProfile: (EnhanceProfile) -> Unit,
    onSelectGenre: (MusicGenre) -> Unit
) {
    val d = state.deckParams
    Text("GENRE & TARGET PROFILE", style = MaterialTheme.typography.labelSmall, color = TechSlate)
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MusicGenre.entries.forEach { genre ->
            val selected = genre == state.selectedGenre
            Surface(
                onClick = { onSelectGenre(genre) },
                color = if (selected) GlyphCrimsonMuted else ConsolePanel,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (selected) GlyphCrimson else HairlineBorder),
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Text(genre.title, style = MaterialTheme.typography.labelSmall, color = SignalWhite, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp))
            }
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EnhanceProfile.entries.forEach { profile ->
            val selected = profile == state.selectedProfile
            Surface(
                onClick = { onSelectProfile(profile) },
                color = if (selected) GlyphCrimsonMuted else ConsolePanel,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (selected) GlyphCrimson else HairlineBorder),
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Text(profile.title, style = MaterialTheme.typography.labelSmall, color = SignalWhite, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp))
            }
        }
    }

    TactileRotarySlider("HARMONIC DRIVE", d.drive, "${(d.drive * 100).toInt()}%", { v -> onUpdateDeck { it.copy(drive = v) } }, "deck_drive_slider")
    TactileRotarySlider("STEREO WIDTH (M/S)", d.width, "${(d.width * 150).toInt()}%", { v -> onUpdateDeck { it.copy(width = v) } }, "deck_width_slider")
    TactileRotarySlider("TRANSIENT PUNCH", d.punch, "${(d.punch * 100).toInt()}%", { v -> onUpdateDeck { it.copy(punch = v) } }, "deck_punch_slider")
    TactileRotarySlider("AIR EXCITER (10kHz)", d.air, String.format(Locale.US, "%+.1f dB", (d.air - 0.35f) * 9f), { v -> onUpdateDeck { it.copy(air = v) } }, "deck_air_slider")
    TactileRotarySlider("LIMITER CEILING", d.ceilingDb, String.format(Locale.US, "%.1f dBTP", d.ceilingDb), { v -> onUpdateDeck { it.copy(ceilingDb = v) } }, "deck_ceiling_slider", valueRange = -3.0f..-0.1f)
}

@Composable
private fun ProSheetStemsContent(
    state: WaviqUiState,
    onUpdateStem: (String, Float?, Boolean, Boolean) -> Unit,
    onResetStems: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("FREQUENCY-BAND MIXER (CROSSOVER)", style = MaterialTheme.typography.labelSmall, color = TechSlate)
        OutlinedButton(
            onClick = onResetStems,
            border = BorderStroke(1.dp, HairlineBorder),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.minimumInteractiveComponentSize().testTag("stems_reset_button")
        ) {
            Text("UNITY", style = MaterialTheme.typography.labelSmall, color = SignalWhite)
        }
    }

    state.stemMixer.asList().forEach { stem ->
        Surface(color = ConsolePanel, shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, if (stem.soloed) GlyphCrimson else HairlineBorder)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stem.label, style = MaterialTheme.typography.labelLarge, color = SignalWhite)
                        Text(stem.bandDescription, style = MaterialTheme.typography.labelSmall, color = TechSlate)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            onClick = { onUpdateStem(stem.id, null, false, true) },
                            color = if (stem.soloed) PeakAmber else Color(0xFF28282C),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("stem_solo_${stem.id}")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text("S", style = MaterialTheme.typography.labelMedium, color = if (stem.soloed) Color.Black else SignalWhite)
                            }
                        }
                        Surface(
                            onClick = { onUpdateStem(stem.id, null, true, false) },
                            color = if (stem.muted) GlyphCrimson else Color(0xFF28282C),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("stem_mute_${stem.id}")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text("M", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                            }
                        }
                    }
                }
                Slider(
                    value = stem.level,
                    onValueChange = { onUpdateStem(stem.id, it, false, false) },
                    valueRange = 0f..1.5f,
                    colors = SliderDefaults.colors(thumbColor = SignalWhite, activeTrackColor = if (stem.muted) MutedGrid else GlyphCrimson, inactiveTrackColor = HairlineBorder),
                    modifier = Modifier.testTag("stem_slider_${stem.id}")
                )
            }
        }
    }
}

@Composable
private fun ProSheetEngineContent(
    state: WaviqUiState,
    onToggleDspHook: (String, Boolean) -> Unit,
    onSetOversampling: (OversamplingFactor) -> Unit,
    onToggleHiResKhz: () -> Unit,
    onSelectGlyphPattern: (GlyphPatternMode) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OversamplingFactor.entries.forEach { os ->
            val selected = state.oversamplingFactor == os
            Surface(
                onClick = { onSetOversampling(os) },
                color = if (selected) GlyphCrimson else ConsolePanel,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (selected) GlyphCrimson else HairlineBorder),
                modifier = Modifier.weight(1f).minimumInteractiveComponentSize().testTag("oversampling_${os.factor}x_button")
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(10.dp)) {
                    Text("${os.factor}X ISP", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                }
            }
        }
        Surface(
            onClick = onToggleHiResKhz,
            color = ConsolePanel,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, BusGreen),
            modifier = Modifier.weight(1f).minimumInteractiveComponentSize().testTag("hires_khz_toggle_button")
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(10.dp)) {
                Text("${state.hiResTargetKhz}KHZ", style = MaterialTheme.typography.labelMedium, color = BusGreen)
            }
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TelemetryStatTile("LOW GR", String.format(Locale.US, "-%.1f dB", state.telemetry.lowBandGrDb), Modifier.weight(1f), GlyphCrimson)
        TelemetryStatTile("MID GR", String.format(Locale.US, "-%.1f dB", state.telemetry.midBandGrDb), Modifier.weight(1f), PeakAmber)
        TelemetryStatTile("HIGH GR", String.format(Locale.US, "-%.1f dB", state.telemetry.highBandGrDb), Modifier.weight(1f), CyanScope)
    }

    Text("64-BIT DSP PIPELINE HOOKS", style = MaterialTheme.typography.labelSmall, color = TechSlate)
    state.dspHooks.forEach { hook ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(hook.name, style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                Text("id: ${hook.id}", style = MaterialTheme.typography.labelSmall, color = TechSlate)
            }
            Switch(
                checked = hook.isEnabled,
                onCheckedChange = { onToggleDspHook(hook.id, it) },
                colors = SwitchDefaults.colors(checkedThumbColor = SignalWhite, checkedTrackColor = GlyphCrimson),
                modifier = Modifier.testTag("dsp_hook_switch_${hook.id}")
            )
        }
    }

    Text("NOTHING GLYPH VU PATTERN", style = MaterialTheme.typography.labelSmall, color = TechSlate)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        GlyphPatternMode.entries.forEach { mode ->
            val selected = state.glyphPatternMode == mode
            Surface(
                onClick = { onSelectGlyphPattern(mode) },
                color = if (selected) GlyphCrimson else ConsolePanel,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, if (selected) GlyphCrimson else HairlineBorder),
                modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                    Text(mode.label, style = MaterialTheme.typography.labelSmall, color = SignalWhite, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun ProSheetDiagnosticsContent(state: WaviqUiState) {
    val context = LocalContext.current
    val report = state.trackInfo.diagnostic.analysisReport
    val models = OnDeviceModelRegistry.getRegisteredModels(context)

    Text("BS.1770 LOUDNESS & PEAK MATRIX", style = MaterialTheme.typography.labelSmall, color = TechSlate)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TelemetryStatTile(
            label = "INTEGRATED",
            value = String.format(Locale.US, "%.1f LUFS", report.integratedLufs),
            accentColor = BusGreen,
            modifier = Modifier.weight(1f)
        )
        TelemetryStatTile(
            label = "LRA",
            value = String.format(Locale.US, "%.1f LU", report.loudnessRangeLu),
            accentColor = CyanScope,
            modifier = Modifier.weight(1f)
        )
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TelemetryStatTile(
            label = "MOMENTARY MAX",
            value = String.format(Locale.US, "%.1f LUFS", report.momentaryMaxLufs),
            accentColor = SignalWhite,
            modifier = Modifier.weight(1f)
        )
        TelemetryStatTile(
            label = "SHORT-TERM MAX",
            value = String.format(Locale.US, "%.1f LUFS", report.shortTermMaxLufs),
            accentColor = SignalWhite,
            modifier = Modifier.weight(1f)
        )
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TelemetryStatTile(
            label = "SAMPLE PEAK",
            value = String.format(Locale.US, "%.1f dBFS", report.samplePeakDb),
            accentColor = PeakAmber,
            modifier = Modifier.weight(1f)
        )
        TelemetryStatTile(
            label = "TRUE PEAK (4X)",
            value = String.format(Locale.US, "%.1f dBTP", report.truePeakDb),
            accentColor = if (report.truePeakDb > -1.0f) GlyphCrimson else BusGreen,
            modifier = Modifier.weight(1f)
        )
    }

    Text("STARTUP GUARD & RUNTIME PROVENANCE", style = MaterialTheme.typography.labelSmall, color = TechSlate)
    Surface(
        color = ConsolePanel,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, HairlineBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("HEALTH: ${state.startupGuard.healthState.label}", style = MaterialTheme.typography.labelMedium, color = BusGreen)
                Text(state.classificationSourceLabel, style = MaterialTheme.typography.labelSmall, color = PeakAmber)
            }
            listOf(
                state.startupGuard.tfliteCheck,
                state.startupGuard.databaseCheck,
                state.startupGuard.permissionCheck
            ).forEach { check ->
                Text("${check.title}: ${check.status.label} — ${check.detail}", style = MaterialTheme.typography.labelSmall, color = TechSlate)
            }
        }
    }

    Text("ON-DEVICE MODEL REGISTRY", style = MaterialTheme.typography.labelSmall, color = TechSlate)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pro_diagnostics_model_registry"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        models.forEach { spec ->
            val verifiedColor = if (spec.isBundledAndVerified) BusGreen else PeakAmber
            Surface(
                color = ConsolePanel,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, verifiedColor.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(spec.title, style = MaterialTheme.typography.labelMedium, color = SignalWhite, modifier = Modifier.weight(1f))
                        Text(
                            text = if (spec.isBundledAndVerified) "BUNDLED TFLITE" else "HEURISTIC MODE",
                            style = MaterialTheme.typography.labelSmall,
                            color = verifiedColor
                        )
                    }
                    Text("Asset: ${spec.assetName} • ${spec.architecture}", style = MaterialTheme.typography.labelSmall, color = TechSlate)
                    Text("Provenance: ${spec.licenseAndProvenance}", style = MaterialTheme.typography.labelSmall, color = TechSlate)
                    Text("Limit: ${spec.knownLimitations}", style = MaterialTheme.typography.labelSmall, color = PeakAmber)
                }
            }
        }
    }
}

