package com.example.audio

import androidx.compose.runtime.Immutable
import com.example.model.GlyphHardwareFrame
import com.example.model.GlyphPatternMode

/**
 * Hardware telemetry interface mapping real-time Float32 DSP gain reduction
 * and stereo RMS loudness levels into Nothing OS rear-chassis Glyph LED PWM patterns.
 */
interface GlyphVuInterface {
    fun mapToHardwareFrame(
        rmsLeftDb: Float,
        rmsRightDb: Float,
        limiterGainReductionDb: Float,
        neuralGateReductionPct: Float,
        peakBlock: Float,
        bins64: List<Float>,
        patternMode: GlyphPatternMode
    ): GlyphHardwareFrame

    fun reset() {}
}

@Immutable
data class GlyphZoneReadout(
    val code: String,
    val label: String,
    val intensity: Float,
    val pwm12Bit: Int
)

class DefaultGlyphVuController(
    private val glyphMapper: GlyphVUMapper = GlyphVUMapper()
) : GlyphVuInterface {

    override fun reset() {
        glyphMapper.reset()
    }

    override fun mapToHardwareFrame(
        rmsLeftDb: Float,
        rmsRightDb: Float,
        limiterGainReductionDb: Float,
        neuralGateReductionPct: Float,
        peakBlock: Float,
        bins64: List<Float>,
        patternMode: GlyphPatternMode
    ): GlyphHardwareFrame {
        val pulsePacket = glyphMapper.mapToGlyphPulsePattern(
            rmsLeftDb = rmsLeftDb,
            rmsRightDb = rmsRightDb,
            gainReductionDb = limiterGainReductionDb,
            neuralGatePct = neuralGateReductionPct,
            patternMode = patternMode
        )

        val z = pulsePacket.zones
        val rmsAvgNorm = (pulsePacket.rmsPercent / 100f).coerceIn(0.04f, 1f)
        val subEnergy = bins64.take(8).averageOrDefault(0.05f)
        val midEnergy = bins64.drop(18).take(16).averageOrDefault(0.05f)
        val airEnergy = bins64.drop(42).take(18).averageOrDefault(0.05f)

        val baseA1 = (z.getOrNull(0)?.intensity0To100 ?: 6) / 100f
        val baseA2 = (z.getOrNull(1)?.intensity0To100 ?: 6) / 100f
        val baseC1 = (z.getOrNull(2)?.intensity0To100 ?: 5) / 100f
        val baseC2 = (z.getOrNull(3)?.intensity0To100 ?: 5) / 100f
        val baseD1 = (z.getOrNull(4)?.intensity0To100 ?: 5) / 100f

        val cameraPill = if (patternMode == GlyphPatternMode.GLYPH_SPECTRUM_MATRIX) {
            (subEnergy * 0.45f + baseA1 * 0.55f).coerceIn(0.06f, 1f)
        } else {
            baseA1.coerceIn(0.06f, 1f)
        }
        val diagonalStrip = if (patternMode == GlyphPatternMode.GLYPH_SPECTRUM_MATRIX) {
            (airEnergy * 0.45f + baseA2 * 0.55f).coerceIn(0.06f, 1f)
        } else {
            baseA2.coerceIn(0.05f, 1f)
        }
        val ringFill = if (patternMode == GlyphPatternMode.STEREO_VU_BRIDGE) {
            (baseC1 * 0.85f + subEnergy * 0.15f).coerceIn(0.05f, 1f)
        } else {
            baseC1.coerceIn(0.06f, 1f)
        }
        val coreDot = if (patternMode == GlyphPatternMode.GLYPH_SPECTRUM_MATRIX) {
            midEnergy.coerceIn(0.05f, 1f)
        } else {
            (baseC2 * 0.7f + midEnergy * 0.3f).coerceIn(0.05f, 1f)
        }
        val usbClamp = maxOf(baseD1, peakBlock * 0.25f).coerceIn(0.06f, 1f)

        // Map continuous fill into 8 addressable wireless-coil C-Ring LEDs
        val ringSegments = List(8) { idx ->
            val segStart = idx / 8f
            val segSpan = 1f / 8f
            ((ringFill - segStart) / segSpan).coerceIn(0.08f, 1f)
        }

        // Map gain reduction & peak clamp into 6 addressable bottom exclamation LEDs
        val exclamationSegments = List(6) { idx ->
            val segStart = idx / 6f
            val segSpan = 1f / 6f
            ((usbClamp - segStart) / segSpan).coerceIn(0.06f, 1f)
        }

        // Convert to Nothing OS 12-bit hardware PWM integers (0..4095)
        val pwmList = listOf(
            toPwm12(cameraPill),
            toPwm12(diagonalStrip),
            toPwm12(ringFill),
            toPwm12(coreDot),
            toPwm12(usbClamp)
        )

        return GlyphHardwareFrame(
            patternMode = patternMode,
            cameraPillLevel = cameraPill,
            diagonalStripLevel = diagonalStrip,
            centerRingSegments = ringSegments,
            coreDotLevel = coreDot,
            usbExclamationSegments = exclamationSegments,
            rmsAvgNorm = rmsAvgNorm,
            gainReductionDb = limiterGainReductionDb,
            rmsPercent = pulsePacket.rmsPercent,
            gainReductionPercent = pulsePacket.gainReductionPercent,
            pulsePatternLabel = pulsePacket.pulseType.label,
            pwm12BitChannels = pwmList
        )
    }

    private fun toPwm12(level: Float): Int = (level.coerceIn(0f, 1f) * 4095f).toInt()

    private fun List<Float>.averageOrDefault(fallback: Float): Float {
        if (isEmpty()) return fallback
        var sum = 0f
        for (v in this) sum += v
        return sum / size
    }
}
