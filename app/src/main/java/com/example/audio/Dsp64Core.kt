package com.example.audio

import androidx.compose.runtime.Immutable
import com.example.model.OversamplingFactor

/**
 * Configuration snapshot for WAVIQ's 64-bit IEEE-754 double-precision DSP bus.
 */
@Immutable
data class Dsp64EngineConfig(
    val sampleRate: Int = 48000,
    val targetHiResKhz: Int = 96,
    val oversamplingFactor: OversamplingFactor = OversamplingFactor.X4,
    val ceilingDb: Float = -1.0f
)

/**
 * Live metering snapshot emitted by a [ThreeBandTruePeakLimiter] stage.
 */
@Immutable
data class ThreeBandLimiterTelemetry(
    val ceilingDb: Float = -1.0f,
    val oversamplingFactor: OversamplingFactor = OversamplingFactor.X4,
    val lowBandGrDb: Float = 0f,
    val midBandGrDb: Float = 0f,
    val highBandGrDb: Float = 0f,
    val masterGainReductionDb: Float = 0f
)

/**
 * State and acoustic restoration metrics emitted by an [Sbr192KhzRestorationModule].
 */
@Immutable
data class SbrRestorationMetrics(
    val targetKhz: Int = 96,
    val configuredSampleRateHz: Int = 48000,
    val restorationAmount: Float = 0.65f,
    val stereoExpansionAmount: Float = 0.48f,
    val hfCrossoverHz: Float = 4500f,
    val is192KhzUltraMode: Boolean = false
)

/**
 * Core 64-bit IEEE-754 Double DSP stage contract.
 * Extends [DspHook] so every 64-bit stage remains directly pluggable into real-time
 * [FloatArray] chains while supporting zero-truncation [DoubleArray] block chaining.
 */
interface Dsp64Stage : DspHook {
    fun processDoubleBlock(
        left: DoubleArray,
        right: DoubleArray,
        frameCount: Int,
        sampleRate: Int
    )

    override fun processBlock(
        left: FloatArray,
        right: FloatArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        if (frameCount <= 0) return
        val n = minOf(frameCount, left.size, right.size)
        val l64 = DoubleArray(n) { left[it].toDouble() }
        val r64 = DoubleArray(n) { right[it].toDouble() }
        processDoubleBlock(l64, r64, n, sampleRate)
        for (i in 0 until n) {
            left[i] = l64[i].toFloat()
            right[i] = r64[i].toFloat()
        }
    }
}

/**
 * Interface for the 64-bit 3-Band True-Peak Inter-Sample Oversampled Brickwall Limiter.
 * Splits Low / Mid / High bands independently to avoid sub-bass pumping vocals/air,
 * then enforces a multi-times (4X / 8X) True-Peak ceiling.
 */
interface ThreeBandTruePeakLimiter : Dsp64Stage {
    var ceilingDb: Float
    var oversamplingFactor: OversamplingFactor

    val lowCrossoverHz: Float
    val highCrossoverHz: Float

    val lastGainReductionDb: Float
    val lastLowBandGrDb: Float
    val lastMidBandGrDb: Float
    val lastHighBandGrDb: Float

    fun configure(
        ceilingDb: Float = this.ceilingDb,
        oversamplingFactor: OversamplingFactor = this.oversamplingFactor,
        sampleRate: Int = 48000
    )

    fun readLimiterTelemetry(): ThreeBandLimiterTelemetry
}

/**
 * Interface for the 64-bit 192kHz Spectral Band Replication (SBR) high-frequency restoration
 * and phase-coherent upper-band stereo shimmer module.
 */
interface Sbr192KhzRestorationModule : Dsp64Stage {
    var restorationAmount: Float
    var targetKhz: Int
    var stereoExpansionAmount: Float

    val hfCrossoverHz: Float

    fun configure(
        sampleRate: Int,
        targetKhz: Int = this.targetKhz,
        restorationAmount: Float = this.restorationAmount,
        stereoExpansionAmount: Float = this.stereoExpansionAmount
    )

    fun readSbrMetrics(): SbrRestorationMetrics
}

/**
 * Contract for WAVIQ's 64-bit internal DSP master bus coordinating sequential [Dsp64Stage]
 * execution without intermediate Float32 truncation or premature hard-clipping.
 */
interface Dsp64BusEngine {
    val sbrModule: Sbr192KhzRestorationModule
    val truePeakLimiter: ThreeBandTruePeakLimiter
    val stages: List<Dsp64Stage>

    fun processChainDouble(
        left: DoubleArray,
        right: DoubleArray,
        frameCount: Int,
        sampleRate: Int
    )

    fun processChainFloat(
        left: FloatArray,
        right: FloatArray,
        frameCount: Int,
        sampleRate: Int
    )

    fun resetAll()
}

/**
 * Default zero-allocation (after initial sizing) 64-bit IEEE-754 DSP master pipeline.
 */
class Waviq64BitDspPipeline(
    override val stages: List<Dsp64Stage>,
    override val sbrModule: Sbr192KhzRestorationModule,
    override val truePeakLimiter: ThreeBandTruePeakLimiter
) : Dsp64BusEngine {

    private var scratchL = DoubleArray(1024)
    private var scratchR = DoubleArray(1024)

    override fun processChainDouble(
        left: DoubleArray,
        right: DoubleArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        if (frameCount <= 0) return
        val n = minOf(frameCount, left.size, right.size)
        for (stage in stages) {
            if (stage.isEnabled) {
                stage.processDoubleBlock(left, right, n, sampleRate)
            }
        }
    }

    override fun processChainFloat(
        left: FloatArray,
        right: FloatArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        if (frameCount <= 0) return
        val n = minOf(frameCount, left.size, right.size)
        if (scratchL.size < n) {
            scratchL = DoubleArray(n)
            scratchR = DoubleArray(n)
        }
        for (i in 0 until n) {
            scratchL[i] = left[i].toDouble()
            scratchR[i] = right[i].toDouble()
        }
        processChainDouble(scratchL, scratchR, n, sampleRate)
        for (i in 0 until n) {
            left[i] = scratchL[i].toFloat()
            right[i] = scratchR[i].toFloat()
        }
    }

    override fun resetAll() {
        stages.forEach { it.reset() }
    }
}

typealias WaviqSbr192KhzModule = HiResUpsamplingHook
typealias WaviqThreeBandTruePeakLimiter = TruePeakLimiterHook
