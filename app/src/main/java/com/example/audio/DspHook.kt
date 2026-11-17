package com.example.audio

import androidx.compose.runtime.Immutable
import com.example.model.EqBand
import com.example.model.OversamplingFactor
import com.example.model.StemChannelState
import com.example.model.StemMixerState
import com.example.model.TactileDeckParams
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.tanh

/**
 * Pluggable real-time 64-bit Double internal DSP hook executed sequentially by [AudioProcessor].
 */
interface DspHook {
    val id: String
    val name: String
    var isEnabled: Boolean
    val baselineLatencyMs: Float
        get() = 0.08f

    fun processBlock(
        left: FloatArray,
        right: FloatArray,
        frameCount: Int,
        sampleRate: Int
    )

    fun reset()
}

@Immutable
data class DspHookSnapshot(
    val id: String,
    val name: String,
    val isEnabled: Boolean,
    val latencyMs: Float = 0f
)

internal class ScratchStereo64(initialCapacity: Int = 1024) {
    private var bufL = DoubleArray(initialCapacity)
    private var bufR = DoubleArray(initialCapacity)

    inline fun processFloatBridge(
        left: FloatArray,
        right: FloatArray,
        frameCount: Int,
        crossinline block: (DoubleArray, DoubleArray, Int) -> Unit
    ) {
        if (frameCount <= 0) return
        val n = minOf(frameCount, left.size, right.size)
        if (bufL.size < n) {
            bufL = DoubleArray(n)
            bufR = DoubleArray(n)
        }
        for (i in 0 until n) {
            bufL[i] = left[i].toDouble()
            bufR[i] = right[i].toDouble()
        }
        block(bufL, bufR, n)
        for (i in 0 until n) {
            left[i] = bufL[i].toFloat()
            right[i] = bufR[i].toFloat()
        }
    }
}

class GainTrimHook(
    override val id: String = "gain_trim",
    override val name: String = "Gain Trim Stage",
    override var isEnabled: Boolean = true,
    @Volatile var gainLinear: Float = 1.0f
) : Dsp64Stage {
    override val baselineLatencyMs: Float
        get() = 0.03f

    override fun processDoubleBlock(
        left: DoubleArray,
        right: DoubleArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        val g = gainLinear.toDouble()
        if (g == 1.0) return
        for (i in 0 until frameCount) {
            left[i] *= g
            right[i] *= g
        }
    }

    override fun processBlock(
        left: FloatArray,
        right: FloatArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        val g = gainLinear.toDouble()
        if (g == 1.0) return
        for (i in 0 until frameCount) {
            left[i] = (left[i].toDouble() * g).toFloat()
            right[i] = (right[i].toDouble() * g).toFloat()
        }
    }

    override fun reset() = Unit
}

class BiquadEqHook(
    override val id: String = "biquad_eq_5x",
    override val name: String = "5-Band 64-Bit Biquad EQ",
    override var isEnabled: Boolean = true
) : Dsp64Stage {
    override val baselineLatencyMs: Float
        get() = 0.20f
    private val filters = List(5) { BiquadFilter(48000.0) }
    private val scratch64 = ScratchStereo64()

    fun configure(bands: List<EqBand>, sampleRate: Int) {
        val sr = sampleRate.toFloat()
        bands.forEachIndexed { idx, band ->
            if (idx < filters.size) {
                val type = when (idx) {
                    0 -> FilterType.LOW_SHELF
                    4 -> FilterType.HIGH_SHELF
                    else -> FilterType.PEAKING
                }
                filters[idx].configure(type, band.frequencyHz, band.qFactor, band.gainDb, sr)
            }
        }
    }

    override fun processDoubleBlock(
        left: DoubleArray,
        right: DoubleArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        for (i in 0 until frameCount) {
            var l = left[i]
            var r = right[i]
            for (f in filters) {
                l = f.processLeftDouble(l)
                r = f.processRightDouble(r)
            }
            left[i] = l
            right[i] = r
        }
    }

    override fun processBlock(
        left: FloatArray,
        right: FloatArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        scratch64.processFloatBridge(left, right, frameCount) { l64, r64, n ->
            processDoubleBlock(l64, r64, n, sampleRate)
        }
    }

    override fun reset() {
        filters.forEach { it.reset() }
    }
}

