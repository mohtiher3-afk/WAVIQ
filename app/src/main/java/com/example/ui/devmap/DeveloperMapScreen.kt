package com.example.ui.devmap

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioTelemetry
import com.example.model.DevMapSchemas
import com.example.model.StartupCheckStatus
import com.example.model.StartupGuardReport
import com.example.model.StartupHealthState
import com.example.model.WaviqRouteRegistry
import com.example.model.WaviqRouteSpec
import com.example.model.WaviqTab
import com.example.model.WaviqUiState
import com.example.ui.components.HardwarePanel
import com.example.ui.components.TelemetryStatTile
import com.example.ui.theme.BusGreen
import com.example.ui.theme.ConsolePanel
import com.example.ui.theme.CyanScope
import com.example.ui.theme.GlyphCrimson
import com.example.ui.theme.GlyphCrimsonMuted
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.PeakAmber
import com.example.ui.theme.SignalWhite
import com.example.ui.theme.TechSlate
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun DeveloperMapScreen(
    state: WaviqUiState,
    onSelectFilter: (WaviqTab?) -> Unit,
    onInspectRoute: (Int) -> Unit,
    onSimulateDispatch: (Int, Boolean) -> Unit,
    onRerunStartupGuard: () -> Unit = {}
) {
    val inspected = WaviqRouteRegistry.byId(state.inspectedRouteId)
    val filteredRoutes = remember(state.devMapFilter) {
        val f = state.devMapFilter
        if (f == null) WaviqRouteRegistry.allRoutes else WaviqRouteRegistry.byTab(f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Dispatch telemetry banner
        Surface(
            color = ConsolePanel,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, GlyphCrimson)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "WAVIQ (1) // DEVELOPER NAVIGATION MAP",
                        style = MaterialTheme.typography.labelLarge,
                        color = SignalWhite
                    )
                    Text(
                        state.lastDispatchedLog,
                        style = MaterialTheme.typography.labelSmall,
                        color = BusGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Module Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipButton(
                label = "ALL (16)",
                selected = state.devMapFilter == null,
                onClick = { onSelectFilter(null) },
                testTag = "devmap_filter_all"
            )
            WaviqTab.entries.forEach { tab ->
                val count = WaviqRouteRegistry.byTab(tab).size
                FilterChipButton(
                    label = "${tab.indexLabel}. ${tab.title} ($count)",
                    selected = state.devMapFilter == tab,
                    onClick = { onSelectFilter(tab) },
                    testTag = "devmap_filter_${tab.name.lowercase()}"
                )
            }
        }

        // 4x4 Screen Architecture Matrix
        HardwarePanel(
            title = "4×4 SCREEN ARCHITECTURE MATRIX",
            badge = "TAP NODE TO AUDIT"
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val columns = if (maxWidth >= 600.dp) 4 else 2
                val rows = filteredRoutes.chunked(columns)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { spec ->
                                MatrixNodeCard(
                                    spec = spec,
                                    isInspected = spec.id == inspected.id,
                                    isActiveLive = spec.id == state.activeRouteId,
                                    onClick = { onInspectRoute(spec.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(columns - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // StartupGuard Pre-Flight Diagnostics
        StartupGuardDiagnosticsPanel(
            report = state.startupGuard,
            onRerunStartupGuard = onRerunStartupGuard
        )

        // Live Interactive Route Inspector
        LiveRouteInspectorPanel(
            spec = inspected,
            telemetry = state.telemetry,
            onSimulateDispatch = { jump -> onSimulateDispatch(inspected.id, jump) }
        )

        // Global 16-Screen State Machine Flow ASCII Diagram
        HardwarePanel(
            title = "GLOBAL 16-SCREEN STATE MACHINE FLOW",
            badge = "BIDIRECTIONAL LOOP"
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF060607))
                    .border(1.dp, HairlineBorder, RoundedCornerShape(8.dp))
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = DevMapSchemas.GLOBAL_STATE_MACHINE_ASCII.trimIndent(),
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    color = SignalWhite
                )
            }
        }
    }
}

@Composable
private fun FilterChipButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        color = if (selected) GlyphCrimson else ConsolePanel,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (selected) GlyphCrimson else HairlineBorder),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = SignalWhite)
        }
    }
}

