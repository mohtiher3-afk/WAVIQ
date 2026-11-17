# Prompt for Google AI Studio — WAVIQ Android project

You are modifying an EXISTING native Android application named WAVIQ. Do not convert it into a website, React app, Flutter app, or a different product.

The attached file `WAVIQ_AI_STUDIO_SOURCE_FILES.md` is a consolidated source reference. It contains multiple original files separated by headings that show their exact project-relative paths. First inspect the entire source reference and map each section back to its original file path. Do not treat the whole document as one Kotlin source file.

## Required platform and constraints
- Native Android application.
- Kotlin + Jetpack Compose + Material 3.
- Preserve the existing package namespace and architecture unless a change is required to fix a verified issue.
- Local-first and offline operation. Do not add cloud AI, API keys, telemetry, or INTERNET permission.
- Optimize for mid-range Android phones and avoid heavyweight models that cannot reasonably run on-device.
- Preserve existing audio DSP, decoding, Room database, local file handling, and export behavior unless a change is required and verified.

## Product direction
Make WAVIQ feel like one intelligent audio-engineering studio rather than a collection of disconnected screens. The main workflow should be:
1. Import or select an audio track.
2. Analyze it locally.
3. Let the user describe the desired result in Arabic or English (for example: clean noise, make vocals clearer, stronger bass, warmer master, streaming-ready master).
4. Convert the request into a transparent processing chain using on-device logic.
5. Preview A/B, adjust intensity if needed, process, and export.
6. Keep advanced controls available but secondary, not spread across many primary screens.

## AI truthfulness requirements
- The current command assistant is rule/keyword driven. Do not call it a full conversational LLM.
- The current `.tflite` asset is a tiny DSP helper, not a general language model. Do not claim it can understand unrestricted natural language.
- Keep the local command interpreter explicit and testable. If a request is not supported, say so and offer the closest supported action.
- Do not add a placeholder progress animation that pretends to process audio. Progress must reflect actual work performed.
- Never label a WAV PCM output as FLAC. Use accurate format labels and file extensions.

## UI direction
- Premium minimalist interface inspired by Nothing OS principles, without copying logos or proprietary assets.
- Monochrome black/white/gray palette with restrained red accent.
- Clear typography, disciplined spacing, subtle dot-matrix details, and a useful audio waveform/spectrum.
- One main studio screen, with a compact assistant command area and collapsible advanced controls.
- Excellent accessibility, readable contrast, responsive layouts, and Arabic RTL support alongside English LTR.

## Working method
1. Inspect and summarize the current implementation before changing it.
2. Identify all missing binary resources listed at the end of the source reference. Do not fabricate replacements or silently delete resources. Ask for, or preserve, each asset at its exact path.
3. Apply changes in small, coherent steps. Preserve existing working code and tests.
4. Add or update unit tests for Arabic/English command parsing, processing-chain selection, and unsupported requests.
5. Build and run tests. Fix compile errors before reporting completion.
6. Report exactly what was changed, what was tested, and what remains incomplete. Never claim an APK build or on-device AI capability unless it was actually verified.

Start by inspecting the source and identifying the smallest safe implementation plan. Do not rewrite the whole project unnecessarily.
