package com.example.audio

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

enum class FilterType {
    PEAKING,
    LOW_SHELF,
    HIGH_SHELF,
    LOW_PASS,
    HIGH_PASS,
    BAND_PASS
}

class BiquadFilter(
    private var sampleRate: Double = 48000.0
) {
    constructor(sampleRate: Float) : this(sampleRate.toDouble())

    private var b0 = 1.0
    private var b1 = 0.0
    private var b2 = 0.0
    private var a1 = 0.0
    private var a2 = 0.0

    // 64-bit IEEE-754 Left & Right delay lines prevent denormal drift & quantization error at 192kHz
    private var x1L = 0.0
    private var x2L = 0.0
    private var y1L = 0.0
    private var y2L = 0.0

    private var x1R = 0.0
    private var x2R = 0.0
    private var y1R = 0.0
    private var y2R = 0.0

    fun configure(
        type: FilterType,
        freqHz: Float,
        q: Float,
        gainDb: Float,
        sr: Float = sampleRate.toFloat()
    ) {
        sampleRate = sr.toDouble().coerceAtLeast(8000.0)
        val f0 = freqHz.toDouble().coerceIn(20.0, sampleRate * 0.45)
        val safeQ = q.toDouble().coerceIn(0.1, 12.0)
        val w0 = 2.0 * PI * f0 / sampleRate
        val cosW0 = cos(w0)
        val sinW0 = sin(w0)
        val alpha = sinW0 / (2.0 * safeQ)
        val a = 10.0.pow(gainDb.toDouble() / 40.0)

        var nb0 = 1.0
        var nb1 = 0.0
        var nb2 = 0.0
        var na0 = 1.0
        var na1 = 0.0
        var na2 = 0.0

        when (type) {
            FilterType.PEAKING -> {
                nb0 = 1.0 + alpha * a
                nb1 = -2.0 * cosW0
                nb2 = 1.0 - alpha * a
                na0 = 1.0 + alpha / a
                na1 = -2.0 * cosW0
                na2 = 1.0 - alpha / a
            }
            FilterType.LOW_SHELF -> {
                val twoSqrtAAlpha = 2.0 * sqrt(a) * alpha
                nb0 = a * ((a + 1.0) - (a - 1.0) * cosW0 + twoSqrtAAlpha)
                nb1 = 2.0 * a * ((a - 1.0) - (a + 1.0) * cosW0)
                nb2 = a * ((a + 1.0) - (a - 1.0) * cosW0 - twoSqrtAAlpha)
                na0 = (a + 1.0) + (a - 1.0) * cosW0 + twoSqrtAAlpha
                na1 = -2.0 * ((a - 1.0) + (a + 1.0) * cosW0)
                na2 = (a + 1.0) + (a - 1.0) * cosW0 - twoSqrtAAlpha
            }
            FilterType.HIGH_SHELF -> {
                val twoSqrtAAlpha = 2.0 * sqrt(a) * alpha
                nb0 = a * ((a + 1.0) + (a - 1.0) * cosW0 + twoSqrtAAlpha)
                nb1 = -2.0 * a * ((a - 1.0) + (a + 1.0) * cosW0)
                nb2 = a * ((a + 1.0) + (a - 1.0) * cosW0 - twoSqrtAAlpha)
                na0 = (a + 1.0) - (a - 1.0) * cosW0 + twoSqrtAAlpha
                na1 = 2.0 * ((a - 1.0) - (a + 1.0) * cosW0)
                na2 = (a + 1.0) - (a - 1.0) * cosW0 - twoSqrtAAlpha
            }
            FilterType.LOW_PASS -> {
                nb0 = (1.0 - cosW0) / 2.0
                nb1 = 1.0 - cosW0
                nb2 = (1.0 - cosW0) / 2.0
                na0 = 1.0 + alpha
                na1 = -2.0 * cosW0
                na2 = 1.0 - alpha
            }
            FilterType.HIGH_PASS -> {
                nb0 = (1.0 + cosW0) / 2.0
                nb1 = -(1.0 + cosW0)
                nb2 = (1.0 + cosW0) / 2.0
                na0 = 1.0 + alpha
                na1 = -2.0 * cosW0
                na2 = 1.0 - alpha
            }
            FilterType.BAND_PASS -> {
                nb0 = alpha
                nb1 = 0.0
                nb2 = -alpha
                na0 = 1.0 + alpha
                na1 = -2.0 * cosW0
                na2 = 1.0 - alpha
            }
        }

        b0 = nb0 / na0
        b1 = nb1 / na0
        b2 = nb2 / na0
        a1 = na1 / na0
        a2 = na2 / na0
    }

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

    fun processLeft(x: Float): Float = processLeftDouble(x.toDouble()).toFloat()

    fun processRight(x: Float): Float = processRightDouble(x.toDouble()).toFloat()

    fun reset() {
        x1L = 0.0; x2L = 0.0; y1L = 0.0; y2L = 0.0
        x1R = 0.0; x2R = 0.0; y1R = 0.0; y2R = 0.0
    }

    fun magnitudeDbAt(freqHz: Float): Float {
        val w = (2.0 * PI * freqHz / sampleRate)
        val cosW = cos(w)
        val sinW = sin(w)
        val cos2W = cos(2.0 * w)
        val sin2W = sin(2.0 * w)

        val numRe = b0 + b1 * cosW + b2 * cos2W
        val numIm = -b1 * sinW - b2 * sin2W
        val denRe = 1.0 + a1 * cosW + a2 * cos2W
        val denIm = -a1 * sinW - a2 * sin2W

        val numMag2 = numRe * numRe + numIm * numIm
        val denMag2 = (denRe * denRe + denIm * denIm).coerceAtLeast(1e-12)
        val mag = sqrt(numMag2 / denMag2).coerceAtLeast(1e-6)
        return (20.0 * kotlin.math.log10(mag)).toFloat()
    }
}
