# WAVIQ Android — Source Audit & Implementation Brief

**Purpose:** Continue development of the existing WAVIQ Android application using the supplied repository. This document is based on the source files in `waviq (1).zip` and is intended to be used as the implementation brief for Google AI Studio or an Android development agent.

## 1. Non-negotiable project constraints

- Keep WAVIQ a **native Android application**.
- Use the existing Kotlin, Jetpack Compose, Material 3, Room, and Android audio APIs unless a verified technical reason requires a change.
- Preserve the current project identity, data, audio import/playback/export functionality, and working tests.
- Keep the product **local-first and offline**. Do not add cloud AI, network requests, API keys, `INTERNET` permission, remote analytics, or account requirements.
- Optimize for mid-range Android phones: bounded memory, cancellable background work, sensible model size, CPU fallback, and no unnecessary full-track copies.
- Do not invent model capabilities, processing results, confidence values, loudness measurements, true-peak guarantees, or completed builds.

## 2. What is already in the repository

| Area | Existing implementation |
|---|---|
| Platform | Kotlin Android app, Compose UI, Material 3, minSdk 24, targetSdk 36 |
| Main navigation | `Studio` and `Library`; adaptive bottom navigation / rail |
| Audio import | Storage Access Framework + `MediaExtractor` / `MediaCodec` |
| Playback | `AudioTrack` streaming through a block-based processing chain |
| DSP | Biquad EQ, drive/saturation, stereo width, a three-band limiter and telemetry |
| Local model | `app/src/main/assets/ai_audio_mastering_model.tflite` (704 bytes) via LiteRT |
| Local command assistant | `LocalAiMasteringAgent`, deterministic Arabic/English keyword matching |
| Persistence | Room database for saved master records |
| Export | Custom WAV writer for PCM 16-bit, PCM 24-bit and IEEE Float 32-bit at selected rates |
| UI | Unified Studio screen, Library screen, optional advanced tuning bottom sheet, Nothing-inspired dark styling |
| Tests | UI, command parsing, DSP, startup guard and navigation tests exist |

Important source locations:

- `app/src/main/java/com/example/model/WaviqState.kt`
- `app/src/main/java/com/example/audio/AudioDecoder.kt`
- `app/src/main/java/com/example/audio/AudioProcessor.kt`
- `app/src/main/java/com/example/audio/AudioDspEngine.kt`
- `app/src/main/java/com/example/audio/DspHook.kt`
- `app/src/main/java/com/example/audio/TfliteDspHook.kt`
- `app/src/main/java/com/example/audio/LocalAiMasteringAgent.kt`
- `app/src/main/java/com/example/viewmodel/WaviqViewModel.kt`
- `app/src/main/java/com/example/ui/screens/UnifiedStudioScreen.kt`
- `app/src/main/java/com/example/ui/screens/UnifiedLibraryScreen.kt`
- `app/src/main/AndroidManifest.xml`
- `app/build.gradle.kts`, `metadata.json`, `.env.example`, `README.md`

## 3. Verified gaps and risks in the supplied source

### 3.1 Genre identification is not yet a real audio genre classifier

`AudioDecoder.analyzeBuffer()` currently selects from only nine `MusicGenre` enum values. The decision is heavily influenced by the file name and a few coarse measurements (RMS-derived level, peak-to-RMS difference, stereo width and a finite-difference high-frequency estimate). For example, a title containing `808` can trigger `TRAP`. These heuristics cannot reliably distinguish Trap, Drill, Hip-Hop, R&B, Techno, Phonk, or their subgenres from audio alone.

The displayed confidence percentages are fixed rule outputs, not calibrated probabilities from a trained classifier. Do not present them as measured AI confidence.

### 3.2 The existing TFLite file is not a general music-understanding model

The bundled model is 704 bytes. `TfliteDspHook` supplies four block-level features (RMS, peak, zero-crossing rate and a difference-based high-frequency measure), then reads two values that the code interprets as gate strength and gain. Its fallback is a deterministic formula. This component is not a language model, music-genre classifier, stem separator, BPM/key detector, or general mastering agent.

Keep its role narrow unless model inputs, outputs, training provenance, and quality are independently verified. Do not rename it or describe it as a full AI mastering model.

### 3.3 Auto Master progress does not represent a full render

`WaviqViewModel.runAutoMasterAi()` advances through fixed delays, then calls `LocalAiMasteringAgent.autoCalibrate()` using the already-created `AudioTrackInfo` diagnostics. The button does not currently perform a complete offline analysis and mastering render during those progress stages. Replace simulated progress with progress emitted by real analysis/render tasks.