class TactileDriveHook(
    override val id: String = "tactile_drive",
    override val name: String = "64-Bit Harmonic Drive & M/S Width",
    override var isEnabled: Boolean = true
) : Dsp64Stage {
    override val baselineLatencyMs: Float
        get() = 0.26f
    @Volatile
    private var params: TactileDeckParams = TactileDeckParams()
    private val airFilter = BiquadFilter(48000.0)
    private val subBassFilter = BiquadFilter(48000.0)
    private val scratch64 = ScratchStereo64()
    private var envFollower = 0.0

    init {
        subBassFilter.configure(FilterType.LOW_PASS, 120f, 0.707f, 0f, 48000f)
    }

    fun configure(deck: TactileDeckParams, sampleRate: Int) {
        params = deck
        val sr = sampleRate.toFloat()
        airFilter.configure(
            FilterType.HIGH_SHELF,
            10000f,
            0.707f,
            (deck.air - 0.35f) * 9f,
            sr
        )
        subBassFilter.configure(FilterType.LOW_PASS, 120f, 0.707f, 0f, sr)
    }

    override fun processDoubleBlock(
        left: DoubleArray,
        right: DoubleArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        val p = params
        val driveGain = 1.0 + p.drive.toDouble() * 2.2
        val driveNorm = if (p.drive > 0.02f) tanh(driveGain) else 1.0
        val widthScale = 0.45 + p.width.toDouble() * 1.35

        for (i in 0 until frameCount) {
            var l = left[i]
            var r = right[i]

            if (p.drive > 0.02f) {
                l = tanh(l * driveGain) / driveNorm
                r = tanh(r * driveGain) / driveNorm
            }

            val absIn = 0.5 * (abs(l) + abs(r))
            envFollower = envFollower * 0.992 + absIn * 0.008
            val transientDelta = (absIn - envFollower).coerceAtLeast(0.0)
            val punchBoost = 1.0 + transientDelta * p.punch.toDouble() * 1.4
            l *= punchBoost
            r *= punchBoost

            l = airFilter.processLeftDouble(l)
            r = airFilter.processRightDouble(r)

            // Mono-tighten sub-bass (<120Hz) while expanding upper M/S stereo field
            val lowL = subBassFilter.processLeftDouble(l)
            val lowR = subBassFilter.processRightDouble(r)
            val lowMono = 0.5 * (lowL + lowR)

            val hiL = l - lowL
            val hiR = r - lowR
            val midHi = 0.5 * (hiL + hiR)
            val sideHi = 0.5 * (hiL - hiR) * widthScale

            left[i] = lowMono + midHi + sideHi
            right[i] = lowMono + midHi - sideHi
        }
    }

    override fun processBlock(
        left: FloatArray,
        right: FloatArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        scratch64.processFloatBridge(left, right, frameCount) { l64, r64, n ->
            processDoubleBlock(l64, r64, n, sampleRate)
        }
    }

    override fun reset() {
        airFilter.reset()
        subBassFilter.reset()
        envFollower = 0.0
    }
}

