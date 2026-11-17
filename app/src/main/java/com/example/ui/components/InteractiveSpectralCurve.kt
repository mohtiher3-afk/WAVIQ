package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.MusicGenre
import com.example.ui.theme.BusGreen
import com.example.ui.theme.ConsolePanel
import com.example.ui.theme.CyanScope
import com.example.ui.theme.GlyphCrimson
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.MutedGrid
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.PeakAmber
import com.example.ui.theme.SignalWhite
import com.example.ui.theme.TechSlate
import java.util.Locale

/**
 * Interactive Real-Time Spectral Distribution Curve Canvas.
 * Renders the frequency distribution (20Hz - 20kHz) mapped to the detected/selected
 * MusicGenre DSP calibration with touch inspection and target LUFS indicators.
 */
@Composable
fun InteractiveSpectralCurve(
    genre: MusicGenre,
    bandwidthKhz: Float = 18.5f,
    modifier: Modifier = Modifier,
    isRealTime: Boolean = false
) {
    val genreAccent = when (genre) {
        MusicGenre.CLASSICAL, MusicGenre.ACOUSTIC -> BusGreen
        MusicGenre.POP, MusicGenre.ELECTRONIC, MusicGenre.TECHNO -> CyanScope
        MusicGenre.ROCK, MusicGenre.HIP_HOP, MusicGenre.TRAP -> PeakAmber
        MusicGenre.DRILL -> GlyphCrimson
    }

    var inspectFraction by remember { mutableFloatStateOf(0.5f) }

    val bandLabels = listOf("60Hz", "250Hz", "1kHz", "4kHz", "12kHz")
    val offsets = genre.eqOffsetsDb.ifEmpty { listOf(0f, 0f, 0f, 0f, 0f) }

    Surface(
        color = ConsolePanel,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, HairlineBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_spectral_curve")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(genreAccent, CircleShape)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "SPECTRAL DISTRIBUTION // ${genre.title}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SignalWhite
                    )
                }

                Surface(
                    color = genreAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, genreAccent.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "TARGET ${genre.targetLufs} LUFS",
                        style = MaterialTheme.typography.labelSmall,
                        color = genreAccent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Main Interactive Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ObsidianVoid)
                    .border(1.dp, HairlineBorder, RoundedCornerShape(6.dp))
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            inspectFraction = (offset.x / size.width).coerceIn(0.02f, 0.98f)
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            inspectFraction = (change.position.x / size.width).coerceIn(0.02f, 0.98f)
                            change.consume()
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val midY = h * 0.52f

                    // Gridlines
                    val gridXSteps = 5
                    for (i in 1..gridXSteps) {
                        val gx = w * (i.toFloat() / (gridXSteps + 1))
                        drawLine(
                            color = MutedGrid.copy(alpha = 0.4f),
                            start = Offset(gx, 0f),
                            end = Offset(gx, h),
                            strokeWidth = 1f
                        )
                    }

                    // Horizontal zero-dB baseline
                    drawLine(
                        color = MutedGrid.copy(alpha = 0.6f),
                        start = Offset(0f, midY),
                        end = Offset(w, midY),
                        strokeWidth = 1f
                    )

                    // Control points across the 5 EQ bands
                    val points = mutableListOf<Offset>()
                    val marginX = w * 0.08f
                    val spanX = w - 2f * marginX

                    for (i in 0 until 5) {
                        val px = marginX + (spanX * (i.toFloat() / 4f))
                        val gainDb = offsets.getOrElse(i) { 0f }
                        // Scale: +6dB is near top, -6dB is near bottom
                        val py = (midY - (gainDb * (h * 0.065f))).coerceIn(h * 0.12f, h * 0.88f)
                        points.add(Offset(px, py))
                    }

                    // Build smooth cubic Bézier curve path
                    val curvePath = Path().apply {
                        moveTo(0f, midY - (offsets[0] * (h * 0.04f)))
                        lineTo(points[0].x, points[0].y)

                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val controlX = (p0.x + p1.x) / 2f
                            cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                        }

                        // Air rolloff past bandwidth
                        val endY = if (bandwidthKhz < 15f) h * 0.82f else midY - (offsets.last() * (h * 0.04f))
                        lineTo(w, endY)
                    }

                    // Gradient fill under the curve
                    val fillPath = Path().apply {
                        addPath(curvePath)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                genreAccent.copy(alpha = 0.30f),
                                genreAccent.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = h
                        )
                    )

                    // Draw main spectral contour line
                    drawPath(
                        path = curvePath,
                        color = genreAccent,
                        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                    )

                    // Draw node circles on the 5 bands
                    points.forEachIndexed { idx, pt ->
                        drawCircle(
                            color = ObsidianVoid,
                            radius = 5.5f,
                            center = pt
                        )
                        drawCircle(
                            color = genreAccent,
                            radius = 3.5f,
                            center = pt
                        )
                    }

                    // Interactive touch cursor
                    val cursorX = w * inspectFraction
                    drawLine(
                        color = SignalWhite.copy(alpha = 0.75f),
                        start = Offset(cursorX, 0f),
                        end = Offset(cursorX, h),
                        strokeWidth = 1.5f
                    )
                    drawCircle(
                        color = SignalWhite,
                        radius = 4f,
                        center = Offset(cursorX, midY)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Frequency band labels footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                bandLabels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TechSlate
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Inspection Telemetry Footer
            val inspectHz = (20.0 * Math.pow(1000.0, inspectFraction.toDouble())).toInt().coerceIn(20, 20000)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CURSOR: ${inspectHz}Hz",
                    style = MaterialTheme.typography.labelSmall,
                    color = SignalWhite
                )
                Text(
                    text = "CEILING: ${genre.ceilingDb} dBTP • ${genre.oversampling.factor}X",
                    style = MaterialTheme.typography.labelSmall,
                    color = TechSlate
                )
            }
        }
    }
}
