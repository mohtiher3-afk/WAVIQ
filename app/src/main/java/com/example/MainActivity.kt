package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.model.ProTuningTab
import com.example.model.WaviqRouteRegistry
import com.example.model.WaviqTab
import com.example.ui.components.StudioTopBar
import com.example.ui.components.WaviqBottomNavBar
import com.example.ui.components.WaviqLeftNavRail
import com.example.ui.devmap.DeveloperMapScreen
import com.example.ui.screens.UnifiedLibraryScreen
import com.example.ui.screens.UnifiedStudioScreen
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.WaviqTheme
import com.example.viewmodel.WaviqViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            WaviqTheme {
                WaviqApp()
            }
        }
    }
}

@Composable
fun WaviqApp(viewModel: WaviqViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val savedMasters by viewModel.savedMasters.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentNavRoute = backStackEntry?.destination?.route

    val activeTab = remember(currentNavRoute, state.activeTab) {
        WaviqTab.entries.firstOrNull { it.routePrefix == currentNavRoute } ?: state.activeTab
    }

    val activeRouteSpec = remember(state.activeRouteId, activeTab) {
        WaviqRouteRegistry.byId(state.activeRouteId)
    }

    val studioScrollState = rememberSaveable(saver = ScrollState.Saver) { ScrollState(0) }
    val libraryScrollState = rememberSaveable(saver = ScrollState.Saver) { ScrollState(0) }

    val navigateToTab: (WaviqTab) -> Unit = remember(navController, currentNavRoute) {
        { tab ->
            viewModel.selectTab(tab)
            if (currentNavRoute != tab.routePrefix) {
                navController.navigate(tab.routePrefix) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    val navigateFromDevMapRouteId: (Int) -> Unit = remember(navController) {
        { routeId ->
            val spec = WaviqRouteRegistry.byId(routeId)
            viewModel.onRouteChanged(spec.id)
            navController.navigate(spec.tab.routePrefix) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
            when (routeId) {
                3 -> {
                    viewModel.setProTuningTab(ProTuningTab.EQ)
                    viewModel.setProSheetOpen(true)
                }
                7 -> {
                    viewModel.setProTuningTab(ProTuningTab.DECK)
                    viewModel.setProSheetOpen(true)
                }
                10 -> {
                    viewModel.setProTuningTab(ProTuningTab.STEMS)
                    viewModel.setProSheetOpen(true)
                }
                9, 11, 12 -> {
                    viewModel.setProTuningTab(ProTuningTab.ENGINE)
                    viewModel.setProSheetOpen(true)
                }
                else -> viewModel.setProSheetOpen(false)
            }
        }
    }

    val openAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importLocalAudio(uri)
        }
    }

    val context = LocalContext.current

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startMicCapture()
        } else {
            viewModel.onMicPermissionDenied()
        }
    }

    val saveExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("audio/wav")
    ) { destUri ->
        if (destUri != null) {
            viewModel.copyLastExportToUri(destUri) { ok ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        if (ok) "Master WAV saved to selected device folder." else "Failed to save WAV file."
                    )
                }
            }
        }
    }

    BackHandler(enabled = state.isDevMapOpen) {
        viewModel.toggleDevMap(false)
    }

    BackHandler(enabled = !state.isDevMapOpen && state.isProSheetOpen) {
        viewModel.setProSheetOpen(false)
    }

    BackHandler(enabled = !state.isDevMapOpen && !state.isProSheetOpen && activeTab != WaviqTab.STUDIO) {
        navigateToTab(WaviqTab.STUDIO)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            containerColor = ObsidianVoid,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                StudioTopBar(
                    activeRoute = activeRouteSpec,
                    busModeOverride = state.busModeOverride,
                    isPlaying = state.telemetry.isPlaying,
                    dspLatencyMs = state.telemetry.dspLatencyMs,
                    isBypassed = state.telemetry.isBypassed,
                    isDevMapOpen = state.isDevMapOpen,
                    aiConfidencePct = state.aiConfidencePct,
                    onTogglePlay = { viewModel.togglePlayPause() },
                    onToggleDevMap = { viewModel.toggleDevMap(!state.isDevMapOpen) }
                )
            },
            bottomBar = {
                if (!isWideScreen && !state.isDevMapOpen) {
                    WaviqBottomNavBar(
                        activeTab = activeTab,
                        onSelectTab = navigateToTab
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen && !state.isDevMapOpen) {
                    WaviqLeftNavRail(
                        activeTab = activeTab,
                        onSelectTab = navigateToTab
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .then(
                            if (isWideScreen) Modifier.navigationBarsPadding() else Modifier
                        ),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 860.dp)
                            .fillMaxSize()
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = WaviqTab.STUDIO.routePrefix,
                            enterTransition = { fadeIn(animationSpec = tween(180)) },
                            exitTransition = { fadeOut(animationSpec = tween(160)) }
                        ) {
                            composable(WaviqTab.STUDIO.routePrefix) {
                                UnifiedStudioScreen(
                                    state = state,
                                    onImportAudioClick = {
                                        openAudioLauncher.launch(arrayOf("audio/*"))
                                    },
                                    onLoadReferenceClick = { viewModel.loadBundledReference(autoPlay = true) },
                                    onStartMicCaptureClick = {
                                        val granted = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (granted) {
                                            viewModel.startMicCapture()
                                        } else {
                                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    },
                                    onStopMicCaptureClick = { viewModel.stopMicCaptureAndLoad() },
                                    onSeek = { viewModel.seekToFraction(it) },
                                    onToggleAb = { viewModel.toggleAbEnhanced(it) },
                                    onRunAutoMasterAi = { viewModel.runAutoMasterAi() },
                                    onUpdateAiInput = { viewModel.updateAiCommandInput(it) },
                                    onSubmitAiCommand = { viewModel.submitAiCommand(it) },
                                    onSetProSheetOpen = { viewModel.setProSheetOpen(it) },
                                    onSelectProTuningTab = { viewModel.setProTuningTab(it) },
                                    onUpdateBand = { id, db -> viewModel.updateEqBand(id, db) },
                                    onResetEq = { viewModel.resetEq() },
                                    onUpdateDeck = { viewModel.updateDeckParams(it) },
                                    onSelectProfile = { viewModel.selectEnhanceProfile(it) },
                                    onSelectGenre = { viewModel.selectMusicGenre(it) },
                                    onUpdateStem = { id, lvl, mute, solo ->
                                        viewModel.updateStem(id, lvl, mute, solo)
                                    },
                                    onResetStems = { viewModel.resetStems() },
                                    onToggleDspHook = { id, enabled -> viewModel.toggleDspHook(id, enabled) },
                                    onSetOversampling = { factor -> viewModel.setOversamplingFactor(factor) },
                                    onToggleHiResKhz = { viewModel.toggleHiResTargetKhz() },
                                    onSelectGlyphPattern = { mode -> viewModel.setGlyphPatternMode(mode) },
                                    onApplyMasteringPreset = { viewModel.applyMasteringPreset(it) },
                                    onRerunStartupGuard = { viewModel.rerunStartupGuard() },
                                    onDismissStartupBanner = { viewModel.dismissStartupFallbackBanner() },
                                    onCancelProcessing = { viewModel.cancelProcessing() },
                                    onClearFileError = { viewModel.clearFileError() },
                                    scrollState = studioScrollState
                                )
                            }

                            composable(WaviqTab.LIBRARY.routePrefix) {
                                UnifiedLibraryScreen(
                                    state = state,
                                    savedMasters = savedMasters,
                                    onUpdateConfig = { fmt, dither, norm ->
                                        viewModel.updateTranscodeConfig(fmt, dither, norm)
                                    },
                                    onStartExport = {
                                        viewModel.startExportMaster(
                                            onReadyForModal = { },
                                            onCompleteDone = {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        "Master WAV verified & saved to offline library."
                                                    )
                                                }
                                            }
                                        )
                                    },
                                    onSaveToDeviceClick = {
                                        val suggested = state.exportJob.lastExportedTitle ?: "WAVIQ_MASTER.wav"
                                        saveExportLauncher.launch(suggested)
                                    },
                                    onTogglePlay = { viewModel.togglePlayPause() },
                                    onSeek = { viewModel.seekToFraction(it) },
                                    onToggleAb = { viewModel.toggleAbEnhanced(it) },
                                    onLoadSavedMaster = { viewModel.loadSavedMasterForPlayback(it) },
                                    onDeleteSavedMaster = { viewModel.deleteSavedMaster(it) },
                                    onCancelExport = { viewModel.cancelExportMaster() },
                                    onGoToStudioClick = { navigateToTab(WaviqTab.STUDIO) },
                                    scrollState = libraryScrollState
                                )
                            }
                        }

                        // Developer & StartupGuard Diagnostics Overlay
                        androidx.compose.animation.AnimatedVisibility(
                            visible = state.isDevMapOpen,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier
                                .fillMaxSize()
                                .navigationBarsPadding()
                        ) {
                            DeveloperMapScreen(
                                state = state,
                                onSelectFilter = { viewModel.setDevMapFilter(it) },
                                onInspectRoute = { viewModel.inspectRoute(it) },
                                onSimulateDispatch = { id, jump ->
                                    viewModel.simulateDispatch(id, jump, navigateFromDevMapRouteId)
                                },
                                onRerunStartupGuard = { viewModel.rerunStartupGuard() }
                            )
                        }
                    }
                }
            }
        }
    }
}
