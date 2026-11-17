package com.example.model

object DevMapSchemas {

    const val GLOBAL_STATE_MACHINE_ASCII = """
┌─────────────────────────────────────────────────────────────────────────┐
│             WAVIQ (1) — GLOBAL 16-SCREEN STATE MACHINE FLOW             │
└─────────────────────────────────────────────────────────────────────────┘

  [SAF / REF LOAD]
         │
         ▼
┌─────────────────┐   spectral tap   ┌─────────────────┐   5-band biquad
│ #01 INSPECTOR   ├─────────────────►│ #02 LIVE SCOPE  ├─────────────────┐
│ /analyze/inspect│                  │ /analyze/scope  │                 │
└────────▲────────┘                  └─────────────────┘                 ▼
         │                                                      ┌─────────────────┐
         │ loop back                                            │ #03 PARAM EQ    │
         │                                                      │ /analyze/eq     │
┌────────┴────────┐                                             └────────┬────────┘
│ #16 PLAYER      │◄────────────────────────────────────────────┐        │
│ /library/player │                                             │        ▼
└────────▲────────┘                                             │ ┌─────────────────┐
         │                                                      │ │ #04 REPORTS     │
┌────────┴────────┐   seal 100%      ┌─────────────────┐        │ │ /analyze/reports│
│ #15 MASTER DONE │◄─────────────────┤ #14 EXPORT IO   │        │ └────────┬────────┘
│ /library/done   │                  │ /library/export │        │          │
└─────────────────┘                  └────────▲────────┘        │          ▼
                                              │                 │ ┌─────────────────┐
                                     ┌────────┴────────┐        │ │ #05 AI ENGINE   │
                                     │ #13 TRANSCODE   │◄───┐   │ │ /enhance/engine │
                                     │ /library/transc.│    │   │ └────────┬────────┘
                                     └─────────────────┘    │   │          │
                                                            │   │          ▼
┌─────────────────┐   4-way matrix   ┌─────────────────┐    │   │ ┌─────────────────┐
│ #12 GLYPH VU    │◄─────────────────┤ #11 BYPASS RELAY│◄───┼───┼─┤ #06 3.5S PASS   │
│ /studio/glyph   │                  │ /studio/bypass  │    │   │ │ /enhance/proc.  │
└────────┬────────┘                  └────────▲────────┘    │   │ └────────┬────────┘
         │                                    │             │   │          │
         └────────────────────────────────────┼─────────────┘   │          ▼
                                     ┌────────┴────────┐        │ ┌─────────────────┐
                                     │ #10 BAND MIXER  │◄───────┼─┤ #07 TACTILE DECK│
                                     │ /studio/stems   │        │ │ /enhance/deck   │
                                     └────────▲────────┘        │ └────────┬────────┘
                                              │                 │          │
                                     ┌────────┴────────┐        │          ▼
                                     │ #09 PRO SUITE   │◄───────┴─┤ #08 A/B COMPARE │
                                     │ /studio/pro     │          │ /enhance/ab     │
                                     └─────────────────┘          └─────────────────┘
    """

    const val SCHEMA_01 = """// RoutePayloadSchema.ts — #01 Audio Track Inspector
export interface Route01InspectorPayload {
  routeId: '#01';
  path: '/analyze/inspector';
  busMode: 'SPECTRAL_TAP';
  sampleRateHz: 48000 | 96000;
  channels: 1 | 2;
  integratedLufs: number;
  truePeakDb: number;
  sourceType: 'SAF_DOCUMENT' | 'BUNDLED_REF';
}"""

    const val SCHEMA_02 = """// RoutePayloadSchema.ts — #02 Live Scope Analyzer
export interface Route02ScopePayload {
  routeId: '#02';
  path: '/analyze/scope';
  busMode: 'SPECTRAL_TAP';
  fftBins: 64;
  windowFn: 'HANN';
  stereoCorrelation: number;
  refreshHz: 30 | 60;
}"""

    const val SCHEMA_03 = """// RoutePayloadSchema.ts — #03 Parametric EQ & Filters
export interface Route03ParamEqPayload {
  routeId: '#03';
  path: '/analyze/eq';
  busMode: 'NPU_DSP_ACTIVE';
  crossfadeMs: 15;
  bands: Array<{
    id: 'sub' | 'bass' | 'mid' | 'pres' | 'air';
    freqHz: number;
    gainDb: number;
    q: number;
  }>;
}"""

    const val SCHEMA_04 = """// RoutePayloadSchema.ts — #04 Analysis Reports
export interface Route04ReportsPayload {
  routeId: '#04';
  path: '/analyze/reports';
  busMode: 'SPECTRAL_TAP';
  headroomDb: number;
  lufsDeltaToMinus14: number;
  dynamicRangeDb: number;
  clippingDetected: boolean;
}"""