@Composable
private fun MatrixNodeCard(
    spec: WaviqRouteSpec,
    isInspected: Boolean,
    isActiveLive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    Surface(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        color = if (isInspected) GlyphCrimsonMuted else ConsolePanel,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = if (isInspected) 1.5.dp else 1.dp,
            color = when {
                isInspected -> GlyphCrimson
                isActiveLive -> BusGreen
                else -> HairlineBorder
            }
        ),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag("devmap_node_${spec.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = spec.code,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isInspected) SignalWhite else GlyphCrimson,
                    fontWeight = FontWeight.Bold
                )
                if (isActiveLive) {
                    Text("LIVE", style = MaterialTheme.typography.labelSmall, color = BusGreen)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = spec.title,
                style = MaterialTheme.typography.labelMedium,
                color = SignalWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = spec.path,
                style = MaterialTheme.typography.labelSmall,
                color = TechSlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LiveRouteInspectorPanel(
    spec: WaviqRouteSpec,
    telemetry: AudioTelemetry,
    onSimulateDispatch: (jumpToScreen: Boolean) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    var copiedRecently by remember(spec.id) { mutableStateOf(false) }

    LaunchedEffect(copiedRecently) {
        if (copiedRecently) {
            delay(1800)
            copiedRecently = false
        }
    }

    val effLatencyMs = if (telemetry.isBypassed) 0f else telemetry.dspLatencyMs
    val latencyAccent = when {
        telemetry.isBypassed -> TechSlate
        effLatencyMs > 15.0f -> GlyphCrimson
        effLatencyMs > 8.0f -> PeakAmber
        else -> BusGreen
    }

    HardwarePanel(
        title = "ROUTE INSPECTOR // ${spec.code} ${spec.title.uppercase()}",
        badge = spec.tab.title,
        accentBorder = true
    ) {
        // 1. Audio Core Bus State
        Text("AUDIO CORE BUS STATE", style = MaterialTheme.typography.labelSmall, color = TechSlate)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryStatTile("STATUS", spec.busStatus, Modifier.weight(1.2f), BusGreen)
            TelemetryStatTile("BUS MODE", spec.busMode.code, Modifier.weight(1f), GlyphCrimson)
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryStatTile(
                label = "DSP CHAIN LATENCY",
                value = String.format(Locale.US, "%.2f ms", effLatencyMs),
                accentColor = latencyAccent,
                modifier = Modifier
                    .weight(1f)
                    .testTag("devmap_bus_latency_tile")
            )
            TelemetryStatTile(
                label = "CROSSFADE SMOOTHING",
                value = spec.crossfadeSpec,
                accentColor = CyanScope,
                modifier = Modifier.weight(1.2f)
            )
        }

        Spacer(Modifier.height(14.dp))

        // 2. Transition Triggers
        Text("TRANSITION TRIGGERS", style = MaterialTheme.typography.labelSmall, color = TechSlate)
        Spacer(Modifier.height(6.dp))
        Surface(
            color = ConsolePanel,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, HairlineBorder)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("INBOUND: ${spec.inboundTriggers.joinToString(" | ")}", style = MaterialTheme.typography.labelSmall, color = SignalWhite)
                Text("OUTBOUND: ${spec.outboundSuccess}", style = MaterialTheme.typography.labelSmall, color = BusGreen)
                Text("BYPASS ALT: ${spec.alternateBypass}", style = MaterialTheme.typography.labelSmall, color = PeakAmber)
            }
        }

        Spacer(Modifier.height(14.dp))

        // 3. Hardware & Glyph Telemetry
        Text("HARDWARE & GLYPH TELEMETRY", style = MaterialTheme.typography.labelSmall, color = TechSlate)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryStatTile("RAM BUDGET", spec.peakRamBudget, Modifier.weight(1f))
            TelemetryStatTile("NPU ENGINE", spec.npuStatus, Modifier.weight(1f), BusGreen)
        }
        Spacer(Modifier.height(8.dp))
        Surface(
            color = ConsolePanel,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, HairlineBorder)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("HAPTIC: ${spec.hapticProfile}", style = MaterialTheme.typography.labelSmall, color = SignalWhite)
                Text("GLYPH LED: ${spec.glyphZoneAssignment}", style = MaterialTheme.typography.labelSmall, color = GlyphCrimson)
            }
        }

        Spacer(Modifier.height(14.dp))

        // 4. TypeScript Payload Schema + One-Click Copy
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("RoutePayloadSchema.ts", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
            OutlinedButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    clipboard.setText(AnnotatedString(spec.tsSchema))
                    copiedRecently = true
                },
                border = BorderStroke(1.dp, if (copiedRecently) BusGreen else HairlineBorder),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("copy_ts_type_button")
            ) {
                Icon(
                    imageVector = if (copiedRecently) Icons.Filled.Check else Icons.Filled.ContentCopy,
                    contentDescription = "Copy TypeScript type",
                    tint = if (copiedRecently) BusGreen else SignalWhite,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (copiedRecently) "COPIED" else "COPY TS TYPE",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (copiedRecently) BusGreen else SignalWhite
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF060607))
                .border(1.dp, HairlineBorder, RoundedCornerShape(8.dp))
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = spec.tsSchema,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = CyanScope
            )
        }

        Spacer(Modifier.height(14.dp))

        // 5. Simulate Dispatch & Jump Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSimulateDispatch(false)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ConsolePanel, contentColor = SignalWhite),
                border = BorderStroke(1.dp, GlyphCrimson),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("simulate_dispatch_button")
            ) {
                Icon(Icons.Filled.PlayCircleOutline, contentDescription = null, tint = GlyphCrimson, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("SIMULATE DISPATCH", style = MaterialTheme.typography.labelSmall)
            }

            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSimulateDispatch(true)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("jump_to_screen_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Launch, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("OPEN ${spec.code}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun StartupGuardDiagnosticsPanel(
    report: StartupGuardReport,
    onRerunStartupGuard: () -> Unit
) {
    val badgeColor = when (report.healthState) {
        StartupHealthState.HEALTHY -> BusGreen
        StartupHealthState.PERMISSION_ADVISORY, StartupHealthState.VALIDATING -> PeakAmber
        StartupHealthState.DEGRADED_FALLBACK -> GlyphCrimson
    }
    HardwarePanel(
        title = "STARTUP GUARD // PRE-FLIGHT DIAGNOSTICS",
        badge = report.healthState.label,
        accentBorder = report.isDegraded,
        modifier = Modifier.testTag("devmap_startup_guard_panel")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(report.tfliteCheck, report.databaseCheck, report.permissionCheck).forEach { check ->
                val color = when (check.status) {
                    StartupCheckStatus.PASS -> BusGreen
                    StartupCheckStatus.ADVISORY -> PeakAmber
                    StartupCheckStatus.FALLBACK -> GlyphCrimson
                }
                TelemetryStatTile(
                    label = check.title,
                    value = check.status.label,
                    accentColor = color,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Surface(
            color = ConsolePanel,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, HairlineBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = report.fallbackSummary,
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeColor
                )
                Text(
                    text = "TFLITE: ${report.tfliteCheck.detail}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TechSlate
                )
                Text(
                    text = "ROOM: ${report.databaseCheck.detail}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TechSlate
                )
                Text(
                    text = "HW PERM: ${report.permissionCheck.detail}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TechSlate
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onRerunStartupGuard,
            border = BorderStroke(1.dp, badgeColor),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("devmap_startup_guard_rerun_button")
        ) {
            Text("RE-RUN STARTUP GUARD", style = MaterialTheme.typography.labelSmall, color = SignalWhite)
        }
    }
}