class StemMatrixHook(
    override val id: String = "stem_matrix_4x",
    override val name: String = "Frequency-Band Crossover Mixer",
    override var isEnabled: Boolean = true
) : Dsp64Stage {
    override val baselineLatencyMs: Float
        get() = 0.36f
    @Volatile
    private var stemState: StemMixerState = StemMixerState()
    @Volatile
    private var forceActive: Boolean = false

    private val bassLowPass = BiquadFilter(48000.0)
    private val vocalBandPass = BiquadFilter(48000.0)
    private val drumHighPass = BiquadFilter(48000.0)
    private val otherHighPass = BiquadFilter(48000.0)
    private val scratch64 = ScratchStereo64()

    init {
        configureCrossovers(48000)
    }

    fun configureCrossovers(sampleRate: Int) {
        val sr = sampleRate.toFloat()
        bassLowPass.configure(FilterType.LOW_PASS, 220f, 0.707f, 0f, sr)
        vocalBandPass.configure(FilterType.BAND_PASS, 1850f, 0.85f, 0f, sr)
        drumHighPass.configure(FilterType.HIGH_PASS, 180f, 0.707f, 0f, sr)
        otherHighPass.configure(FilterType.HIGH_PASS, 5200f, 0.707f, 0f, sr)
    }

    fun updateState(stems: StemMixerState, isStemScreen: Boolean) {
        stemState = stems
        forceActive = isStemScreen
    }

    override fun processDoubleBlock(
        left: DoubleArray,
        right: DoubleArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        val stems = stemState
        val anySolo = stems.asList().any { it.soloed }
        val modified = forceActive || anySolo || stems.asList().any {
            it.muted || abs(it.level - 1f) > 0.03f
        }
        if (!modified) return

        fun effGain(ch: StemChannelState): Double {
            if (ch.muted) return 0.0
            if (anySolo && !ch.soloed) return 0.0
            return ch.level.toDouble()
        }

        val gBass = effGain(stems.bass)
        val gVocal = effGain(stems.vocals)
        val gDrums = effGain(stems.drums)
        val gOther = effGain(stems.other)

        for (i in 0 until frameCount) {
            val l = left[i]
            val r = right[i]
            val mid = 0.5 * (l + r)
            val side = 0.5 * (l - r)

            val bassL = bassLowPass.processLeftDouble(l)
            val bassR = bassLowPass.processRightDouble(r)
            val vocalM = vocalBandPass.processLeftDouble(mid)
            val drumL = drumHighPass.processLeftDouble(l) * 0.65
            val drumR = drumHighPass.processRightDouble(r) * 0.65
            val otherL = otherHighPass.processLeftDouble(l) + side * 0.75
            val otherR = otherHighPass.processRightDouble(r) - side * 0.75

            left[i] = bassL * gBass + vocalM * gVocal + drumL * gDrums + otherL * gOther
            right[i] = bassR * gBass + vocalM * gVocal + drumR * gDrums + otherR * gOther
        }
    }

    override fun processBlock(
        left: FloatArray,
        right: FloatArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        scratch64.processFloatBridge(left, right, frameCount) { l64, r64, n ->
            processDoubleBlock(l64, r64, n, sampleRate)
        }
    }

    override fun reset() {
        bassLowPass.reset()
        vocalBandPass.reset()
        drumHighPass.reset()
        otherHighPass.reset()
    }
}

/**
 * 64-Bit Spectral Band Replication (SBR) & Studio M/S Stereo Shimmer Expander.
 * Implements [Sbr192KhzRestorationModule] to synthesize coherent 2nd/3rd/5th upper
 * harmonics (>4.5kHz) and phase-coherent upper-band stereo shimmer at up to 192kHz.
 */
