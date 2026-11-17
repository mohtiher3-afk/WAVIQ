package com.example.audio

import com.example.model.AudioTrackInfo
import com.example.model.EnhanceProfile
import com.example.model.EqBand
import com.example.model.MusicGenre
import com.example.model.OversamplingFactor
import com.example.model.TactileDeckParams
import java.util.Locale

data class AiMasterDecision(
    val selectedGenre: MusicGenre,
    val selectedProfile: EnhanceProfile,
    val eqBands: List<EqBand>,
    val deckParams: TactileDeckParams,
    val targetKhz: Int,
    val oversampling: OversamplingFactor,
    val restorationAmount: Float,
    val stereoExpansionAmount: Float,
    val confidencePct: Int,
    val appliedSummary: String,
    val isSupported: Boolean = true,
    val unsupportedGuidance: String? = null
)

/**
 * Fully offline, on-device AI Mastering Assistant & Acoustic Decision Agent.
 * Combines real-time spectral/crest diagnostics with bilingual (Arabic & English)
 * natural-language intent interpretation to configure the 64-bit DSP chain.
 */
object LocalAiMasteringAgent {

    data class QuickIntentPreset(
        val id: String,
        val label: String,
        val prompt: String
    )

    val quickPresets: List<QuickIntentPreset> = listOf(
        QuickIntentPreset("auto", "AUTO MASTER", "auto balance smart master"),
        QuickIntentPreset("drill", "دريل DRILL", "دريل باص 808 قوي وسناب حاد"),
        QuickIntentPreset("techno", "تكنو TECHNO", "تكنو ركلة 4/4 ورمبل صناعي"),
        QuickIntentPreset("trap", "تراب TRAP", "تراب 808 مشبع مع هاي-هاتس نقية"),
        QuickIntentPreset("bass", "باص أقوى", "باص أعمق وقوي 808"),
        QuickIntentPreset("vocals", "وضوح الغناء", "وضوح الغناء ونقاء الصوت"),
        QuickIntentPreset("hires", "192kHz لمعان", "ترددات عالية 192kHz لمعان وتفاصيل"),
        QuickIntentPreset("wide", "توسيع ستيريو", "توسيع الستيريو مسرح سينمائي"),
        QuickIntentPreset("warm", "دفء تناظري", "دفء تناظري كلاسيكي"),
        QuickIntentPreset("stream", "معيار -14 LUFS", "توازن البث -14 LUFS")
    )

    fun autoCalibrate(
        info: AudioTrackInfo,
        currentEq: List<EqBand>
    ): AiMasterDecision {
        val diag = info.diagnostic
        val genre = diag.detectedGenre
        val hfNeed = diag.hfDeficitPct

        val calibratedEq = currentEq.mapIndexed { idx, band ->
            val baseGain = genre.eqOffsetsDb.getOrElse(idx) { 0f }
            val extraAir = if (idx == currentEq.lastIndex && (hfNeed >= 30 || genre == MusicGenre.POP)) {
                (hfNeed / 45f).coerceIn(0.4f, 1.8f)
            } else {
                0f
            }
            val deMud = if (idx == 1 && info.integratedLufs > -13f) -0.5f else 0f
            band.copy(gainDb = (baseGain + extraAir + deMud).coerceIn(-12f, 12f))
        }

        val targetAir = (genre.air + (hfNeed / 350f)).coerceIn(0.25f, 0.95f)
        val stereoExp = when {
            info.stereoWidthPct < 30 -> 0.72f
            info.stereoWidthPct > 75 -> 0.42f
            else -> 0.56f
        }

        val summary = buildString {
            append("${genre.title} (${diag.genreConfidencePct}%) • ")
            append("${genre.targetKhz}kHz SBR ")
            append("+${(diag.recommendedRestorationAmount * 100).toInt()}% • ")
            append("3-Band @ ${String.format(Locale.US, "%.1f", genre.ceilingDb)} dBTP")
        }

        return AiMasterDecision(
            selectedGenre = genre,
            selectedProfile = genre.recommendedProfile,
            eqBands = calibratedEq,
            deckParams = TactileDeckParams(
                drive = genre.drive,
                width = genre.width,
                punch = genre.punch,
                air = targetAir,
                ceilingDb = genre.ceilingDb
            ),
            targetKhz = genre.targetKhz,
            oversampling = genre.oversampling,
            restorationAmount = diag.recommendedRestorationAmount,
            stereoExpansionAmount = stereoExp,
            confidencePct = diag.genreConfidencePct,
            appliedSummary = summary
        )
    }

