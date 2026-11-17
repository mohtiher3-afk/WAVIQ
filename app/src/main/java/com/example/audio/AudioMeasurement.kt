package com.example.audio

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.tan

/**
 * Standards-aligned audio telemetry & resampling utilities for offline mastering verification:
 * - ITU-R BS.1770-4 / EBU R128 K-weighted integrated loudness (400ms gated blocks, 75% overlap)
 * - 4x Catmull-Rom oversampled inter-sample True Peak (dBTP) & Sample Peak (dBFS)
 * - Anti-aliased sample-rate conversion (4th-order low-pass pre-filter when downsampling)
 */
object AudioMeasurement {

    private const val MIN_DB = -96.0f
    private const val SILENCE_THRESHOLD = 1e-9

    /**
     * Measures sample peak in dBFS across stereo buffers. Safely ignores non-finite values.
     */
    fun measureSamplePeakDb(left: FloatArray, right: FloatArray): Float {
        val len = minOf(left.size, right.size)
        if (len <= 0) return MIN_DB
        var peak = 0.0
        for (i in 0 until len) {
            val l = left[i]
            val r = right[i]
            if (l.isFinite()) {
                val al = abs(l.toDouble())
                if (al > peak) peak = al
            }
            if (r.isFinite()) {
                val ar = abs(r.toDouble())
                if (ar > peak) peak = ar
            }
        }
        return linearToDb(peak)
    }

    /**
     * Measures 4x Catmull-Rom oversampled inter-sample True Peak linear amplitude.
     */
    fun measureTruePeakLinear(left: FloatArray, right: FloatArray): Double {
        val len = minOf(left.size, right.size)
        if (len <= 0) return 0.0
        var maxPeak = 0.0
        val last = len - 1
        for (i in 0 until len) {
            val l1 = finiteDouble(left[i])
            val r1 = finiteDouble(right[i])
            val al = abs(l1)
            val ar = abs(r1)
            if (al > maxPeak) maxPeak = al
            if (ar > maxPeak) maxPeak = ar

            if (i < last) {
                val l0 = finiteDouble(left[(i - 1).coerceAtLeast(0)])
                val l2 = finiteDouble(left[i + 1])
                val l3 = finiteDouble(left[(i + 2).coerceAtMost(last)])

                val r0 = finiteDouble(right[(i - 1).coerceAtLeast(0)])
                val r2 = finiteDouble(right[i + 1])
                val r3 = finiteDouble(right[(i + 2).coerceAtMost(last)])

                // Evaluate sub-sample points t = 0.25, 0.50, 0.75
                for (step in 1..3) {
                    val t = step * 0.25
                    val ispL = abs(catmullRom(l0, l1, l2, l3, t))
                    val ispR = abs(catmullRom(r0, r1, r2, r3, t))
                    if (ispL > maxPeak) maxPeak = ispL
                    if (ispR > maxPeak) maxPeak = ispR
                }
            }
        }
        return maxPeak
    }

    /**
     * Measures 4x oversampled Inter-Sample True Peak in dBTP.
     */
    fun measureTruePeakDb(left: FloatArray, right: FloatArray): Float {
        val peak = measureTruePeakLinear(left, right)
        return linearToDb(peak)
    }

    /**
     * Complete standards-based BS.1770-4 / EBU R128 loudness profile.
     */
    data class DetailedLoudnessMetrics(
        val integratedLufs: Float,
        val momentaryMaxLufs: Float,
        val shortTermMaxLufs: Float,
        val loudnessRangeLu: Float
    )

    /**
     * Measures ITU-R BS.1770-4 / EBU R128 K-weighted integrated loudness (LUFS).
     * Applies sample-rate-accurate Stage 1 high-shelf + Stage 2 RLB high-pass K-weighting,
     * followed by 400ms / 100ms-hop dual-stage gating (-70 LUFS absolute, -10 LU relative).
     * For buffers shorter than 400ms, falls back to ungated K-weighted energy over available frames.
     */
    fun measureIntegratedLufs(
        left: FloatArray,
        right: FloatArray,
        sampleRate: Int = 48000
    ): Float = measureLoudnessDetails(left, right, sampleRate).integratedLufs

