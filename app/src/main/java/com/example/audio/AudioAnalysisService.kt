package com.example.audio

import android.content.Context
import com.example.model.AudioAnalysisReport
import com.example.model.AudioTechnicalFinding
import com.example.model.FindingSeverity
import com.example.model.RhythmEstimate
import com.example.model.VocalActivityEstimate
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Phase 2 Audio Analysis & Taxonomy Mapping Service.
 *
 * Strictly separates:
 * 1) Objective standards-based / filter-based acoustic measurements
 * 2) Vocal/speech activity detection & onset-autocorrelation tempo estimation
 * 3) Technical defect & headroom findings
 * 4) Heuristic multi-label style candidates mapped against [music_taxonomy.json]
 *
 * File names are never allowed to override contradictory acoustic evidence or synthesize BPM/mood.
 */
object AudioAnalysisService {

    private const val STYLE_CONFIDENCE_THRESHOLD = 0.50f
    private const val BPM_CONFIDENCE_THRESHOLD = 0.45f

    fun analyze(
        context: Context? = null,
        left: FloatArray,
        right: FloatArray,
        sampleRate: Int,
        channels: Int,
        titleHint: String? = null
    ): AudioAnalysisReport {
        val taxonomy = MusicTaxonomyRepository.load(context)
        val len = minOf(left.size, right.size)
        val sr = sampleRate.coerceAtLeast(8000)
        if (len <= 0) {
            return AudioAnalysisReport(
                taxonomyVersion = taxonomy.version,
                isUnknownOrMixedStyle = true,
                primaryTaxonomyId = MusicTaxonomyRepository.UNKNOWN_MIXED_ID,
                primaryStyleLabel = "Unknown / Mixed",
                styleConfidence = 0f
            )
        }

        val loudness = AudioMeasurement.measureLoudnessDetails(left, right, sr)
        val samplePeakDb = AudioMeasurement.measureSamplePeakDb(left, right)
        val truePeakDb = AudioMeasurement.measureTruePeakDb(left, right)

        // Sub-sample or inspect multi-window windows up to ~30s total for Biquad spectral split & onset flux
        val maxSpectralFrames = sr * 30
        val segments: List<IntRange> = if (len <= maxSpectralFrames) {
            listOf(0 until len)
        } else {
            val segLen = sr * 10
            val midStart = ((len / 2) - (segLen / 2)).coerceIn(0, (len - segLen).coerceAtLeast(0))
            val lateStart = (len - segLen).coerceAtLeast(0)
            listOf(0 until segLen, midStart until (midStart + segLen), lateStart until len)
        }

        val lowFilter = BiquadFilter(sr.toDouble()).apply {
            configure(FilterType.LOW_PASS, 250f, 0.707f, 0f, sr.toFloat())
        }
        val midHpFilter = BiquadFilter(sr.toDouble()).apply {
            configure(FilterType.HIGH_PASS, 250f, 0.707f, 0f, sr.toFloat())
        }
        val midLpFilter = BiquadFilter(sr.toDouble()).apply {
            configure(FilterType.LOW_PASS, 4000f, 0.707f, 0f, sr.toFloat())
        }
        val highFilter = BiquadFilter(sr.toDouble()).apply {
            configure(FilterType.HIGH_PASS, 4000f, 0.707f, 0f, sr.toFloat())
        }
        val vocalFilter = BiquadFilter(sr.toDouble()).apply {
            configure(FilterType.BAND_PASS, 1200f, 0.70f, 0f, sr.toFloat())
        }

        var sumSq = 0.0
        var lrDot = 0.0
        var lSq = 0.0
        var rSq = 0.0
        var midSq = 0.0
        var sideSq = 0.0
        var lowSq = 0.0
        var midBandSq = 0.0
        var highBandSq = 0.0
        var vocalBandSq = 0.0
        var hfDiff2Sq = 0.0
        var totalSamplesAnalyzed = 0

        // Frame-level envelopes (~20ms hops) for vocal modulation & onset BPM estimation
        val frameHop = (sr * 0.02).toInt().coerceAtLeast(160)
        val onsetFrames = ArrayList<Double>()
        val vocalEnvFrames = ArrayList<Double>()
        var blockEnergyAcc = 0.0
        var blockVocalAcc = 0.0
        var blockCounter = 0

        for (seg in segments) {
            lowFilter.reset()
            midHpFilter.reset()
            midLpFilter.reset()
            highFilter.reset()
            vocalFilter.reset()
            var prevMid = 0.0
            var prevPrevMid = 0.0
            var segCount = 0

            for (i in seg) {
                val l = if (left[i].isFinite()) left[i].toDouble() else 0.0
                val r = if (right[i].isFinite()) right[i].toDouble() else 0.0

                sumSq += 0.5 * (l * l + r * r)
                lrDot += l * r
                lSq += l * l
                rSq += r * r

                val m = 0.5 * (l + r)
                val s = 0.5 * (l - r)
                midSq += m * m
                sideSq += s * s

                val lpL = lowFilter.processLeftDouble(l)
                val lpR = lowFilter.processRightDouble(r)
                val bpL = midLpFilter.processLeftDouble(midHpFilter.processLeftDouble(l))
                val bpR = midLpFilter.processRightDouble(midHpFilter.processRightDouble(r))
                val hpL = highFilter.processLeftDouble(l)
                val hpR = highFilter.processRightDouble(r)

                lowSq += 0.5 * (lpL * lpL + lpR * lpR)
                midBandSq += 0.5 * (bpL * bpL + bpR * bpR)
                highBandSq += 0.5 * (hpL * hpL + hpR * hpR)

                val voc = vocalFilter.processLeftDouble(m)
                vocalBandSq += voc * voc
                blockVocalAcc += voc * voc
                blockEnergyAcc += m * m
                blockCounter++

                if (blockCounter >= frameHop) {
                    onsetFrames.add(sqrt(blockEnergyAcc / blockCounter))
                    vocalEnvFrames.add(sqrt(blockVocalAcc / blockCounter))
                    blockEnergyAcc = 0.0
                    blockVocalAcc = 0.0
                    blockCounter = 0
                }

                if (segCount >= 2) {
                    val d2 = m - 2.0 * prevMid + prevPrevMid
                    hfDiff2Sq += d2 * d2
                }
                prevPrevMid = prevMid
                prevMid = m
                segCount++
                totalSamplesAnalyzed++
            }
        }

        val denom = totalSamplesAnalyzed.coerceAtLeast(1).toDouble()
        val rms = sqrt((sumSq / denom).coerceAtLeast(1e-12)).toFloat()
        val rmsDb = (20f * log10(rms.coerceAtLeast(1e-6f))).coerceIn(-60f, 0f)
        val crestFactorDb = (samplePeakDb - rmsDb).coerceIn(1.5f, 24f)

        val totalBandEnergy = (lowSq + midBandSq + highBandSq).coerceAtLeast(1e-12)
        val lowPct = ((lowSq / totalBandEnergy) * 100.0).roundToInt().coerceIn(0, 100)
        val midPct = ((midBandSq / totalBandEnergy) * 100.0).roundToInt().coerceIn(0, 100)
        val highPct = (100 - lowPct - midPct).coerceIn(0, 100)

        val stereoWidthPct = if (channels <= 1) {
            5
        } else {
            ((sideSq / (midSq + sideSq).coerceAtLeast(1e-12)) * 200.0).roundToInt().coerceIn(5, 100)
        }
        val corrDenom = sqrt((lSq * rSq).coerceAtLeast(1e-18))
        val stereoCorrelation = if (channels <= 1) {
            1.0f
        } else {
            (lrDot / corrDenom).toFloat().coerceIn(-1.0f, 1.0f)
        }
        val hasPhaseRisk = channels >= 2 && stereoCorrelation < 0.15f

        val hfRatio = sqrt((hfDiff2Sq / midSq.coerceAtLeast(1e-12)).coerceAtLeast(0.0)).toFloat()
        val estimatedBandwidthKhz = (9.5f + hfRatio * 8.2f).coerceIn(9.5f, 20.0f)

        // Separate Task A: Vocal / Speech Activity Estimation (center 300Hz-3.4kHz ratio + syllabic modulation)
        val vocalEstimate = estimateVocalActivity(
            vocalBandSq = vocalBandSq,
            midSq = midSq,
            sideSq = sideSq,
            vocalEnvFrames = vocalEnvFrames,
            lowPct = lowPct,
            midPct = midPct
        )

        // Separate Task B: Tempo / Onset Autocorrelation (no filename guesswork!)
        val rhythmEstimate = estimateRhythm(
            onsetFrames = onsetFrames,
            frameRateHz = sr.toDouble() / frameHop.toDouble()
        )

        // Technical Issues & Mastering Diagnostics
        val findings = buildTechnicalFindings(
            integratedLufs = loudness.integratedLufs,
            truePeakDb = truePeakDb,
            crestFactorDb = crestFactorDb,
            lowPct = lowPct,
            midPct = midPct,
            highPct = highPct,
            stereoCorrelation = stereoCorrelation,
            hasPhaseRisk = hasPhaseRisk,
            bandwidthKhz = estimatedBandwidthKhz,
            vocalPresencePct = vocalEstimate.presencePct
        )

        // Multi-label Heuristic Style Scoring strictly grounded in acoustic metrics
        val styleScoring = scoreStylesAcoustically(
            taxonomy = taxonomy,
            channels = channels,
            integratedLufs = loudness.integratedLufs,
            crestFactorDb = crestFactorDb,
            lowPct = lowPct,
            midPct = midPct,
            highPct = highPct,
            stereoWidthPct = stereoWidthPct,
            bpm = rhythmEstimate.bpm,
            vocalPresencePct = vocalEstimate.presencePct,
            hfBandwidthKhz = estimatedBandwidthKhz,
            isPureStaticTone = isNearMonochromaticTone(onsetFrames, highPct, lowPct, midPct)
        )

        val moodTags = deriveMoodTags(
            integratedLufs = loudness.integratedLufs,
            crestFactorDb = crestFactorDb,
            lowPct = lowPct,
            highPct = highPct
        )
        val productionTags = deriveProductionStyleTags(
            lowPct = lowPct,
            highPct = highPct,
            crestFactorDb = crestFactorDb,
            stereoWidthPct = stereoWidthPct,
            hasPhaseRisk = hasPhaseRisk,
            integratedLufs = loudness.integratedLufs
        )
        val vocalTags = listOf(vocalEstimate.label)

        // Audit weak filename contradictions transparently without overriding audio scores
        val filenameNote = evaluateFilenameHintAudit(
            titleHint = titleHint,
            taxonomy = taxonomy,
            topTags = styleScoring.tags
        )

        val topTag = styleScoring.tags.firstOrNull()
        val isUnknown = styleScoring.isUnknownOrMixed || topTag == null
        val primaryId = if (isUnknown) MusicTaxonomyRepository.UNKNOWN_MIXED_ID else topTag!!.taxonomyId
        val primaryLabel = if (isUnknown) "Unknown / Mixed" else topTag!!.displayName
        val topConfidence = topTag?.score ?: 0f

        return AudioAnalysisReport(
            taxonomyVersion = taxonomy.version,
            integratedLufs = loudness.integratedLufs,
            momentaryMaxLufs = loudness.momentaryMaxLufs,
            shortTermMaxLufs = loudness.shortTermMaxLufs,
            loudnessRangeLu = loudness.loudnessRangeLu,
            samplePeakDb = samplePeakDb,
            truePeakDb = truePeakDb,
            rmsDb = rmsDb,
            crestFactorDb = crestFactorDb,
            lowEnergyPct = lowPct,
            midEnergyPct = midPct,
            highEnergyPct = highPct,
            estimatedBandwidthKhz = estimatedBandwidthKhz,
            stereoWidthPct = stereoWidthPct,
            stereoCorrelation = stereoCorrelation,
            hasPhaseRisk = hasPhaseRisk,
            isUnknownOrMixedStyle = isUnknown,
            primaryTaxonomyId = primaryId,
            primaryStyleLabel = primaryLabel,
            styleConfidence = topConfidence,
            multiLabelStyles = styleScoring.tags,
            moodTags = moodTags,
            productionStyleTags = productionTags,
            vocalStyleTags = vocalTags,
            vocalActivity = vocalEstimate,
            rhythm = rhythmEstimate,
            technicalFindings = findings,
            filenameHintNote = filenameNote
        )
    }

