package com.example.model

import androidx.compose.runtime.Immutable

enum class WaviqTab(
    val indexLabel: String,
    val title: String,
    val routePrefix: String
) {
    STUDIO("1", "STUDIO", "/studio"),
    LIBRARY("2", "LIBRARY", "/library")
}

enum class ProTuningTab(val label: String) {
    EQ("5-BAND EQ"),
    DECK("TACTILE DECK"),
    STEMS("BAND MIXER"),
    ENGINE("64-BIT BUS")
}

enum class AudioBusMode(val code: String) {
    BYPASS_RAW("BYPASS_RAW"),
    SPECTRAL_TAP("SPECTRAL_TAP"),
    NPU_DSP_ACTIVE("NPU_DSP_ACTIVE"),
    DUAL_BUFFER("DUAL_BUFFER"),
    STEM_MATRIX_4X("STEM_MATRIX_4X"),
    GLYPH_VU_SYNC("GLYPH_VU_SYNC"),
    OFFLINE_RENDER("OFFLINE_RENDER"),
    PLAYER_PLAYBACK("PLAYER_PLAYBACK")
}

@Immutable
data class EqBand(
    val id: String,
    val label: String,
    val frequencyHz: Float,
    val gainDb: Float,
    val qFactor: Float
)

enum class EnhanceProfile(
    val title: String,
    val subtitle: String,
    val targetLufs: Float,
    val drive: Float,
    val width: Float,
    val punch: Float,
    val air: Float,
    val ceilingDb: Float,
    val eqOffsetsDb: List<Float>
) {
    STREAMING_MASTER(
        title = "STREAMING -14 LUFS",
        subtitle = "Balanced headroom for Spotify / Apple Music",
        targetLufs = -14.0f,
        drive = 0.28f,
        width = 0.58f,
        punch = 0.55f,
        air = 0.45f,
        ceilingDb = -1.0f,
        eqOffsetsDb = listOf(1.5f, 0.8f, -0.5f, 1.2f, 2.0f)
    ),
    CLUB_PUNCH(
        title = "CLUB MASTER -9 LUFS",
        subtitle = "High-energy low-end impact & brickwall limiting",
        targetLufs = -9.0f,
        drive = 0.58f,
        width = 0.68f,
        punch = 0.82f,
        air = 0.52f,
        ceilingDb = -0.3f,
        eqOffsetsDb = listOf(3.5f, 2.2f, -1.2f, 1.5f, 2.5f)
    ),
    VOCAL_CLARITY(
        title = "VOCAL PRESENCE",
        subtitle = "Focused 2–5kHz intelligibility & de-mudded low-mids",
        targetLufs = -13.0f,
        drive = 0.22f,
        width = 0.48f,
        punch = 0.45f,
        air = 0.68f,
        ceilingDb = -1.0f,
        eqOffsetsDb = listOf(-1.0f, -1.5f, 0.8f, 3.2f, 2.8f)
    ),
    WARM_ANALOG(
        title = "ANALOG TAPE WARMTH",
        subtitle = "Even-harmonic saturation with smooth top-end rolloff",
        targetLufs = -12.5f,
        drive = 0.66f,
        width = 0.44f,
        punch = 0.60f,
        air = 0.22f,
        ceilingDb = -0.8f,
        eqOffsetsDb = listOf(2.4f, 1.8f, 0.6f, -0.8f, -1.2f)
    )
}

enum class OversamplingFactor(val factor: Int, val label: String) {
    X4(4, "4X OVERSAMPLED"),
    X8(8, "8X OVERSAMPLED")
}