    /**
     * Computes ITU-R BS.1770-4 integrated LUFS alongside EBU R128 momentary max (400ms),
     * short-term max (3s), and Loudness Range (LRA in LU, 10th-95th percentile of gated 3s blocks).
     */
    fun measureLoudnessDetails(
        left: FloatArray,
        right: FloatArray,
        sampleRate: Int = 48000
    ): DetailedLoudnessMetrics {
        val len = minOf(left.size, right.size)
        if (len <= 0) return DetailedLoudnessMetrics(-70.0f, -70.0f, -70.0f, 0.0f)
        val sr = sampleRate.coerceAtLeast(8000)

        val stage1 = createBs1770Stage1Filter(sr)
        val stage2 = createBs1770Stage2Filter(sr)

        val energy = DoubleArray(len)
        var anyNonZero = false
        for (i in 0 until len) {
            val inL = finiteDouble(left[i])
            val inR = finiteDouble(right[i])
            if (inL != 0.0 || inR != 0.0) anyNonZero = true

            val yL = stage2.processLeftDouble(stage1.processLeftDouble(inL))
            val yR = stage2.processRightDouble(stage1.processRightDouble(inR))
            energy[i] = yL * yL + yR * yR
        }

        if (!anyNonZero) return DetailedLoudnessMetrics(-70.0f, -70.0f, -70.0f, 0.0f)

        val blockSize = (sr * 0.4).toInt().coerceAtLeast(1)
        val hopSize = (sr * 0.1).toInt().coerceAtLeast(1)

        if (len < blockSize) {
            var sum = 0.0
            for (i in 0 until len) sum += energy[i]
            val meanSq = sum / len.toDouble()
            if (meanSq <= 1e-12) return DetailedLoudnessMetrics(-70.0f, -70.0f, -70.0f, 0.0f)
            val lufs = (-0.691 + 10.0 * log10(meanSq)).toFloat().coerceIn(-70.0f, 0.0f)
            return DetailedLoudnessMetrics(lufs, lufs, lufs, 0.0f)
        }

        // Prefix sum for O(1) mean-square computation per gating block
        val prefix = DoubleArray(len + 1)
        for (i in 0 until len) {
            prefix[i + 1] = prefix[i] + energy[i]
        }

        val blockEnergies = ArrayList<Double>(max(1, (len - blockSize) / hopSize + 1))
        var maxMomentaryEnergy = 0.0
        var start = 0
        while (start + blockSize <= len) {
            val meanBlockEnergy = (prefix[start + blockSize] - prefix[start]) / blockSize.toDouble()
            blockEnergies.add(meanBlockEnergy)
            if (meanBlockEnergy > maxMomentaryEnergy) maxMomentaryEnergy = meanBlockEnergy
            start += hopSize
        }
        if (blockEnergies.isEmpty()) {
            val meanBlockEnergy = prefix[len] / len.toDouble()
            blockEnergies.add(meanBlockEnergy)
            maxMomentaryEnergy = meanBlockEnergy
        }

        val momentaryMaxLufs = if (maxMomentaryEnergy > 1e-12) {
            (-0.691 + 10.0 * log10(maxMomentaryEnergy)).toFloat().coerceIn(-70.0f, 0.0f)
        } else {
            -70.0f
        }

        // Gate 1: Absolute threshold (-70.0 LUFS)
        val absThresholdEnergy = 10.0.pow((-70.0 + 0.691) / 10.0)
        var absSum = 0.0
        var absCount = 0
        for (e in blockEnergies) {
            if (e > absThresholdEnergy) {
                absSum += e
                absCount++
            }
        }
        if (absCount == 0) return DetailedLoudnessMetrics(-70.0f, -70.0f, -70.0f, 0.0f)

        val gammaAbs = -0.691 + 10.0 * log10(absSum / absCount.toDouble())
        val relThresholdLu = gammaAbs - 10.0
        val relThresholdEnergy = 10.0.pow((relThresholdLu + 0.691) / 10.0)

        // Gate 2: Relative threshold (-10 LU below Stage-1 average)
        var relSum = 0.0
        var relCount = 0
        for (e in blockEnergies) {
            if (e > absThresholdEnergy && e > relThresholdEnergy) {
                relSum += e
                relCount++
            }
        }
        val integrated = if (relCount == 0) {
            gammaAbs.toFloat().coerceIn(-70.0f, 0.0f)
        } else {
            (-0.691 + 10.0 * log10(relSum / relCount.toDouble())).toFloat().coerceIn(-70.0f, 0.0f)
        }

        // Short-term (3.0s) sliding blocks for Short-Term Max & EBU R128 LRA
        val stWindow = (sr * 3.0).toInt().coerceAtLeast(blockSize)
        val stHop = (sr * 0.5).toInt().coerceAtLeast(hopSize)
        val stLufsList = ArrayList<Double>()
        var maxStEnergy = 0.0
        if (len >= stWindow) {
            var stStart = 0
            while (stStart + stWindow <= len) {
                val e = (prefix[stStart + stWindow] - prefix[stStart]) / stWindow.toDouble()
                if (e > maxStEnergy) maxStEnergy = e
                if (e > absThresholdEnergy) {
                    stLufsList.add(-0.691 + 10.0 * log10(e))
                }
                stStart += stHop
            }
        } else {
            val wholeE = prefix[len] / len.toDouble()
            maxStEnergy = wholeE
        }

        val shortTermMaxLufs = if (maxStEnergy > 1e-12) {
            (-0.691 + 10.0 * log10(maxStEnergy)).toFloat().coerceIn(-70.0f, 0.0f)
        } else {
            integrated
        }

        var lraLu = 0.0f
        if (stLufsList.size >= 2) {
            val avgStPower = stLufsList.sumOf { 10.0.pow((it + 0.691) / 10.0) } / stLufsList.size
            val stMeanLu = -0.691 + 10.0 * log10(avgStPower.coerceAtLeast(1e-12))
            val lraGated = stLufsList.filter { it >= stMeanLu - 20.0 }.sorted()
            if (lraGated.size >= 2) {
                val p10Idx = ((lraGated.size - 1) * 0.10).toInt().coerceIn(0, lraGated.lastIndex)
                val p95Idx = ((lraGated.size - 1) * 0.95).toInt().coerceIn(0, lraGated.lastIndex)
                lraLu = (lraGated[p95Idx] - lraGated[p10Idx]).toFloat().coerceIn(0.0f, 35.0f)
            }
        }

        return DetailedLoudnessMetrics(
            integratedLufs = integrated,
            momentaryMaxLufs = momentaryMaxLufs,
            shortTermMaxLufs = shortTermMaxLufs,
            loudnessRangeLu = lraLu
        )
    }

