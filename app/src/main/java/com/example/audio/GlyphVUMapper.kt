package com.example.audio

import androidx.compose.runtime.Immutable
import com.example.model.GlyphPatternMode
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.roundToInt

enum class GlyphPulseType(val label: String, val periodMs: Int) {
    IDLE_HEARTBEAT("IDLE HEARTBEAT", 950),
    RMS_BREATHE_WAVE("RMS BREATHE", 160),
    TRANSIENT_EDGE_STROBE("TRANSIENT STROBE", 55),
    GR_CLAMP_PULSE("GR CLAMP PULSE", 35)
}

@Immutable
data class GlyphZonePulse(
    val zoneId: String,
    val label: String,
    val intensity0To100: Int,
    val pulsePeriodMs: Int,
    val activePulse: Boolean
)

@Immutable
data class GlyphPulsePacket(
    val rmsPercent: Int,
    val rmsLeftPercent: Int,
    val rmsRightPercent: Int,
    val gainReductionPercent: Int,
    val pulseType: GlyphPulseType,
    val zones: List<GlyphZonePulse>
)

/**
 * Stateful visualizer mapper that normalizes real-time DSP RMS loudness and True-Peak
 * Gain Reduction values into calibrated 0-100 envelopes using fast-attack / exponential-decay
 * ballistics, then maps them onto the 5-zone Nothing Phone rear chassis Glyph LED topology.
 */