enum class MusicGenre(
    val code: String,
    val title: String,
    val subtitle: String,
    val recommendedProfile: EnhanceProfile,
    val targetKhz: Int,
    val oversampling: OversamplingFactor,
    val ceilingDb: Float,
    val drive: Float,
    val width: Float,
    val punch: Float,
    val air: Float,
    val eqOffsetsDb: List<Float>,
    val targetLufs: Float = -14.0f
) {
    POP(
        code = "POP",
        title = "POP",
        subtitle = "Air shelf & tight punch for streaming",
        recommendedProfile = EnhanceProfile.STREAMING_MASTER,
        targetKhz = 96,
        oversampling = OversamplingFactor.X4,
        ceilingDb = -1.0f,
        drive = 0.30f,
        width = 0.60f,
        punch = 0.65f,
        air = 0.60f,
        eqOffsetsDb = listOf(2.0f, 0.5f, -0.6f, 1.8f, 2.8f),
        targetLufs = -14.0f
    ),
    ELECTRONIC(
        code = "ELECTRONIC",
        title = "ELECTRONIC",
        subtitle = "Sub-bass impact & ultra wide air",
        recommendedProfile = EnhanceProfile.CLUB_PUNCH,
        targetKhz = 96,
        oversampling = OversamplingFactor.X4,
        ceilingDb = -0.5f,
        drive = 0.45f,
        width = 0.72f,
        punch = 0.85f,
        air = 0.55f,
        eqOffsetsDb = listOf(3.8f, 1.5f, -1.2f, 1.8f, 3.2f),
        targetLufs = -14.0f
    ),
    CLASSICAL(
        code = "CLASSICAL",
        title = "CLASSICAL",
        subtitle = "Preserved dynamic range & warm acoustic fidelity",
        recommendedProfile = EnhanceProfile.WARM_ANALOG,
        targetKhz = 192,
        oversampling = OversamplingFactor.X8,
        ceilingDb = -1.2f,
        drive = 0.12f,
        width = 0.52f,
        punch = 0.35f,
        air = 0.38f,
        eqOffsetsDb = listOf(0.8f, 0.5f, 0.2f, 0.4f, 0.6f),
        targetLufs = -18.0f
    ),
    ACOUSTIC(
        code = "ACOUSTIC",
        title = "ACOUSTIC",
        subtitle = "Natural warmth, high dynamic headroom & organic air",
        recommendedProfile = EnhanceProfile.VOCAL_CLARITY,
        targetKhz = 192,
        oversampling = OversamplingFactor.X8,
        ceilingDb = -1.0f,
        drive = 0.18f,
        width = 0.48f,
        punch = 0.40f,
        air = 0.50f,
        eqOffsetsDb = listOf(0.5f, -0.5f, 0.8f, 2.2f, 1.8f),
        targetLufs = -16.0f
    ),
    ROCK(
        code = "ROCK",
        title = "ROCK",
        subtitle = "Harmonic tape drive & mid-presence bite",
        recommendedProfile = EnhanceProfile.WARM_ANALOG,
        targetKhz = 96,
        oversampling = OversamplingFactor.X4,
        ceilingDb = -0.8f,
        drive = 0.65f,
        width = 0.55f,
        punch = 0.70f,
        air = 0.40f,
        eqOffsetsDb = listOf(2.5f, 1.8f, 1.0f, 1.5f, 0.8f),
        targetLufs = -14.0f
    ),
    HIP_HOP(
        code = "HIP_HOP",
        title = "HIP-HOP",
        subtitle = "Heavy 808 transient punch & crisp vocal articulation",
        recommendedProfile = EnhanceProfile.CLUB_PUNCH,
        targetKhz = 96,
        oversampling = OversamplingFactor.X4,
        ceilingDb = -0.6f,
        drive = 0.52f,
        width = 0.64f,
        punch = 0.88f,
        air = 0.48f,
        eqOffsetsDb = listOf(4.2f, 1.0f, -1.2f, 2.5f, 2.2f),
        targetLufs = -14.0f
    ),
    DRILL(
        code = "DRILL",
        title = "DRILL",
        subtitle = "Deep sliding 808 sub, hard transient snap & crisp hats",
        recommendedProfile = EnhanceProfile.CLUB_PUNCH,
        targetKhz = 96,
        oversampling = OversamplingFactor.X8,
        ceilingDb = -0.4f,
        drive = 0.58f,
        width = 0.65f,
        punch = 0.94f,
        air = 0.55f,
        eqOffsetsDb = listOf(4.6f, 1.2f, -1.8f, 2.4f, 3.2f),
        targetLufs = -8.5f
    ),
    TECHNO(
        code = "TECHNO",
        title = "TECHNO",
        subtitle = "Driving 4/4 industrial kick, hypnotic sub rumble & wide air",
        recommendedProfile = EnhanceProfile.CLUB_PUNCH,
        targetKhz = 96,
        oversampling = OversamplingFactor.X4,
        ceilingDb = -0.5f,
        drive = 0.60f,
        width = 0.78f,
        punch = 0.82f,
        air = 0.50f,
        eqOffsetsDb = listOf(3.6f, 2.0f, -0.8f, 1.4f, 2.2f),
        targetLufs = -10.0f
    ),
    TRAP(
        code = "TRAP",
        title = "TRAP",
        subtitle = "Heavy distorted 808 boom, aggressive claps & high sheen",
        recommendedProfile = EnhanceProfile.CLUB_PUNCH,
        targetKhz = 96,
        oversampling = OversamplingFactor.X4,
        ceilingDb = -0.3f,
        drive = 0.52f,
        width = 0.66f,
        punch = 0.88f,
        air = 0.62f,
        eqOffsetsDb = listOf(4.8f, 1.0f, -1.6f, 2.2f, 3.4f),
        targetLufs = -9.0f
    )
}