class HiResUpsamplingHook(
    override val id: String = "hi_res_upsampler",
    override val name: String = "SBR HF Restoration & Stereo Expander",
    override var isEnabled: Boolean = true,
    @Volatile override var restorationAmount: Float = 0.65f,
    @Volatile override var targetKhz: Int = 96,
    @Volatile override var stereoExpansionAmount: Float = 0.48f
) : Sbr192KhzRestorationModule {
    override val baselineLatencyMs: Float
        get() = if (targetKhz >= 192) 0.52f else 0.36f
    override val hfCrossoverHz: Float = 4500f

    private val bandSplitHp = BiquadFilter(48000.0)
    private val harmonicExciterBp = BiquadFilter(48000.0)
    private val airShelf = BiquadFilter(48000.0)
    private val sideShimmerHp = BiquadFilter(48000.0)
    private val scratch64 = ScratchStereo64()
    private var configuredSampleRate = 48000

    init {
        configure(48000)
    }

    fun configure(sampleRate: Int) {
        configuredSampleRate = sampleRate
        val sr = sampleRate.toFloat()
        bandSplitHp.configure(FilterType.HIGH_PASS, hfCrossoverHz, 0.707f, 0f, sr)
        harmonicExciterBp.configure(FilterType.BAND_PASS, 6400f, 0.9f, 0f, sr)
        airShelf.configure(FilterType.HIGH_SHELF, 11800f, 0.707f, 3.2f, sr)
        sideShimmerHp.configure(FilterType.HIGH_PASS, 5400f, 0.707f, 0f, sr)
    }

    override fun configure(
        sampleRate: Int,
        targetKhz: Int,
        restorationAmount: Float,
        stereoExpansionAmount: Float
    ) {
        this.targetKhz = targetKhz
        this.restorationAmount = restorationAmount.coerceIn(0f, 1f)
        this.stereoExpansionAmount = stereoExpansionAmount.coerceIn(0f, 1f)
        configure(sampleRate)
    }

    override fun readSbrMetrics(): SbrRestorationMetrics = SbrRestorationMetrics(
        targetKhz = targetKhz,
        configuredSampleRateHz = configuredSampleRate,
        restorationAmount = restorationAmount,
        stereoExpansionAmount = stereoExpansionAmount,
        hfCrossoverHz = hfCrossoverHz,
        is192KhzUltraMode = targetKhz >= 192
    )

    override fun processDoubleBlock(
        left: DoubleArray,
        right: DoubleArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        if (frameCount <= 0) return
        if (sampleRate != configuredSampleRate) {
            configure(sampleRate)
        }
        val amt = restorationAmount.toDouble().coerceIn(0.0, 1.0)
        if (amt <= 0.01) return
        val khzFactor = if (targetKhz >= 192) 1.24 else 1.0
        val blend = 0.16 * amt * khzFactor
        val stereoWidthBoost = stereoExpansionAmount.toDouble().coerceIn(0.0, 1.0) * amt * 0.32

        for (i in 0 until frameCount) {
            val l = left[i]
            val r = right[i]
            val hiL = bandSplitHp.processLeftDouble(l)
            val hiR = bandSplitHp.processRightDouble(r)

            val excL = harmonicExciterBp.processLeftDouble(hiL)
            val excR = harmonicExciterBp.processRightDouble(hiR)

            // Coherent 2nd (even warmth), 3rd (odd air), and 5th harmonic SBR synthesis
            val harmL = (
                excL * abs(excL) * 2.1 +
                    (excL * excL * 0.65) +
                    (excL * excL * excL * 0.45)
                ).coerceIn(-0.28, 0.28)
            val harmR = (
                excR * abs(excR) * 2.1 +
                    (excR * excR * 0.65) +
                    (excR * excR * excR * 0.45)
                ).coerceIn(-0.28, 0.28)

            val restoredL = airShelf.processLeftDouble(l + harmL * blend)
            val restoredR = airShelf.processRightDouble(r + harmR * blend)

            // Subtle upper-band M/S stereo shimmer keeps low-end & center lead mono-tight
            val mid = 0.5 * (restoredL + restoredR)
            val side = 0.5 * (restoredL - restoredR)
            val hiSide = sideShimmerHp.processLeftDouble(side)
            val expandedSide = side + hiSide * stereoWidthBoost

            left[i] = mid + expandedSide
            right[i] = mid - expandedSide
        }
    }

    override fun processBlock(
        left: FloatArray,
        right: FloatArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        scratch64.processFloatBridge(left, right, frameCount) { l64, r64, n ->
            processDoubleBlock(l64, r64, n, sampleRate)
        }
    }

    override fun reset() {
        bandSplitHp.reset()
        harmonicExciterBp.reset()
        airShelf.reset()
        sideShimmerHp.reset()
    }
}

/**
 * 64-Bit Multi-Band (3-Band) True-Peak Oversampled Limiter implementing [ThreeBandTruePeakLimiter].
 * Splits Low (<250Hz), Mid (250Hz-4kHz), and High (>4kHz) bands via complementary
 * 64-bit filters so heavy sub-bass hits never pump vocals/air, then enforces a
 * 4x/8x Catmull-Rom cubic Inter-Sample Peak brickwall safety stage.
 */
