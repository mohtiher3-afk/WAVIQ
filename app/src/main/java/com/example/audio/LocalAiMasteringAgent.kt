package com.example.audio

import com.example.model.AudioTrackInfo
import com.example.model.DspDecisionStage
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
    val unsupportedGuidance: String? = null,
    val stages: List<DspDecisionStage> = emptyList(),
    val safetyWarnings: List<String> = emptyList(),
    val parsedIntents: List<String> = emptyList(),
    val measurementsUsed: List<String> = emptyList(),
    val targetStandardLabel: String? = null
)

/**
 * Offline Bilingual Command Parser + Rule-Based Mastering Safety & Decision Engine.
 *
 * Uses measured BS.1770 loudness, LRA, True Peak, crest factor, 3-band spectral energy,
 * and stereo phase correlation from [AudioAnalysisReport] alongside structured bilingual
 * intents to configure the 64-bit DSP chain safely and traceably.
 * Detected style/genre is used strictly as weak context, never as blind hardcoded EQ boosts.
 */
object LocalAiMasteringAgent {

    enum class IntentAction(val label: String) {
        RESET_AUTO("Auto-Calibrate from Measurements"),
        BASS_UP("Sub/Bass Boost"),
        BASS_DOWN("Sub/Bass Cut & Clean"),
        VOCAL_UP("Vocal Presence"),
        VOCAL_DOWN("Vocal Recess"),
        AIR_UP("192kHz SBR Air"),
        AIR_DOWN("De-Harsh / Soften Top"),
        WIDTH_UP("M/S Stereo Wide"),
        WIDTH_DOWN("Mono / Narrow Stereo"),
        WARM_ANALOG("Analog Tape Warmth"),
        LOUD_CLUB("Club Loudness"),
        DYNAMIC_PRESERVE("Preserve Dynamics"),
        TARGET_STREAMING("Streaming -14 LUFS"),
        GENRE_DRILL("Drill 808 & Transient Snap"),
        GENRE_TECHNO("Techno 4/4 Rumble & Saturation"),
        GENRE_TRAP("Trap Heavy 808 & Sheen")
    }

    data class ParsedCommandResult(
        val actions: Set<IntentAction>,
        val conflicts: List<String>,
        val unsupportedReason: String?
    )

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