    const val SCHEMA_05 = """// RoutePayloadSchema.ts — #05 Autonomous AI Engine
export interface Route05EnginePayload {
  routeId: '#05';
  path: '/enhance/engine';
  busMode: 'NPU_DSP_ACTIVE';
  profile: 'STREAMING_MASTER' | 'CLUB_PUNCH' | 'VOCAL_CLARITY' | 'WARM_ANALOG';
  targetLufs: number;
  airGappedNpu: true;
}"""

    const val SCHEMA_06 = """// RoutePayloadSchema.ts — #06 Deep 3.5s Processing
export interface Route06ProcessingPayload {
  routeId: '#06';
  path: '/enhance/processing';
  busMode: 'NPU_DSP_ACTIVE';
  durationMs: 3500;
  passes: ['AUDIT', 'HARMONIC', 'DYNAMICS', 'LIMITER'];
  progress: number;
}"""

    const val SCHEMA_07 = """// RoutePayloadSchema.ts — #07 Tactile Deck
export interface Route07TactileDeckPayload {
  routeId: '#07';
  path: '/enhance/deck';
  busMode: 'NPU_DSP_ACTIVE';
  drive: number;
  width: number;
  punch: number;
  air: number;
  ceilingDb: number;
  hapticDetentMs: 10;
}"""

    const val SCHEMA_08 = """// RoutePayloadSchema.ts — #08 Original vs Enhanced A/B
export interface Route08ABComparePayload {
  routeId: '#08';
  path: '/enhance/ab';
  busMode: 'DUAL_BUFFER';
  activeMonitor: 'ORIGINAL_DRY' | 'ENHANCED_WET';
  crossfadeCurve: 'CONSTANT_POWER_15MS';
  loudnessMatched: boolean;
}"""

    const val SCHEMA_09 = """// RoutePayloadSchema.ts — #09 AI Studio Pro Suite
export interface Route09ProSuitePayload {
  routeId: '#09';
  path: '/studio/pro';
  busMode: 'NPU_DSP_ACTIVE';
  chainOrder: ['EQ_5X', 'SAT_TUBE', 'MS_IMAGER', 'TRUE_PEAK_LIM'];
  headroomMarginDb: number;
}"""

    const val SCHEMA_10 = """// RoutePayloadSchema.ts — #10 Frequency-Band Crossover Mixer
export interface Route10BandMixerPayload {
  routeId: '#10';
  path: '/studio/stems';
  busMode: 'STEM_MATRIX_4X';
  bands: {
    mid: { gain: number; mute: boolean; solo: boolean };
    transients: { gain: number; mute: boolean; solo: boolean };
    sub: { gain: number; mute: boolean; solo: boolean };
    air: { gain: number; mute: boolean; solo: boolean };
  };
}"""

    const val SCHEMA_11 = """// RoutePayloadSchema.ts — #11 Zero-Latency Bypass vs Engaged
export interface Route11BypassPayload {
  routeId: '#11';
  path: '/studio/bypass';
  busMode: 'BYPASS_RAW' | 'NPU_DSP_ACTIVE';
  relayEngaged: boolean;
  crossfadeMs: 15;
  latencyMs: number;
}"""

    const val SCHEMA_12 = """// RoutePayloadSchema.ts — #12 Glyph VU Sync
export interface Route12GlyphVuPayload {
  routeId: '#12';
  path: '/studio/glyph';
  busMode: 'GLYPH_VU_SYNC';
  zones: [number, number, number, number, number];
  accentHex: '#FF2A2A';
  syncFps: 60;
}"""

    const val SCHEMA_13 = """// RoutePayloadSchema.ts — #13 Transcode & Encoding Matrix
export interface Route13TranscodePayload {
  routeId: '#13';
  path: '/library/transcode';
  busMode: 'OFFLINE_RENDER';
  format: 'WAV_24_48' | 'WAV_16_44' | 'WAV_24_96' | 'WAV_24_192' | 'WAV_32F_192';
  tpdfDither: boolean;
  normalizeTargetDb: number;
}"""

    const val SCHEMA_14 = """// RoutePayloadSchema.ts — #14 Export Telemetry Modal
export interface Route14ExportModalPayload {
  routeId: '#14';
  path: '/library/export';
  busMode: 'OFFLINE_RENDER';
  progress: number;
  bytesWritten: number;
  peakRamMb: number;
}"""

    const val SCHEMA_15 = """// RoutePayloadSchema.ts — #15 Master Done 100%
export interface Route15MasterDonePayload {
  routeId: '#15';
  path: '/library/done';
  busMode: 'OFFLINE_RENDER';
  verified: true;
  finalLufs: number;
  finalTruePeakDb: number;
  outputUri: string;
}"""

    const val SCHEMA_16 = """// RoutePayloadSchema.ts — #16 Master Audio Player
export interface Route16MasterPlayerPayload {
  routeId: '#16';
  path: '/library/player';
  busMode: 'PLAYER_PLAYBACK';
  trackId: number;
  offlineStorage: 'ROOM_SQLITE';
  abToggleEnabled: boolean;
}"""
}