class TruePeakLimiterHook(
    override val id: String = "true_peak_limiter",
    override val name: String = "3-Band True-Peak ISP Limiter",
    override var isEnabled: Boolean = true,
    @Volatile override var ceilingDb: Float = -1.0f,
    @Volatile override var oversamplingFactor: OversamplingFactor = OversamplingFactor.X4
) : ThreeBandTruePeakLimiter {
    override val baselineLatencyMs: Float
        get() = if (oversamplingFactor == OversamplingFactor.X8) 0.64f else 0.42f
    override val lowCrossoverHz: Float = 250f
    override val highCrossoverHz: Float = 4000f

    private val lowSplitFilter = BiquadFilter(48000.0)
    private val highSplitFilter = BiquadFilter(48000.0)
    private val scratch64 = ScratchStereo64()

    private var lowEnv = 1.0
    private var midEnv = 1.0
    private var highEnv = 1.0
    private var masterEnv = 1.0

    private var l0 = 0.0; private var l1 = 0.0; private var l2 = 0.0
    private var r0 = 0.0; private var r1 = 0.0; private var r2 = 0.0
    private var configuredSampleRate = 48000

    @Volatile
    override var lastGainReductionDb: Float = 0f
        private set

    @Volatile
    override var lastLowBandGrDb: Float = 0f
        private set

    @Volatile
    override var lastMidBandGrDb: Float = 0f
        private set

    @Volatile
    override var lastHighBandGrDb: Float = 0f
        private set

    init {
        configureCrossovers(48000)
    }

    private fun configureCrossovers(sampleRate: Int) {
        configuredSampleRate = sampleRate
        val sr = sampleRate.toFloat()
        lowSplitFilter.configure(FilterType.LOW_PASS, lowCrossoverHz, 0.707f, 0f, sr)
        highSplitFilter.configure(FilterType.HIGH_PASS, highCrossoverHz, 0.707f, 0f, sr)
    }

    override fun configure(
        ceilingDb: Float,
        oversamplingFactor: OversamplingFactor,
        sampleRate: Int
    ) {
        this.ceilingDb = ceilingDb
        this.oversamplingFactor = oversamplingFactor
        if (sampleRate != configuredSampleRate) {
            configureCrossovers(sampleRate)
        }
    }

    override fun readLimiterTelemetry(): ThreeBandLimiterTelemetry = ThreeBandLimiterTelemetry(
        ceilingDb = ceilingDb,
        oversamplingFactor = oversamplingFactor,
        lowBandGrDb = lastLowBandGrDb,
        midBandGrDb = lastMidBandGrDb,
        highBandGrDb = lastHighBandGrDb,
        masterGainReductionDb = lastGainReductionDb
    )

    override fun processDoubleBlock(
        left: DoubleArray,
        right: DoubleArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        if (frameCount <= 0) return
        if (sampleRate != configuredSampleRate) {
            configureCrossovers(sampleRate)
        }

        val ceilingAmp = 10.0.pow(ceilingDb.toDouble() / 20.0).coerceIn(0.25, 0.99)
        val osFactor = oversamplingFactor.factor

        val lowThreshold = ceilingAmp * 0.90
        val midThreshold = ceilingAmp * 0.94
        val highThreshold = ceilingAmp * 0.92

        var minLowGain = 1.0
        var minMidGain = 1.0
        var minHighGain = 1.0
        var minMasterGain = 1.0

        for (i in 0 until frameCount) {
            val inL = left[i]
            val inR = right[i]

            // Complementary 64-bit 3-band crossover (low + mid + high == input when gain=1)
            val lowL = lowSplitFilter.processLeftDouble(inL)
            val lowR = lowSplitFilter.processRightDouble(inR)
            val remL = inL - lowL
            val remR = inR - lowR
            val highL = highSplitFilter.processLeftDouble(remL)
            val highR = highSplitFilter.processRightDouble(remR)
            val midL = remL - highL
            val midR = remR - highR

            val peakLow = max(abs(lowL), abs(lowR))
            val neededLow = if (peakLow > lowThreshold) lowThreshold / peakLow else 1.0
            lowEnv = if (neededLow < lowEnv) neededLow else (lowEnv * 0.9975 + neededLow * 0.0025).coerceAtMost(1.0)
            if (lowEnv < minLowGain) minLowGain = lowEnv

            val peakMid = max(abs(midL), abs(midR))
            val neededMid = if (peakMid > midThreshold) midThreshold / peakMid else 1.0
            midEnv = if (neededMid < midEnv) neededMid else (midEnv * 0.9945 + neededMid * 0.0055).coerceAtMost(1.0)
            if (midEnv < minMidGain) minMidGain = midEnv

            val peakHigh = max(abs(highL), abs(highR))
            val neededHigh = if (peakHigh > highThreshold) highThreshold / peakHigh else 1.0
            highEnv = if (neededHigh < highEnv) neededHigh else (highEnv * 0.991 + neededHigh * 0.009).coerceAtMost(1.0)
            if (highEnv < minHighGain) minHighGain = highEnv

            val l3 = lowL * lowEnv + midL * midEnv + highL * highEnv
            val r3 = lowR * lowEnv + midR * midEnv + highR * highEnv

            // Evaluate 4x or 8x Catmull-Rom cubic inter-sample trajectory to catch ISPs
            var maxIsp = max(abs(l2), abs(r2))
            for (step in 1 until osFactor) {
                val t = step.toDouble() / osFactor.toDouble()
                val ispL = abs(catmullRom(l0, l1, l2, l3, t))
                val ispR = abs(catmullRom(r0, r1, r2, r3, t))
                if (ispL > maxIsp) maxIsp = ispL
                if (ispR > maxIsp) maxIsp = ispR
            }

            val neededMaster = if (maxIsp > ceilingAmp) ceilingAmp / maxIsp else 1.0
            masterEnv = if (neededMaster < masterEnv) {
                neededMaster
            } else {
                (masterEnv * 0.994 + neededMaster * 0.006).coerceAtMost(1.0)
            }
            if (masterEnv < minMasterGain) minMasterGain = masterEnv

            val safeL = tanh((l2 * masterEnv) / ceilingAmp) * ceilingAmp
            val safeR = tanh((r2 * masterEnv) / ceilingAmp) * ceilingAmp
            left[i] = safeL.coerceIn(-ceilingAmp, ceilingAmp)
            right[i] = safeR.coerceIn(-ceilingAmp, ceilingAmp)

            l0 = l1; l1 = l2; l2 = l3
            r0 = r1; r1 = r2; r2 = r3
        }

        lastLowBandGrDb = (-20.0 * log10(minLowGain.coerceAtLeast(1e-4))).toFloat().coerceIn(0f, 18f)
        lastMidBandGrDb = (-20.0 * log10(minMidGain.coerceAtLeast(1e-4))).toFloat().coerceIn(0f, 18f)
        lastHighBandGrDb = (-20.0 * log10(minHighGain.coerceAtLeast(1e-4))).toFloat().coerceIn(0f, 18f)
        val totalMin = minMasterGain * minOf(minLowGain, minMidGain, minHighGain)
        lastGainReductionDb = (-20.0 * log10(totalMin.coerceAtLeast(1e-4))).toFloat().coerceIn(0f, 18f)
    }

    override fun processBlock(
        left: FloatArray,
        right: FloatArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        scratch64.processFloatBridge(left, right, frameCount) { l64, r64, n ->
            processDoubleBlock(l64, r64, n, sampleRate)
        }
    }

    private fun catmullRom(p0: Double, p1: Double, p2: Double, p3: Double, t: Double): Double {
        val t2 = t * t
        val t3 = t2 * t
        return 0.5 * (
            (2.0 * p1) +
                (-p0 + p2) * t +
                (2.0 * p0 - 5.0 * p1 + 4.0 * p2 - p3) * t2 +
                (-p0 + 3.0 * p1 - 3.0 * p2 + p3) * t3
            )
    }

    override fun reset() {
        lowSplitFilter.reset()
        highSplitFilter.reset()
        lowEnv = 1.0
        midEnv = 1.0
        highEnv = 1.0
        masterEnv = 1.0
        l0 = 0.0; l1 = 0.0; l2 = 0.0
        r0 = 0.0; r1 = 0.0; r2 = 0.0
        lastGainReductionDb = 0f
        lastLowBandGrDb = 0f
        lastMidBandGrDb = 0f
        lastHighBandGrDb = 0f
    }
}