class GlyphVUMapper(
    val rmsFloorDb: Float = DEFAULT_FLOOR_DB,
    val rmsCeilDb: Float = DEFAULT_CEIL_DB,
    val maxGrDb: Float = DEFAULT_MAX_GR_DB,
    attackAlpha: Float = DEFAULT_ATTACK_ALPHA,
    rmsDecayAlpha: Float = DEFAULT_RMS_DECAY_ALPHA,
    grDecayAlpha: Float = DEFAULT_GR_DECAY_ALPHA
) {
    private val attackCoeff = attackAlpha.coerceIn(0.05f, 1.0f)
    private val rmsDecayCoeff = rmsDecayAlpha.coerceIn(0.02f, 1.0f)
    private val grDecayCoeff = grDecayAlpha.coerceIn(0.02f, 1.0f)

    private val stateLock = Any()
    private var smoothedLeftRms: Float = 0f
    private var smoothedRightRms: Float = 0f
    private var smoothedGr: Float = 0f
    private var smoothedGate: Float = 0f

    fun normalizeRmsDb(rmsDb: Float): Int =
        Companion.normalizeRmsDb(rmsDb, floorDb = rmsFloorDb, ceilDb = rmsCeilDb)

    fun normalizeRmsLinear(rmsLinear: Float): Int =
        Companion.normalizeRmsLinear(rmsLinear, floorDb = rmsFloorDb, ceilDb = rmsCeilDb)

    fun normalizeGainReductionDb(gainReductionDb: Float): Int =
        Companion.normalizeGainReductionDb(gainReductionDb, maxGrDb = maxGrDb)

    fun reset() {
        synchronized(stateLock) {
            smoothedLeftRms = 0f
            smoothedRightRms = 0f
            smoothedGr = 0f
            smoothedGate = 0f
        }
    }

    fun mapToGlyphPulsePattern(
        rmsLeftDb: Float,
        rmsRightDb: Float,
        gainReductionDb: Float,
        neuralGatePct: Float = 0f,
        patternMode: GlyphPatternMode = GlyphPatternMode.STEREO_VU_BRIDGE
    ): GlyphPulsePacket {
        val targetL = normalizeRmsDb(rmsLeftDb).toFloat()
        val targetR = normalizeRmsDb(rmsRightDb).toFloat()
        val targetGr = normalizeGainReductionDb(gainReductionDb).toFloat()
        val targetGate = if (neuralGatePct.isFinite()) neuralGatePct.coerceIn(0f, 100f) else 0f

        val rmsL: Int
        val rmsR: Int
        val grPct: Int
        val gatePct: Int

        synchronized(stateLock) {
            smoothedLeftRms = applyBallistics(targetL, smoothedLeftRms, rmsDecayCoeff)
            smoothedRightRms = applyBallistics(targetR, smoothedRightRms, rmsDecayCoeff)
            smoothedGr = applyBallistics(targetGr, smoothedGr, grDecayCoeff)
            smoothedGate = applyBallistics(targetGate, smoothedGate, grDecayCoeff)

            rmsL = smoothedLeftRms.roundToInt().coerceIn(0, 100)
            rmsR = smoothedRightRms.roundToInt().coerceIn(0, 100)
            grPct = smoothedGr.roundToInt().coerceIn(0, 100)
            gatePct = smoothedGate.roundToInt().coerceIn(0, 100)
        }

        val rmsAvg = ((rmsL + rmsR) * 0.5f).roundToInt().coerceIn(0, 100)
        val combinedGr = maxOf(grPct, (gatePct * 0.75f).roundToInt()).coerceIn(0, 100)

        val pulseType = when {
            grPct >= 25 -> GlyphPulseType.GR_CLAMP_PULSE
            rmsAvg >= 78 -> GlyphPulseType.TRANSIENT_EDGE_STROBE
            rmsAvg >= 10 || gatePct >= 10 || grPct >= 8 -> GlyphPulseType.RMS_BREATHE_WAVE
            else -> GlyphPulseType.IDLE_HEARTBEAT
        }

        // 5-zone Nothing Phone layout: C-Ring (C1) stays anchored to RMS loudness,
        // USB-C Exclamation Bar (D1) stays anchored to limiter Gain Reduction clamp.
        val (a1Level, a2Level, c1Level, c2Level, d1Level) = when (patternMode) {
            GlyphPatternMode.STEREO_VU_BRIDGE -> listOf(
                rmsL,
                rmsR,
                rmsAvg,
                ((rmsAvg * 0.85f) + (gatePct * 0.15f)).roundToInt().coerceIn(0, 100),
                grPct
            )
            GlyphPatternMode.GAIN_REDUCTION_CLAMP -> listOf(
                gatePct,
                grPct,
                rmsAvg,
                ((100 - abs(rmsL - rmsR)) * (rmsAvg / 100f)).roundToInt().coerceIn(0, 100),
                combinedGr
            )
            GlyphPatternMode.GLYPH_SPECTRUM_MATRIX -> listOf(
                maxOf(rmsL, gatePct / 2),
                maxOf(rmsR, grPct / 2),
                rmsAvg,
                ((rmsAvg * 0.65f) + (combinedGr * 0.35f)).roundToInt().coerceIn(0, 100),
                maxOf(grPct, combinedGr / 2)
            )
        }

        val zones = listOf(
            GlyphZonePulse("ZONE_A1", "CAMERA PILL", a1Level, pulseType.periodMs, a1Level >= 12),
            GlyphZonePulse("ZONE_A2", "DIAGONAL STRIP", a2Level, pulseType.periodMs, a2Level >= 12),
            GlyphZonePulse("ZONE_C1", "COIL C-RING", c1Level, pulseType.periodMs, c1Level >= 10),
            GlyphZonePulse("ZONE_C2", "VOCAL CORE", c2Level, pulseType.periodMs * 2, c2Level >= 10),
            GlyphZonePulse("ZONE_D1", "USB-C CLAMP BAR", d1Level, GlyphPulseType.GR_CLAMP_PULSE.periodMs, d1Level >= 10)
        )

        return GlyphPulsePacket(
            rmsPercent = rmsAvg,
            rmsLeftPercent = rmsL,
            rmsRightPercent = rmsR,
            gainReductionPercent = grPct,
            pulseType = pulseType,
            zones = zones
        )
    }

    private fun applyBallistics(target: Float, current: Float, decayAlpha: Float): Float {
        val alpha = if (target >= current) attackCoeff else decayAlpha
        val next = current + alpha * (target - current)
        return if (target == 0f && next < 0.5f) 0f else next.coerceIn(0f, 100f)
    }

    companion object {
        const val DEFAULT_FLOOR_DB = -48f
        const val DEFAULT_CEIL_DB = -3f
        const val DEFAULT_MAX_GR_DB = 6.0f
        const val DEFAULT_ATTACK_ALPHA = 0.90f
        const val DEFAULT_RMS_DECAY_ALPHA = 0.24f
        const val DEFAULT_GR_DECAY_ALPHA = 0.18f

        fun normalizeRmsDb(
            rmsDb: Float,
            floorDb: Float = DEFAULT_FLOOR_DB,
            ceilDb: Float = DEFAULT_CEIL_DB
        ): Int {
            if (!rmsDb.isFinite()) return 0
            val span = (ceilDb - floorDb).coerceAtLeast(1f)
            val norm = ((rmsDb - floorDb) / span).coerceIn(0f, 1f)
            return (norm * 100f).roundToInt().coerceIn(0, 100)
        }

        fun normalizeRmsLinear(
            rmsLinear: Float,
            floorDb: Float = DEFAULT_FLOOR_DB,
            ceilDb: Float = DEFAULT_CEIL_DB
        ): Int {
            if (!rmsLinear.isFinite() || rmsLinear <= 1e-6f) return 0
            val db = 20f * log10(rmsLinear.coerceAtLeast(1e-6f))
            return normalizeRmsDb(db, floorDb = floorDb, ceilDb = ceilDb)
        }

        fun normalizeGainReductionDb(
            gainReductionDb: Float,
            maxGrDb: Float = DEFAULT_MAX_GR_DB
        ): Int {
            if (!gainReductionDb.isFinite() || gainReductionDb <= 0f) return 0
            val norm = (gainReductionDb / maxGrDb.coerceAtLeast(0.5f)).coerceIn(0f, 1f)
            return (norm * 100f).roundToInt().coerceIn(0, 100)
        }
    }
}