    private fun estimateVocalActivity(
        vocalBandSq: Double,
        midSq: Double,
        sideSq: Double,
        vocalEnvFrames: List<Double>,
        lowPct: Int,
        midPct: Int
    ): VocalActivityEstimate {
        if (vocalEnvFrames.size < 4 || midSq < 1e-10) {
            return VocalActivityEstimate(0, "INSTRUMENTAL / SILENT")
        }
        val centerRatio = (midSq / (midSq + sideSq).coerceAtLeast(1e-12)).toFloat()
        val meanEnv = vocalEnvFrames.average().coerceAtLeast(1e-9)
        val envVar = vocalEnvFrames.map { (it - meanEnv) * (it - meanEnv) }.average()
        val modulationCoeff = (sqrt(envVar) / meanEnv).toFloat().coerceIn(0f, 1.5f)
        val vocalRatio = (vocalBandSq / (midSq).coerceAtLeast(1e-12)).toFloat().coerceIn(0f, 1.5f)

        // Speech/vocals combine strong mid-band concentration + temporal syllabic modulation + center focus
        val rawPresence = (
            (midPct / 100f) * 0.42f +
                (vocalRatio * 0.30f) +
                (modulationCoeff.coerceIn(0f, 1f) * 0.25f) +
                (centerRatio * 0.15f) -
                (if (lowPct > 60) 0.18f else 0f)
            )
        val pct = (rawPresence * 100f).roundToInt().coerceIn(0, 100)
        val label = when {
            pct >= 58 -> "VOCAL / SPEECH PROMINENT"
            pct >= 34 -> "MIXED VOCAL & INSTRUMENTAL"
            else -> "INSTRUMENTAL / NO LEAD VOICE"
        }
        return VocalActivityEstimate(presencePct = pct, label = label)
    }

