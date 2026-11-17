package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
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
import com.example.ui.theme.PeakAmber
import com.example.ui.theme.SignalWhite
import com.example.ui.theme.TechSlate
import java.util.Locale

@Composable
fun UnifiedLibraryScreen(
    state: WaviqUiState,
    savedMasters: List<MasterTrackEntity>,
    onUpdateConfig: (ExportFormat?, Boolean?, Boolean?) -> Unit,
    onStartExport: () -> Unit,
    onSaveToDeviceClick: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleAb: (Boolean) -> Unit,
    onLoadSavedMaster: (MasterTrackEntity) -> Unit,
    onDeleteSavedMaster: (Int) -> Unit,
    onCancelExport: () -> Unit = {},
    onGoToStudioClick: () -> Unit = {},
    scrollState: ScrollState = rememberScrollState()
) {
    val cfg = state.transcodeConfig
    val job = state.exportJob
    val tel = state.telemetry
    val haptics = LocalHapticFeedback.current
    val progress = if (tel.durationMs > 0L) (tel.positionMs.toFloat() / tel.durationMs).coerceIn(0f, 1f) else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Hi-Res Render & Export Card
        val matrixBadge = when {
            job.isExporting -> "RENDERING"
            job.isCancelled -> "CANCELLED"
            job.errorMessage != null -> "RENDER ERROR"
            job.progress >= 0.99f && job.lastExportedPath != null -> "100% READY"
            else -> "64-BIT OFFLINE BUS"
        }
        HardwarePanel(
            title = "EXPORT & HI-RES MASTER MATRIX",
            badge = matrixBadge,
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
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (fmt.bitDepth == 32) "32F // 192K" else "${fmt.bitDepth}-BIT",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) SignalWhite else TechSlate
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            val isFloat32Format = cfg.format.bitDepth == 32
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("TPDF SHAPED DITHER", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                    Text(
                        text = if (isFloat32Format) {
                            "Bypassed automatically for 32-bit IEEE Float exports"
                        } else {
                            "Triangular dither for 16/24-bit integer PCM exports"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TechSlate
                    )
                }
                Switch(
                    checked = cfg.ditherEnabled && !isFloat32Format,
                    enabled = !isFloat32Format,
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
                    Text(
                        text = String.format(Locale.US, "NORMALIZE CEILING (%.1f dBTP)", cfg.targetTruePeakDb),
                        style = MaterialTheme.typography.labelMedium,
                        color = SignalWhite
                    )
                    Text("3-band True-Peak ISP compliant limiting", style = MaterialTheme.typography.bodyMedium, color = TechSlate)
                }
                Switch(
                    checked = cfg.normalizeToTarget,
                    onCheckedChange = { onUpdateConfig(null, null, it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = GlyphCrimson, checkedThumbColor = SignalWhite),
                    modifier = Modifier.testTag("transcode_normalize_switch")
                )
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onStartExport()
                },
                enabled = !job.isExporting,
                colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("start_export_button")
            ) {
                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (job.isExporting) "${job.stageLabel} (${(job.progress * 100).toInt()}%)" else "RENDER & EXPORT MASTER",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            AnimatedVisibility(visible = job.isExporting) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { job.progress.coerceIn(0f, 1f) },
                        color = GlyphCrimson,
                        trackColor = ConsolePanel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    OutlinedButton(
                        onClick = onCancelExport,
                        border = BorderStroke(1.dp, PeakAmber),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("cancel_export_button")
                    ) {
                        Text("CANCEL RENDER", style = MaterialTheme.typography.labelMedium, color = PeakAmber)
                    }
                }
            }

            if (!job.isExporting && job.isCancelled) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = ConsolePanel,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, PeakAmber),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("library_export_cancelled_banner")
                ) {
                    Text(
                        text = "Render cancelled — incomplete output discarded cleanly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PeakAmber,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }

            job.errorMessage?.let { err ->
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = ConsolePanel,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GlyphCrimson),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("library_export_error_banner")
                ) {
                    Text(
                        text = err,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlyphCrimson,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }

            if (!job.isExporting && job.lastExportedPath != null) {
                Spacer(Modifier.height(14.dp))
                Surface(
                    color = ConsolePanel,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BusGreen.copy(alpha = 0.6f)),
                    modifier = Modifier.testTag("library_export_completed_card")
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Master export ready", tint = BusGreen, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = job.lastExportedTitle ?: "MASTER.wav",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SignalWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = job.lastExportedFormat ?: cfg.format.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TechSlate
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TelemetryStatTile("LUFS", String.format(Locale.US, "%.1f", job.lastFinalLufs), Modifier.weight(1f), BusGreen)
                            TelemetryStatTile("PEAK", String.format(Locale.US, "%.1f dBTP", job.lastFinalPeakDb), Modifier.weight(1f), SignalWhite)
                            TelemetryStatTile("SIZE", "${(job.bytesWritten / 1024).coerceAtLeast(1)} KB", Modifier.weight(1f))
                        }

                        OutlinedButton(
                            onClick = onSaveToDeviceClick,
                            border = BorderStroke(1.dp, BusGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("save_master_to_device_button")
                        ) {
                            Icon(Icons.Filled.SaveAlt, contentDescription = null, tint = SignalWhite, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("SAVE MASTER WAV TO DEVICE", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                        }
                    }
                }
            }
        }

        // 2. Active Player + Room SQLite Library Card
        HardwarePanel(
            title = "SAVED MASTERS & OFFLINE PLAYER (${savedMasters.size})",
            badge = "ROOM SQLITE"
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

            Spacer(Modifier.height(10.dp))
            WaveformScrubBar(
                peaks = state.trackInfo.waveformPeaks,
                progressFraction = progress,
                onSeek = onSeek
            )

            Spacer(Modifier.height(10.dp))
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

            Spacer(Modifier.height(14.dp))

            if (savedMasters.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .testTag("library_empty_state"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.LibraryMusic, contentDescription = null, tint = TechSlate, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "No mastered WAV exports saved yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SignalWhite
                    )
                    Text(
                        text = "Tap RENDER & EXPORT MASTER above to save and replay offline.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TechSlate
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onGoToStudioClick,
                        border = BorderStroke(1.dp, HairlineBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("library_empty_back_to_studio_button")
                    ) {
                        Text("OPEN STUDIO", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
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