    /**
     * Resamples stereo buffers from [srcSr] to [dstSr].
     * Applies a cascaded 4th-order low-pass anti-aliasing filter (fc = 0.45 * dstSr)
     * whenever downsampling ([dstSr] < [srcSr]) prior to Catmull-Rom cubic interpolation.
     */
    fun resampleWithAntiAliasing(
        srcL: FloatArray,
        srcR: FloatArray,
        srcSr: Int,
        dstSr: Int
    ): Pair<FloatArray, FloatArray> {
        val total = minOf(srcL.size, srcR.size)
        if (total <= 0) return FloatArray(0) to FloatArray(0)
        if (srcSr == dstSr || srcSr <= 0 || dstSr <= 0) {
            return srcL.copyOf(total) to srcR.copyOf(total)
        }

        val filteredL: FloatArray
        val filteredR: FloatArray
        if (dstSr < srcSr) {
            val cutoffHz = (dstSr * 0.45f).coerceIn(1000f, srcSr * 0.45f)
            val lp1 = BiquadFilter(srcSr.toDouble()).apply {
                configure(FilterType.LOW_PASS, cutoffHz, 0.5411961f, 0f, srcSr.toFloat())
            }
            val lp2 = BiquadFilter(srcSr.toDouble()).apply {
                configure(FilterType.LOW_PASS, cutoffHz, 1.3065630f, 0f, srcSr.toFloat())
            }
            filteredL = FloatArray(total)
            filteredR = FloatArray(total)
            for (i in 0 until total) {
                val l = lp2.processLeftDouble(lp1.processLeftDouble(finiteDouble(srcL[i])))
                val r = lp2.processRightDouble(lp1.processRightDouble(finiteDouble(srcR[i])))
                filteredL[i] = l.toFloat()
                filteredR[i] = r.toFloat()
            }
        } else {
            filteredL = srcL
            filteredR = srcR
        }

        val ratio = srcSr.toDouble() / dstSr.toDouble()
        val newLen = (total / ratio).toInt().coerceAtLeast(1)
        val outL = FloatArray(newLen)
        val outR = FloatArray(newLen)
        val last = total - 1

        for (i in 0 until newLen) {
            val pos = i * ratio
            val i1 = pos.toInt().coerceIn(0, last)
            val i0 = (i1 - 1).coerceAtLeast(0)
            val i2 = (i1 + 1).coerceAtMost(last)
            val i3 = (i1 + 2).coerceAtMost(last)
            val t = pos - i1
            outL[i] = catmullRom(
                finiteDouble(filteredL[i0]),
                finiteDouble(filteredL[i1]),
                finiteDouble(filteredL[i2]),
                finiteDouble(filteredL[i3]),
                t
            ).coerceIn(-1.0, 1.0).toFloat()
            outR[i] = catmullRom(
                finiteDouble(filteredR[i0]),
                finiteDouble(filteredR[i1]),
                finiteDouble(filteredR[i2]),
                finiteDouble(filteredR[i3]),
                t
            ).coerceIn(-1.0, 1.0).toFloat()
        }
        return outL to outR
    }

