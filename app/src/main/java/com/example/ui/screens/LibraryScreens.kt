package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.MasterTrackEntity
import com.example.model.ExportFormat
import com.example.model.WaviqUiState
import com.example.ui.components.HardwarePanel
import com.example.ui.components.TelemetryStatTile
import com.example.ui.components.WaveformScrubBar
import com.example.ui.components.formatMs
import com.example.ui.theme.BusGreen
import com.example.ui.theme.ConsolePanel
import com.example.ui.theme.GlyphCrimson
import com.example.ui.theme.GlyphCrimsonMuted
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.SignalWhite
import com.example.ui.theme.TechSlate
import java.util.Locale

@Composable
fun Screen13TranscodeMatrix(
    state: WaviqUiState,
    onUpdateConfig: (ExportFormat?, Boolean?, Boolean?) -> Unit,
    onStartExport: () -> Unit
) {
    val cfg = state.transcodeConfig
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#13 // TRANSCODE & ENCODING MATRIX",
            badge = "OFFLINE PCM RENDER",
            accentBorder = true
        ) {
            ExportFormat.entries.forEach { fmt ->
                val selected = cfg.format == fmt
                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onUpdateConfig(fmt, null, null)
                    },
                    color = if (selected) GlyphCrimsonMuted else ConsolePanel,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (selected) GlyphCrimson else HairlineBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("export_format_${fmt.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(fmt.label, style = MaterialTheme.typography.labelLarge, color = SignalWhite)
                            Text(fmt.codecDesc, style = MaterialTheme.typography.bodyMedium, color = TechSlate)
                        }
                        Text(
                            text = if (fmt.bitDepth == 32) "32F // 192K" else "${fmt.bitDepth}-BIT",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) SignalWhite else TechSlate
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("TPDF SHAPED DITHER", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                    Text("Triangular noise-shaping for 16-bit & 24-bit PCM masters", style = MaterialTheme.typography.bodyMedium, color = TechSlate)
                }
                Switch(
                    checked = cfg.ditherEnabled,
                    onCheckedChange = { onUpdateConfig(null, it, null) },
                    colors = SwitchDefaults.colors(checkedTrackColor = GlyphCrimson, checkedThumbColor = SignalWhite),
                    modifier = Modifier.testTag("transcode_dither_switch")
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("NORMALIZE CEILING (-1.0 dBTP)", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                    Text("True-peak compliant brickwall seal", style = MaterialTheme.typography.bodyMedium, color = TechSlate)
                }
                Switch(
                    checked = cfg.normalizeToTarget,
                    onCheckedChange = { onUpdateConfig(null, null, it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = GlyphCrimson, checkedThumbColor = SignalWhite),
                    modifier = Modifier.testTag("transcode_normalize_switch")
                )
            }
        }

        Button(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onStartExport()
            },
            colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("start_export_button")
        ) {
            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("RENDER & EXPORT MASTER (#14)", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun Screen14ExportTelemetryModal(
    state: WaviqUiState,
    onStartOrRetryExport: () -> Unit,
    onGoToDone: () -> Unit
) {
    val job = state.exportJob
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#14 // EXPORT TELEMETRY MODAL",
            badge = if (job.isExporting) "WAV_WRITE_IO" else if (job.progress >= 0.99f) "100% SEALED" else "READY",
            accentBorder = true
        ) {
            Text(
                text = job.stageLabel,
                style = MaterialTheme.typography.labelLarge,
                color = if (job.errorMessage != null) GlyphCrimson else SignalWhite
            )
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { job.progress.coerceIn(0f, 1f) },
                color = if (job.progress >= 0.99f) BusGreen else GlyphCrimson,
                trackColor = ConsolePanel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
            )
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryStatTile(
                    label = "PROGRESS",
                    value = "${(job.progress * 100).toInt()}%",
                    accentColor = GlyphCrimson,
                    modifier = Modifier.weight(1f)
                )
                TelemetryStatTile(
                    label = "BYTES WRITTEN",
                    value = "${job.bytesWritten / 1024} KB",
                    modifier = Modifier.weight(1f)
                )
                TelemetryStatTile(
                    label = "PEAK RAM",
                    value = "162 / 180 MB",
                    accentColor = BusGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            job.errorMessage?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = GlyphCrimson)
            }
        }

        if (!job.isExporting) {
            Button(
                onClick = if (job.progress >= 0.99f) onGoToDone else onStartOrRetryExport,
                colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("export_modal_action_button")
            ) {
                Text(
                    if (job.progress >= 0.99f) "VIEW #15 MASTER DONE 100%" else "DISPATCH WAV EXPORT",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun Screen15MasterDone(
    state: WaviqUiState,
    onSaveToDeviceClick: () -> Unit,
    onOpenPlayer: () -> Unit,
    onGoToInspector: () -> Unit
) {
    val job = state.exportJob
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#15 // MASTER DONE 100%",
            badge = "RIFF WAV VERIFIED",
            accentBorder = true
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Master verified",
                    tint = BusGreen,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = job.lastExportedTitle ?: "NO RECENT EXPORT — RUN #13",
                        style = MaterialTheme.typography.titleMedium,
                        color = SignalWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = job.lastExportedFormat ?: state.transcodeConfig.format.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TechSlate
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryStatTile(
                    label = "FINAL LUFS",
                    value = String.format(Locale.US, "%.1f LUFS", job.lastFinalLufs),
                    accentColor = BusGreen,
                    modifier = Modifier.weight(1f)
                )
                TelemetryStatTile(
                    label = "FINAL PEAK",
                    value = String.format(Locale.US, "%.1f dBTP", job.lastFinalPeakDb),
                    accentColor = SignalWhite,
                    modifier = Modifier.weight(1f)
                )
                TelemetryStatTile(
                    label = "SIZE",
                    value = "${(job.bytesWritten / 1024).coerceAtLeast(1)} KB",
                    modifier = Modifier.weight(1f)
                )
            }

            if (job.lastExportedPath != null) {
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onSaveToDeviceClick,
                    border = BorderStroke(1.dp, HairlineBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("save_master_to_device_button")
                ) {
                    Icon(Icons.Filled.SaveAlt, contentDescription = null, tint = SignalWhite, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("SAVE WAV TO DEVICE STORAGE", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                }
            }
        }

        Button(
            onClick = onOpenPlayer,
            colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("open_master_player_button")
        ) {
            Text("OPEN #16 MASTER AUDIO PLAYER", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }

        OutlinedButton(
            onClick = onGoToInspector,
            border = BorderStroke(1.dp, HairlineBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("loop_back_inspector_button")
        ) {
            Text("LOOP BACK: #01 NEW TRACK INSPECTOR", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
        }
    }
}

@Composable
fun Screen16MasterPlayer(
    state: WaviqUiState,
    savedMasters: List<MasterTrackEntity>,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleAb: (Boolean) -> Unit,
    onLoadSavedMaster: (MasterTrackEntity) -> Unit,
    onDeleteSavedMaster: (Int) -> Unit,
    onGoToTranscode: () -> Unit
) {
    val tel = state.telemetry
    val progress = if (tel.durationMs > 0L) (tel.positionMs.toFloat() / tel.durationMs).coerceIn(0f, 1f) else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#16 // MASTER AUDIO PLAYER",
            badge = "OFFLINE ROOM STORAGE",
            accentBorder = true
        ) {
            Text(
                text = state.trackInfo.title,
                style = MaterialTheme.typography.titleMedium,
                color = SignalWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${state.trackInfo.sourceLabel}  •  ${formatMs(tel.positionMs)} / ${formatMs(tel.durationMs)}",
                style = MaterialTheme.typography.labelSmall,
                color = TechSlate
            )

            Spacer(Modifier.height(12.dp))
            WaveformScrubBar(
                peaks = state.trackInfo.waveformPeaks,
                progressFraction = progress,
                onSeek = onSeek
            )

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onTogglePlay,
                    colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("player_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (tel.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (tel.isPlaying) "PAUSE" else "PLAY", style = MaterialTheme.typography.labelLarge)
                }

                OutlinedButton(
                    onClick = { onToggleAb(!state.abEnhancedEngaged) },
                    border = BorderStroke(1.dp, if (state.abEnhancedEngaged) BusGreen else HairlineBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("player_ab_toggle_button")
                ) {
                    Text(
                        text = if (state.abEnhancedEngaged) "DSP: ENHANCED" else "DSP: ORIGINAL",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (state.abEnhancedEngaged) BusGreen else SignalWhite
                    )
                }
            }
        }

        HardwarePanel(
            title = "OFFLINE MASTER LIBRARY (${savedMasters.size})",
            badge = "ROOM SQLITE"
        ) {
            if (savedMasters.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.LibraryMusic,
                        contentDescription = null,
                        tint = TechSlate,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "No mastered WAV exports saved yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SignalWhite
                    )
                    Text(
                        text = "Render a master in #13 Transcode to store and replay it offline.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TechSlate
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onGoToTranscode,
                        colors = ButtonDefaults.buttonColors(containerColor = ConsolePanel, contentColor = SignalWhite),
                        border = BorderStroke(1.dp, HairlineBorder),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("empty_library_transcode_button")
                    ) {
                        Text("GO TO #13 TRANSCODE", style = MaterialTheme.typography.labelMedium)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    savedMasters.forEach { master ->
                        val active = state.activePlayerTrackId == master.id
                        Surface(
                            onClick = { onLoadSavedMaster(master) },
                            color = if (active) GlyphCrimsonMuted else ConsolePanel,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (active) GlyphCrimson else HairlineBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("saved_master_card_${master.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Play master",
                                    tint = if (active) SignalWhite else GlyphCrimson
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        master.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = SignalWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        String.format(
                                            Locale.US,
                                            "%s  •  %.1f LUFS  •  %.1f dBTP",
                                            master.formatLabel,
                                            master.finalLufs,
                                            master.finalPeakDb
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TechSlate
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteSavedMaster(master.id) },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("delete_master_${master.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = "Delete master",
                                        tint = TechSlate
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