    private fun estimateRhythm(
        onsetFrames: List<Double>,
        frameRateHz: Double
    ): RhythmEstimate {
        if (onsetFrames.size < 40 || frameRateHz <= 0.0) {
            return RhythmEstimate(bpm = null, confidence = 0f, feelLabel = "UNDETERMINED")
        }
        val meanEnv = onsetFrames.average().coerceAtLeast(1e-9)
        val envVar = onsetFrames.map { (it - meanEnv) * (it - meanEnv) }.average()
        val envCv = sqrt(envVar) / meanEnv

        // Compute first-order rectified onset flux
        val flux = DoubleArray(onsetFrames.size - 1)
        var maxFlux = 0.0
        for (i in flux.indices) {
            val d = (onsetFrames[i + 1] - onsetFrames[i]).coerceAtLeast(0.0)
            flux[i] = d
            if (d > maxFlux) maxFlux = d
        }
        // Stationary tones or ambient pads without real transient flux must never fabricate tempo
        if (maxFlux < 1e-5 || envCv < 0.05 || (maxFlux / meanEnv) < 0.06) {
            return RhythmEstimate(bpm = null, confidence = 0f, feelLabel = "STEADY / AMBIENT")
        }
        val meanFlux = flux.average()
        val centered = DoubleArray(flux.size) { flux[it] - meanFlux }
        var totalEnergy = 0.0
        for (v in centered) totalEnergy += v * v
        if (totalEnergy < 1e-10) {
            return RhythmEstimate(bpm = null, confidence = 0f, feelLabel = "UNDETERMINED")
        }

        // Autocorrelate over musically meaningful tempo range: 65 BPM .. 175 BPM
        val minLag = ((frameRateHz * 60.0) / 175.0).roundToInt().coerceAtLeast(2)
        val maxLag = ((frameRateHz * 60.0) / 65.0).roundToInt().coerceAtMost(flux.size / 2)
        if (maxLag <= minLag) {
            return RhythmEstimate(bpm = null, confidence = 0f, feelLabel = "UNDETERMINED")
        }

        var bestLag = minLag
        var bestNormCorr = -1.0
        for (lag in minLag..maxLag) {
            var num = 0.0
            var denA = 0.0
            var denB = 0.0
            val count = centered.size - lag
            for (i in 0 until count) {
                val a = centered[i]
                val b = centered[i + lag]
                num += a * b
                denA += a * a
                denB += b * b
            }
            val den = sqrt((denA * denB).coerceAtLeast(1e-12))
            val corr = num / den
            if (corr > bestNormCorr) {
                bestNormCorr = corr
                bestLag = lag
            }
        }

        val conf = bestNormCorr.toFloat().coerceIn(0f, 1f)
        return if (conf >= BPM_CONFIDENCE_THRESHOLD) {
            val rawBpm = ((frameRateHz * 60.0) / bestLag.toDouble()).roundToInt().coerceIn(60, 180)
            val feel = when {
                rawBpm >= 132 -> "UPTEMPO / DRIVING"
                rawBpm >= 100 -> "MID-TEMPO GROOVE"
                else -> "HALF-TIME / LAID BACK"
            }
            RhythmEstimate(bpm = rawBpm, confidence = conf, feelLabel = feel)
        } else {
            RhythmEstimate(bpm = null, confidence = conf, feelLabel = "RUBATO / LOW CONFIDENCE")
        }
    }