    fun applyIntentCommand(
        rawCommand: String,
        info: AudioTrackInfo,
        currentEq: List<EqBand>,
        currentDeck: TactileDeckParams,
        currentGenre: MusicGenre,
        currentProfile: EnhanceProfile,
        currentTargetKhz: Int,
        currentOversampling: OversamplingFactor,
        currentRestoration: Float
    ): AiMasterDecision {
        val q = rawCommand.trim().lowercase(Locale.ROOT)
        if (q.isEmpty() || containsAny(q, "auto", "reset", "default", "تلقائي", "افتراضي", "إعادة", "ضبط تلقائي")) {
            return autoCalibrate(info, currentEq)
        }

        var eq = currentEq.map { it.copy() }
        var deck = currentDeck
        var genre = currentGenre
        var profile = currentProfile
        var khz = currentTargetKhz
        var os = currentOversampling
        var restoration = currentRestoration
        var stereoExp = 0.54f
        val tags = mutableListOf<String>()

        fun bumpBand(index: Int, delta: Float, ceiling: Float = 8.5f) {
            if (index in eq.indices) {
                eq = eq.mapIndexed { idx, band ->
                    if (idx == index) band.copy(gainDb = (band.gainDb + delta).coerceIn(-9f, ceiling)) else band
                }
            }
        }

        if (containsAny(q, "drill", "دريل")) {
            bumpBand(0, 3.6f)
            bumpBand(1, -1.2f)
            bumpBand(4, 2.4f)
            genre = MusicGenre.DRILL
            profile = EnhanceProfile.CLUB_PUNCH
            khz = 96
            os = OversamplingFactor.X8
            deck = deck.copy(
                punch = 0.94f,
                drive = 0.58f,
                ceilingDb = -0.4f,
                air = 0.55f
            )
            tags.add("Drill 808 & Transient Snap")
        }

        if (containsAny(q, "techno", "تكنو", "تيكنو", "rave", "ريڤ")) {
            bumpBand(0, 3.0f)
            bumpBand(1, 1.5f)
            bumpBand(4, 1.8f)
            genre = MusicGenre.TECHNO
            profile = EnhanceProfile.CLUB_PUNCH
            khz = 96
            os = OversamplingFactor.X4
            deck = deck.copy(
                punch = 0.82f,
                drive = 0.60f,
                width = 0.78f,
                ceilingDb = -0.5f
            )
            tags.add("Techno 4/4 Rumble & Saturation")
        }

        if (containsAny(q, "trap", "تراب", "تارب")) {
            bumpBand(0, 4.0f)
            bumpBand(1, -1.0f)
            bumpBand(3, 1.8f)
            bumpBand(4, 2.6f)
            genre = MusicGenre.TRAP
            profile = EnhanceProfile.CLUB_PUNCH
            khz = 96
            os = OversamplingFactor.X4
            deck = deck.copy(
                punch = 0.88f,
                drive = 0.52f,
                width = 0.66f,
                ceilingDb = -0.3f,
                air = 0.62f
            )
            tags.add("Trap Heavy 808 & Sheen")
        }

        if (containsAny(q, "bass", "sub", "808", "low", "punch", "باص", "بيس", "عمق", "ثقيل", "ضرب")) {
            bumpBand(0, 2.4f)
            bumpBand(1, 1.1f)
            deck = deck.copy(
                punch = (deck.punch + 0.14f).coerceAtMost(0.95f),
                drive = (deck.drive + 0.08f).coerceAtMost(0.85f)
            )
            tags.add("Sub/Bass Boost")
        }

        if (containsAny(q, "vocal", "voice", "clarity", "speech", "present", "غناء", "صوت", "وضوح", "كلمات", "نقاء")) {
            bumpBand(1, -1.1f)
            bumpBand(2, 0.8f)
            bumpBand(3, 2.5f)
            deck = deck.copy(
                air = (deck.air + 0.10f).coerceAtMost(0.95f),
                width = deck.width.coerceAtMost(0.62f)
            )
            profile = EnhanceProfile.VOCAL_CLARITY
            tags.add("Vocal Presence")
        }

        if (containsAny(q, "air", "bright", "crisp", "sparkle", "sbr", "hi-res", "192", "high", "ترددات", "لمعان", "هواء", "تفاصيل", "حدة", "نقية")) {
            bumpBand(4, 2.4f)
            khz = 192
            os = OversamplingFactor.X8
            restoration = (restoration + 0.22f).coerceIn(0.55f, 0.96f)
            deck = deck.copy(air = (deck.air + 0.18f).coerceAtMost(0.95f))
            tags.add("192kHz SBR Air")
        }

        if (containsAny(q, "wide", "width", "stereo", "spatial", "cinematic", "3d", "توسيع", "ستيريو", "مسرح", "محيطي", "اتساع", "سينما")) {
            deck = deck.copy(width = (deck.width + 0.20f).coerceIn(0.65f, 0.95f))
            stereoExp = 0.85f
            tags.add("M/S Stereo Wide")
        }

        if (containsAny(q, "warm", "analog", "tape", "vinyl", "vintage", "soft", "دفء", "دافئ", "تناظري", "شريط", "كلاسيك", "ناعم")) {
            bumpBand(0, 1.2f)
            bumpBand(4, -1.0f)
            deck = deck.copy(
                drive = (deck.drive + 0.20f).coerceIn(0.45f, 0.85f),
                air = (deck.air - 0.10f).coerceAtLeast(0.25f)
            )
            profile = EnhanceProfile.WARM_ANALOG
            tags.add("Analog Tape Warmth")
        }

        if (containsAny(q, "loud", "club", "hard", "edm", "energy", "نادي", "صاخب", "قوي", "حماس")) {
            deck = deck.copy(
                ceilingDb = -0.4f,
                punch = (deck.punch + 0.12f).coerceAtMost(0.95f),
                drive = (deck.drive + 0.12f).coerceAtMost(0.85f)
            )
            profile = EnhanceProfile.CLUB_PUNCH
            os = OversamplingFactor.X8
            tags.add("Club Loudness")
        }

        if (containsAny(q, "stream", "spotify", "apple", "youtube", "14", "balance", "بث", "سبوتيفاي", "يوتيوب", "توازن", "معيار")) {
            deck = deck.copy(ceilingDb = -1.0f)
            profile = EnhanceProfile.STREAMING_MASTER
            tags.add("Streaming -14 LUFS")
        }

        if (tags.isEmpty()) {
            return AiMasterDecision(
                selectedGenre = currentGenre,
                selectedProfile = currentProfile,
                eqBands = currentEq,
                deckParams = currentDeck,
                targetKhz = currentTargetKhz,
                oversampling = currentOversampling,
                restorationAmount = currentRestoration,
                stereoExpansionAmount = 0.54f,
                confidencePct = 0,
                isSupported = false,
                unsupportedGuidance = "Command not recognized. Try: 'باص أقوى', 'وضوح الغناء', '192kHz لمعان', 'توسيع ستيريو', 'دريل', 'تكنو', 'تراب'",
                appliedSummary = "UNSUPPORTED COMMAND // No DSP modifications applied"
            )
        }

        return AiMasterDecision(
            selectedGenre = genre,
            selectedProfile = profile,
            eqBands = eq,
            deckParams = deck,
            targetKhz = khz,
            oversampling = os,
            restorationAmount = restoration,
            stereoExpansionAmount = stereoExp,
            confidencePct = minOf(100, 80 + tags.size * 5),
            appliedSummary = "AI INTENT // ${tags.joinToString(" + ")}",
            isSupported = true
        )
    }

    private fun containsAny(text: String, vararg tokens: String): Boolean {
        return tokens.any { text.contains(it) }
    }
}