### 3.4 Loudness and peak labels need engineering corrections

- `AudioDecoder.analyzeBuffer()` estimates a value called LUFS as RMS dB minus a constant. This is not a standards-compliant EBU R128 / ITU-R BS.1770 integrated loudness measurement.
- `AudioDspEngine.exportMasterWav()` uses peak normalization when `normalizeToTarget` is enabled. It does not normalize the result to `MusicGenre.targetLufs`.
- The final peak value is calculated from output samples. A true-peak result requires oversampled inter-sample measurement of the rendered output.
- Genre-specific target LUFS values are therefore currently descriptive metadata, not achieved export targets. Do not display them as achieved values.

Implement proper loudness measurement and loudness targeting separately from peak limiting. Report measured Integrated LUFS, loudness range (when implemented), sample peak and true peak with accurate names.

### 3.5 “Hi-res restoration” is currently an exciter effect, not genuine recovery

`HiResUpsamplingHook` adds generated harmonics, applies a high shelf and expands upper-band stereo. `targetKhz` changes a processing factor; it does not reconstruct the original missing recording information. Export resampling to 96/192 kHz changes the output sample rate, but does not create authentic captured detail above the source bandwidth.

Use accurate terminology such as *harmonic enhancement* where appropriate. Keep sample-rate conversion separate from enhancement, and use a proper anti-aliasing resampler for downsampling.

### 3.6 “Stem” controls are frequency-band controls, not source separation

`StemMatrixHook` uses fixed low-pass, band-pass and high-pass filters to create rough frequency regions. These are not isolated vocal, drum, bass and accompaniment stems. In addition, `AudioDspEngine.exportMasterWav()` creates offline EQ, drive, high-frequency and limiter hooks, but does not recreate/apply `StemMatrixHook`. This can make preview and exported audio differ.

Until a suitable local source-separation model is integrated and verified, rename the current feature to *Band Mixer* or *Frequency-Band Controls*. Do not call its outputs isolated stems. Make preview and export use the same processing graph and settings.

### 3.7 Imported audio is capped at 90 seconds

`AudioDecoder.MAX_DECODE_SECONDS` is set to 90. Longer songs may be truncated. Implement full-track, memory-safe processing (prefer streaming/chunked decode and render where feasible), or show a clear duration limit before processing. Never silently master only the first 90 seconds of a full song.

### 3.8 Unsupported commands receive arbitrary enhancement

When `LocalAiMasteringAgent.applyIntentCommand()` does not recognize any command tokens, it currently applies a generic EQ/air enhancement and returns a confident “Balanced Pro Polish” result. An unsupported request must not trigger arbitrary sound changes. Return an explicit unsupported/ambiguous result and ask the user to choose a supported action or describe the goal differently.

Manual command decisions currently return a fixed 95% confidence. Remove this ungrounded confidence or represent rule-match certainty separately from audio-classifier confidence.

### 3.9 Project metadata and setup instructions conflict with offline operation

The manifest does not request `INTERNET`, and Firebase/network dependencies are commented out, but `.env.example`, `README.md`, and `metadata.json` still refer to Gemini API keys or server-side Gemini capability. Align setup documentation and metadata with the actual offline Android product. Do not add remote AI to solve local-model gaps.

### 3.10 Build verification is currently blocked in this handoff environment

The ZIP contains `gradle/wrapper/gradle-wrapper.properties`, but does not contain `gradlew`, `gradlew.bat`, or `gradle-wrapper.jar`. No system Gradle or Android SDK was available in the inspection environment. Therefore, no successful Gradle build or test run can be claimed from this audit. Restore/use a valid Gradle wrapper and Android SDK in the development environment, then run the build and tests.

## 4. Product direction: one intelligent mastering workspace

Make the app feel like one audio-engineering assistant, not a collection of manual tools.

### Primary workflow

1. Import a complete audio file or record a take.
2. Analyze the audio locally and show what was actually measured.
3. Detect likely genres as **multiple labels**, with calibrated confidence and an `Unknown / Mixed` outcome.
4. Identify technical issues (excessive sub energy, harshness, weak vocal presence, clipping, noise, stereo/phase issues, excessive dynamics compression, etc.).
5. Recommend a mastering intent and explain the proposed processing chain.
6. Render a real preview/master with cancellable progress.
7. Provide A/B comparison, one simple intensity control, undo/reset, and a compact advanced panel.
8. Export the entire processed track with truthful format, loudness and peak metadata.

### Main screen hierarchy