    private fun buildTechnicalFindings(
        integratedLufs: Float,
        truePeakDb: Float,
        crestFactorDb: Float,
        lowPct: Int,
        midPct: Int,
        highPct: Int,
        stereoCorrelation: Float,
        hasPhaseRisk: Boolean,
        bandwidthKhz: Float,
        vocalPresencePct: Int
    ): List<AudioTechnicalFinding> {
        val list = ArrayList<AudioTechnicalFinding>()
        if (truePeakDb > -0.5f) {
            list.add(
                AudioTechnicalFinding(
                    code = "HOT_TRUE_PEAK",
                    title = "Intersample Peak Risk (${String.format(Locale.US, "%.1f dBTP", truePeakDb)})",
                    detail = "Peaks exceed -0.5 dBTP; engage -1.0 dBTP limiter ceiling for streaming codecs.",
                    severity = FindingSeverity.WARNING
                )
            )
        }
        if (hasPhaseRisk) {
            list.add(
                AudioTechnicalFinding(
                    code = "STEREO_PHASE_RISK",
                    title = "Mono Phase Cancellation Risk (r=${String.format(Locale.US, "%.2f", stereoCorrelation)})",
                    detail = "Low L/R phase correlation risks cancellation when summed to mono speakers.",
                    severity = FindingSeverity.WARNING
                )
            )
        }
        if (lowPct > 52) {
            list.add(
                AudioTechnicalFinding(
                    code = "EXCESSIVE_SUB_ENERGY",
                    title = "Excessive Sub/Low Energy (${lowPct}%)",
                    detail = "Over half of spectral energy sits below 250 Hz; keep sub-bass mono and tame 60–180 Hz.",
                    severity = FindingSeverity.ADVISORY
                )
            )
        }
        if (crestFactorDb < 6.0f && integratedLufs > -11.5f) {
            list.add(
                AudioTechnicalFinding(
                    code = "OVER_COMPRESSED",
                    title = "Tightly Compressed Dynamics (${String.format(Locale.US, "%.1f dB crest", crestFactorDb)})",
                    detail = "Limited transient headroom; avoid aggressive saturation or additional heavy limiting.",
                    severity = FindingSeverity.ADVISORY
                )
            )
        }
        if (highPct < 7 || bandwidthKhz < 12.2f) {
            list.add(
                AudioTechnicalFinding(
                    code = "DULL_UPPER_BAND",
                    title = "Rolled-Off Upper Band (${String.format(Locale.US, "%.1f kHz", bandwidthKhz)})",
                    detail = "Top-end air above 4 kHz is sparse (${highPct}%); gentle high-shelf lift may improve clarity.",
                    severity = FindingSeverity.INFO
                )
            )
        }
        if (integratedLufs < -20.0f && crestFactorDb >= 11.5f) {
            list.add(
                AudioTechnicalFinding(
                    code = "UNMASTERED_HEADROOM",
                    title = "Wide Pre-Master Headroom (${String.format(Locale.US, "%.1f LUFS", integratedLufs)})",
                    detail = "Healthy dynamic headroom detected; safe for clean gain staging toward delivery targets.",
                    severity = FindingSeverity.INFO
                )
            )
        }
        if (vocalPresencePct >= 58 && midPct < 35) {
            list.add(
                AudioTechnicalFinding(
                    code = "VOCAL_POCKET_MASKING",
                    title = "Mid-Band Vocal Pocket Thin",
                    detail = "Center vocal modulation is active but surrounding energy dominates the 250 Hz–4 kHz range.",
                    severity = FindingSeverity.INFO
                )
            )
        }
        return list
    }

