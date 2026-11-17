package com.example.audio

import android.content.Context
import android.util.Log
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.Environment
import com.google.ai.edge.litert.TensorBuffer
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * On-device AI denoising and dynamic mastering [DspHook] powered by LiteRT (.tflite).
 * Lazily initializes [CompiledModel] with CPU XNNPACK options and reusable [TensorBuffer]s on
 * the audio processing thread, with a deterministic DSP spectral-gating fallback when native
 * runtime libraries are unavailable.
 */
class TfliteDspHook(
    private val context: Context,
    private val modelAssetName: String = "ai_audio_mastering_model.tflite",
    override val id: String = "tflite_ai_master",
    override val name: String = "On-Device TFLite AI Denoise & Master",
    override var isEnabled: Boolean = true
) : DspHook, AutoCloseable {

    override val baselineLatencyMs: Float
        get() = if (runtimeMode == "LITERT_COMPILED") 0.85f else 0.42f

    private val lock = Any()
    private var initialized = false
    private var compiledModel: CompiledModel? = null
    private var inputBuffers: List<TensorBuffer> = emptyList()
    private var outputBuffers: List<TensorBuffer> = emptyList()

    private val featureArray = FloatArray(4)
    private var smoothNoiseGate = 1.0f
    private var smoothDynGain = 1.0f

    @Volatile
    var runtimeMode: String = "INITIALIZING"
        private set

    @Volatile
    var lastGateReductionPct: Float = 0f
        private set

    @Volatile
    var lastDynGainDb: Float = 0f
        private set

    private fun ensureInitializedOnBackgroundThread() {
        if (initialized) return
        synchronized(lock) {
            if (initialized) return
            initialized = true
            runCatching {
                val env = getOrCreateSharedEnv() ?: error("LiteRT environment unavailable")
                val options = CompiledModel.Options(Accelerator.CPU).apply {
                    cpuOptions = CompiledModel.CpuOptions(numThreads = 2)
                }
                val model = CompiledModel.create(
                    context.assets,
                    modelAssetName,
                    options,
                    env
                )
                val ins = model.createInputBuffers()
                val outs = model.createOutputBuffers()
                // Warm-up inference with pre-allocated reusable buffers
                ins[0].writeFloat(floatArrayOf(0.15f, 0.45f, 0.10f, 0.25f))
                model.run(ins, outs)
                outs[0].readFloat()

                compiledModel = model
                inputBuffers = ins
                outputBuffers = outs
                runtimeMode = "LITERT_COMPILED"
            }.onFailure { err ->
                runCatching {
                    Log.w("TfliteDspHook", "Falling back to DSP spectral gate ($modelAssetName): ${err.message}")
                }
                closeInternal()
                runtimeMode = "DSP_FALLBACK"
            }
        }
    }

    override fun processBlock(
        left: FloatArray,
        right: FloatArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        if (frameCount <= 0) return
        ensureInitializedOnBackgroundThread()

        // Extract compact block telemetry features [rms, peak, zcrNoise, hfEnergy]
        var sumSq = 0f
        var peak = 0f
        var zeroCrossings = 0
        var hfDiffSum = 0f
        var prevMid = 0f

        for (i in 0 until frameCount) {
            val mid = 0.5f * (left[i] + right[i])
            sumSq += mid * mid
            val a = abs(mid)
            if (a > peak) peak = a
            if (i > 0) {
                if ((mid >= 0f && prevMid < 0f) || (mid < 0f && prevMid >= 0f)) {
                    zeroCrossings++
                }
                hfDiffSum += abs(mid - prevMid)
            }
            prevMid = mid
        }

        val rms = sqrt((sumSq / frameCount).coerceAtLeast(1e-12f)).coerceIn(0f, 1f)
        val zcr = (zeroCrossings.toFloat() / frameCount).coerceIn(0f, 1f)
        val hfEnergy = (hfDiffSum / frameCount).coerceIn(0f, 1f)

        val (targetDenoiseStrength, targetDynGain) = runInference(rms, peak, zcr, hfEnergy)

        // Smooth block-to-block parameter trajectories to prevent pumping
        smoothNoiseGate = smoothNoiseGate * 0.75f + targetDenoiseStrength * 0.25f
        smoothDynGain = smoothDynGain * 0.80f + targetDynGain * 0.20f

        val noiseFloorThreshold = 0.008f + smoothNoiseGate * 0.020f
        val gain = smoothDynGain.coerceIn(0.75f, 1.45f)
        var gateSum = 0f

        for (i in 0 until frameCount) {
            var l = left[i]
            var r = right[i]
            val env = 0.5f * (abs(l) + abs(r))
            // Soft downward expander suppresses low-level hiss/noise below threshold
            val gateFactor = if (env < noiseFloorThreshold) {
                val ratio = (env / noiseFloorThreshold).coerceIn(0f, 1f)
                0.35f + 0.65f * (ratio * ratio)
            } else {
                1.0f
            }
            gateSum += gateFactor
            l = tanh(l * gateFactor * gain)
            r = tanh(r * gateFactor * gain)
            left[i] = l
            right[i] = r
        }

        val avgGateFactor = (gateSum / frameCount).coerceIn(0f, 1f)
        lastGateReductionPct = ((1f - avgGateFactor) * 100f).coerceIn(0f, 65f)
        lastDynGainDb = (20f * log10(gain.coerceAtLeast(1e-4f))).coerceIn(-6f, 6f)
    }

    private fun runInference(
        rms: Float,
        peak: Float,
        zcr: Float,
        hfEnergy: Float
    ): Pair<Float, Float> {
        val model = compiledModel
        if (model != null && inputBuffers.isNotEmpty() && outputBuffers.isNotEmpty()) {
            val out = runCatching {
                featureArray[0] = rms
                featureArray[1] = peak
                featureArray[2] = zcr
                featureArray[3] = hfEnergy
                inputBuffers[0].writeFloat(featureArray)
                model.run(inputBuffers, outputBuffers)
                outputBuffers[0].readFloat()
            }.getOrNull()
            if (out != null && out.size >= 2 && out[0].isFinite() && out[1].isFinite()) {
                return out[0].coerceIn(0f, 1f) to out[1].coerceIn(0.75f, 1.45f)
            }
        }
        // Deterministic fallback mirroring the bundled TFLite FC weights
        val denoise = (-0.65f * rms - 0.10f * peak + 1.25f * zcr - 0.25f * hfEnergy + 0.20f)
            .coerceIn(0f, 1f)
        val dynGain = (-0.55f * rms - 0.45f * peak - 0.15f * zcr + 0.20f * hfEnergy + 1.15f)
            .coerceIn(0.75f, 1.45f)
        return denoise to dynGain
    }

    override fun reset() {
        smoothNoiseGate = 1.0f
        smoothDynGain = 1.0f
        lastGateReductionPct = 0f
        lastDynGainDb = 0f
    }

    override fun close() {
        synchronized(lock) {
            closeInternal()
        }
    }

    private fun closeInternal() {
        runCatching {
            (inputBuffers + outputBuffers).forEach { it.close() }
            compiledModel?.close()
        }
        inputBuffers = emptyList()
        outputBuffers = emptyList()
        compiledModel = null
    }

    companion object {
        @Volatile
        private var cachedEnv: Environment? = null

        private fun getOrCreateSharedEnv(): Environment? {
            return cachedEnv ?: synchronized(this) {
                cachedEnv ?: runCatching { Environment.create() }.getOrNull()?.also {
                    cachedEnv = it
                }
            }
        }
    }
}