- **Top:** WAVIQ identity, track title, playback state and a compact status indicator.
- **Center:** waveform / useful spectrum visualization and playback controls.
- **AI analysis card:** detected style tags, mood/energy, technical findings and confidence only when grounded.
- **Assistant input:** one Arabic/English command field with clear supported suggestions.
- **Primary action:** `Analyze` then `Master`; show the real current stage.
- **A/B and intensity:** simple, accessible controls.
- **Advanced controls:** one secondary bottom sheet for EQ, dynamics, stereo, limiter and diagnostics.
- **Library:** separate place for local saved projects/masters and export history.

Keep advanced functionality accessible, but do not make the user navigate through multiple mandatory screens. Remove or retire unused legacy UI only after confirming no tests or navigation paths depend on it.

### Visual direction

- Premium minimal, near-black / graphite / white / grey with a restrained red accent.
- Use dot-matrix details and subtle audio-reactive elements as accents, not as clutter.
- Preserve readable typography, 48dp touch targets, strong contrast and clear disabled/loading/error states.
- Support Arabic RTL and English LTR properly, including mixed-language command text.
- Do not copy Nothing trademarks, Glyph assets or proprietary UI artwork.

## 5. Recommended AI and DSP architecture

Keep genre taxonomy, audio measurement, AI inference, mastering decisions and DSP rendering as separate layers.

### A. Music taxonomy asset

Create a versioned asset such as `app/src/main/assets/music_taxonomy.json`. Do not expand a Kotlin enum to hundreds of values. Each entry should support:

- Stable ID and English display name
- Parent genre and optional subgenre
- Aliases / search terms
- Related and fusion genres
- Optional region / era tags
- Production-style tags
- Vocal-style tags
- Mood / energy tags
- Instrumentation / rhythm tags
- A mastering-family hint (not a fixed EQ curve)

Represent blended music with multiple tags, for example `R&B + Trap Soul + Alternative R&B`, rather than forcing a single exclusive class. The taxonomy is metadata and search vocabulary; it is not itself an audio classifier.

### B. Audio analysis layer

Create a dedicated analysis result model with independently verified metrics:

- Integrated loudness using a standards-based implementation
- Short-term / momentary loudness and loudness range where supported
- Sample peak and oversampled true peak
- RMS, crest factor and dynamic profile
- Spectral balance and low/mid/high energy
- Stereo width and correlation / phase risk
- BPM and rhythmic feel when confidence is adequate
- Musical key only when reliable
- Speech/vocal presence as a separate task
- Track duration and source format metadata

Do not infer BPM, key, genre or mood from file names. File names may be weak metadata hints only.

### C. On-device ML layer

Use a dedicated, documented, quantized on-device model for audio genre/style classification if a suitable model can be integrated within the size and latency budget. The model must be compatible with LiteRT / Android and run without network access.

Before adopting a model, document:

- Model architecture, input shape and output labels
- Model license and redistribution terms
- Source/training provenance
- Quantized model size and peak memory
- CPU latency on a mid-range phone
- Evaluation method and known limitations

Use windowed audio/features and aggregate predictions across the track. Return multi-label scores, calibrated confidence, and an unknown/mixed fallback. If no valid classifier model is available, label results as heuristic estimates instead of claiming AI genre detection.

Keep command understanding separate from audio classification. The existing keyword interpreter may remain as a small local intent parser, but it must return structured supported intents and explicit unsupported results. It is not a conversational LLM.

### D. Mastering decision engine

Use detected genres as weak starting context, not fixed processing instructions. Let measured audio defects and user intent dominate the final decision.

The decision object should include:

- User intent and detected style tags
- Measurements used and confidence
- Ordered processing chain and per-stage parameters
- Target delivery standard / loudness target when explicitly chosen
- Safety limits and reason for each processing stage
- Estimated or measured post-render result
- Warnings when the requested outcome risks clipping, pumping, distortion or lost dynamics

Avoid universal genre boosts such as automatic large 808 boosts for every Trap or Drill track. Never widen low frequencies indiscriminately. Keep low-end mono/phase-safe and verify stereo changes.

### E. DSP render engine

Use the same deterministic processing graph for preview and export. Include only stages actually implemented and tested, for example:

1. Optional cleanup (only when noise is detected and the source needs it)
2. Corrective EQ / dynamic EQ
3. Dynamics control / multiband compression when justified
4. Optional harmonic saturation
5. Stereo management with mono compatibility checks
6. True-peak limiting
7. Loudness normalization to the selected target
8. High-quality sample-rate conversion and bit-depth conversion/dither

Do not apply a noise gate to a finished mix by default. Avoid always-on tanh saturation or automatic boosts without an A/B safety check.