    private data class InternalStyleScore(
        val isUnknownOrMixed: Boolean,
        val tags: List<ScoredTaxonomyTag>
    )

    private fun isNearMonochromaticTone(
        onsetFrames: List<Double>,
        highPct: Int,
        lowPct: Int,
        midPct: Int
    ): Boolean {
        if (onsetFrames.size < 4) return false
        val mean = onsetFrames.average().coerceAtLeast(1e-9)
        val cv = sqrt(onsetFrames.map { (it - mean) * (it - mean) }.average()) / mean
        val maxBand = maxOf(lowPct, midPct, highPct)
        // Pure test sine waves / stationary calibration tones have near-zero envelope variance and dominant single band energy
        return (cv < 0.045 && maxBand >= 80) || cv < 0.025
    }

    private fun scoreStylesAcoustically(
        taxonomy: LoadedMusicTaxonomy,
        channels: Int,
        integratedLufs: Float,
        crestFactorDb: Float,
        lowPct: Int,
        midPct: Int,
        highPct: Int,
        stereoWidthPct: Int,
        bpm: Int?,
        vocalPresencePct: Int,
        hfBandwidthKhz: Float,
        isPureStaticTone: Boolean
    ): InternalStyleScore {
        if (isPureStaticTone || integratedLufs <= -55.0f) {
            val unknownEntry = MusicTaxonomyRepository.findById(taxonomy, MusicTaxonomyRepository.UNKNOWN_MIXED_ID)
            val fallbackTag = ScoredTaxonomyTag(
                taxonomyId = MusicTaxonomyRepository.UNKNOWN_MIXED_ID,
                displayName = unknownEntry?.displayName ?: "Unknown / Mixed",
                parentGenre = unknownEntry?.parentGenre ?: "Unclassified",
                score = 0.15f,
                isHeuristic = true
            )
            return InternalStyleScore(isUnknownOrMixed = true, tags = listOf(fallbackTag))
        }

        // Compute normalized heuristic scores (0.0..0.95) strictly from acoustic evidence
        val candidates = ArrayList<Pair<String, Float>>()

        fun addScore(id: String, raw: Float) {
            val entry = MusicTaxonomyRepository.findById(taxonomy, id) ?: return
            val s = raw.coerceIn(0.05f, 0.92f)
            candidates.add(entry.id to s)
        }

        // 1. Classical / Orchestral: wide dynamic crest + moderate loudness + stereo hall
        val classicalScore =
            (if (crestFactorDb >= 14.5f) 0.55f else if (crestFactorDb >= 11.5f) 0.28f else 0.05f) +
                (if (integratedLufs <= -18.5f) 0.25f else 0.0f) +
                (if (channels >= 2 && stereoWidthPct in 22..75) 0.10f else 0f) -
                (if (lowPct > 55) 0.20f else 0f)
        addScore("classical_orchestral", classicalScore)

        // 2. Acoustic / Folk: dynamic + vocal/mid balance + restrained stereo/sub
        val acousticScore =
            (if (crestFactorDb in 9.5f..15.5f) 0.38f else 0.10f) +
                (if (midPct >= 42 && lowPct <= 35) 0.24f else 0f) +
                (if (channels == 1 || stereoWidthPct <= 48) 0.16f else 0f) +
                (if (integratedLufs <= -15.5f) 0.10f else 0f)
        addScore("acoustic_folk", acousticScore)

        // 3. Electronic / EDM / House / Techno
        val wideClubBase =
            (if (stereoWidthPct >= 48) 0.32f else 0.08f) +
                (if (lowPct in 26..58) 0.20f else 0f) +
                (if (crestFactorDb in 6.0f..13.5f) 0.22f else 0.05f) +
                (if (highPct >= 14 && hfBandwidthKhz >= 14.5f) 0.14f else 0f)
        addScore("electronic_edm", wideClubBase)

        val technoScore =
            (if (bpm != null && bpm in 122..145) 0.26f else 0.05f) +
                (if (lowPct >= 34 && stereoWidthPct >= 52) 0.30f else 0.10f) +
                (if (integratedLufs >= -13.0f && vocalPresencePct < 45) 0.24f else 0.06f)
        addScore("techno_industrial", technoScore)

        val houseScore =
            (if (bpm != null && bpm in 116..130) 0.28f else 0.06f) +
                (if (lowPct in 28..48 && midPct in 30..50 && stereoWidthPct >= 44) 0.32f else 0.10f) +
                (if (crestFactorDb in 6.5f..11.5f) 0.16f else 0f)
        addScore("house_tech", houseScore)

        // 4. Hip-Hop / Trap / Drill / Trap Soul / R&B family
        val trapScore =
            (if (lowPct >= 40) 0.34f else 0.08f) +
                (if (highPct >= 12) 0.18f else 0.05f) +
                (if (bpm != null && (bpm in 125..160 || bpm in 65..82)) 0.18f else 0.05f) +
                (if (integratedLufs >= -14.5f) 0.14f else 0f)
        addScore("trap_808", trapScore)

        val drillScore =
            (if (lowPct >= 42 && crestFactorDb in 5.5f..11.5f) 0.34f else 0.06f) +
                (if (bpm != null && bpm in 134..152) 0.22f else 0.04f) +
                (if (highPct >= 15 && vocalPresencePct >= 35) 0.20f else 0.06f)
        addScore("drill_uk_ny", drillScore)

        val hipHopScore =
            (if (lowPct in 30..48 && vocalPresencePct >= 42) 0.32f else 0.10f) +
                (if (crestFactorDb in 6.5f..12.5f && stereoWidthPct in 20..58) 0.26f else 0.08f) +
                (if (bpm != null && bpm in 80..108) 0.18f else 0.05f)
        addScore("hip_hop_modern", hipHopScore)

        val rnbScore =
            (if (vocalPresencePct >= 45 && midPct >= 36) 0.32f else 0.10f) +
                (if (lowPct in 24..44 && highPct in 10..28) 0.22f else 0.08f) +
                (if (crestFactorDb in 7.5f..13.5f) 0.18f else 0.05f)
        addScore("rnb_contemporary", rnbScore)

        val trapSoulScore =
            (if (lowPct in 32..50 && vocalPresencePct >= 40) 0.30f else 0.08f) +
                (if (midPct >= 30 && highPct in 8..22) 0.22f else 0.06f) +
                (if (stereoWidthPct in 30..68 && crestFactorDb in 7.0f..12.5f) 0.18f else 0.05f)
        addScore("trap_soul", trapSoulScore)

        val altRnbScore =
            (if (stereoWidthPct >= 46 && lowPct in 25..46 && vocalPresencePct >= 35) 0.34f else 0.08f) +
                (if (crestFactorDb in 8.0f..14.0f) 0.20f else 0.06f)
        addScore("alternative_rnb", altRnbScore)

        // 5. Pop & Rock
        val popScore =
            (if (midPct in 34..54 && highPct in 12..32 && lowPct in 20..40) 0.36f else 0.10f) +
                (if (vocalPresencePct >= 45 && crestFactorDb in 6.5f..12.5f) 0.26f else 0.08f) +
                (if (stereoWidthPct in 30..72) 0.14f else 0f)
        addScore("pop_contemporary", popScore)

        val rockScore =
            (if (midPct >= 45 && crestFactorDb in 5.5f..11.0f) 0.38f else 0.08f) +
                (if (lowPct in 18..36 && highPct in 12..28) 0.22f else 0.06f) +
                (if (bpm != null && bpm in 100..150) 0.14f else 0.04f)
        addScore("rock_modern", rockScore)

        val sorted = candidates.sortedByDescending { it.second }
        val topScore = sorted.firstOrNull()?.second ?: 0f
        if (topScore < STYLE_CONFIDENCE_THRESHOLD) {
            val unknownEntry = MusicTaxonomyRepository.findById(taxonomy, MusicTaxonomyRepository.UNKNOWN_MIXED_ID)
            val topWithFallback = listOf(
                ScoredTaxonomyTag(
                    taxonomyId = MusicTaxonomyRepository.UNKNOWN_MIXED_ID,
                    displayName = unknownEntry?.displayName ?: "Unknown / Mixed",
                    parentGenre = unknownEntry?.parentGenre ?: "Unclassified",
                    score = topScore.coerceIn(0f, 0.49f),
                    isHeuristic = true
                )
            ) + sorted.take(2).mapNotNull { (id, s) ->
                val e = MusicTaxonomyRepository.findById(taxonomy, id) ?: return@mapNotNull null
                ScoredTaxonomyTag(
                    taxonomyId = e.id,
                    displayName = e.displayName,
                    parentGenre = e.parentGenre,
                    score = s,
                    isHeuristic = true,
                    relatedIds = e.relatedGenres,
                    fusionLabels = e.fusionGenres,
                    masteringFamilyHint = e.masteringFamilyHint
                )
            }
            return InternalStyleScore(isUnknownOrMixed = true, tags = topWithFallback)
        }

        // Return top multi-label candidates within 0.18 of peak score (up to 3 labels)
        val multiLabels = sorted
            .filter { it.second >= (topScore - 0.18f).coerceAtLeast(0.40f) }
            .take(3)
            .mapNotNull { (id, s) ->
                val e = MusicTaxonomyRepository.findById(taxonomy, id) ?: return@mapNotNull null
                ScoredTaxonomyTag(
                    taxonomyId = e.id,
                    displayName = e.displayName,
                    parentGenre = e.parentGenre,
                    score = s,
                    isHeuristic = true,
                    relatedIds = e.relatedGenres,
                    fusionLabels = e.fusionGenres,
                    masteringFamilyHint = e.masteringFamilyHint
                )
            }

        return InternalStyleScore(isUnknownOrMixed = false, tags = multiLabels)
    }