@Immutable
data class TactileDeckParams(
    val drive: Float = 0.28f,
    val width: Float = 0.56f,
    val punch: Float = 0.52f,
    val air: Float = 0.42f,
    val ceilingDb: Float = -1.0f
)

@Immutable
data class StemChannelState(
    val id: String,
    val label: String,
    val bandDescription: String,
    val level: Float = 1.0f,
    val muted: Boolean = false,
    val soloed: Boolean = false
)

@Immutable
data class StemMixerState(
    val vocals: StemChannelState = StemChannelState("vocals", "MID / VOCALS", "1.2kHz–5.5kHz Band-Pass"),
    val drums: StemChannelState = StemChannelState("drums", "TRANSIENTS", "180Hz+ High-Pass Snap"),
    val bass: StemChannelState = StemChannelState("bass", "SUB / BASS", "20Hz–220Hz Low-Pass"),
    val other: StemChannelState = StemChannelState("other", "AIR / SHEEN", "5.2kHz+ High-Shelf")
) {
    fun asList(): List<StemChannelState> = listOf(vocals, drums, bass, other)
}

enum class ExportFormat(
    val label: String,
    val bitDepth: Int,
    val sampleRate: Int,
    val extension: String,
    val codecDesc: String
) {
    WAV_24_48("WAV 24-BIT / 48kHz", 24, 48000, "wav", "Uncompressed PCM Master"),
    WAV_16_44("WAV 16-BIT / 44.1kHz", 16, 44100, "wav", "Red Book CD Standard (TPDF)"),
    WAV_24_96("WAV 24-BIT / 96kHz", 24, 96000, "wav", "Hi-Res Studio Archival"),
    WAV_24_192("WAV 24-BIT / 192kHz", 24, 192000, "wav", "Ultra Hi-Res 192kHz PCM Master"),
    WAV_32F_192("WAV 32-BIT FLOAT / 192kHz", 32, 192000, "wav", "64->32f IEEE Float Studio Master")
}

@Immutable
data class TranscodeConfig(
    val format: ExportFormat = ExportFormat.WAV_24_48,
    val ditherEnabled: Boolean = true,
    val normalizeToTarget: Boolean = true,
    val targetTruePeakDb: Float = -1.0f
)

@Immutable
data class TrackDiagnosticReport(
    val recordingTypeLabel: String = "FULL STUDIO MIX",
    val estimatedBandwidthKhz: Float = 18.2f,
    val hfDeficitPct: Int = 18,
    val clarityNeedLabel: String = "MODERATE",
    val recommendedProfile: EnhanceProfile = EnhanceProfile.STREAMING_MASTER,
    val recommendedRestorationAmount: Float = 0.55f,
    val autoAppliedSummary: String = "TFLITE GATE + HF RESTORE + -1.0 dBTP LIMITER",
    val detectedGenre: MusicGenre = MusicGenre.POP,
    val genreConfidencePct: Int = 88,
    val genreRationale: String = "Estimated from acoustic energy and crest ratio"
)

