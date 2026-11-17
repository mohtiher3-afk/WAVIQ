package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.StartupGuard
import com.example.audio.AiMasterDecision
import com.example.audio.AudioDecoder
import com.example.audio.AudioDspEngine
import com.example.audio.LocalAiMasteringAgent
import com.example.audio.OnDeviceModelRegistry
import com.example.data.MasterLibraryRepository
import com.example.data.MasterTrackEntity
import com.example.data.WaviqDatabase
import com.example.model.AudioBusMode
import com.example.model.AudioTrackInfo
import com.example.model.EnhanceProfile
import com.example.model.EqBand
import com.example.model.ExportFormat
import com.example.model.ExportJobState
import com.example.model.GlyphPatternMode
import com.example.model.MicCaptureState
import com.example.model.MusicGenre
import com.example.model.OversamplingFactor
import com.example.model.ProTuningTab
import com.example.model.ProcessingStatus
import com.example.model.StartupCheckStatus
import com.example.model.StartupHealthState
import com.example.model.StemMixerState
import com.example.model.TactileDeckParams
import com.example.model.TranscodeConfig
import com.example.model.WaviqRouteRegistry
import com.example.model.WaviqTab
import com.example.model.WaviqUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class WaviqViewModel(application: Application) : AndroidViewModel(application) {

    private val dspEngine = AudioDspEngine(application, viewModelScope)
    private val startupGuard = StartupGuard(application)
    private val repository = MasterLibraryRepository(
        WaviqDatabase.getInstance(application).masterTrackDao()
    )

    private val _uiState = MutableStateFlow(WaviqUiState())
    val uiState: StateFlow<WaviqUiState> = _uiState.asStateFlow()

    val savedMasters: StateFlow<List<MasterTrackEntity>> = repository.tracks
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var processingJob: Job? = null
    private var exportJob: Job? = null
    private var activeExportTargetFile: File? = null

    init {
        dspEngine.telemetry
            .onEach { tel ->
                runCatching {
                    val modeBadge = when (dspEngine.getTfliteRuntimeLabel()) {
                        "LITERT_COMPILED" -> "LITERT TFLITE"
                        "DSP_FALLBACK" -> "ON-DEVICE TFLITE DSP"
                        else -> "ON-DEVICE TFLITE"
                    }
                    val (gatePct, dynDb) = if (tel.isPlaying) {
                        dspEngine.getNeuralTelemetry()
                    } else {
                        0f to 0f
                    }
                    val limiterGrDb = if (tel.isPlaying) {
                        dspEngine.getLimiterGainReductionDb()
                    } else {
                        0f
                    }
                    val (lowGr, midGr, highGr) = if (tel.isPlaying) {
                        dspEngine.getMultiBandLimiterTelemetry()
                    } else {
                        Triple(0f, 0f, 0f)
                    }
                    val activePattern = _uiState.value.glyphPatternMode
                    val glyphFrame = dspEngine.buildGlyphFrame(
                        tel = tel,
                        neuralGatePct = gatePct,
                        limiterGrDb = limiterGrDb,
                        patternMode = activePattern
                    )
                    val syncedZones = listOf(
                        glyphFrame.cameraPillLevel,
                        glyphFrame.diagonalStripLevel,
                        glyphFrame.centerRingSegments.average().toFloat(),
                        glyphFrame.coreDotLevel,
                        glyphFrame.usbExclamationSegments.maxOrNull() ?: 0.05f
                    )
                    _uiState.update {
                        it.copy(
                            telemetry = tel.copy(
                                neuralGateReductionPct = gatePct,
                                neuralDynGainDb = dynDb,
                                limiterGainReductionDb = limiterGrDb,
                                lowBandGrDb = lowGr,
                                midBandGrDb = midGr,
                                highBandGrDb = highGr,
                                glyphZones = syncedZones,
                                glyphFrame = glyphFrame
                            ),
                            tfliteRuntimeMode = modeBadge
                        )
                    }
                }
            }
            .launchIn(viewModelScope)

        dspEngine.hookSnapshots
            .onEach { hooks ->
                _uiState.update { it.copy(dspHooks = hooks) }
            }
            .launchIn(viewModelScope)

        runStartupGuardValidation(loadReferenceAfter = true)
    }

    fun rerunStartupGuard() {
        runStartupGuardValidation(loadReferenceAfter = false)
    }

    fun dismissStartupFallbackBanner() {
        _uiState.update { it.copy(isStartupBannerDismissed = true) }
    }

    private fun runStartupGuardValidation(loadReferenceAfter: Boolean) {
        viewModelScope.launch {
            val report = runCatching { startupGuard.validate() }.getOrElse { err ->
                _uiState.value.startupGuard.copy(
                    healthState = StartupHealthState.DEGRADED_FALLBACK,
                    fallbackSummary = "Startup validation fallback: ${err.message ?: "unexpected error"}"
                )
            }
            val classSource = OnDeviceModelRegistry.detectStyleClassificationSource(getApplication())
            _uiState.update { state ->
                state.copy(
                    startupGuard = report,
                    classificationSourceLabel = classSource.badgeLabel,
                    isStartupBannerDismissed = if (report.isDegraded) false else state.isStartupBannerDismissed,
                    tfliteRuntimeMode = if (report.tfliteCheck.status == StartupCheckStatus.FALLBACK) {
                        "DSP FALLBACK"
                    } else {
                        state.tfliteRuntimeMode
                    },
                    lastDispatchedLog = "STARTUP_GUARD // ${report.healthState.label}"
                )
            }
            if (loadReferenceAfter) {
                loadBundledReference(autoPlay = false)
            }
        }
    }

    fun startMicCapture() {
        _uiState.update {
            it.copy(
                micCapture = MicCaptureState(
                    isRecording = true,
                    durationMs = 0L,
                    inputLevel = 0f,
                    permissionDenied = false,
                    errorMessage = null
                ),
                lastDispatchedLog = "AUDIORECORD // MIC CAPTURE ACTIVE"
            )
        }
        dspEngine.startMicRecording(
            onMeterUpdate = { level, durMs ->
                _uiState.update {
                    it.copy(
                        micCapture = it.micCapture.copy(
                            isRecording = true,
                            inputLevel = level,
                            durationMs = durMs
                        )
                    )
                }
            },
            onError = { msg ->
                _uiState.update {
                    it.copy(
                        micCapture = it.micCapture.copy(
                            isRecording = false,
                            errorMessage = msg
                        )
                    )
                }
            }
        )
    }

    fun stopMicCaptureAndLoad() {
        val captured = dspEngine.stopMicRecording()
        _uiState.update {
            it.copy(
                micCapture = MicCaptureState(isRecording = false)
            )
        }
        if (captured != null) {
            _uiState.update {
                it.copy(
                    trackInfo = captured.info,
                    activePlayerTrackId = null
                )
            }
            applyAutonomousMasteringCalibration(captured.info)
        }
    }

    fun onMicPermissionDenied() {
        _uiState.update {
            it.copy(
                micCapture = it.micCapture.copy(
                    isRecording = false,
                    permissionDenied = true,
                    errorMessage = "Microphone permission is optional; enable RECORD_AUDIO to capture live takes."
                )
            )
        }
    }

    fun toggleDspHook(hookId: String, enabled: Boolean) {
        dspEngine.toggleHook(hookId, enabled)
        _uiState.update {
            it.copy(lastDispatchedLog = "DSP_HOOK // $hookId = ${if (enabled) "ON" else "BYPASS"}")
        }
    }

    fun setOversamplingFactor(factor: OversamplingFactor) {
        dspEngine.setOversamplingFactor(factor)
        _uiState.update {
            it.copy(
                oversamplingFactor = factor,
                lastDispatchedLog = "TRUE_PEAK // ${factor.label}"
            )
        }
    }

    fun toggleHiResTargetKhz() {
        val next = if (_uiState.value.hiResTargetKhz == 96) 192 else 96
        dspEngine.setHiResTargetKhz(next)
        _uiState.update {
            it.copy(
                hiResTargetKhz = next,
                lastDispatchedLog = "HI_RES_ENGINE // ${next}kHz MASTER TARGET"
            )
        }
    }

    fun setGlyphPatternMode(mode: GlyphPatternMode) {
        _uiState.update { state ->
            val frame = dspEngine.buildGlyphFrame(
                tel = state.telemetry,
                neuralGatePct = state.telemetry.neuralGateReductionPct,
                limiterGrDb = state.telemetry.limiterGainReductionDb,
                patternMode = mode
            )
            state.copy(
                glyphPatternMode = mode,
                telemetry = state.telemetry.copy(glyphFrame = frame),
                lastDispatchedLog = "GLYPH_VU // ${mode.label} (${mode.badge})"
            )
        }
    }

    fun loadBundledReference(autoPlay: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingFile = true, fileErrorMessage = null) }
            runCatching {
                withContext(Dispatchers.Default) {
                    AudioDecoder.generateStudioReferenceTrack()
                }
            }.onSuccess { decoded ->
                dspEngine.loadDecodedAudio(decoded)
                _uiState.update {
                    it.copy(
                        isLoadingFile = false,
                        trackInfo = decoded.info,
                        activePlayerTrackId = null
                    )
                }
                applyAutonomousMasteringCalibration(decoded.info)
                if (autoPlay) dspEngine.play()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoadingFile = false,
                        fileErrorMessage = err.message ?: "Studio reference initialization deferred."
                    )
                }
            }
        }
    }

    fun importLocalAudio(uri: Uri) {
        viewModelScope.launch {
            dspEngine.pause()
            _uiState.update { it.copy(isLoadingFile = true, fileErrorMessage = null) }
            runCatching {
                withContext(Dispatchers.IO) {
                    AudioDecoder.decodeUriToPcm(getApplication(), uri)
                }
            }.onSuccess { decoded ->
                dspEngine.loadDecodedAudio(decoded)
                _uiState.update {
                    it.copy(
                        isLoadingFile = false,
                        trackInfo = decoded.info,
                        activePlayerTrackId = null
                    )
                }
                applyAutonomousMasteringCalibration(decoded.info)
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoadingFile = false,
                        fileErrorMessage = err.message ?: "Unable to decode audio file."
                    )
                }
            }
        }
    }

    fun clearFileError() {
        _uiState.update { it.copy(fileErrorMessage = null) }
    }

    fun loadSavedMasterForPlayback(track: MasterTrackEntity) {
        viewModelScope.launch {
            val file = File(track.filePath)
            if (!file.exists()) {
                _uiState.update {
                    it.copy(fileErrorMessage = "Master file missing from local storage.")
                }
                return@launch
            }
            dspEngine.pause()
            _uiState.update { it.copy(isLoadingFile = true, fileErrorMessage = null) }
            runCatching {
                withContext(Dispatchers.IO) {
                    AudioDecoder.decodeUriToPcm(getApplication(), Uri.fromFile(file))
                }
            }.onSuccess { decoded ->
                val updatedInfo = decoded.info.copy(
                    title = track.title,
                    sourceLabel = "OFFLINE MASTER (${track.formatLabel})",
                    integratedLufs = track.finalLufs,
                    truePeakDb = track.finalPeakDb
                )
                dspEngine.loadDecodedAudio(decoded)
                syncDspToEngine()
                _uiState.update {
                    it.copy(
                        isLoadingFile = false,
                        trackInfo = updatedInfo,
                        activePlayerTrackId = track.id,
                        lastDispatchedLog = "PLAYER // LOADED ${track.title}"
                    )
                }
                dspEngine.play()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoadingFile = false,
                        fileErrorMessage = err.message ?: "Playback load error."
                    )
                }
            }
        }
    }

    fun deleteSavedMaster(id: Int) {
        viewModelScope.launch {
            repository.deleteMaster(id)
        }
    }

    fun selectTab(tab: WaviqTab) {
        val mappedRouteId = if (tab == WaviqTab.STUDIO) 1 else 13
        _uiState.update {
            it.copy(
                activeTab = tab,
                activeRouteId = mappedRouteId,
                inspectedRouteId = mappedRouteId,
                lastDispatchedLog = "WORKSPACE // ${tab.title}"
            )
        }
        syncDspToEngine()
    }

    fun updateAiCommandInput(input: String) {
        _uiState.update { it.copy(aiCommandInput = input) }
    }

    fun submitAiCommand(command: String = _uiState.value.aiCommandInput) {
        val state = _uiState.value
        val decision = LocalAiMasteringAgent.applyIntentCommand(
            rawCommand = command,
            info = state.trackInfo,
            currentEq = state.eqBands,
            currentDeck = state.deckParams,
            currentGenre = state.selectedGenre,
            currentProfile = state.selectedProfile,
            currentTargetKhz = state.hiResTargetKhz,
            currentOversampling = state.oversamplingFactor,
            currentRestoration = dspEngine.getHiResRestorationAmount()
        )
        applyAiDecision(decision, clearInput = true, autoDetected = false)
    }

    fun cancelProcessing() {
        processingJob?.cancel()
        processingJob = null
        _uiState.update {
            it.copy(
                processing = ProcessingStatus(
                    isRunning = false,
                    progress = 0f,
                    currentStage = "CANCELLED BY USER",
                    completedPasses = 0,
                    hasCompletedOnce = it.processing.hasCompletedOnce,
                    isCancelled = true,
                    errorMessage = null
                ),
                lastDispatchedLog = "PROCESSING CANCELLED // DSP STATE PRESERVED"
            )
        }
    }

    fun runAutoMasterAi() {
        processingJob?.cancel()
        processingJob = viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        processing = ProcessingStatus(
                            isRunning = true,
                            progress = 0.30f,
                            currentStage = "ANALYSIS // EVALUATING ACOUSTIC BALANCE & CREST RATIO",
                            completedPasses = 1,
                            hasCompletedOnce = it.processing.hasCompletedOnce,
                            isCancelled = false,
                            errorMessage = null
                        )
                    )
                }
                val cur = _uiState.value
                val decision = withContext(Dispatchers.Default) {
                    LocalAiMasteringAgent.autoCalibrate(cur.trackInfo, cur.eqBands)
                }
                _uiState.update {
                    it.copy(
                        processing = it.processing.copy(
                            progress = 0.75f,
                            currentStage = "DSP CONFIG // APPLYING 64-BIT CALIBRATION & 3-BAND LIMITER",
                            completedPasses = 2
                        )
                    )
                }
                applyAiDecision(decision, clearInput = false, autoDetected = true)
                _uiState.update {
                    it.copy(
                        processing = ProcessingStatus(
                            isRunning = false,
                            progress = 1.0f,
                            currentStage = "RECOMMENDATIONS APPLIED // ${decision.appliedSummary}",
                            completedPasses = 3,
                            hasCompletedOnce = true,
                            isCancelled = false,
                            errorMessage = null
                        )
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (err: Throwable) {
                _uiState.update {
                    it.copy(
                        processing = ProcessingStatus(
                            isRunning = false,
                            progress = 0f,
                            currentStage = "ERROR // ${err.message ?: "Calibration failed"}",
                            hasCompletedOnce = it.processing.hasCompletedOnce,
                            isCancelled = false,
                            errorMessage = err.message ?: "Calibration failed"
                        )
                    )
                }
            }
        }
    }

    fun setProSheetOpen(open: Boolean) {
        _uiState.update { it.copy(isProSheetOpen = open) }
        syncDspToEngine()
    }

    fun setProTuningTab(tab: ProTuningTab) {
        _uiState.update { it.copy(proTuningTab = tab) }
        syncDspToEngine()
    }

    fun onRouteChanged(routeId: Int) {
        val spec = WaviqRouteRegistry.byId(routeId)
        _uiState.update {
            it.copy(
                activeRouteId = routeId,
                activeTab = spec.tab,
                inspectedRouteId = routeId,
                busModeOverride = null,
                lastDispatchedLog = "ROUTE ${spec.code} // ${spec.busMode.code}"
            )
        }
        syncDspToEngine()
    }

    fun toggleDevMap(open: Boolean) {
        _uiState.update { it.copy(isDevMapOpen = open) }
    }

    fun setDevMapFilter(tab: WaviqTab?) {
        _uiState.update { it.copy(devMapFilter = tab) }
    }

    fun inspectRoute(routeId: Int) {
        _uiState.update { it.copy(inspectedRouteId = routeId) }
    }

    fun simulateDispatch(routeId: Int, jumpToScreen: Boolean, onNavigate: (Int) -> Unit) {
        val spec = WaviqRouteRegistry.byId(routeId)
        val newWet = spec.busMode != AudioBusMode.BYPASS_RAW
        _uiState.update {
            it.copy(
                inspectedRouteId = routeId,
                busModeOverride = spec.busMode,
                abEnhancedEngaged = newWet,
                bypassEngaged = newWet,
                lastDispatchedLog = "DISPATCHED ${spec.code} -> ${spec.path} [${spec.busMode.code}]",
                isDevMapOpen = if (jumpToScreen) false else it.isDevMapOpen
            )
        }
        syncDspToEngine()
        if (jumpToScreen) {
            onNavigate(routeId)
        }
    }

    fun togglePlayPause() {
        dspEngine.togglePlayPause()
    }

    fun seekToFraction(fraction: Float) {
        dspEngine.seekToFraction(fraction)
    }

    fun updateEqBand(bandId: String, gainDb: Float) {
        _uiState.update { state ->
            val updated = state.eqBands.map {
                if (it.id == bandId) it.copy(gainDb = gainDb.coerceIn(-12f, 12f)) else it
            }
            state.copy(eqBands = updated)
        }
        syncDspToEngine()
    }

    fun resetEq() {
        _uiState.update { state ->
            state.copy(eqBands = state.eqBands.map { it.copy(gainDb = 0f) })
        }
        syncDspToEngine()
    }

    fun selectEnhanceProfile(profile: EnhanceProfile) {
        _uiState.update { state ->
            val updatedEq = state.eqBands.mapIndexed { idx, band ->
                band.copy(gainDb = profile.eqOffsetsDb.getOrElse(idx) { 0f })
            }
            state.copy(
                selectedProfile = profile,
                eqBands = updatedEq,
                deckParams = TactileDeckParams(
                    drive = profile.drive,
                    width = profile.width,
                    punch = profile.punch,
                    air = profile.air,
                    ceilingDb = profile.ceilingDb
                ),
                transcodeConfig = state.transcodeConfig.copy(targetTruePeakDb = profile.ceilingDb),
                abEnhancedEngaged = true,
                bypassEngaged = true
            )
        }
        syncDspToEngine()
    }

    fun selectMusicGenre(genre: MusicGenre) {
        dspEngine.setHiResTargetKhz(genre.targetKhz)
        dspEngine.setOversamplingFactor(genre.oversampling)
        _uiState.update { state ->
            val updatedEq = state.eqBands.mapIndexed { idx, band ->
                val baseGain = genre.eqOffsetsDb.getOrElse(idx) { 0f }
                band.copy(gainDb = baseGain.coerceIn(-12f, 12f))
            }
            state.copy(
                selectedGenre = genre,
                selectedProfile = genre.recommendedProfile,
                isGenreAutoDetected = false,
                oversamplingFactor = genre.oversampling,
                hiResTargetKhz = genre.targetKhz,
                eqBands = updatedEq,
                deckParams = TactileDeckParams(
                    drive = genre.drive,
                    width = genre.width,
                    punch = genre.punch,
                    air = genre.air,
                    ceilingDb = genre.ceilingDb
                ),
                transcodeConfig = state.transcodeConfig.copy(targetTruePeakDb = genre.ceilingDb),
                abEnhancedEngaged = true,
                bypassEngaged = true,
                lastDispatchedLog = "GENRE SET // ${genre.title} (${genre.targetKhz}kHz ${genre.oversampling.label})"
            )
        }
        syncDspToEngine()
    }

    fun applyMasteringPreset(presetName: String) {
        when (presetName.lowercase()) {
            "warm tape" -> {
                dspEngine.setHiResTargetKhz(96)
                dspEngine.setOversamplingFactor(OversamplingFactor.X8)
                selectEnhanceProfile(EnhanceProfile.WARM_ANALOG)
                updateDeckParams { it.copy(drive = 0.65f, punch = 0.45f, air = 0.30f, ceilingDb = -1.5f) }
                _uiState.update { it.copy(hiResTargetKhz = 96, oversamplingFactor = OversamplingFactor.X8) }
            }
            "transparent punch" -> {
                dspEngine.setHiResTargetKhz(192)
                dspEngine.setOversamplingFactor(OversamplingFactor.X4)
                selectEnhanceProfile(EnhanceProfile.CLUB_PUNCH)
                updateDeckParams { it.copy(drive = 0.15f, punch = 0.85f, air = 0.75f, ceilingDb = -1.0f) }
                _uiState.update { it.copy(hiResTargetKhz = 192, oversamplingFactor = OversamplingFactor.X4) }
            }
            "broadcast" -> {
                dspEngine.setHiResTargetKhz(96)
                dspEngine.setOversamplingFactor(OversamplingFactor.X4)
                selectEnhanceProfile(EnhanceProfile.VOCAL_CLARITY)
                updateDeckParams { it.copy(drive = 0.10f, punch = 0.50f, air = 0.55f, ceilingDb = -2.0f) }
                _uiState.update { it.copy(hiResTargetKhz = 96, oversamplingFactor = OversamplingFactor.X4) }
            }
        }
        _uiState.update { it.copy(lastDispatchedLog = "MASTERING PRESET // $presetName APPLIED") }
    }

    fun startDeepProcessing(onComplete: () -> Unit) {
        processingJob?.cancel()
        processingJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    processing = ProcessingStatus(
                        isRunning = true,
                        progress = 0.25f,
                        currentStage = "PASS 1/3 // BS.1770 LUFS & CREST AUDIT",
                        completedPasses = 1,
                        hasCompletedOnce = it.processing.hasCompletedOnce
                    )
                )
            }
            val selectedProfile = _uiState.value.selectedProfile
            withContext(Dispatchers.Default) {
                val (l, r) = dspEngine.audioProcessor.getCurrentBuffers()
                if (l.isNotEmpty() && r.isNotEmpty()) {
                    com.example.audio.AudioMeasurement.measureIntegratedLufs(l, r, dspEngine.audioProcessor.sampleRate)
                    com.example.audio.AudioMeasurement.measureTruePeakDb(l, r)
                }
            }
            _uiState.update {
                it.copy(
                    processing = it.processing.copy(
                        progress = 0.70f,
                        currentStage = "PASS 2/3 // CONFIGURING 64-BIT EQ & 3-BAND LIMITER",
                        completedPasses = 2
                    )
                )
            }
            selectEnhanceProfile(selectedProfile)
            _uiState.update {
                it.copy(
                    processing = ProcessingStatus(
                        isRunning = false,
                        progress = 1f,
                        currentStage = "CALIBRATION LOCKED // ${selectedProfile.title}",
                        completedPasses = 3,
                        hasCompletedOnce = true
                    )
                )
            }
            onComplete()
        }
    }

    fun updateDeckParams(transform: (TactileDeckParams) -> TactileDeckParams) {
        _uiState.update {
            val next = transform(it.deckParams)
            it.copy(
                deckParams = next,
                transcodeConfig = it.transcodeConfig.copy(targetTruePeakDb = next.ceilingDb)
            )
        }
        syncDspToEngine()
    }

    fun toggleAbEnhanced(enhanced: Boolean) {
        _uiState.update {
            it.copy(abEnhancedEngaged = enhanced, bypassEngaged = enhanced)
        }
        syncDspToEngine()
    }

    fun updateStem(
        stemId: String,
        level: Float? = null,
        toggleMute: Boolean = false,
        toggleSolo: Boolean = false
    ) {
        _uiState.update { state ->
            fun updateChannel(ch: com.example.model.StemChannelState): com.example.model.StemChannelState {
                if (ch.id != stemId) return ch
                return ch.copy(
                    level = level?.coerceIn(0f, 1.5f) ?: ch.level,
                    muted = if (toggleMute) !ch.muted else ch.muted,
                    soloed = if (toggleSolo) !ch.soloed else ch.soloed
                )
            }
            val m = state.stemMixer
            state.copy(
                stemMixer = StemMixerState(
                    vocals = updateChannel(m.vocals),
                    drums = updateChannel(m.drums),
                    bass = updateChannel(m.bass),
                    other = updateChannel(m.other)
                )
            )
        }
        syncDspToEngine()
    }

    fun resetStems() {
        _uiState.update { it.copy(stemMixer = StemMixerState()) }
        syncDspToEngine()
    }

    fun updateTranscodeConfig(
        format: ExportFormat? = null,
        dither: Boolean? = null,
        normalize: Boolean? = null
    ) {
        _uiState.update { state ->
            val cur = state.transcodeConfig
            state.copy(
                transcodeConfig = cur.copy(
                    format = format ?: cur.format,
                    ditherEnabled = dither ?: cur.ditherEnabled,
                    normalizeToTarget = normalize ?: cur.normalizeToTarget
                )
            )
        }
    }

    fun cancelExportMaster() {
        exportJob?.cancel()
        exportJob = null
        activeExportTargetFile?.let { f ->
            if (f.exists()) runCatching { f.delete() }
        }
        activeExportTargetFile = null
        _uiState.update { cur ->
            cur.copy(
                exportJob = cur.exportJob.copy(
                    isExporting = false,
                    progress = 0f,
                    stageLabel = "CANCELLED BY USER",
                    bytesWritten = 0L,
                    isCancelled = true,
                    errorMessage = null
                ),
                lastDispatchedLog = "EXPORT CANCELLED // PARTIAL FILE REMOVED"
            )
        }
    }

    fun startExportMaster(onReadyForModal: () -> Unit, onCompleteDone: () -> Unit) {
        if (_uiState.value.exportJob.isExporting) return
        onReadyForModal()
        exportJob?.cancel()
        exportJob = viewModelScope.launch {
            val state = _uiState.value
            val config = state.transcodeConfig
            val safeBase = state.trackInfo.title
                .substringBeforeLast('.')
                .replace(Regex("[^A-Za-z0-9_\\-]"), "_")
                .take(28)
                .ifEmpty { "WAVIQ_MASTER" }
            val timestamp = System.currentTimeMillis()
            val fileName = "${safeBase}_MASTER_${timestamp}.${config.format.extension}"
            val outDir = File(getApplication<Application>().filesDir, "masters")
            val outFile = File(outDir, fileName)
            activeExportTargetFile = outFile

            _uiState.update {
                it.copy(
                    exportJob = it.exportJob.copy(
                        isExporting = true,
                        progress = 0.02f,
                        stageLabel = "INITIALIZING PCM RENDER BUS",
                        bytesWritten = 0L,
                        isCancelled = false,
                        errorMessage = null
                    )
                )
            }

            try {
                val (finalLufs, finalPeak) = dspEngine.exportMasterWav(outFile, config) { p, bytes ->
                    val label = when {
                        p < 0.45f -> "APPLYING BIQUAD EQ + HARMONIC DSP"
                        p < 0.85f -> "ENCODING ${config.format.label}"
                        else -> "SEALING RIFF HEADER & VERIFYING"
                    }
                    _uiState.update { cur ->
                        cur.copy(
                            exportJob = cur.exportJob.copy(
                                isExporting = true,
                                progress = p,
                                stageLabel = label,
                                bytesWritten = bytes
                            )
                        )
                    }
                }

                val entity = MasterTrackEntity(
                    title = "${safeBase}_MASTER.${config.format.extension}",
                    formatLabel = config.format.label,
                    sampleRate = config.format.sampleRate,
                    bitDepth = config.format.bitDepth,
                    durationMs = state.trackInfo.durationMs,
                    finalLufs = finalLufs,
                    finalPeakDb = finalPeak,
                    profileName = state.selectedProfile.title,
                    filePath = outFile.absolutePath
                )
                repository.saveMaster(entity)
                activeExportTargetFile = null

                _uiState.update { cur ->
                    cur.copy(
                        exportJob = ExportJobState(
                            isExporting = false,
                            progress = 1f,
                            stageLabel = "100% VERIFIED & STORED",
                            bytesWritten = outFile.length(),
                            lastExportedPath = outFile.absolutePath,
                            lastExportedTitle = entity.title,
                            lastExportedFormat = config.format.label,
                            lastFinalLufs = finalLufs,
                            lastFinalPeakDb = finalPeak,
                            isCancelled = false,
                            errorMessage = null
                        ),
                        lastDispatchedLog = "EXPORT 100% // ${entity.title}"
                    )
                }
                onCompleteDone()
            } catch (e: CancellationException) {
                if (outFile.exists()) runCatching { outFile.delete() }
                activeExportTargetFile = null
                throw e
            } catch (err: Throwable) {
                if (outFile.exists()) runCatching { outFile.delete() }
                activeExportTargetFile = null
                _uiState.update { cur ->
                    cur.copy(
                        exportJob = cur.exportJob.copy(
                            isExporting = false,
                            isCancelled = false,
                            errorMessage = err.message ?: "Export failed."
                        )
                    )
                }
            }
        }
    }

    fun copyLastExportToUri(destUri: Uri, onResult: (Boolean) -> Unit) {
        val srcPath = _uiState.value.exportJob.lastExportedPath ?: return onResult(false)
        viewModelScope.launch(Dispatchers.IO) {
            val ok = runCatching {
                val src = File(srcPath)
                getApplication<Application>().contentResolver.openOutputStream(destUri)?.use { out ->
                    src.inputStream().use { inp -> inp.copyTo(out) }
                } ?: error("Cannot open destination URI")
            }.isSuccess
            withContext(Dispatchers.Main) { onResult(ok) }
        }
    }

    private fun applyAutonomousMasteringCalibration(info: AudioTrackInfo) {
        val decision = LocalAiMasteringAgent.autoCalibrate(info, _uiState.value.eqBands)
        applyAiDecision(decision, clearInput = false, autoDetected = true)
    }

    private fun applyAiDecision(
        decision: AiMasterDecision,
        clearInput: Boolean,
        autoDetected: Boolean
    ) {
        if (!decision.isSupported) {
            _uiState.update { state ->
                state.copy(
                    aiAppliedSummary = decision.unsupportedGuidance ?: decision.appliedSummary,
                    lastDispatchedLog = decision.appliedSummary,
                    aiCommandInput = if (clearInput) "" else state.aiCommandInput,
                    aiConfidencePct = 0,
                    lastCommandUnsupported = true
                )
            }
            return
        }

        dspEngine.enableAutonomousMasteringHooks()
        dspEngine.setHiResTargetKhz(decision.targetKhz)
        dspEngine.setOversamplingFactor(decision.oversampling)
        dspEngine.setHiResRestorationAmount(decision.restorationAmount)
        dspEngine.setHiResStereoExpansionAmount(decision.stereoExpansionAmount)

        val classSource = OnDeviceModelRegistry.detectStyleClassificationSource(getApplication())
        _uiState.update { state ->
            state.copy(
                selectedProfile = decision.selectedProfile,
                selectedGenre = decision.selectedGenre,
                isGenreAutoDetected = autoDetected,
                oversamplingFactor = decision.oversampling,
                hiResTargetKhz = decision.targetKhz,
                eqBands = decision.eqBands,
                deckParams = decision.deckParams,
                transcodeConfig = state.transcodeConfig.copy(targetTruePeakDb = decision.deckParams.ceilingDb),
                abEnhancedEngaged = true,
                bypassEngaged = true,
                aiCommandInput = if (clearInput) "" else state.aiCommandInput,
                aiAppliedSummary = decision.appliedSummary,
                aiConfidencePct = decision.confidencePct,
                lastCommandUnsupported = false,
                decisionStages = decision.stages,
                decisionSafetyWarnings = decision.safetyWarnings,
                classificationSourceLabel = classSource.badgeLabel,
                lastParsedIntents = decision.parsedIntents,
                decisionTargetStandard = decision.targetStandardLabel,
                lastDispatchedLog = decision.appliedSummary
            )
        }
        syncDspToEngine()
    }

    private fun syncDspToEngine() {
        val s = _uiState.value
        val wet = s.abEnhancedEngaged && s.bypassEngaged
        val stemsActive = s.isProSheetOpen && s.proTuningTab == ProTuningTab.STEMS
        dspEngine.updateDsp(
            bands = s.eqBands,
            deck = s.deckParams,
            stems = s.stemMixer,
            isStemScreen = stemsActive || s.activeRouteId == 10,
            wetEnabled = wet
        )
    }

    override fun onCleared() {
        super.onCleared()
        dspEngine.release()
    }
}