    private fun deriveMoodTags(
        integratedLufs: Float,
        crestFactorDb: Float,
        lowPct: Int,
        highPct: Int
    ): List<String> {
        val moods = ArrayList<String>()
        when {
            integratedLufs >= -11.5f && crestFactorDb < 8.5f -> moods.add("High Energy / Intense")
            crestFactorDb >= 13.0f && integratedLufs <= -17.5f -> moods.add("Spacious / Expressive")
            else -> moods.add("Balanced / Controlled")
        }
        if (lowPct >= 45 && highPct <= 14) {
            moods.add("Dark / Sub-Warm")
        } else if (highPct >= 24) {
            moods.add("Bright / Crisp")
        }
        return moods
    }

    private fun deriveProductionStyleTags(
        lowPct: Int,
        highPct: Int,
        crestFactorDb: Float,
        stereoWidthPct: Int,
        hasPhaseRisk: Boolean,
        integratedLufs: Float
    ): List<String> {
        val tags = ArrayList<String>()
        if (lowPct >= 44) tags.add("Sub-Heavy Low End")
        if (stereoWidthPct >= 58) tags.add("Wide Stereo Image")
        else if (stereoWidthPct <= 22) tags.add("Focused / Narrow Center")
        if (crestFactorDb < 7.0f && integratedLufs > -12.0f) tags.add("Dense Club Limiting")
        else if (crestFactorDb >= 12.5f) tags.add("Natural Dynamic Range")
        if (highPct < 10) tags.add("Warm / Rolled-Off Top")
        if (hasPhaseRisk) tags.add("Side-Dominant / Phase Alert")
        if (tags.isEmpty()) tags.add("Modern Balanced Mix")
        return tags
    }

    private fun evaluateFilenameHintAudit(
        titleHint: String?,
        taxonomy: LoadedMusicTaxonomy,
        topTags: List<ScoredTaxonomyTag>
    ): String? {
        if (titleHint.isNullOrBlank()) return null
        val matchedFromName = taxonomy.entries.filter { entry ->
            entry.id != MusicTaxonomyRepository.UNKNOWN_MIXED_ID &&
                entry.aliases.any { alias ->
                    alias.length >= 3 && titleHint.contains(alias, ignoreCase = true)
                }
        }
        if (matchedFromName.isEmpty()) return null
        val topIds = topTags.map { it.taxonomyId }.toSet()
        val contradicts = matchedFromName.none { it.id in topIds || it.relatedGenres.any { r -> r in topIds } }
        return if (contradicts) {
            "Filename hints '${matchedFromName.first().displayName}' but was NOT allowed to override measured acoustic features."
        } else {
            null
        }
    }
}