@Immutable
data class AudioTrackInfo(
    val id: String = "ref_studio_01",
    val title: String = "NOTHING_SESSION_01_RAW.wav",
    val sourceLabel: String = "BUNDLED STUDIO REF",
    val sampleRate: Int = 48000,
    val channels: Int = 2,
    val durationMs: Long = 16000L,
    val bitDepthLabel: String = "64-BIT DOUBLE DSP",
    val integratedLufs: Float = -16.4f,
    val truePeakDb: Float = -2.8f,
    val dynamicRangeDb: Float = 9.4f,
    val stereoWidthPct: Int = 64,
    val waveformPeaks: List<Float> = emptyList(),
    val isBundledRef: Boolean = true,
    val diagnostic: TrackDiagnosticReport = TrackDiagnosticReport()
)

enum class GlyphPatternMode(val label: String, val badge: String) {
    STEREO_VU_BRIDGE("STEREO VU", "L/R RMS + GR"),
    GAIN_REDUCTION_CLAMP("GR CLAMP", "DSP REDUCTION"),
    GLYPH_SPECTRUM_MATRIX("5-ZONE MATRIX", "FULL CHASSIS")
}

@Immutable
data class GlyphHardwareFrame(
    val patternMode: GlyphPatternMode = GlyphPatternMode.STEREO_VU_BRIDGE,
    val cameraPillLevel: Float = 0.08f,
    val diagonalStripLevel: Float = 0.08f,
    val centerRingSegments: List<Float> = List(8) { 0.08f },
    val coreDotLevel: Float = 0.08f,
    val usbExclamationSegments: List<Float> = List(6) { 0.06f },
    val rmsAvgNorm: Float = 0.05f,
    val gainReductionDb: Float = 0f,
    val rmsPercent: Int = 0,
    val gainReductionPercent: Int = 0,
    val pulsePatternLabel: String = "IDLE HEARTBEAT",
    val pwm12BitChannels: List<Int> = List(5) { 256 }
)

@Immutable
data class AudioTelemetry(
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 16000L,
    val spectrumBins: List<Float> = List(64) { 0.05f },
    val scopeSamples: List<Float> = List(96) { 0f },
    val leftVuDb: Float = -60f,
    val rightVuDb: Float = -60f,
    val integratedLufs: Float = -16.4f,
    val truePeakDb: Float = -2.8f,
    val stereoCorrelation: Float = 0.72f,
    val glyphZones: List<Float> = List(5) { 0.05f },
    val effectiveSampleRate: Int = 48000,
    val neuralGateReductionPct: Float = 0f,
    val neuralDynGainDb: Float = 0f,
    val limiterGainReductionDb: Float = 0f,
    val lowBandGrDb: Float = 0f,
    val midBandGrDb: Float = 0f,
    val highBandGrDb: Float = 0f,
    val dspLatencyMs: Float = 0f,
    val isBypassed: Boolean = false,
    val glyphFrame: GlyphHardwareFrame = GlyphHardwareFrame()
)

@Immutable
data class ProcessingStatus(
    val isRunning: Boolean = false,
    val progress: Float = 0f,
    val currentStage: String = "IDLE — READY FOR AUDIT",
    val completedPasses: Int = 0,
    val hasCompletedOnce: Boolean = false
)

@Immutable
data class ExportJobState(
    val isExporting: Boolean = false,
    val progress: Float = 0f,
    val stageLabel: String = "READY",
    val bytesWritten: Long = 0L,
    val lastExportedPath: String? = null,
    val lastExportedTitle: String? = null,
    val lastExportedFormat: String? = null,
    val lastFinalLufs: Float = -14.0f,
    val lastFinalPeakDb: Float = -1.0f,
    val errorMessage: String? = null
)

@Immutable
data class MicCaptureState(
    val isRecording: Boolean = false,
    val durationMs: Long = 0L,
    val inputLevel: Float = 0f,
    val permissionDenied: Boolean = false,
    val errorMessage: String? = null
)

enum class StartupCheckStatus(val label: String) {
    PASS("PASS"),
    ADVISORY("ADVISORY"),
    FALLBACK("FALLBACK")
}