    fun parseCommand(rawCommand: String): ParsedCommandResult {
        val normalized = rawCommand.trim()
            .lowercase(Locale.ROOT)
            .replace('’', '\'')
            .replace(Regex("\\s+"), " ")

        if (normalized.isEmpty()) {
            return ParsedCommandResult(
                actions = setOf(IntentAction.RESET_AUTO),
                conflicts = emptyList(),
                unsupportedReason = null
            )
        }

        // Explicitly reject out-of-scope source-separation prompts truthfully
        val stemRegexEn = Regex("\\b(separate|split|extract|isolate|remove)\\b.*\\b(stem|stems|vocal|vocals|drum|drums|instrumental|acapella|karaoke)\\b")
        if (stemRegexEn.containsMatchIn(normalized) ||
            containsAny(
                normalized,
                "acapella", "karaoke", "stem separation", "source separation",
                "فصل الآلات", "عزل الصوت", "عزل الغناء", "فصل الغناء", "فصل المسارات", "فصل الصوت"
            )
        ) {
            return ParsedCommandResult(
                actions = emptySet(),
                conflicts = emptyList(),
                unsupportedReason = "True source/stem separation is not bundled. Use the Band Mixer tab to balance 4 frequency crossover bands instead."
            )
        }

        val actions = LinkedHashSet<IntentAction>()
        // Split on natural clause separators (EN & AR) so negation applies per clause cleanly
        val clauses = normalized
            .split(Regex("(?:,|;|\\band\\b|\\bbut\\b|\\bwhile\\b|\\bwith\\b|&|\\s+و\\s+|\\s+لكن\\s+|\\s+مع\\s+|(?<=\\S)\\s+(?=و(?:بدون|قلل|خفف|لا)))"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        for (clause in clauses) {
            val isAuto = containsAny(clause, "auto", "reset", "default", "smart master", "تلقائي", "افتراضي", "إعادة", "ضبط تلقائي")
            if (isAuto && clauses.size == 1 && !containsAny(clause, "bass", "vocal", "stereo", "wide", "bright", "ba")) {
                actions.add(IntentAction.RESET_AUTO)
            }

            // 1. Sub / Bass
            val bassDownTokens = arrayOf(
                "no bass", "no sub", "no 808", "without bass", "without 808", "less bass", "less sub",
                "reduce bass", "cut bass", "cut sub", "lower bass", "remove bass", "clean mud", "tame bass",
                "بدون باص", "لا باص", "لا تضيف باص", "قلل الباص", "خفف الباص", "خفف البيس", "نقص الباص", "بدون بيس"
            )
            val bassUpTokens = arrayOf("bass", "sub", "808", "boom", "low end", "low-end", "punch", "باص", "بيس", "عمق", "تحت")
            when {
                containsAny(clause, *bassDownTokens) -> actions.add(IntentAction.BASS_DOWN)
                containsAny(clause, *bassUpTokens) && !isNegatedTopic(clause, *bassUpTokens) -> actions.add(IntentAction.BASS_UP)
                containsAny(clause, *bassUpTokens) && isNegatedTopic(clause, *bassUpTokens) -> actions.add(IntentAction.BASS_DOWN)
            }

            // 2. Vocals
            val vocalDownTokens = arrayOf("less vocal", "no vocal", "without vocal", "tuck vocal", "قلل الصوت", "بدون غناء", "خفف الصوت")
            val vocalUpTokens = arrayOf("vocal", "voice", "clarity", "speech", "present", "presence", "غناء", "صوت", "وضوح", "كلمات", "نقاء")
            when {
                containsAny(clause, *vocalDownTokens) -> actions.add(IntentAction.VOCAL_DOWN)
                containsAny(clause, *vocalUpTokens) && !isNegatedTopic(clause, *vocalUpTokens) -> actions.add(IntentAction.VOCAL_UP)
                containsAny(clause, *vocalUpTokens) && isNegatedTopic(clause, *vocalUpTokens) -> actions.add(IntentAction.VOCAL_DOWN)
            }

            // 3. Highs / Air
            val airDownTokens = arrayOf(
                "less bright", "no bright", "not harsh", "not shrill", "less high", "less treble",
                "tame high", "de-ess", "softer high", "roll off",
                "تخفيف الحدة", "بدون لمعان", "خفف الحدة", "قلل الحدة", "بدون حدة"
            )
            val airUpTokens = arrayOf("air", "bright", "crisp", "sparkle", "sbr", "hi-res", "192", "treble", "highs", "sheen", "ترددات", "لمعان", "هواء", "تفاصيل", "حدة", "نقية")
            when {
                containsAny(clause, *airDownTokens) -> actions.add(IntentAction.AIR_DOWN)
                containsAny(clause, *airUpTokens) && !isNegatedTopic(clause, *airUpTokens) -> actions.add(IntentAction.AIR_UP)
                containsAny(clause, *airUpTokens) && isNegatedTopic(clause, *airUpTokens) -> actions.add(IntentAction.AIR_DOWN)
            }

            // 4. Stereo Width
            val widthDownTokens = arrayOf(
                "no wide", "don't widen", "dont widen", "do not widen", "without widening", "less stereo",
                "less wide", "narrow", "mono", "keep center",
                "بدون توسيع", "لا توسع", "مونو", "ضيق", "قلل الستيريو", "بدون ستيريو"
            )
            val widthUpTokens = arrayOf("wide", "widen", "width", "stereo", "spatial", "cinematic", "3d", "توسيع", "ستيريو", "مسرح", "محيطي", "اتساع", "سينما")
            when {
                containsAny(clause, *widthDownTokens) -> actions.add(IntentAction.WIDTH_DOWN)
                containsAny(clause, *widthUpTokens) && !isNegatedTopic(clause, *widthUpTokens) -> actions.add(IntentAction.WIDTH_UP)
                containsAny(clause, *widthUpTokens) && isNegatedTopic(clause, *widthUpTokens) -> actions.add(IntentAction.WIDTH_DOWN)
            }

            // 5. Analog Warmth
            val warmTokens = arrayOf("warm", "analog", "tape", "vinyl", "vintage", "دفء", "دافئ", "تناظري", "شريط", "كلاسيك")
            if (containsAny(clause, *warmTokens) && !isNegatedTopic(clause, *warmTokens)) {
                actions.add(IntentAction.WARM_ANALOG)
            }

            // 6. Loudness vs Dynamic Preservation
            val dynPreserveTokens = arrayOf(
                "preserve dynamic", "keep dynamic", "more dynamic", "don't squash", "dont squash",
                "no limiting", "less loud", "less compression", "not loud", "uncompressed",
                "حافظ على الديناميكية", "بدون ضغط", "لا تضغط", "ديناميكية", "خفف الضغط"
            )
            val loudTokens = arrayOf("loud", "club", "hard", "slam", "نادي", "صاخب", "قوي", "حماس", "أعلى صوت")
            when {
                containsAny(clause, *dynPreserveTokens) -> actions.add(IntentAction.DYNAMIC_PRESERVE)
                containsAny(clause, *loudTokens) && !isNegatedTopic(clause, *loudTokens) -> actions.add(IntentAction.LOUD_CLUB)
                containsAny(clause, *loudTokens) && isNegatedTopic(clause, *loudTokens) -> actions.add(IntentAction.DYNAMIC_PRESERVE)
            }

            // 7. Streaming standard (-14 LUFS)
            val streamTokens = arrayOf("stream", "spotify", "apple", "youtube", "-14", "14 lufs", "lufs", "بث", "سبوتيفاي", "يوتيوب", "توازن", "معيار")
            if (containsAny(clause, *streamTokens) && !isNegatedTopic(clause, *streamTokens)) {
                actions.add(IntentAction.TARGET_STREAMING)
            }

            // 8. Genre Style Context
            if (containsAny(clause, "drill", "دريل") && !isNegatedTopic(clause, "drill", "دريل")) {
                actions.add(IntentAction.GENRE_DRILL)
            }
            if (containsAny(clause, "techno", "تكنو", "تيكنو", "rave") && !isNegatedTopic(clause, "techno", "تكنو", "تيكنو", "rave")) {
                actions.add(IntentAction.GENRE_TECHNO)
            }
            if (containsAny(clause, "trap", "تراب") && !isNegatedTopic(clause, "trap", "تراب")) {
                actions.add(IntentAction.GENRE_TRAP)
            }
        }

        // Check contradictions on opposing axes
        val conflicts = ArrayList<String>()
        if (IntentAction.BASS_UP in actions && IntentAction.BASS_DOWN in actions) {
            conflicts.add("Conflicting Sub/Bass boost & cut")
        }
        if (IntentAction.AIR_UP in actions && IntentAction.AIR_DOWN in actions) {
            conflicts.add("Conflicting Brightness boost & cut")
        }
        if (IntentAction.WIDTH_UP in actions && IntentAction.WIDTH_DOWN in actions) {
            conflicts.add("Conflicting Stereo widening & mono")
        }
        if (IntentAction.LOUD_CLUB in actions && IntentAction.DYNAMIC_PRESERVE in actions) {
            conflicts.add("Conflicting Club loudness & preserve dynamics")
        }
        if (IntentAction.VOCAL_UP in actions && IntentAction.VOCAL_DOWN in actions) {
            conflicts.add("Conflicting Vocal boost & cut")
        }

        return ParsedCommandResult(
            actions = actions,
            conflicts = conflicts,
            unsupportedReason = null
        )
    }

    fun autoCalibrate(
        info: AudioTrackInfo,
        currentEq: List<EqBand>
    ): AiMasterDecision {
        val diag = info.diagnostic
        val report = diag.analysisReport
        val genre = diag.detectedGenre
        val hfNeed = diag.hfDeficitPct

        // Start from conservative measurement-driven EQ rather than blind full genre boosts
        val eqOffsets = MutableList(currentEq.size.coerceAtLeast(5)) { 0f }
        // Sub shelf (60 Hz): scale down if source already carries heavy sub-bass
        val baseSub = genre.eqOffsetsDb.getOrElse(0) { 0f }
        eqOffsets[0] = when {
            report.lowEnergyPct >= 48 -> baseSub.coerceAtMost(0.8f)
            else -> (baseSub * 0.65f).coerceIn(-2.0f, 3.0f)
        }
        // Low-mid (180 Hz): gently clean mud when louder or sub-heavy
        eqOffsets[1] = if (report.lowEnergyPct >= 44 || report.integratedLufs > -13f) -0.8f else 0.2f
        // Mid (900 Hz): subtle pocket for vocal activity
        eqOffsets[2] = if (report.vocalActivity.presencePct >= 55 && report.midEnergyPct < 36) 0.9f else (genre.eqOffsetsDb.getOrElse(2) { 0f } * 0.5f)
        // Presence (3.6 kHz): modest lift when mid/vocal balance warrants it
        eqOffsets[3] = if (report.vocalActivity.presencePct >= 45) (genre.eqOffsetsDb.getOrElse(3) { 0f } * 0.75f) else 0.6f
        // Air shelf (12 kHz): responsive to actual high-frequency roll-off / deficit
        val baseAir = genre.eqOffsetsDb.getOrElse(4) { 0f } * 0.65f
        val extraAir = if (report.highEnergyPct < 10 || hfNeed >= 30) (hfNeed / 45f).coerceIn(0.4f, 1.8f) else 0f
        eqOffsets[4] = (baseAir + extraAir).coerceIn(-2.0f, 4.0f)

        val calibratedEq = currentEq.mapIndexed { idx, band ->
            band.copy(gainDb = eqOffsets.getOrElse(idx) { 0f }.coerceIn(-12f, 12f))
        }

        val targetAir = (genre.air + (hfNeed / 350f)).coerceIn(0.25f, 0.88f)
        val rawStereo = when {
            info.stereoWidthPct < 30 -> 0.68f
            info.stereoWidthPct > 75 -> 0.42f
            else -> genre.width.coerceIn(0.42f, 0.72f)
        }

        val draftDeck = TactileDeckParams(
            drive = genre.drive.coerceAtMost(0.55f),
            width = rawStereo,
            punch = genre.punch.coerceAtMost(0.86f),
            air = targetAir,
            ceilingDb = genre.ceilingDb.coerceAtMost(-0.6f)
        )

        return finalizeWithSafetyAndExplainability(
            info = info,
            baseGenre = genre,
            baseProfile = genre.recommendedProfile,
            draftEq = calibratedEq,
            draftDeck = draftDeck,
            targetKhz = genre.targetKhz,
            oversampling = genre.oversampling,
            restoration = diag.recommendedRestorationAmount,
            stereoExp = rawStereo,
            parsedIntents = listOf("AUTO_CALIBRATE_MEASUREMENTS"),
            summaryPrefix = "HEURISTIC AUTO-CALIBRATE // ${report.primaryStyleLabel} • ${genre.targetKhz}kHz SBR +${(diag.recommendedRestorationAmount * 100).toInt()}%",
            confidencePct = if (report.isUnknownOrMixedStyle) (report.styleConfidence * 100).toInt().coerceIn(25, 49) else diag.genreConfidencePct,
            targetStandard = "Streaming Balanced (${String.format(Locale.US, "%.1f", genre.targetLufs)} LUFS context)"
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
        val parsed = parseCommand(rawCommand)

        if (parsed.unsupportedReason != null) {
            return buildUnsupportedDecision(
                currentGenre = currentGenre,
                currentProfile = currentProfile,
                currentEq = currentEq,
                currentDeck = currentDeck,
                currentTargetKhz = currentTargetKhz,
                currentOversampling = currentOversampling,
                currentRestoration = currentRestoration,
                guidance = parsed.unsupportedReason,
                summary = "UNSUPPORTED COMMAND // ${parsed.unsupportedReason}"
            )
        }

        if (parsed.conflicts.isNotEmpty()) {
            val conflictText = parsed.conflicts.joinToString("; ")
            return buildUnsupportedDecision(
                currentGenre = currentGenre,
                currentProfile = currentProfile,
                currentEq = currentEq,
                currentDeck = currentDeck,
                currentTargetKhz = currentTargetKhz,
                currentOversampling = currentOversampling,
                currentRestoration = currentRestoration,
                guidance = "Contradictory instructions: $conflictText. Clarify one direction so DSP stays safe.",
                summary = "CONTRADICTORY COMMAND // $conflictText — No DSP changes applied"
            )
        }

        if (parsed.actions.isEmpty()) {
            return buildUnsupportedDecision(
                currentGenre = currentGenre,
                currentProfile = currentProfile,
                currentEq = currentEq,
                currentDeck = currentDeck,
                currentTargetKhz = currentTargetKhz,
                currentOversampling = currentOversampling,
                currentRestoration = currentRestoration,
                guidance = "Command not recognized. Try: 'باص أقوى', 'بدون باص', 'وضوح الغناء', '192kHz لمعان', 'توسيع ستيريو', 'حافظ على الديناميكية'",
                summary = "UNSUPPORTED COMMAND // No DSP modifications applied"
            )
        }

        if (parsed.actions == setOf(IntentAction.RESET_AUTO)) {
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
        var targetStandard: String? = null
        val tags = mutableListOf<String>()

        fun bumpBand(index: Int, delta: Float, floor: Float = -9f, ceiling: Float = 8.5f) {
            if (index in eq.indices) {
                eq = eq.mapIndexed { idx, band ->
                    if (idx == index) band.copy(gainDb = (band.gainDb + delta).coerceIn(floor, ceiling)) else band
                }
            }
        }

        // Apply optional genre style context cleanly
        if (IntentAction.GENRE_DRILL in parsed.actions) {
            bumpBand(0, 3.2f)
            bumpBand(1, -1.2f)
            bumpBand(4, 2.2f)
            genre = MusicGenre.DRILL
            profile = EnhanceProfile.CLUB_PUNCH
            khz = 96
            os = OversamplingFactor.X8
            deck = deck.copy(punch = 0.94f, drive = 0.54f, ceilingDb = -0.6f, air = 0.55f)
            tags.add(IntentAction.GENRE_DRILL.label)
        }
        if (IntentAction.GENRE_TECHNO in parsed.actions) {
            bumpBand(0, 2.8f)
            bumpBand(1, 1.3f)
            bumpBand(4, 1.8f)
            genre = MusicGenre.TECHNO
            profile = EnhanceProfile.CLUB_PUNCH
            khz = 96
            os = OversamplingFactor.X4
            deck = deck.copy(punch = 0.82f, drive = 0.56f, width = 0.78f, ceilingDb = -0.6f)
            tags.add(IntentAction.GENRE_TECHNO.label)
        }
        if (IntentAction.GENRE_TRAP in parsed.actions) {
            bumpBand(0, 3.4f)
            bumpBand(1, -1.0f)
            bumpBand(3, 1.6f)
            bumpBand(4, 2.4f)
            genre = MusicGenre.TRAP
            profile = EnhanceProfile.CLUB_PUNCH
            khz = 96
            os = OversamplingFactor.X4
            deck = deck.copy(punch = 0.88f, drive = 0.50f, width = 0.66f, ceilingDb = -0.5f, air = 0.62f)
            tags.add(IntentAction.GENRE_TRAP.label)
        }

        if (IntentAction.BASS_UP in parsed.actions) {
            bumpBand(0, 2.4f)
            bumpBand(1, 0.9f)
            deck = deck.copy(
                punch = (deck.punch + 0.14f).coerceAtMost(0.95f),
                drive = (deck.drive + 0.08f).coerceAtMost(0.85f)
            )
            tags.add(IntentAction.BASS_UP.label)
        }
        if (IntentAction.BASS_DOWN in parsed.actions) {
            bumpBand(0, -2.6f, floor = -8f, ceiling = 1f)
            bumpBand(1, -1.4f, floor = -8f, ceiling = 1f)
            deck = deck.copy(punch = (deck.punch - 0.08f).coerceAtLeast(0.25f))
            tags.add(IntentAction.BASS_DOWN.label)
        }

        if (IntentAction.VOCAL_UP in parsed.actions) {
            bumpBand(1, -1.1f)
            bumpBand(2, 0.8f)
            bumpBand(3, 2.5f)
            deck = deck.copy(
                air = (deck.air + 0.10f).coerceAtMost(0.95f),
                width = deck.width.coerceAtMost(0.62f)
            )
            profile = EnhanceProfile.VOCAL_CLARITY
            tags.add(IntentAction.VOCAL_UP.label)
        }
        if (IntentAction.VOCAL_DOWN in parsed.actions) {
            bumpBand(2, -1.0f)
            bumpBand(3, -2.0f)
            tags.add(IntentAction.VOCAL_DOWN.label)
        }

        if (IntentAction.AIR_UP in parsed.actions) {
            bumpBand(4, 2.4f)
            khz = 192
            os = OversamplingFactor.X8
            restoration = (restoration + 0.22f).coerceIn(0.55f, 0.96f)
            deck = deck.copy(air = (deck.air + 0.18f).coerceAtMost(0.95f))
            tags.add(IntentAction.AIR_UP.label)
        }
        if (IntentAction.AIR_DOWN in parsed.actions) {
            bumpBand(4, -2.0f, floor = -8f, ceiling = 1f)
            restoration = (restoration - 0.22f).coerceIn(0.15f, 0.50f)
            deck = deck.copy(air = (deck.air - 0.18f).coerceAtLeast(0.20f))
            tags.add(IntentAction.AIR_DOWN.label)
        }

        if (IntentAction.WIDTH_UP in parsed.actions) {
            deck = deck.copy(width = (deck.width + 0.20f).coerceIn(0.65f, 0.95f))
            stereoExp = 0.85f
            tags.add(IntentAction.WIDTH_UP.label)
        }
        if (IntentAction.WIDTH_DOWN in parsed.actions) {
            deck = deck.copy(width = 0.32f)
            stereoExp = 0.15f
            tags.add(IntentAction.WIDTH_DOWN.label)
        }

        if (IntentAction.WARM_ANALOG in parsed.actions) {
            bumpBand(0, 1.2f)
            bumpBand(4, -1.0f)
            deck = deck.copy(
                drive = (deck.drive + 0.20f).coerceIn(0.45f, 0.85f),
                air = (deck.air - 0.10f).coerceAtLeast(0.25f)
            )
            profile = EnhanceProfile.WARM_ANALOG
            tags.add(IntentAction.WARM_ANALOG.label)
        }

        if (IntentAction.LOUD_CLUB in parsed.actions) {
            deck = deck.copy(
                ceilingDb = -0.5f,
                punch = (deck.punch + 0.12f).coerceAtMost(0.95f),
                drive = (deck.drive + 0.12f).coerceAtMost(0.85f)
            )
            profile = EnhanceProfile.CLUB_PUNCH
            os = OversamplingFactor.X8
            targetStandard = "Club Loud (-9.0 LUFS target)"
            tags.add(IntentAction.LOUD_CLUB.label)
        }

        if (IntentAction.DYNAMIC_PRESERVE in parsed.actions) {
            deck = deck.copy(
                drive = deck.drive.coerceAtMost(0.18f),
                punch = deck.punch.coerceAtMost(0.45f),
                ceilingDb = deck.ceilingDb.coerceAtMost(-1.2f)
            )
            profile = EnhanceProfile.STREAMING_MASTER
            os = OversamplingFactor.X8
            targetStandard = "High-Headroom Dynamic (-16 to -14 LUFS)"
            tags.add(IntentAction.DYNAMIC_PRESERVE.label)
        }

        if (IntentAction.TARGET_STREAMING in parsed.actions) {
            deck = deck.copy(ceilingDb = -1.0f)
            profile = EnhanceProfile.STREAMING_MASTER
            targetStandard = "Streaming Delivery (-14.0 LUFS / -1.0 dBTP)"
            tags.add(IntentAction.TARGET_STREAMING.label)
        }

        return finalizeWithSafetyAndExplainability(
            info = info,
            baseGenre = genre,
            baseProfile = profile,
            draftEq = eq,
            draftDeck = deck,
            targetKhz = khz,
            oversampling = os,
            restoration = restoration,
            stereoExp = stereoExp,
            parsedIntents = parsed.actions.map { it.name },
            summaryPrefix = "AI INTENT // ${tags.joinToString(" + ")}",
            confidencePct = minOf(100, 80 + tags.size * 5),
            targetStandard = targetStandard
        )
    }

    /**
     * Applies acoustic safety guards from measured [AudioAnalysisReport] telemetry and
     * builds traceable per-stage DSP explanations so every adjustment links back to a
     * measurement or explicit user command.
     */
    private fun finalizeWithSafetyAndExplainability(
        info: AudioTrackInfo,
        baseGenre: MusicGenre,
        baseProfile: EnhanceProfile,
        draftEq: List<EqBand>,
        draftDeck: TactileDeckParams,
        targetKhz: Int,
        oversampling: OversamplingFactor,
        restoration: Float,
        stereoExp: Float,
        parsedIntents: List<String>,
        summaryPrefix: String,
        confidencePct: Int,
        targetStandard: String?
    ): AiMasterDecision {
        val report = info.diagnostic.analysisReport
        val warnings = ArrayList<String>()
        var safeEq = draftEq.map { it.copy() }
        var safeDeck = draftDeck
        var safeOs = oversampling
        var safeStereoExp = stereoExp

        var eqClamped = false
        var driveClamped = false
        var widthClamped = false
        var limiterClamped = false

        // Guard 1: Excessive Sub Energy (>=48% <250Hz) -> prevent mud from stacked sub boosts
        if (report.lowEnergyPct >= 48 && safeEq.isNotEmpty() && safeEq[0].gainDb > 1.5f) {
            safeEq = safeEq.mapIndexed { idx, band ->
                if (idx == 0) band.copy(gainDb = 1.5f) else band
            }
            eqClamped = true
            warnings.add(
                "Low-end safety clamp: Sub band already carries ${report.lowEnergyPct}% energy; capped 60 Hz shelf at +1.5 dB."
            )
        }

        // Guard 2: Phase Risk / Low Stereo Correlation (r < 0.25) -> never widen blindly
        if (report.hasPhaseRisk || report.stereoCorrelation < 0.25f) {
            if (safeDeck.width > 0.45f || safeStereoExp > 0.28f) {
                safeDeck = safeDeck.copy(width = safeDeck.width.coerceAtMost(0.42f))
                safeStereoExp = safeStereoExp.coerceAtMost(0.25f)
                widthClamped = true
                warnings.add(
                    "Stereo safety clamp: Restricted M/S width (${(safeDeck.width * 100).toInt()}%) because phase correlation is r=${String.format(Locale.US, "%+.2f", report.stereoCorrelation)}."
                )
            }
        }

        // Guard 3: Over-Compressed Dynamics (crest < 6.5 dB & loud >-11.5 LUFS) -> avoid pumping/clipping
        if (report.crestFactorDb < 6.5f && report.integratedLufs > -11.5f) {
            if (safeDeck.drive > 0.30f || safeDeck.ceilingDb > -1.0f) {
                safeDeck = safeDeck.copy(
                    drive = safeDeck.drive.coerceAtMost(0.28f),
                    punch = safeDeck.punch.coerceAtMost(0.58f),
                    ceilingDb = safeDeck.ceilingDb.coerceAtMost(-1.0f)
                )
                driveClamped = true
                limiterClamped = true
                safeOs = OversamplingFactor.X8
                warnings.add(
                    "Dynamics safety clamp: Tight crest (${String.format(Locale.US, "%.1f", report.crestFactorDb)} dB at ${String.format(Locale.US, "%.1f", report.integratedLufs)} LUFS); capped drive <= 28% and ceiling <= -1.0 dBTP."
                )
            }
        }

        // Guard 4: Hot Intersample Peaks (truePeak > -0.5 dBTP) -> lock brickwall headroom
        if (report.truePeakDb > -0.5f && safeDeck.ceilingDb > -1.0f) {
            safeDeck = safeDeck.copy(ceilingDb = -1.0f)
            safeOs = OversamplingFactor.X8
            limiterClamped = true
            warnings.add(
                "True-Peak guard: Source hits ${String.format(Locale.US, "%.1f", report.truePeakDb)} dBTP; locked ceiling at -1.0 dBTP (8X oversampling)."
            )
        }

        val eqSummary = safeEq.joinToString(" • ") {
            "${it.label} ${String.format(Locale.US, "%+.1f", it.gainDb)}dB"
        }
        val stages = listOf(
            DspDecisionStage(
                stageName = "Corrective 5-Band EQ",
                parameterSummary = eqSummary,
                reason = "Measured L/M/H split (${report.lowEnergyPct}%/${report.midEnergyPct}%/${report.highEnergyPct}%), vocal ${report.vocalActivity.presencePct}%, intents: ${parsedIntents.joinToString()}",
                safetyClamped = eqClamped
            ),
            DspDecisionStage(
                stageName = "Harmonic Drive & Transients",
                parameterSummary = "Drive ${(safeDeck.drive * 100).toInt()}% • Punch ${(safeDeck.punch * 100).toInt()}%",
                reason = "Crest factor ${String.format(Locale.US, "%.1f", report.crestFactorDb)} dB (${String.format(Locale.US, "%.1f", report.integratedLufs)} LUFS)",
                safetyClamped = driveClamped
            ),
            DspDecisionStage(
                stageName = "Sub-Mono & M/S Stereo Bus",
                parameterSummary = "Width ${(safeDeck.width * 100).toInt()}% • Sub <120Hz Mono",
                reason = "Phase correlation r=${String.format(Locale.US, "%+.2f", report.stereoCorrelation)} (phaseRisk=${report.hasPhaseRisk})",
                safetyClamped = widthClamped
            ),
            DspDecisionStage(
                stageName = "SBR HF Exciter (${targetKhz}kHz)",
                parameterSummary = "Restore ${(restoration * 100).toInt()}% • Air ${(safeDeck.air * 100).toInt()}%",
                reason = "Measured upper bandwidth ${String.format(Locale.US, "%.1f", report.estimatedBandwidthKhz)} kHz (${report.highEnergyPct}% >4kHz energy)",
                safetyClamped = false
            ),
            DspDecisionStage(
                stageName = "3-Band True-Peak Limiter",
                parameterSummary = "${String.format(Locale.US, "%.1f", safeDeck.ceilingDb)} dBTP • ${safeOs.factor}X ISP",
                reason = "Source True Peak ${String.format(Locale.US, "%.1f", report.truePeakDb)} dBTP; LRA ${String.format(Locale.US, "%.1f", report.loudnessRangeLu)} LU",
                safetyClamped = limiterClamped
            )
        )

        val measurementsUsed = listOf(
            "BS.1770 ${String.format(Locale.US, "%.1f", report.integratedLufs)} LUFS",
            "LRA ${String.format(Locale.US, "%.1f", report.loudnessRangeLu)} LU",
            "True Peak ${String.format(Locale.US, "%.1f", report.truePeakDb)} dBTP",
            "Crest ${String.format(Locale.US, "%.1f", report.crestFactorDb)} dB",
            "L/M/H ${report.lowEnergyPct}/${report.midEnergyPct}/${report.highEnergyPct}%",
            "Phase r=${String.format(Locale.US, "%+.2f", report.stereoCorrelation)}"
        )

        val finalSummary = if (warnings.isEmpty()) {
            summaryPrefix
        } else {
            "$summaryPrefix [SAFE CLAMPED]"
        }

        return AiMasterDecision(
            selectedGenre = baseGenre,
            selectedProfile = baseProfile,
            eqBands = safeEq,
            deckParams = safeDeck,
            targetKhz = targetKhz,
            oversampling = safeOs,
            restorationAmount = restoration,
            stereoExpansionAmount = safeStereoExp,
            confidencePct = confidencePct,
            appliedSummary = finalSummary,
            isSupported = true,
            stages = stages,
            safetyWarnings = warnings,
            parsedIntents = parsedIntents,
            measurementsUsed = measurementsUsed,
            targetStandardLabel = targetStandard
        )
    }

    private fun buildUnsupportedDecision(
        currentGenre: MusicGenre,
        currentProfile: EnhanceProfile,
        currentEq: List<EqBand>,
        currentDeck: TactileDeckParams,
        currentTargetKhz: Int,
        currentOversampling: OversamplingFactor,
        currentRestoration: Float,
        guidance: String,
        summary: String
    ): AiMasterDecision {
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
            appliedSummary = summary,
            isSupported = false,
            unsupportedGuidance = guidance,
            stages = emptyList(),
            safetyWarnings = emptyList(),
            parsedIntents = emptyList()
        )
    }

    private fun isNegatedTopic(clause: String, vararg topicTokens: String): Boolean {
        val negationMarkers = arrayOf(
            "no ", "not ", "don't ", "dont ", "do not ", "without ", "less ", "reduce ",
            "cut ", "remove ", "lower ", "avoid ", "stop ", "tame ",
            "بدون ", "لا ", "قلل ", "خفف ", "تخفيف ", "نقص ", "بلا ", "دون "
        )
        for (tok in topicTokens) {
            val idx = clause.indexOf(tok)
            if (idx >= 0) {
                val windowStart = (idx - 22).coerceAtLeast(0)
                val prefixWindow = clause.substring(windowStart, idx + tok.length.coerceAtMost(clause.length - idx))
                if (negationMarkers.any { prefixWindow.contains(it) }) {
                    return true
                }
            }
        }
        return false
    }

    private fun containsAny(text: String, vararg tokens: String): Boolean {
        return tokens.any { text.contains(it) }
    }
}
