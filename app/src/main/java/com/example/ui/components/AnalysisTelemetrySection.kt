package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AudioAnalysisReport
import com.example.model.FindingSeverity
import com.example.ui.theme.BusGreen
import com.example.ui.theme.ConsolePanel
import com.example.ui.theme.CyanScope
import com.example.ui.theme.GlyphCrimson
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.PeakAmber
import com.example.ui.theme.SignalWhite
import com.example.ui.theme.TechSlate
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Shared Phase 2 audio taxonomy, acoustic balance, vocal/tempo telemetry,
 * and technical finding surface rendered cleanly inside Studio & Inspector panels.
 */
@Composable
fun AnalysisTelemetrySection(
    report: AudioAnalysisReport,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Multi-label Style Tags or Explicit Unknown / Mixed Fallback
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (report.isUnknownOrMixedStyle) {
                        "STYLE TAXONOMY // UNKNOWN / MIXED"
                    } else {
                        "MULTI-LABEL STYLE CANDIDATES (v${report.taxonomyVersion})"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = TechSlate
                )
                Text(
                    text = "CONF ${(report.styleConfidence * 100f).roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (report.isUnknownOrMixedStyle) PeakAmber else accentColor
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .testTag("analysis_style_tags_row"),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (report.isUnknownOrMixedStyle || report.multiLabelStyles.isEmpty()) {
                    val unknownLabel = if (report.isUnknownOrMixedStyle) "UNKNOWN / MIXED" else report.primaryStyleLabel.uppercase(Locale.US)
                    StyleBadgeChip(
                        text = unknownLabel,
                        accent = if (report.isUnknownOrMixedStyle) PeakAmber else accentColor
                    )
                    report.multiLabelStyles
                        .filter { it.taxonomyId != "unknown_mixed" }
                        .take(2)
                        .forEach { cand ->
                            StyleBadgeChip(
                                text = "${cand.displayName} ${(cand.score * 100f).roundToInt()}%",
                                accent = TechSlate
                            )
                        }
                } else {
                    report.multiLabelStyles.forEachIndexed { idx, tag ->
                        val chipAccent = if (idx == 0) accentColor else CyanScope
                        StyleBadgeChip(
                            text = "${tag.displayName} ${(tag.score * 100f).roundToInt()}%",
                            accent = chipAccent
                        )
                    }
                }
            }
        }

        // 2. Strictly Separated Mood, Production & Vocal Tags
        Surface(
            color = ConsolePanel,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, HairlineBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("analysis_separated_tags_card")
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SeparatedTagLine(
                    category = "MOOD",
                    value = report.moodTags.ifEmpty { listOf("Balanced / Controlled") }.joinToString(" • "),
                    accent = CyanScope
                )
                SeparatedTagLine(
                    category = "PRODUCTION",
                    value = report.productionStyleTags.ifEmpty { listOf("Modern Balanced Mix") }.joinToString(" • "),
                    accent = BusGreen
                )
                SeparatedTagLine(
                    category = "VOCAL",
                    value = "${report.vocalActivity.label} (${report.vocalActivity.presencePct}%)",
                    accent = if (report.vocalActivity.presencePct >= 58) PeakAmber else TechSlate
                )
            }
        }

        // 3. Measured Spectral Split, Phase Correlation, LRA & Confident-Only Tempo Strip
        Surface(
            color = ConsolePanel,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, HairlineBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("analysis_spectral_balance_row")
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SPECTRAL SPLIT",
                        style = MaterialTheme.typography.labelSmall,
                        color = TechSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "LOW ${report.lowEnergyPct}% • MID ${report.midEnergyPct}% • HIGH ${report.highEnergyPct}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = SignalWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryStatTile(
                        label = "PHASE // LRA",
                        value = String.format(
                            Locale.US,
                            "r=%+.2f • %.1f LU",
                            report.stereoCorrelation,
                            report.loudnessRangeLu
                        ),
                        accentColor = if (report.hasPhaseRisk) GlyphCrimson else CyanScope,
                        modifier = Modifier.weight(1.15f)
                    )
                    TelemetryStatTile(
                        label = "TEMPO EST.",
                        value = report.rhythm.bpm?.let { "$it BPM" } ?: "N/A",
                        accentColor = if (report.rhythm.bpm != null) BusGreen else TechSlate,
                        modifier = Modifier.weight(0.85f)
                    )
                }
            }
        }

        // 4. Technical Findings & Contradictory Filename Audit Note
        if (report.technicalFindings.isNotEmpty() || report.filenameHintNote != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analysis_technical_findings"),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                report.technicalFindings.forEach { finding ->
                    val severityColor = when (finding.severity) {
                        FindingSeverity.WARNING -> GlyphCrimson
                        FindingSeverity.ADVISORY -> PeakAmber
                        FindingSeverity.INFO -> CyanScope
                    }
                    Surface(
                        color = ConsolePanel,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, severityColor.copy(alpha = 0.55f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (finding.severity == FindingSeverity.INFO) {
                                    Icons.Filled.Info
                                } else {
                                    Icons.Filled.Warning
                                },
                                contentDescription = "${finding.severity.name} technical finding",
                                tint = severityColor,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = finding.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = severityColor
                                )
                                Text(
                                    text = finding.detail,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TechSlate
                                )
                            }
                        }
                    }
                }

                report.filenameHintNote?.let { note ->
                    Surface(
                        color = ConsolePanel,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, PeakAmber.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "AUDIT GUARD: $note",
                            style = MaterialTheme.typography.labelSmall,
                            color = PeakAmber,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StyleBadgeChip(
    text: String,
    accent: Color
) {
    Surface(
        color = ConsolePanel,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.7f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun SeparatedTagLine(
    category: String,
    value: String,
    accent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            maxLines = 1
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = SignalWhite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
