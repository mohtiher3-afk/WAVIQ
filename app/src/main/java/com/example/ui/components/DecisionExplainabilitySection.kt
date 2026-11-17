package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.DspDecisionStage
import com.example.ui.theme.BusGreen
import com.example.ui.theme.ConsolePanel
import com.example.ui.theme.CyanScope
import com.example.ui.theme.GlyphCrimson
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.PeakAmber
import com.example.ui.theme.SignalWhite
import com.example.ui.theme.TechSlate

/**
 * Phase 3 Explainability & Safety Layer Card:
 * Displays honest model provenance (Heuristic vs LiteRT), active safety clamps,
 * and stage-by-stage DSP rationale traceable to measurements or user commands.
 */
@Composable
fun DecisionExplainabilitySection(
    classificationSourceLabel: String,
    targetStandard: String?,
    safetyWarnings: List<String>,
    stages: List<DspDecisionStage>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = ConsolePanel,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, CyanScope.copy(alpha = 0.5f)),
                modifier = Modifier.testTag("classification_source_badge")
            ) {
                Text(
                    text = "CLASSIFIER: $classificationSourceLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanScope,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            targetStandard?.let { std ->
                Spacer(Modifier.width(8.dp))
                Text(
                    text = std,
                    style = MaterialTheme.typography.labelSmall,
                    color = BusGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (safetyWarnings.isNotEmpty()) {
            Surface(
                color = ConsolePanel,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, PeakAmber.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("decision_safety_warnings")
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = "Active acoustic safety guard",
                            tint = PeakAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "SAFETY GUARDRAILS ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = PeakAmber
                        )
                    }
                    safetyWarnings.forEach { warn ->
                        Text(
                            text = "• $warn",
                            style = MaterialTheme.typography.labelSmall,
                            color = SignalWhite
                        )
                    }
                }
            }
        }

        if (stages.isNotEmpty()) {
            Surface(
                color = ConsolePanel,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, HairlineBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("decision_explainability_card")
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "DSP DECISION RATIONALE (${stages.size} STAGES)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TechSlate
                    )
                    stages.forEach { stage ->
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stage.stageName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (stage.safetyClamped) PeakAmber else SignalWhite
                                )
                                if (stage.safetyClamped) {
                                    Text(
                                        text = "SAFE CLAMP",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GlyphCrimson
                                    )
                                }
                            }
                            Text(
                                text = stage.parameterSummary,
                                style = MaterialTheme.typography.labelSmall,
                                color = BusGreen
                            )
                            Text(
                                text = stage.reason,
                                style = MaterialTheme.typography.labelSmall,
                                color = TechSlate
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}