### F. Stem separation

Keep true stem separation as a separate optional feature. Only show `Vocals`, `Drums`, `Bass`, `Other` as stems after a real source-separation model has produced independent stems. Otherwise label the present filters as frequency bands and make sure export applies those same band controls.

## 6. Implementation phases

### Phase 1 — Truthfulness and correctness

- Correct diagnostic labels and remove fake confidence values.
- Replace fake Auto Master progress with real work stages or clearly rename the action to `Apply Recommendations` until offline rendering exists.
- Prevent unknown commands from changing audio.
- Fix preview/export parity, including band/stem controls and bypass handling.
- Correct loudness, peak, true-peak and sample-rate metadata.
- Make unsupported export formats impossible to select. `FLAC_PROFILE` is currently an internal enum name for a WAV file; either implement genuine FLAC encoding or remove/rename the enum.
- Address the 90-second truncation with full-track streaming or an explicit limit.

### Phase 2 — Taxonomy and analysis

- Add the versioned genre/style taxonomy asset.
- Replace the nine-value `MusicGenre`-only approach with stable taxonomy IDs and multi-label results.
- Add an audio-analysis service with tested measurements.
- Add clear confidence thresholds and `Unknown / Mixed` cases.
- Keep genre and mood separate from production-style and vocal-style tags.

### Phase 3 — Real local intelligence

- Integrate a documented, quantized on-device genre/style classifier after model suitability is verified.
- Keep command parsing local, structured and bilingual.
- Add a rule-based safety/decision layer that explains every DSP adjustment.
- Add optional local vocal/source detection. Do not advertise stem separation until a real separation model is present.

### Phase 4 — UI simplification and finish

- Make the single Studio workflow the primary interface.
- Keep Library as the second main destination.
- Move EQ, band controls, dynamics and diagnostics into one advanced panel.
- Improve empty, analyzing, rendering, completed, cancelled and error states.
- Remove dead legacy screens only after reference search and tests confirm they are unused.
- Keep Nothing-inspired styling minimal and accessible.

## 7. Required tests and acceptance criteria

### Audio correctness

- Decode mono and stereo WAV/MP3/AAC/FLAC inputs supported by Android codecs.
- Test files shorter than, equal to, and longer than 90 seconds.
- Test sample-rate conversion in both upsampling and downsampling directions.
- Verify no NaN/Infinity, runaway gain, clipping, or unbounded memory growth.
- Verify preview and exported render match within a defined numerical tolerance.
- Verify bypass produces an unprocessed export when bypass is selected.
- Verify the limiter's reported true peak against an independent oversampled measurement.
- Verify dither only when reducing bit depth; preserve float output correctly.
- Verify the output file header, codec, sample rate, bit depth, channel count, duration and extension agree.

### AI / decision correctness

- Test genre aliases and mixed-genre outputs.
- Test low-confidence and unknown audio.
- Ensure file names never override strong contradictory audio evidence.
- Test Arabic and English commands, synonyms, negation, contradictory commands and unsupported requests.
- Verify unsupported prompts never change DSP parameters.
- Verify each applied parameter is traceable to a measurement or user instruction.
- Verify model-not-found / LiteRT-unavailable fallback is truthful and safe.

### UI and product behavior

- One primary import → analyze → master → A/B → export journey.
- Real progress and cancellation.
- Correct RTL / LTR rendering and accessible controls.
- No mandatory navigation through advanced settings.
- Library persistence and playback remain functional offline.

### Build verification

- Restore a complete Gradle wrapper (`gradlew`, `gradlew.bat`, wrapper JAR and properties) or provide an explicitly configured supported Gradle installation.
- Build debug and run the complete unit/UI test suite using the actual Android SDK.
- Report exact commands and results. Never claim a successful build, model inference, or device test unless it was executed.

## 8. Agent working rules

1. Inspect the actual file before editing it.
2. Make small, cohesive changes and preserve existing architecture.
3. Do not replace the app with a website, Flutter app, or unrelated template.
4. Do not delete binary assets or legacy source until dependencies are checked.
5. Do not invent or synthesize a trained model and present it as real.
6. Do not describe frequency-band filtering as stem separation.
7. Do not describe sample-rate upsampling as recovery of lost source detail.
8. Do not claim LUFS or true-peak compliance without correct measurement.
9. Run build/tests after changes; if the environment prevents this, state the blocker exactly.
10. At the end of each phase, list changed files, behavior changes, tests run, known limitations and next phase.

**Start with Phase 1. Do not attempt a full rewrite or begin with decorative UI changes while audio correctness and preview/export parity remain unresolved.**