    private fun linearToDb(amp: Double): Float {
        if (!amp.isFinite() || amp <= SILENCE_THRESHOLD) return MIN_DB
        return (20.0 * log10(amp)).toFloat().coerceIn(MIN_DB, 6.0f)
    }

    private fun finiteDouble(v: Float): Double = if (v.isFinite()) v.toDouble() else 0.0

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

    private fun createBs1770Stage1Filter(sampleRate: Int): DirectBiquad {
        val sr = sampleRate.toDouble()
        val db = 3.999843853973347
        val f0 = 1681.974450955533
        val q = 0.7071752369554196
        val k = tan(PI * f0 / sr)
        val vh = 10.0.pow(db / 20.0)
        val vb = vh.pow(0.4996667741545416)
        val a0 = 1.0 + k / q + k * k
        return DirectBiquad(
            b0 = (vh + vb * k / q + k * k) / a0,
            b1 = (2.0 * (k * k - vh)) / a0,
            b2 = (vh - vb * k / q + k * k) / a0,
            a1 = (2.0 * (k * k - 1.0)) / a0,
            a2 = (1.0 - k / q + k * k) / a0
        )
    }

    private fun createBs1770Stage2Filter(sampleRate: Int): DirectBiquad {
        val sr = sampleRate.toDouble()
        val f0 = 38.13547087602444
        val q = 0.5003270373238773
        val k = tan(PI * f0 / sr)
        val a0 = 1.0 + k / q + k * k
        return DirectBiquad(
            b0 = 1.0,
            b1 = -2.0,
            b2 = 1.0,
            a1 = (2.0 * (k * k - 1.0)) / a0,
            a2 = (1.0 - k / q + k * k) / a0
        )
    }

    private class DirectBiquad(
        private val b0: Double,
        private val b1: Double,
        private val b2: Double,
        private val a1: Double,
        private val a2: Double
    ) {
        private var x1L = 0.0
        private var x2L = 0.0
        private var y1L = 0.0
        private var y2L = 0.0

        private var x1R = 0.0
        private var x2R = 0.0
        private var y1R = 0.0
        private var y2R = 0.0

        fun processLeftDouble(x: Double): Double {
            val y = b0 * x + b1 * x1L + b2 * x2L - a1 * y1L - a2 * y2L
            x2L = x1L
            x1L = x
            y2L = y1L
            y1L = if (y.isFinite()) y else 0.0
            return y1L
        }

        fun processRightDouble(x: Double): Double {
            val y = b0 * x + b1 * x1R + b2 * x2R - a1 * y1R - a2 * y2R
            x2R = x1R
            x1R = x
            y2R = y1R
            y1R = if (y.isFinite()) y else 0.0
            return y1R
        }
    }
}