enum class StartupHealthState(val label: String) {
    VALIDATING("VALIDATING"),
    HEALTHY("HEALTHY"),
    PERMISSION_ADVISORY("PERMISSION ADVISORY"),
    DEGRADED_FALLBACK("DEGRADED FALLBACK")
}

@Immutable
data class StartupSubsystemCheck(
    val title: String,
    val status: StartupCheckStatus,
    val detail: String
)

@Immutable
data class StartupGuardReport(
    val healthState: StartupHealthState = StartupHealthState.VALIDATING,
    val tfliteCheck: StartupSubsystemCheck = StartupSubsystemCheck(
        title = "TFLITE MODEL",
        status = StartupCheckStatus.PASS,
        detail = "Pending verification"
    ),
    val permissionCheck: StartupSubsystemCheck = StartupSubsystemCheck(
        title = "RECORD_AUDIO",
        status = StartupCheckStatus.ADVISORY,
        detail = "Pending verification"
    ),
    val databaseCheck: StartupSubsystemCheck = StartupSubsystemCheck(
        title = "ROOM SQLITE",
        status = StartupCheckStatus.PASS,
        detail = "Pending verification"
    ),
    val fallbackSummary: String = "Pre-flight startup validation in progress.",
    val checkedAtMs: Long = 0L
) {
    val isDegraded: Boolean
        get() = healthState == StartupHealthState.DEGRADED_FALLBACK
}

@Immutable
data class WaviqUiState(
    val activeRouteId: Int = 1,
    val activeTab: WaviqTab = WaviqTab.STUDIO,
    val isDevMapOpen: Boolean = false,
    val devMapFilter: WaviqTab? = null,
    val inspectedRouteId: Int = 1,
    val lastDispatchedLog: String = "LOCAL AI + 64-BIT STUDIO READY",
    val busModeOverride: AudioBusMode? = null,
    val trackInfo: AudioTrackInfo = AudioTrackInfo(),
    val telemetry: AudioTelemetry = AudioTelemetry(),
    val eqBands: List<EqBand> = defaultEqBands(),
    val selectedProfile: EnhanceProfile = EnhanceProfile.STREAMING_MASTER,
    val selectedGenre: MusicGenre = MusicGenre.POP,
    val isGenreAutoDetected: Boolean = true,
    val deckParams: TactileDeckParams = TactileDeckParams(),
    val stemMixer: StemMixerState = StemMixerState(),
    val abEnhancedEngaged: Boolean = true,
    val bypassEngaged: Boolean = true,
    val processing: ProcessingStatus = ProcessingStatus(),
    val transcodeConfig: TranscodeConfig = TranscodeConfig(),
    val exportJob: ExportJobState = ExportJobState(),
    val isLoadingFile: Boolean = false,
    val fileErrorMessage: String? = null,
    val activePlayerTrackId: Int? = null,
    val micCapture: MicCaptureState = MicCaptureState(),
    val dspHooks: List<com.example.audio.DspHookSnapshot> = emptyList(),
    val oversamplingFactor: OversamplingFactor = OversamplingFactor.X4,
    val hiResTargetKhz: Int = 96,
    val tfliteRuntimeMode: String = "ON-DEVICE TFLITE",
    val glyphPatternMode: GlyphPatternMode = GlyphPatternMode.STEREO_VU_BRIDGE,
    val startupGuard: StartupGuardReport = StartupGuardReport(),
    val isStartupBannerDismissed: Boolean = false,
    val aiCommandInput: String = "",
    val aiAppliedSummary: String = "Auto-tuned 64-bit EQ, SBR High-Freq & 3-Band ISP Ceiling",
    val aiConfidencePct: Int = 92,
    val isProSheetOpen: Boolean = false,
    val proTuningTab: ProTuningTab = ProTuningTab.EQ
)

fun defaultEqBands(): List<EqBand> = listOf(
    EqBand("sub", "SUB", 60f, 1.2f, 0.8f),
    EqBand("bass", "LOW", 180f, 0.6f, 0.9f),
    EqBand("mid", "MID", 900f, -0.4f, 1.1f),
    EqBand("pres", "PRES", 3600f, 1.4f, 1.0f),
    EqBand("air", "AIR", 12000f, 2.0f, 0.7f)
)
