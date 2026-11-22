# WAVIQ — Text Source Transfer Pack

This document consolidates the text-based source and configuration files from the WAVIQ Local AI UI Upgrade project. Each section is labeled with its original project-relative path. Recreate each file at that exact path when applying the source to an Android project.

> Important: this is a source-context document, not a ready-to-build Android project archive. Binary resources (images, launcher icons, fonts, and the `.tflite` asset) are listed separately at the end and are not embedded here. The nested archive under `public/assets/aistudio/` is intentionally excluded.

## Included files

- `.aistudio/artifacts/brain/2e57c603-c228-4687-9a91-9656cbe13179/implementation_plan.md`
- `.gitignore`
- `README.md`
- `app/.gitignore`
- `app/build.gradle.kts`
- `app/proguard-rules.pro`
- `app/src/androidTest/java/com/example/ExampleInstrumentedTest.kt`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/example/MainActivity.kt`
- `app/src/main/java/com/example/StartupGuard.kt`
- `app/src/main/java/com/example/assistant/WaviqLocalAssistant.kt`
- `app/src/main/java/com/example/audio/AudioDecoder.kt`
- `app/src/main/java/com/example/audio/AudioDspEngine.kt`
- `app/src/main/java/com/example/audio/AudioProcessor.kt`
- `app/src/main/java/com/example/audio/BiquadFilter.kt`
- `app/src/main/java/com/example/audio/DspHook.kt`
- `app/src/main/java/com/example/audio/GlyphVUMapper.kt`
- `app/src/main/java/com/example/audio/GlyphVuTelemetry.kt`
- `app/src/main/java/com/example/audio/TfliteDspHook.kt`
- `app/src/main/java/com/example/data/MasterLibraryRepository.kt`
- `app/src/main/java/com/example/data/MasterTrackDao.kt`
- `app/src/main/java/com/example/data/MasterTrackEntity.kt`
- `app/src/main/java/com/example/data/WaviqDatabase.kt`
- `app/src/main/java/com/example/model/DevMapSchemas.kt`
- `app/src/main/java/com/example/model/WaviqRoute.kt`
- `app/src/main/java/com/example/model/WaviqState.kt`
- `app/src/main/java/com/example/ui/components/InteractiveSpectralCurve.kt`
- `app/src/main/java/com/example/ui/components/WaviqChrome.kt`
- `app/src/main/java/com/example/ui/devmap/DeveloperMapScreen.kt`
- `app/src/main/java/com/example/ui/home/WaviqHomeScreen.kt`
- `app/src/main/java/com/example/ui/screens/AnalyzeScreens.kt`
- `app/src/main/java/com/example/ui/screens/EnhanceScreens.kt`
- `app/src/main/java/com/example/ui/screens/LibraryScreens.kt`
- `app/src/main/java/com/example/ui/screens/StudioScreens.kt`
- `app/src/main/java/com/example/ui/theme/Color.kt`
- `app/src/main/java/com/example/ui/theme/Theme.kt`
- `app/src/main/java/com/example/ui/theme/Type.kt`
- `app/src/main/java/com/example/viewmodel/WaviqViewModel.kt`
- `app/src/main/res/drawable/ic_launcher_background.xml`
- `app/src/main/res/drawable/ic_launcher_foreground.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values/themes.xml`
- `app/src/main/res/xml/backup_rules.xml`
- `app/src/main/res/xml/data_extraction_rules.xml`
- `app/src/test/java/com/example/ExampleRobolectricTest.kt`
- `app/src/test/java/com/example/ExampleUnitTest.kt`
- `app/src/test/java/com/example/MainActivityStartupTest.kt`
- `app/src/test/java/com/example/ProAudioDspTest.kt`
- `app/src/test/java/com/example/StartupGuardTest.kt`
- `app/src/test/java/com/example/assistant/WaviqLocalAssistantTest.kt`
- `build.gradle.kts`
- `gradle/libs.versions.toml`
- `gradle/wrapper/gradle-wrapper.properties`
- `gradle.properties`
- `metadata.json`
- `settings.gradle.kts`

---

## `.aistudio/artifacts/brain/2e57c603-c228-4687-9a91-9656cbe13179/implementation_plan.md`

```markdown
# خطة ترقية تقنيات معالجة الصوت إلى مستوى الاستوديو الاحترافي (WAVIQ Pro Precision Engine)

ترقية شاملة لسلسلة المعالجة الصوتية ومحرك التصدير في التطبيق للوصول إلى دقة الماسترينج الاحترافي عبر حسابات 64-Bit الداخلية، وإعادة بناء الترددات العالية المفقودة مع توسيع الستيريو، ومحدد ذروة متعدد النطاقات، وتصدير عالي الدقة حتى 192kHz مع TPDF Dither.

## مراجعة المستخدم والقرارات الحاسمة

> [!IMPORTANT]
> تم تأكيد التوجهات التقنية التالية بناءً على اختياراتك في المرحلة الأولى:

- **القرار المؤكد 1 (الارتقاء الصوتي الأساسي)**: التركيز على **إعادة بناء الترددات العالية (Spectral Band Replication - SBR)** واستعادة اللمعان الهوائي مع **توسيع المسرح الصوتي الستيريو (M/S Stereo Expansion)** المتزن طورياً.
- **القرار المؤكد 2 (الدقة الداخلية والتصدير)**: ترقية نواة الفلاتر والمعالجة الداخلية إلى **64-Bit (`Double` IEEE-754)** لتفادي ضوضاء التكميم والانجراف الرقمي، مع دعم التصدير حتى **192kHz بدقة 24-Bit PCM و 32-Bit IEEE Float**.
- **القرار المؤكد 3 (الصقل النهائي والحماية)**: إضافة **TPDF Dither** حقيقي لتقليل تشوه التكميم عند التصدير، و**محدد ذروة True-Peak متعدد النطاقات (3-Band Oversampled ISP Limiter)** يمنع ضخ الصوت (Pumping) مع التزام تام بسقف الذروة.

---

## 1. النظرة العامة والمفهوم الأساسي

- **ما الذي يقدمه التحديث**: يحوّل سلسلة المعالجة الصوتية في WAVIQ من معالجة أحادية النطاق بدقة 32-Bit إلى محرك استوديو عالي الدقة يحافظ على نقاء الباص والمدى الديناميكي، ويعيد بناء التوافقيات فوق 10kHz للملفات المضغوطة، ويسمح بإخراج ماسترات جاهزة للتوزيع والبث بدقة تصل إلى 192kHz / 32-Bit Float.
- **الفئة المستهدفة**: مهندسو الصوت، ومنتجو الموسيقى، والمستخدمون الباحثون عن تحسين واضح في وضوح وتفاصيل واتساع الأغاني دون تشويه أو إجهاد سمعي.
- **القيمة المضافة**:
  - تلافي فقدان الدقة في الفلاتر العميقة (مثل Sub-Bass وAir Shelf) عند الترددات العالية بفضل حسابات `64-Bit Double`.
  - تحرير الترددات المتوسطة والعالية (الغناء والإيقاعات الدقيقة) من ضغط الباص القوي عبر فصل محدد الذروة إلى 3 نطاقات مستقلة.
  - إعادة إحياء الصوت في المقاطع المضغوطة (MP3/AAC) عبر تخليق توافقيات عالية متناغمة وتوسيع ستيريو ذكي يحافظ على تماسك الـ Mono في الترددات المنخفضة.

---

## 2. تجربة المستخدم والتصميم البصري

- **تدفقات المستخدم الرئيسية**:
  1. **التحكم في الاستوديو الاحترافي (`#09 AI Studio Pro Suite`)**: يظهر للمستخدم مؤشر حالة الدقة الجديدة (`64-BIT FLOAT DSP` مع شارات `192KHZ UPSAMPLER` و`3-BAND ISP LIMITER`) مع قدرة فورية على التبديل بين معاملات التضخيم الفائقة (4X / 8X Oversampling) ودقة الاستهداف (96kHz / 192kHz) ومراقبة تقليل الكسب في الوقت الفعلي.
  2. **إعادة بناء الترددات وتوسيع الستيريو**: عند تفعيل التحسين أو الماستر التلقائي، يعمل محرك الاستعادة الطيفية وتوسيع الجانب العالي بتناغم مع معاملات `AIR` و`WIDTH` دون تشتيت مركز الغناء أو الباص.
  3. **مصفوفة التصدير الاحترافية (`#13 Transcode & Encoding Matrix`)**: تعرض صيغاً جديدة محدثة تشمل `WAV 24-BIT / 192kHz` و`WAV 32-BIT FLOAT / 192kHz` إلى جانب صيغ البث والأقراص، مع تفعيل حقيقي لخيارات `TPDF SHAPED DITHER` و`NORMALIZE CEILING` أثناء المعالجة غير المتصلة بالشبكة.
- **الهوية البصرية والاتساق**:
  - الالتزام الكامل بهوية **Nothing OS Dark Hardware** (`ObsidianVoid`، `ConsolePanel`، `GlyphCrimson`، `BusGreen`، `CyanScope`، `PeakAmber`) مع الحفاظ على مناطق لمس لا تقل عن `48dp` ووسوم الاختبار البرمجي (`testTag`).

---

## 3. القرارات التقنية والمفاضلات الرئيسية

- **القرار 1: استخدام دقة `64-Bit Double` داخل الفلاتر وخطوط التأخير مع الإبقاء على واجهة نقل مرنة**
  - *النهج المختار*: ترقية المعاملات الداخلية وخطوط التأخير (`x1..y2`) في مرشحات `BiquadFilter` إلى `Double`، وتمرير عينات الماسترينج بدقة 64-Bit داخل مرشحات EQ والمحدد.
  - *السبب*: الفلاتر ذات الترددات المنخفضة جداً (أقل من 80Hz) عند معدلات عينة عالية (96kHz / 192kHz) تعاني من أخطاء تقريب في 32-Bit Float، بينما توفر حسابات 64-Bit استقراراً رقمياً واستجابة طورية فائقة النقاء مع أثر أدائي طفيف جداً على المعالجات الحديثة.
  - *البدائل المستبعدة*: إبقاء كل المعالجة بـ 32-Bit، وهو ما يحد من دقة الاستوديو عند تفعيل التصدير بدقة 192kHz.

- **القرار 2: محدد ذروة متعدد النطاقات (Linkwitz-Riley 3-Band Crossover + 4x/8x ISP)**
  - *النهج المختار*: تقسيم الإشارة إلى 3 نطاقات (منخفض < 250Hz، متوسط 250Hz–4kHz، مرتفع > 4kHz) عبر مرشحات متقاطعة بدقة 64-Bit، مع مغلفات استجابة مستقلة لكل نطاق، متبوعة بمرحلة حماية نهائية بكشف الذروات البينية (4x/8x Catmull-Rom Inter-Sample Peaks).
  - *السبب*: يمنع ضربات الـ 808 أو الباص الثقيل من سحب صوت الغناء أو الـ Hi-Hats للأسفل، ويحافظ على شفافية الماستر في المستويات الصاخبة.
  - *البدائل المستبعدة*: المحدد أحادي النطاق واسع الطيف، الذي يسبب تموجاً مسموعاً في الترددات العالية عند الضغط العالي.

- **القرار 3: إعادة التشكيل المكعب (4-Point Catmull-Rom Resampling) وتنسيق `32-Bit IEEE Float WAV` مع TPDF Dither**
  - *النهج المختار*: استبدال الاستيفاء الخطي في محرك التصدير باستيفاء مكعب رباعي النقاط عند الرفع إلى 96kHz / 192kHz (أو التحويل إلى 44.1kHz)، وتطبيق ضوضاء تكميم مثلثية (`TPDF`) عند التكميم إلى 16/24-Bit PCM، وكتابة ترويسة RIFF Wave بصيغة `AudioFormat = 3` (IEEE Float) عند اختيار 32-Bit Float.
  - *السبب*: الاستيفاء الخطي يضعف الترددات العالية ويُحدث تداخلاً (Aliasing)، بينما يحافظ الاستيفاء رباعي النقاط مع التكميم المثلثي على نعومة النهايات السمعية وسلامة السقف النهائي (`-1.0 dBTP`).

---

## 4. البنية التقنية واستراتيجية البيانات

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                  64-BIT DOUBLE PRECISION MASTERING PIPELINE                  │
└──────────────────────────────────────────────────────────────────────────────┘
                                       │
     ┌─────────────────────────────────┼─────────────────────────────────┐
     ▼                                 ▼                                 ▼
┌──────────────────────┐   ┌────────────────────────┐   ┌───────────────────────┐
│ 64-Bit Biquad Core   │   │ SBR + M/S Spatial      │   │ 3-Band True-Peak ISP  │
│ - Double coeffs &    │   │ - >4.5kHz Harmonics    │   │ - L/M/H Crossover     │
│   delay lines (L/R)  │   │ - Phase-safe HF sheen  │   │ - Per-band envelopes  │
│ - Sub/192k stability │   │ - Mono-tight low end   │   │ - 4x/8x Cubic Limiter │
└──────────────────────┘   └────────────────────────┘   └───────────────────────┘
     │                                 │                                 │
     └─────────────────────────────────┼─────────────────────────────────┘
                                       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                 OFFLINE HI-RES RENDERER & QUANTIZATION ENGINE                │
│  1. 4-Tap Cubic Resampler (44.1 / 48 / 96 / 192 kHz)                         │
│  2. Target True-Peak Ceiling Normalizer (-1.0 dBTP)                          │
│  3. TPDF Triangular Dither Engine (16-Bit & 24-Bit Integer PCM)              │
│  4. Dual Container Writer: Int PCM (Format 1) & 32-Bit IEEE Float (Format 3) │
└──────────────────────────────────────────────────────────────────────────────┘
                                       │
                     ┌─────────────────┴─────────────────┐
                     ▼                                   ▼
┌─────────────────────────────────────┐ ┌──────────────────────────────────────┐
│ #09 AI Studio Pro Suite             │ │ #13 Transcode & Encoding Matrix      │
│ - 64-BIT DSP / 3-BAND ISP Badges    │ │ - WAV 24-BIT / 192kHz                │
│ - Real-time Band GR & Latency Read  │ │ - WAV 32-BIT FLOAT / 192kHz          │
└─────────────────────────────────────┘ └──────────────────────────────────────┘
```

- **توزيع الحالات والبيانات**:
  - **نموذج التصدير (`TranscodeConfig` و `ExportFormat`)**: توسعة الخيارات لتضمين `WAV 24-BIT / 192kHz` و`WAV 32-BIT FLOAT / 192kHz` مع ربط مباشر بحالة الـ Dither والمعايرة السقفية (`Normalize Ceiling`).
  - **سلسلة المعالجة (`DSP Hooks`)**: تحديث وحدة الاستعادة الطيفية وتوسيع الستيريو لتوليد توافقيات متزنة مع توسيع نطاق الجانب العالي (`Side Channel HF Shimmer`) دون تفريغ المنتصف، وتحديث محدد الذروة ليحسب تقليل الكسب الإجمالي ولكل نطاق بدقة.
  - **مصفوفة التصدير الفعلي**: تفعيل كامل لسلسلة التمرير غير المتصل بالشبكة (64-Bit EQ -> SBR/Stereo -> TFLite AI -> 3-Band Limiter -> Cubic Resample -> Ceiling Norm -> TPDF Dither -> RIFF PCM/Float Writer) مع اختبارات Robolectric تتحقق من صحة الترويسة (`Format=3` للـ 32-Bit ومعدل `192000` Hz) وأداء التوافقيات العالية وتقليل التشوه عبر TPDF Dither.
```

---

## `.gitignore`

```text
*.iml
.gradle
.kotlin
/local.properties
/.idea/caches
/.idea/libraries
/.idea/modules.xml
/.idea/workspace.xml
/.idea/navEditor.xml
/.idea/assetWizardSettings.xml
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
.env
debug.keystore
```

---

## `README.md`

```markdown
# WAVIQ — Local Audio Intelligence

**WAVIQ** is an offline-first Android audio enhancement and mastering studio. Its updated workflow is built around one assistant-led workspace: import a track, describe the sound you want in Arabic or English, and let WAVIQ select an available processing profile and render the result on the device.

> **Project status:** The unified home workspace, bilingual rule-based command assistant, local DSP/LiteRT processing path, local master library, and WAV export workflow are present in this codebase. The assistant is **not yet a general-purpose generative AI model**. See [AI capabilities and limitations](#ai-capabilities-and-limitations) before planning the next development phase.

## Highlights

- **Assistant-first workflow:** A single primary workspace combines track import, playback, command input, quick actions, mastering controls, export options, and access to saved masters.
- **Arabic and English commands:** A fully local intent router recognizes a fixed set of supported audio requests.
- **Offline audio processing:** Audio decoding, DSP rendering, and file export run locally. The Android manifest does not declare the `INTERNET` permission.
- **Audio processing chain:** Existing processing components include biquad EQ, dynamics/limiting, harmonic processing, noise-gate/spectral-gate behavior, stereo controls, and a LiteRT hook with a DSP fallback.
- **Playback and preview:** Imported tracks and saved masters can be previewed in the app.
- **Local master library:** Room stores master metadata; rendered files are kept in the app's private storage until the user exports them.
- **Focused visual design:** A dark, high-contrast interface with restrained red accents, waveform/audio telemetry elements, and optional fine controls.

## How to use WAVIQ

1. Open WAVIQ and tap **IMPORT AUDIO FILE**.
2. Choose an audio file through Android's system file picker.
3. Enter a supported request in Arabic or English, or select one of the quick actions.
4. Tap **RUN WAVIQ AUTO MASTER**. WAVIQ selects the mapped profile and renders the audio locally.
5. Preview the result. The completed master is added to the local WAVIQ library.
6. Use **SAVE MASTER TO DEVICE** to export the rendered WAV file to a folder you choose.

### Example commands

| Request | Current command route |
|---|---|
| `نظّف الصوت من الضوضاء` | Noise-cleanup request → available vocal-clarity/noise-gate processing profile |
| `Make the vocals clearer` | Vocal-presence profile |
| `Give it a strong 808 club punch` | Hip-hop / punch-focused processing selection |
| `Make it warmer and analog` | Warm-analogue profile |
| `Prepare a Spotify streaming master` | Balanced streaming profile |
| `حلل المقطع` / `Analyze this track` | Displays the current track's available analysis summary |
| `حسن جودة هذا المقطع` / `Master this track` | Renders using the currently selected or calibrated settings |

The command router is intentionally conservative: unsupported requests return examples of commands it understands rather than being treated as arbitrary instructions.

## AI capabilities and limitations

It is important to distinguish the current local automation from the planned full AI experience.

### Implemented now

- `WaviqLocalAssistant` normalizes Arabic and English text and maps recognized words/phrases to a fixed set of intents.
- The selected intent updates an existing processing profile or genre choice in `WaviqViewModel`.
- The audio engine renders through the app's local DSP chain.
- `TfliteDspHook` attempts to initialize the bundled LiteRT model. If the runtime or model initialization fails, it falls back to local DSP spectral-gate behavior.
- The current hook extracts compact audio features such as RMS level, peak level, zero-crossing rate, and high-frequency change, then smooths the resulting gate/gain controls.

### Not implemented yet

- No open-ended local language model (LLM) is bundled. WAVIQ cannot yet understand arbitrary multi-step conversation or invent a new processing chain from a free-form request.
- The bundled `app/src/main/assets/ai_audio_mastering_model.tflite` is approximately **704 bytes**. It is a small audio-processing model asset, not a general-purpose language model or a full neural audio restoration network.
- The present command for noise removal routes to the existing vocal-clarity/noise-gate processing path. It should not be marketed as studio-grade neural denoising until a dedicated, tested denoising model is integrated.
- Neural source separation, true vocal isolation, and independent high-quality stem extraction are not established capabilities of the current implementation.
- There is no cloud AI fallback, by design.

### Recommended next AI phase

1. Select and license a compact quantized language model that can run on target mid-range Android devices.
2. Connect the model to a strict, validated action schema. The model should propose actions; the app should validate them against supported DSP operations before execution.
3. Add dedicated audio models for clearly scoped tasks such as denoising or vocal enhancement. Evaluate quality against reference recordings rather than relying on UI labels.
4. Preserve the original audio, make every processing decision inspectable, and provide a reversible A/B comparison.
5. Benchmark memory use, latency, battery impact, and thermal behavior on representative Android hardware. Keep a DSP-only fallback for devices that cannot run the models.

## Processing profiles and export

The current profile definitions include:

- **Streaming Master:** balanced processing, configured around a `-14 LUFS` target and `-1.0 dBTP` ceiling.
- **Club Punch:** higher-energy low-end and punch settings, configured around a `-9 LUFS` target and `-0.3 dBTP` ceiling.
- **Vocal Presence:** presence-focused EQ and a `-13 LUFS` target.
- **Analog Tape Warmth:** harmonic warmth and smoother high-frequency balance, configured around a `-12.5 LUFS` target.

These are application presets, not guarantees that every source will reach the exact target loudness or sound equally good. Always listen to the rendered file before publishing it.

The export choices currently defined in `ExportFormat` are WAV PCM configurations:

- WAV 24-bit / 48 kHz
- WAV 16-bit / 44.1 kHz
- WAV 24-bit / 96 kHz
- WAV 24-bit / 192 kHz
- WAV 32-bit float / 192 kHz
- A legacy option labelled `WAV-PCM LOSSLESS 24B`

**Format note:** The legacy `FLAC_PROFILE` enum entry currently uses the `.wav` extension and WAV PCM description. It does not currently produce a genuine `.flac` file. Do not select or advertise it as FLAC until a FLAC encoder and correct file extension are implemented.

## Privacy and local data

- The application manifest currently does **not** request Android's `INTERNET` permission.
- Audio is selected using Android's Storage Access Framework and processed on the device.
- Master metadata is stored in a local Room database named `waviq_masters.db`.
- Rendered masters are saved under the app's private `files/masters` directory. The user can export a copy to a chosen location.
- No cloud account or API key is required by the current local assistant.

The manifest currently declares `RECORD_AUDIO`; review whether that permission is still needed for the shipped feature set before a production release.

## Technology stack

| Component | Current project configuration |
|---|---|
| Platform | Android |
| Language | Kotlin `2.2.10` |
| UI | Jetpack Compose + Material 3 |
| Android Gradle Plugin | `9.1.1` |
| Gradle distribution | `9.3.1` |
| Compile SDK | Android API `36`, minor API level `1` |
| Minimum SDK | API `24` (Android 7.0) |
| Target SDK | API `36` |
| Local database | Room `2.7.0` |
| Local model runtime | LiteRT `2.3.0` |
| Async work | Kotlin Coroutines `1.10.2` |
| Tests | JUnit, AndroidX test, Robolectric, Compose UI test dependencies |

The app's Java source/target compatibility is set to Java 11. The build tooling requires a JDK compatible with Gradle 9.3.1 and Android Gradle Plugin 9.1.1; use **JDK 17 or newer**.

## Build requirements

Install the following on your development machine:

- Android Studio with support for the project's Android Gradle Plugin version.
- JDK 17 or newer.
- Android SDK Platform **36.1** (API 36, minor API level 1).
- Android SDK Build-Tools **36.x**.
- Android SDK Platform-Tools.
- Android SDK Command-line Tools (latest).
- Gradle **9.3.1**, if you need to generate the missing wrapper files manually.

### Important: Gradle Wrapper files

This project contains `gradle/wrapper/gradle-wrapper.properties`, which points to Gradle 9.3.1. The supplied project archive does **not** contain the wrapper launcher scripts (`gradlew`, `gradlew.bat`) or `gradle-wrapper.jar`.

If Gradle 9.3.1 is installed locally, open a terminal in the project root and generate the wrapper files:

```bash
gradle wrapper --gradle-version 9.3.1
```

After the wrapper files have been generated, use the commands below.

### Build a debug APK

**Windows PowerShell:**

```powershell
.\gradlew.bat :app:assembleDebug
```

**macOS / Linux:**

```bash
./gradlew :app:assembleDebug
```

The debug APK is normally written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Run unit tests

**Windows PowerShell:**

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

**macOS / Linux:**

```bash
./gradlew :app:testDebugUnitTest
```

The repository includes tests for the local assistant's Arabic/English intent routing, startup behavior, and audio DSP components. A successful build and test run has not been verified in every developer environment; resolve any SDK or dependency-sync issues reported by your local Android Studio installation.

### Release signing

Release signing is configured in `app/build.gradle.kts` using environment variables:

- `KEYSTORE_PATH` — path to your upload keystore. If unset, the build script looks for `my-upload-key.jks` in the project root.
- `STORE_PASSWORD` — keystore password.
- `KEY_PASSWORD` — key password.

The configured key alias is `upload`. Keep the keystore and passwords out of source control. Debug builds use the standard Android debug key.

## Project structure

```text
WAVIQ/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── assets/
│   │   │   │   └── ai_audio_mastering_model.tflite
│   │   │   ├── java/com/example/
│   │   │   │   ├── assistant/       # Offline Arabic/English command router
│   │   │   │   ├── audio/           # Decoder, DSP engine, hooks and processing
│   │   │   │   ├── data/            # Room database and master library
│   │   │   │   ├── model/           # UI state, profiles and route models
│   │   │   │   ├── ui/
│   │   │   │   │   ├── home/        # Unified assistant-first workspace
│   │   │   │   │   ├── components/  # Shared controls and visual components
│   │   │   │   │   ├── screens/     # Existing detailed studio screens
│   │   │   │   │   └── theme/       # Color, typography and Compose theme
│   │   │   │   ├── viewmodel/       # Assistant orchestration and app state
│   │   │   │   └── MainActivity.kt  # Android entry point
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                    # Local unit and UI-related tests
│   │   └── androidTest/             # Instrumented Android tests
│   └── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml           # Dependency and plugin versions
│   └── wrapper/gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Key implementation files

- `assistant/WaviqLocalAssistant.kt` — text normalization and deterministic intent matching.
- `viewmodel/WaviqViewModel.kt` — routes assistant intents into available profiles and coordinates local rendering.
- `audio/AudioDecoder.kt` — decodes selected audio to PCM using Android media APIs.
- `audio/AudioDspEngine.kt` and `audio/AudioProcessor.kt` — audio playback, DSP, and offline rendering.
- `audio/TfliteDspHook.kt` — LiteRT model initialization and DSP fallback path.
- `ui/home/WaviqHomeScreen.kt` — unified main workspace and assistant controls.
- `data/WaviqDatabase.kt` — local Room database for saved master metadata.

## Development notes

- Keep audio processing off the main/UI thread.
- Avoid overwriting the imported source file.
- Validate model input/output tensor shapes and model licensing before replacing the bundled model.
- Treat all assistant output as untrusted until it has been mapped to a known, validated operation.
- Keep offline operation as a hard requirement: do not add network permission or cloud fallback unless the product requirements are explicitly changed.

---

**WAVIQ — Built for sound. Process locally.**
```

---

## `app/.gitignore`

```text
/build
```

---

## `app/build.gradle.kts`

```kotlin
plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  androidResources {
    noCompress += "tflite"
  }

  defaultConfig {
    applicationId = "com.aistudio.waviq.kmpvxz"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  // Release signing is configured by the developer; debug builds use the standard Android debug key.
  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      // Use Android Gradle Plugin's standard local debug signing configuration.
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

kotlin {
  compilerOptions {
    freeCompilerArgs.add("-Xskip-metadata-version-check")
  }
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  // implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  // implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  // Uncomment to use Firestore:
  // implementation(libs.firebase.firestore)

  // Uncomment ALL FOUR of the following dependencies together to use Firebase Auth and Google
  // Sign-In via Credential Manager:
  // implementation(libs.firebase.auth)
  // implementation(libs.androidx.credentials)
  // implementation(libs.androidx.credentials.play.services)
  // implementation(libs.googleid)
  // implementation(libs.firebase.appcheck.recaptcha)
  // implementation(libs.firebase.appcheck.debug)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.litert)
  // implementation(libs.logging.interceptor)
  // implementation(libs.moshi.kotlin)
  // implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  // implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
}
```

---

## `app/proguard-rules.pro`

```proguard
# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
```

---

## `app/src/androidTest/java/com/example/ExampleInstrumentedTest.kt`

```kotlin
package com.example

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
  @Test
  fun useAppContext() {
    // Context of the app under test.
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    assertEquals(BuildConfig.APPLICATION_ID, appContext.packageName)
  }
}
```

---

## `app/src/main/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MyApplication">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.MyApplication">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

---

## `app/src/main/java/com/example/MainActivity.kt`

```kotlin
package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.TactileDeckParams
import com.example.ui.home.WaviqHomeScreen
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
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val savedMasters = viewModel.savedMasters.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val openAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.importLocalAudio(uri)
    }

    val saveExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("audio/wav")
    ) { destinationUri ->
        if (destinationUri != null) {
            viewModel.copyLastExportToUri(destinationUri) { success ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        if (success) "Master saved to the selected folder." else "Could not save the master file."
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ObsidianVoid,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { contentPadding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize().padding(contentPadding)
        ) {
            WaviqHomeScreen(
                state = state,
                savedMasters = savedMasters,
                onImportAudio = { openAudioLauncher.launch(arrayOf("audio/*")) },
                onRunCommand = viewModel::runLocalAssistant,
                onPlayPause = viewModel::togglePlayPause,
                onSeek = viewModel::seekToFraction,
                onSaveToDevice = { suggestedName -> saveExportLauncher.launch(suggestedName) },
                onLoadMaster = viewModel::loadSavedMasterForPlayback,
                onDeleteMaster = viewModel::deleteSavedMaster,
                onSelectProfile = viewModel::selectEnhanceProfile,
                onUpdateDeck = { drive, width, punch, air ->
                    viewModel.updateDeckParams { current: TactileDeckParams ->
                        current.copy(
                            drive = drive ?: current.drive,
                            width = width ?: current.width,
                            punch = punch ?: current.punch,
                            air = air ?: current.air
                        )
                    }
                },
                onSelectFormat = { format -> viewModel.updateTranscodeConfig(format = format) }
            )
        }
    }
}
```

---

## `app/src/main/java/com/example/StartupGuard.kt`

```kotlin
package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.WaviqDatabase
import com.example.model.StartupCheckStatus
import com.example.model.StartupGuardReport
import com.example.model.StartupHealthState
import com.example.model.StartupSubsystemCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Pre-flight startup validator verifying TFLite asset integrity, optional hardware
 * permissions, and Room SQLite connectivity before studio initialization, producing
 * a structured fallback report for degraded-mode resilience.
 */
class StartupGuard(
    private val context: Context,
    private val modelAssetName: String = DEFAULT_MODEL_ASSET,
    private val assetOpener: (String) -> InputStream = { name -> context.assets.open(name) },
    private val permissionGrantedProvider: (String) -> Boolean = { perm ->
        ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
    },
    private val databasePing: suspend () -> Int = {
        WaviqDatabase.getInstance(context).masterTrackDao().getTrackCount()
    }
) {
    suspend fun validate(): StartupGuardReport = withContext(Dispatchers.IO) {
        val tfliteCheck = validateTfliteModel()
        val permissionCheck = validateHardwarePermissions()
        val databaseCheck = validateRoomDatabase()

        val hasCriticalFallback =
            tfliteCheck.status == StartupCheckStatus.FALLBACK ||
                databaseCheck.status == StartupCheckStatus.FALLBACK

        val healthState = when {
            hasCriticalFallback -> StartupHealthState.DEGRADED_FALLBACK
            permissionCheck.status == StartupCheckStatus.ADVISORY -> StartupHealthState.PERMISSION_ADVISORY
            else -> StartupHealthState.HEALTHY
        }

        val issues = buildList {
            if (tfliteCheck.status == StartupCheckStatus.FALLBACK) add(tfliteCheck.detail)
            if (databaseCheck.status == StartupCheckStatus.FALLBACK) add(databaseCheck.detail)
            if (permissionCheck.status == StartupCheckStatus.ADVISORY) add(permissionCheck.detail)
        }

        val summary = when (healthState) {
            StartupHealthState.HEALTHY -> "All TFLite, hardware permission, and Room DB checks passed."
            StartupHealthState.PERMISSION_ADVISORY ->
                "Studio active. Optional mic permission ungranted; file mastering and DSP playback remain operational."
            StartupHealthState.DEGRADED_FALLBACK ->
                "Degraded mode active: ${issues.joinToString(" | ")}"
            StartupHealthState.VALIDATING -> "Pre-flight validation in progress."
        }

        StartupGuardReport(
            healthState = healthState,
            tfliteCheck = tfliteCheck,
            permissionCheck = permissionCheck,
            databaseCheck = databaseCheck,
            fallbackSummary = summary,
            checkedAtMs = System.currentTimeMillis()
        )
    }

    private fun validateTfliteModel(): StartupSubsystemCheck {
        return runCatching {
            val header = assetOpener(modelAssetName).use { stream ->
                val bytes = ByteArray(MIN_TFLITE_BYTES)
                var totalRead = 0
                while (totalRead < bytes.size) {
                    val read = stream.read(bytes, totalRead, bytes.size - totalRead)
                    if (read <= 0) break
                    totalRead += read
                }
                if (totalRead < 8) {
                    error("Asset '$modelAssetName' truncated ($totalRead bytes)")
                }
                bytes.copyOf(totalRead)
            }
            val isTfl3 = header[4] == 'T'.code.toByte() &&
                header[5] == 'F'.code.toByte() &&
                header[6] == 'L'.code.toByte() &&
                header[7] == '3'.code.toByte()
            if (!isTfl3) {
                error("Invalid FlatBuffer signature in '$modelAssetName'")
            }
            StartupSubsystemCheck(
                title = "TFLITE MODEL",
                status = StartupCheckStatus.PASS,
                detail = "Verified TFL3 asset ($modelAssetName)"
            )
        }.getOrElse { err ->
            runCatching {
                Log.w("StartupGuard", "TFLite validation fallback: ${err.message}")
            }
            StartupSubsystemCheck(
                title = "TFLITE MODEL",
                status = StartupCheckStatus.FALLBACK,
                detail = "TFLite degraded (${err.message ?: "missing asset"}); using DSP spectral gate fallback"
            )
        }
    }

    private fun validateHardwarePermissions(): StartupSubsystemCheck {
        return runCatching {
            val micGranted = permissionGrantedProvider(Manifest.permission.RECORD_AUDIO)
            if (micGranted) {
                StartupSubsystemCheck(
                    title = "RECORD_AUDIO",
                    status = StartupCheckStatus.PASS,
                    detail = "Live AudioRecord hardware capture ready"
                )
            } else {
                StartupSubsystemCheck(
                    title = "RECORD_AUDIO",
                    status = StartupCheckStatus.ADVISORY,
                    detail = "Mic permission optional; grant when capturing live takes"
                )
            }
        }.getOrElse { err ->
            StartupSubsystemCheck(
                title = "RECORD_AUDIO",
                status = StartupCheckStatus.ADVISORY,
                detail = "Permission check deferred (${err.message ?: "unavailable"})"
            )
        }
    }

    private suspend fun validateRoomDatabase(): StartupSubsystemCheck {
        return runCatching {
            val count = databasePing()
            StartupSubsystemCheck(
                title = "ROOM SQLITE",
                status = StartupCheckStatus.PASS,
                detail = "Connected ($count saved master${if (count == 1) "" else "s"})"
            )
        }.getOrElse { err ->
            runCatching {
                Log.w("StartupGuard", "Room DB ping fallback: ${err.message}")
            }
            StartupSubsystemCheck(
                title = "ROOM SQLITE",
                status = StartupCheckStatus.FALLBACK,
                detail = "Room DB degraded (${err.message ?: "unavailable"}); operating in session-only fallback"
            )
        }
    }

    companion object {
        const val DEFAULT_MODEL_ASSET = "ai_audio_mastering_model.tflite"
        private const val MIN_TFLITE_BYTES = 64
    }
}
```

---

## `app/src/main/java/com/example/assistant/WaviqLocalAssistant.kt`

```kotlin
package com.example.assistant

/**
 * Small, deterministic, fully-offline command router for the WAVIQ audio workflow.
 * This is an intent parser, not a generative language model. A LiteRT audio model and
 * the existing DSP chain perform the signal processing after an intent is selected.
 */
object WaviqLocalAssistant {

    enum class Intent {
        MASTER,
        CLEAN_NOISE,
        VOCAL_CLARITY,
        CLUB_PUNCH,
        WARM_ANALOG,
        STREAMING,
        ANALYZE,
        EXPORT,
        UNKNOWN
    }

    fun understand(input: String): Intent {
        val text = normalize(input)
        if (text.isBlank()) return Intent.UNKNOWN

        return when {
            containsAny(text, "analyze", "analyse", "inspect", "audit", "حلل", "تحليل", "افحص", "فحص") ->
                Intent.ANALYZE

            containsAny(
                text,
                "noise", "denoise", "hiss", "hum", "clean", "background sound", "clean audio",
                "remove noise", "remove hiss", "نظف", "تنظيف", "ضوضاء", "تشويش", "طنين", "شيل الضوضاء",
                "احذف الضوضاء", "ازالة الضجيج", "ازاله الضوضاء", "ازالة النويز"
            ) -> Intent.CLEAN_NOISE

            containsAny(
                text,
                "vocal", "vocals", "voice", "speech", "podcast", "clear voice", "dialogue",
                "غناء", "صوت المغني", "الصوت البشري", "وضوح الصوت", "وضح الصوت", "وضح الغناء", "صفاء الصوت", "بودكاست", "الكلام"
            ) -> Intent.VOCAL_CLARITY

            containsAny(text, "warm", "analog", "tape", "smooth", "دافئ", "دفء", "تناظري", "شريطي") ->
                Intent.WARM_ANALOG

            containsAny(
                text,
                "club", "bass", "808", "hip hop", "hip-hop", "punch", "hard hitting", "louder", "loud",
                "كلوب", "باس", "بيس", "جهير", "قوي", "قوة", "ارفع الباس", "زود الباس", "قوي الباس", "ضخم الباس",
                "ارفع الصوت", "زود الصوت", "هيب هوب", "ثقيل"
            ) -> Intent.CLUB_PUNCH

            containsAny(
                text,
                "streaming", "spotify", "apple music", "youtube", "balanced", "normal master",
                "متوازن", "منصات", "سبوتيفاي", "يوتيوب", "بث"
            ) -> Intent.STREAMING

            containsAny(text, "export", "save", "render", "تصدير", "صدر", "احفظ", "حفظ") ->
                Intent.EXPORT

            containsAny(
                text,
                "master", "mastering", "enhance", "improve", "polish", "optimize", "process",
                "ماستر", "ماسترنج", "حسن", "تحسين", "طور", "عالج", "اشتغل", "ابدأ", "ابدا"
            ) -> Intent.MASTER

            else -> Intent.UNKNOWN
        }
    }

    fun normalize(value: String): String {
        return value.lowercase()
            .replace(Regex("[\\u064B-\\u065F\\u0670]"), "")
            .replace('\u0640', ' ')
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ى', 'ي')
            .replace('ة', 'ه')
            .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
            .trim()
    }

    private fun containsAny(text: String, vararg terms: String): Boolean =
        terms.any { normalize(it).let(text::contains) }
}
```

---

## `app/src/main/java/com/example/audio/AudioDecoder.kt`

```kotlin
package com.example.audio

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.provider.OpenableColumns
import com.example.model.AudioTrackInfo
import com.example.model.EnhanceProfile
import com.example.model.MusicGenre
import com.example.model.TrackDiagnosticReport
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

data class DecodedAudio(
    val left: FloatArray,
    val right: FloatArray,
    val sampleRate: Int,
    val info: AudioTrackInfo
)

object AudioDecoder {

    private const val TARGET_SAMPLE_RATE = 48000
    private const val MAX_DECODE_SECONDS = 90

    fun generateStudioReferenceTrack(): DecodedAudio {
        val sr = TARGET_SAMPLE_RATE
        val durationSec = 16
        val numSamples = sr * durationSec
        val left = FloatArray(numSamples)
        val right = FloatArray(numSamples)

        val bassNotes = floatArrayOf(55.0f, 55.0f, 65.41f, 73.42f, 49.0f, 49.0f, 61.74f, 65.41f)
        val padRoots = floatArrayOf(220.0f, 261.63f, 196.0f, 246.94f)
        val leadNotes = floatArrayOf(440f, 523.25f, 659.25f, 587.33f, 523.25f, 440f, 392f, 493.88f)

        val bpm = 120f
        val beatSamples = (sr * 60f / bpm).toInt()
        val eighthSamples = beatSamples / 2

        for (i in 0 until numSamples) {
            val t = i.toFloat() / sr
            val beatIdx = i / beatSamples
            val eighthIdx = i / eighthSamples
            val posInBeat = (i % beatSamples).toFloat() / sr
            val posInEighth = (i % eighthSamples).toFloat() / sr

            // Kick drum on every beat
            val kickEnv = exp(-posInBeat * 14f)
            val kickFreq = 48f + 110f * exp(-posInBeat * 38f)
            val kick = sin(2f * PI.toFloat() * kickFreq * posInBeat) * kickEnv * 0.42f

            // Snare / Clap on beats 2 & 4
            val isSnareBeat = (beatIdx % 2 == 1)
            val snareEnv = if (isSnareBeat) exp(-posInBeat * 22f) else 0f
            val pseudoNoise = sin(i * 12.9898f) * 43758.545f
            val noise = (pseudoNoise - pseudoNoise.toInt()) * 2f - 1f
            val snare = (noise * 0.65f + sin(2f * PI.toFloat() * 195f * posInBeat) * 0.35f) * snareEnv * 0.24f

            // Crisp hi-hat on off-beats
            val hatEnv = exp(-posInEighth * 45f)
            val hat = ((sin(t * 19000f) + sin(t * 27300f) + noise * 0.5f) * 0.33f) * hatEnv * 0.09f

            // Analog sub + harmonic bass
            val bassF = bassNotes[(beatIdx / 2) % bassNotes.size]
            val bassEnv = (1f - exp(-posInEighth * 40f)) * exp(-posInEighth * 3.5f)
            val bass = (sin(2f * PI.toFloat() * bassF * t) * 0.7f +
                sin(2f * PI.toFloat() * bassF * 2f * t) * 0.25f) * bassEnv * 0.30f

            // Wide stereo synth pad
            val padF = padRoots[(beatIdx / 4) % padRoots.size]
            val padL = (sin(2f * PI.toFloat() * padF * t) +
                0.6f * sin(2f * PI.toFloat() * (padF * 1.25f) * t) +
                0.4f * sin(2f * PI.toFloat() * (padF * 1.5f) * t)) * 0.09f
            val padR = (sin(2f * PI.toFloat() * (padF * 1.003f) * t + 0.4f) +
                0.6f * sin(2f * PI.toFloat() * (padF * 1.252f) * t + 0.9f) +
                0.4f * sin(2f * PI.toFloat() * (padF * 1.503f) * t + 1.2f)) * 0.09f

            // Vocal-range center lead
            val leadF = leadNotes[eighthIdx % leadNotes.size]
            val leadEnv = (1f - exp(-posInEighth * 30f)) * exp(-posInEighth * 5f)
            val lead = (sin(2f * PI.toFloat() * leadF * t) * 0.65f +
                sin(2f * PI.toFloat() * leadF * 2f * t) * 0.25f +
                sin(2f * PI.toFloat() * leadF * 3f * t) * 0.1f) * leadEnv * 0.15f

            val l = (kick + snare + hat * 0.85f + bass + padL + lead).coerceIn(-0.95f, 0.95f)
            val r = (kick + snare + hat * 1.15f + bass + padR + lead).coerceIn(-0.95f, 0.95f)
            left[i] = l
            right[i] = r
        }

        val info = analyzeBuffer(
            id = "ref_studio_01",
            title = "NOTHING_SESSION_01_RAW.wav",
            sourceLabel = "BUNDLED STUDIO REF",
            left = left,
            right = right,
            sampleRate = sr,
            channels = 2,
            isBundled = true
        )
        return DecodedAudio(left, right, sr, info)
    }

    fun decodeUriToPcm(context: Context, uri: Uri): DecodedAudio {
        val displayName = resolveDisplayName(context, uri) ?: "IMPORTED_TRACK.wav"
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(context, uri, null)
            var audioTrackIdx = -1
            var format: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val f = extractor.getTrackFormat(i)
                val mime = f.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIdx = i
                    format = f
                    break
                }
            }
            require(audioTrackIdx >= 0 && format != null) { "No audio track found in selected file." }

            extractor.selectTrack(audioTrackIdx)
            val mime = format.getString(MediaFormat.KEY_MIME)!!
            val sourceSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val sourceChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT).coerceAtLeast(1)

            val codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            val pcmBytesOut = ByteArrayOutputStream()
            val maxRawBytes = sourceSampleRate * sourceChannels * 2 * MAX_DECODE_SECONDS
            val bufferInfo = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false
            var currentSampleRate = sourceSampleRate
            var currentChannels = sourceChannels

            while (!outputDone && pcmBytesOut.size() < maxRawBytes) {
                if (!inputDone) {
                    val inIdx = codec.dequeueInputBuffer(10_000)
                    if (inIdx >= 0) {
                        val inBuf = codec.getInputBuffer(inIdx)!!
                        val sampleSize = extractor.readSampleData(inBuf, 0)
                        if (sampleSize < 0) {
                            codec.queueInputBuffer(
                                inIdx, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            inputDone = true
                        } else {
                            codec.queueInputBuffer(
                                inIdx, 0, sampleSize, extractor.sampleTime, 0
                            )
                            extractor.advance()
                        }
                    }
                }

                val outIdx = codec.dequeueOutputBuffer(bufferInfo, 10_000)
                when {
                    outIdx >= 0 -> {
                        val outBuf = codec.getOutputBuffer(outIdx)
                        if (outBuf != null && bufferInfo.size > 0) {
                            outBuf.position(bufferInfo.offset)
                            outBuf.limit(bufferInfo.offset + bufferInfo.size)
                            val chunk = ByteArray(bufferInfo.size)
                            outBuf.get(chunk)
                            pcmBytesOut.write(chunk)
                        }
                        codec.releaseOutputBuffer(outIdx, false)
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            outputDone = true
                        }
                    }
                    outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val outFormat = codec.outputFormat
                        if (outFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                            currentSampleRate = outFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        }
                        if (outFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                            currentChannels = outFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                        }
                    }
                }
            }

            codec.stop()
            codec.release()

            val raw = pcmBytesOut.toByteArray()
            val shortCount = raw.size / 2
            val frameCount = (shortCount / currentChannels).coerceAtLeast(1)
            val srcLeft = FloatArray(frameCount)
            val srcRight = FloatArray(frameCount)

            val bb = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN)
            for (f in 0 until frameCount) {
                val l = bb.getShort().toFloat() / 32768f
                val r = if (currentChannels >= 2) bb.getShort().toFloat() / 32768f else l
                // Skip extra channels if surround
                for (c in 2 until currentChannels) {
                    bb.getShort()
                }
                srcLeft[f] = l
                srcRight[f] = r
            }

            val (left48, right48) = if (currentSampleRate == TARGET_SAMPLE_RATE) {
                srcLeft to srcRight
            } else {
                resampleCubic(srcLeft, srcRight, currentSampleRate, TARGET_SAMPLE_RATE)
            }

            val info = analyzeBuffer(
                id = "import_${System.currentTimeMillis()}",
                title = displayName,
                sourceLabel = "LOCAL SAF IMPORT",
                left = left48,
                right = right48,
                sampleRate = currentSampleRate,
                channels = currentChannels,
                isBundled = false
            )
            return DecodedAudio(left48, right48, TARGET_SAMPLE_RATE, info)
        } finally {
            extractor.release()
        }
    }

    private fun resampleCubic(
        srcL: FloatArray,
        srcR: FloatArray,
        srcSr: Int,
        dstSr: Int
    ): Pair<FloatArray, FloatArray> {
        val ratio = srcSr.toDouble() / dstSr.toDouble()
        val outLen = ((srcL.size / ratio).toInt()).coerceAtLeast(1)
        val outL = FloatArray(outLen)
        val outR = FloatArray(outLen)
        val last = (srcL.size - 1).coerceAtLeast(0)
        for (i in 0 until outLen) {
            val pos = i * ratio
            val i1 = pos.toInt().coerceIn(0, last)
            val i0 = (i1 - 1).coerceAtLeast(0)
            val i2 = (i1 + 1).coerceAtMost(last)
            val i3 = (i1 + 2).coerceAtMost(last)
            val t = pos - i1
            val t2 = t * t
            val t3 = t2 * t
            outL[i] = (0.5 * (2.0 * srcL[i1] + (-srcL[i0] + srcL[i2]) * t + (2.0 * srcL[i0] - 5.0 * srcL[i1] + 4.0 * srcL[i2] - srcL[i3]) * t2 + (-srcL[i0] + 3.0 * srcL[i1] - 3.0 * srcL[i2] + srcL[i3]) * t3)).coerceIn(-1.0, 1.0).toFloat()
            outR[i] = (0.5 * (2.0 * srcR[i1] + (-srcR[i0] + srcR[i2]) * t + (2.0 * srcR[i0] - 5.0 * srcR[i1] + 4.0 * srcR[i2] - srcR[i3]) * t2 + (-srcR[i0] + 3.0 * srcR[i1] - 3.0 * srcR[i2] + srcR[i3]) * t3)).coerceIn(-1.0, 1.0).toFloat()
        }
        return outL to outR
    }

    fun analyzeBuffer(
        id: String,
        title: String,
        sourceLabel: String,
        left: FloatArray,
        right: FloatArray,
        sampleRate: Int,
        channels: Int,
        isBundled: Boolean
    ): AudioTrackInfo {
        val len = left.size.coerceAtLeast(1)
        var peak = 1e-6f
        var sumSq = 0.0
        var midSq = 0.0
        var sideSq = 0.0
        var hfDiff2Sq = 0.0

        val step = max(1, len / 40_000)
        var count = 0
        var prevMid = 0f
        var prevPrevMid = 0f
        for (i in 0 until len step step) {
            val l = left[i]
            val r = right[i]
            val p = max(abs(l), abs(r))
            if (p > peak) peak = p
            sumSq += 0.5 * (l * l + r * r)
            val m = 0.5 * (l + r)
            val s = 0.5 * (l - r)
            midSq += m * m
            sideSq += s * s
            val mF = m.toFloat()
            if (count >= 2 && step == 1) {
                val d2 = mF - 2f * prevMid + prevPrevMid
                hfDiff2Sq += (d2 * d2).toDouble()
            } else if (i >= 2) {
                val m1 = 0.5f * (left[i - 1] + right[i - 1])
                val m2 = 0.5f * (left[i - 2] + right[i - 2])
                val d2 = mF - 2f * m1 + m2
                hfDiff2Sq += (d2 * d2).toDouble()
            }
            prevPrevMid = prevMid
            prevMid = mF
            count++
        }

        val rms = sqrt((sumSq / count.coerceAtLeast(1)).coerceAtLeast(1e-12)).toFloat()
        val rmsDb = (20f * log10(rms.coerceAtLeast(1e-6f))).coerceIn(-60f, 0f)
        val peakDb = (20f * log10(peak.coerceAtLeast(1e-6f))).coerceIn(-60f, 0f)
        val lufs = (rmsDb - 0.7f).coerceIn(-48f, -2f)
        val dr = (peakDb - rmsDb).coerceIn(1.5f, 24f)
        val widthPct = ((sideSq / (midSq + sideSq).coerceAtLeast(1e-9)) * 200.0)
            .toInt().coerceIn(5, 100)

        // Estimate upper-band presence using 2nd-order finite-difference energy vs mid energy
        val hfRatio = sqrt((hfDiff2Sq / midSq.coerceAtLeast(1e-9)).coerceAtLeast(0.0)).toFloat()
        val estimatedBandwidthKhz = (9.5f + (hfRatio * 8.2f)).coerceIn(9.5f, 20.0f)
        val hfDeficitPct = (((20.0f - estimatedBandwidthKhz) / 10.5f) * 100f)
            .toInt().coerceIn(4, 92)

        val (recordingType, recProfile) = when {
            channels == 1 || (widthPct <= 10 && hfDeficitPct >= 35) -> "VOCAL / SPEECH" to EnhanceProfile.VOCAL_CLARITY
            dr >= 17.5f || (dr >= 12.0f && lufs < -20.0f) -> "ACOUSTIC / DYNAMIC" to EnhanceProfile.WARM_ANALOG
            dr < 6.5f || lufs > -11.5f -> "COMPRESSED / CLUB MIX" to EnhanceProfile.STREAMING_MASTER
            else -> "FULL STUDIO MIX" to EnhanceProfile.STREAMING_MASTER
        }

        // Fast spectral & transient classification across 6 standard genres
        val (detectedGenre, genreConfidence, genreRationale) = when {
            title.contains("classic", ignoreCase = true) || title.contains("orchestra", ignoreCase = true) || (channels >= 2 && dr >= 15.0f && lufs < -19.0f) -> {
                Triple(
                    MusicGenre.CLASSICAL,
                    if (dr >= 15.0f) 94 else 88,
                    "Wide ${String.format(Locale.US, "%.1f", dr)}dB dynamic range & uncompressed acoustic headroom"
                )
            }
            title.contains("electronic", ignoreCase = true) || title.contains("techno", ignoreCase = true) || title.contains("edm", ignoreCase = true) || isBundled || (widthPct >= 50 && lufs >= -18.5f && dr in 6.0f..14.5f) -> {
                Triple(
                    MusicGenre.ELECTRONIC,
                    93,
                    "Wide stereo field & high-energy sub-bass electronic extension"
                )
            }
            title.contains("acoustic", ignoreCase = true) || (dr >= 11.5f && (channels == 1 || (widthPct <= 45 && lufs < -18.0f))) -> {
                Triple(
                    MusicGenre.ACOUSTIC,
                    91,
                    "Natural acoustic crest factor & warm harmonic preservation"
                )
            }
            title.contains("hiphop", ignoreCase = true) || title.contains("hip_hop", ignoreCase = true) || title.contains("rap", ignoreCase = true) || (dr in 7.0f..12.5f && widthPct in 20..50 && lufs >= -15.5f) -> {
                Triple(
                    MusicGenre.HIP_HOP,
                    89,
                    "Punchy low-end transient impact & focused vocal articulation"
                )
            }
            title.contains("rock", ignoreCase = true) || title.contains("metal", ignoreCase = true) || (dr in 5.5f..11.0f && widthPct in 25..55 && hfDeficitPct in 18..60) -> {
                Triple(
                    MusicGenre.ROCK,
                    88,
                    "Dense mid-frequency harmonic saturation & driving drum presence"
                )
            }
            else -> {
                Triple(
                    MusicGenre.POP,
                    86,
                    "Modern streaming crest (${String.format(Locale.US, "%.1f", dr)}dB DR) & balanced high-frequency presence"
                )
            }
        }

        val clarityNeed = when {
            hfDeficitPct >= 45 || peakDb > -0.5f || lufs < -20f -> "HIGH"
            hfDeficitPct >= 20 || dr < 7.5f -> "MODERATE"
            else -> "LOW"
        }

        val restorationAmt = (0.35f + (hfDeficitPct / 100f) * 0.60f).coerceIn(0.35f, 0.92f)
        val diagnostic = TrackDiagnosticReport(
            recordingTypeLabel = recordingType,
            estimatedBandwidthKhz = estimatedBandwidthKhz,
            hfDeficitPct = hfDeficitPct,
            clarityNeedLabel = clarityNeed,
            recommendedProfile = detectedGenre.recommendedProfile,
            recommendedRestorationAmount = restorationAmt,
            autoAppliedSummary = "AUTO-GENRE // ${detectedGenre.title} (${detectedGenre.targetKhz}kHz ${detectedGenre.oversampling.label}) + TFLITE GUARD",
            detectedGenre = detectedGenre,
            genreConfidencePct = genreConfidence,
            genreRationale = genreRationale
        )

        val bars = 96
        val barSize = max(1, len / bars)
        val peaks = List(bars) { b ->
            val start = b * barSize
            val end = ((b + 1) * barSize).coerceAtMost(len)
            var maxBar = 0.05f
            for (j in start until end step max(1, barSize / 16)) {
                val amp = max(abs(left[j]), abs(right[j]))
                if (amp > maxBar) maxBar = amp
            }
            maxBar.coerceIn(0.05f, 1f)
        }

        val durationMs = (len.toLong() * 1000L) / TARGET_SAMPLE_RATE
        return AudioTrackInfo(
            id = id,
            title = title,
            sourceLabel = sourceLabel,
            sampleRate = sampleRate,
            channels = channels,
            durationMs = durationMs,
            bitDepthLabel = "64-BIT DOUBLE DSP",
            integratedLufs = lufs,
            truePeakDb = peakDb,
            dynamicRangeDb = dr,
            stereoWidthPct = widthPct,
            waveformPeaks = peaks,
            isBundledRef = isBundled,
            diagnostic = diagnostic
        )
    }

    private fun resolveDisplayName(context: Context, uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
            }
        }.getOrNull()
    }
}
```

---

## `app/src/main/java/com/example/audio/AudioDspEngine.kt`

```kotlin
package com.example.audio

import android.content.Context
import com.example.model.AudioTelemetry
import com.example.model.EqBand
import com.example.model.ExportFormat
import com.example.model.GlyphHardwareFrame
import com.example.model.GlyphPatternMode
import com.example.model.OversamplingFactor
import com.example.model.StemMixerState
import com.example.model.TactileDeckParams
import com.example.model.TranscodeConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * Coordinates WAVIQ's integrated Hi-Res, TFLite AI, Stem, and 4x/8x Oversampled DSP hooks
 * on top of [AudioProcessor].
 */
class AudioDspEngine(
    context: Context,
    scope: CoroutineScope
) {
    private val appContext = context.applicationContext
    val audioProcessor = AudioProcessor(scope = scope, sampleRate = 48000, blockSize = 1024)

    private val eqHook = BiquadEqHook()
    private val tactileHook = TactileDriveHook()
    private val hiResHook = HiResUpsamplingHook()
    private val tfliteHook = TfliteDspHook(appContext, "ai_audio_mastering_model.tflite")
    private val stemHook = StemMatrixHook()
    private val limiterHook = TruePeakLimiterHook()
    private val glyphVuController: GlyphVuInterface = DefaultGlyphVuController()

    @Volatile
    private var cachedEqBands: List<EqBand> = emptyList()
    @Volatile
    private var cachedDeckParams: TactileDeckParams = TactileDeckParams()

    val telemetry: StateFlow<AudioTelemetry> = audioProcessor.telemetry
    val hookSnapshots: StateFlow<List<DspHookSnapshot>> = audioProcessor.hookSnapshots

    init {
        // Integrated 6-stage real-time Float32 DSP chain
        audioProcessor.registerHook(eqHook)
        audioProcessor.registerHook(tactileHook)
        audioProcessor.registerHook(hiResHook)
        audioProcessor.registerHook(tfliteHook)
        audioProcessor.registerHook(stemHook)
        audioProcessor.registerHook(limiterHook)
    }

    fun loadDecodedAudio(audio: DecodedAudio) {
        glyphVuController.reset()
        audioProcessor.loadBuffers(
            left = audio.left,
            right = audio.right,
            infoDurationMs = audio.info.durationMs,
            lufs = audio.info.integratedLufs,
            peakDb = audio.info.truePeakDb
        )
    }

    fun loadLocalPcmFile(file: File): DecodedAudio = audioProcessor.loadLocalPcmFile(file)

    fun updateDsp(
        bands: List<EqBand>,
        deck: TactileDeckParams,
        stems: StemMixerState,
        isStemScreen: Boolean,
        wetEnabled: Boolean
    ) {
        cachedEqBands = bands
        cachedDeckParams = deck

        eqHook.configure(bands, audioProcessor.sampleRate)
        tactileHook.configure(deck, audioProcessor.sampleRate)
        hiResHook.configure(audioProcessor.sampleRate)
        stemHook.updateState(stems, isStemScreen)
        limiterHook.ceilingDb = deck.ceilingDb
        audioProcessor.setWetEnabled(wetEnabled)
    }

    fun setOversamplingFactor(factor: OversamplingFactor) {
        limiterHook.oversamplingFactor = factor
        audioProcessor.refreshLatencyTelemetry()
    }

    fun setHiResTargetKhz(khz: Int) {
        hiResHook.targetKhz = khz
        audioProcessor.refreshLatencyTelemetry()
    }

    fun setHiResRestorationAmount(amount: Float) {
        hiResHook.restorationAmount = amount.coerceIn(0.25f, 1.0f)
    }

    fun enableAutonomousMasteringHooks() {
        audioProcessor.setHookEnabled(eqHook.id, true)
        audioProcessor.setHookEnabled(tactileHook.id, true)
        audioProcessor.setHookEnabled(hiResHook.id, true)
        audioProcessor.setHookEnabled(tfliteHook.id, true)
        audioProcessor.setHookEnabled(limiterHook.id, true)
    }

    fun getTfliteRuntimeLabel(): String = tfliteHook.runtimeMode

    fun getNeuralTelemetry(): Pair<Float, Float> {
        return if (tfliteHook.isEnabled) {
            tfliteHook.lastGateReductionPct to tfliteHook.lastDynGainDb
        } else {
            0f to 0f
        }
    }

    fun getLimiterGainReductionDb(): Float {
        return if (limiterHook.isEnabled) limiterHook.lastGainReductionDb else 0f
    }

    fun getMultiBandLimiterTelemetry(): Triple<Float, Float, Float> {
        return if (limiterHook.isEnabled) {
            Triple(
                limiterHook.lastLowBandGrDb,
                limiterHook.lastMidBandGrDb,
                limiterHook.lastHighBandGrDb
            )
        } else {
            Triple(0f, 0f, 0f)
        }
    }

    fun buildGlyphFrame(
        tel: AudioTelemetry,
        neuralGatePct: Float,
        limiterGrDb: Float,
        patternMode: GlyphPatternMode
    ): GlyphHardwareFrame {
        val peakLinear = 10.0.pow((tel.truePeakDb / 20.0).toDouble()).toFloat().coerceIn(0f, 1f)
        return glyphVuController.mapToHardwareFrame(
            rmsLeftDb = tel.leftVuDb,
            rmsRightDb = tel.rightVuDb,
            limiterGainReductionDb = limiterGrDb,
            neuralGateReductionPct = neuralGatePct,
            peakBlock = peakLinear,
            bins64 = tel.spectrumBins,
            patternMode = patternMode
        )
    }

    fun toggleHook(hookId: String, enabled: Boolean) {
        audioProcessor.setHookEnabled(hookId, enabled)
    }

    fun togglePlayPause() = audioProcessor.togglePlayPause()

    fun play() = audioProcessor.play()

    fun pause() = audioProcessor.pause()

    fun seekToFraction(fraction: Float) = audioProcessor.seekToFraction(fraction)

    fun startMicRecording(
        onMeterUpdate: (levelNorm: Float, durationMs: Long) -> Unit,
        onError: (String) -> Unit
    ) = audioProcessor.startRecording(onMeterUpdate, onError)

    fun stopMicRecording(): DecodedAudio? = audioProcessor.stopRecording()

    fun release() {
        audioProcessor.release()
        tfliteHook.close()
    }

    suspend fun exportMasterWav(
        outFile: File,
        config: TranscodeConfig,
        onProgress: (Float, Long) -> Unit
    ): Pair<Float, Float> = withContext(Dispatchers.IO) {
        val (srcL, srcR) = audioProcessor.getCurrentBuffers()
        val total = srcL.size.coerceAtLeast(1)
        val internalSr = audioProcessor.sampleRate

        val outL = srcL.copyOf()
        val outR = srcR.copyOf()

        // Run offline chunked pass through fresh instances honoring active hook states
        val offEq = BiquadEqHook().apply {
            isEnabled = eqHook.isEnabled
            configure(cachedEqBands, internalSr)
        }
        val offTactile = TactileDriveHook().apply {
            isEnabled = tactileHook.isEnabled
            configure(cachedDeckParams, internalSr)
        }
        val offHiRes = HiResUpsamplingHook().apply {
            isEnabled = hiResHook.isEnabled
            restorationAmount = hiResHook.restorationAmount
            targetKhz = maxOf(hiResHook.targetKhz, config.format.sampleRate / 1000)
            configure(internalSr)
        }
        val offLimiter = TruePeakLimiterHook().apply {
            isEnabled = limiterHook.isEnabled
            ceilingDb = config.targetTruePeakDb
            oversamplingFactor = if (config.format.sampleRate >= 192000) {
                OversamplingFactor.X8
            } else {
                limiterHook.oversamplingFactor
            }
        }
        val offTflite = TfliteDspHook(appContext, "ai_audio_mastering_model.tflite").apply {
            isEnabled = tfliteHook.isEnabled
        }

        val chunkSize = 1024
        val blkL = FloatArray(chunkSize)
        val blkR = FloatArray(chunkSize)
        var pos = 0
        var peak = 1e-6f
        var sumSq = 0.0

        offTflite.use { tflite ->
            while (pos < total) {
                val count = minOf(chunkSize, total - pos)
                System.arraycopy(outL, pos, blkL, 0, count)
                System.arraycopy(outR, pos, blkR, 0, count)

                if (offEq.isEnabled) offEq.processBlock(blkL, blkR, count, internalSr)
                if (offTactile.isEnabled) offTactile.processBlock(blkL, blkR, count, internalSr)
                if (offHiRes.isEnabled) offHiRes.processBlock(blkL, blkR, count, internalSr)
                if (tflite.isEnabled) tflite.processBlock(blkL, blkR, count, internalSr)
                if (offLimiter.isEnabled) offLimiter.processBlock(blkL, blkR, count, internalSr)

                for (i in 0 until count) {
                    val l = blkL[i].coerceIn(-1f, 1f)
                    val r = blkR[i].coerceIn(-1f, 1f)
                    outL[pos + i] = l
                    outR[pos + i] = r
                    val p = max(abs(l), abs(r))
                    if (p > peak) peak = p
                    sumSq += 0.5 * (l * l + r * r)
                }

                pos += count
                if (pos % (chunkSize * 40) == 0) {
                    onProgress((pos.toFloat() / total) * 0.45f, 0L)
                }
            }
        }

        val targetSr = config.format.sampleRate
        val (finL, finR) = if (targetSr == internalSr) {
            outL to outR
        } else {
            resampleCubicCatmullRom(outL, outR, internalSr, targetSr)
        }

        // Normalize ceiling cleanly to configured True-Peak dBTP target when requested
        if (config.normalizeToTarget) {
            var postPeak = 1e-7
            for (i in finL.indices) {
                val p = max(abs(finL[i].toDouble()), abs(finR[i].toDouble()))
                if (p > postPeak) postPeak = p
            }
            val targetCeilingAmp = 10.0.pow(config.targetTruePeakDb.toDouble() / 20.0).coerceIn(0.25, 0.995)
            val normScale = (targetCeilingAmp / postPeak).coerceIn(0.5, 4.0)
            if (abs(normScale - 1.0) > 0.002) {
                sumSq = 0.0
                peak = 1e-6f
                for (i in finL.indices) {
                    val nl = (finL[i].toDouble() * normScale).coerceIn(-targetCeilingAmp, targetCeilingAmp).toFloat()
                    val nr = (finR[i].toDouble() * normScale).coerceIn(-targetCeilingAmp, targetCeilingAmp).toFloat()
                    finL[i] = nl
                    finR[i] = nr
                    val p = max(abs(nl), abs(nr))
                    if (p > peak) peak = p
                    sumSq += 0.5 * (nl * nl + nr * nr)
                }
            }
        }

        val bitDepth = config.format.bitDepth
        val isFloat32 = bitDepth == 32
        val audioFormatTag: Short = if (isFloat32) 3 else 1 // 3 = IEEE_FLOAT, 1 = PCM
        val bytesPerSample = bitDepth / 8
        val dataSize = finL.size * 2 * bytesPerSample

        val rng = java.util.Random(0x5EED2026L)
        val tpdfLsbStep = when (bitDepth) {
            16 -> 1.0 / 32767.0
            24 -> 1.0 / 8388607.0
            else -> 0.0
        }

        outFile.parentFile?.mkdirs()
        BufferedOutputStream(FileOutputStream(outFile)).use { bos ->
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            header.put("RIFF".toByteArray())
            header.putInt(36 + dataSize)
            header.put("WAVE".toByteArray())
            header.put("fmt ".toByteArray())
            header.putInt(16)
            header.putShort(audioFormatTag)
            header.putShort(2)
            header.putInt(targetSr)
            header.putInt(targetSr * 2 * bytesPerSample)
            header.putShort((2 * bytesPerSample).toShort())
            header.putShort(bitDepth.toShort())
            header.put("data".toByteArray())
            header.putInt(dataSize)
            bos.write(header.array())

            val chunkFrames = 4096
            val byteBuf = ByteArray(chunkFrames * 2 * bytesPerSample)
            var written = 44L
            var offset = 0
            while (offset < finL.size) {
                val count = minOf(chunkFrames, finL.size - offset)
                var ptr = 0
                for (k in 0 until count) {
                    val baseL = finL[offset + k].toDouble().coerceIn(-1.0, 1.0)
                    val baseR = finR[offset + k].toDouble().coerceIn(-1.0, 1.0)

                    if (isFloat32) {
                        val bitsL = java.lang.Float.floatToRawIntBits(baseL.toFloat())
                        val bitsR = java.lang.Float.floatToRawIntBits(baseR.toFloat())
                        byteBuf[ptr++] = (bitsL and 0xFF).toByte()
                        byteBuf[ptr++] = ((bitsL ushr 8) and 0xFF).toByte()
                        byteBuf[ptr++] = ((bitsL ushr 16) and 0xFF).toByte()
                        byteBuf[ptr++] = ((bitsL ushr 24) and 0xFF).toByte()
                        byteBuf[ptr++] = (bitsR and 0xFF).toByte()
                        byteBuf[ptr++] = ((bitsR ushr 8) and 0xFF).toByte()
                        byteBuf[ptr++] = ((bitsR ushr 16) and 0xFF).toByte()
                        byteBuf[ptr++] = ((bitsR ushr 24) and 0xFF).toByte()
                    } else {
                        val ditherL = if (config.ditherEnabled) (rng.nextDouble() - rng.nextDouble()) * tpdfLsbStep else 0.0
                        val ditherR = if (config.ditherEnabled) (rng.nextDouble() - rng.nextDouble()) * tpdfLsbStep else 0.0
                        val dL = (baseL + ditherL).coerceIn(-1.0, 1.0)
                        val dR = (baseR + ditherR).coerceIn(-1.0, 1.0)

                        if (bitDepth == 16) {
                            val sL = kotlin.math.round(dL * 32767.0).toInt().coerceIn(-32768, 32767)
                            val sR = kotlin.math.round(dR * 32767.0).toInt().coerceIn(-32768, 32767)
                            byteBuf[ptr++] = (sL and 0xFF).toByte()
                            byteBuf[ptr++] = ((sL shr 8) and 0xFF).toByte()
                            byteBuf[ptr++] = (sR and 0xFF).toByte()
                            byteBuf[ptr++] = ((sR shr 8) and 0xFF).toByte()
                        } else {
                            val sL = kotlin.math.round(dL * 8388607.0).toInt().coerceIn(-8388608, 8388607)
                            val sR = kotlin.math.round(dR * 8388607.0).toInt().coerceIn(-8388608, 8388607)
                            byteBuf[ptr++] = (sL and 0xFF).toByte()
                            byteBuf[ptr++] = ((sL shr 8) and 0xFF).toByte()
                            byteBuf[ptr++] = ((sL shr 16) and 0xFF).toByte()
                            byteBuf[ptr++] = (sR and 0xFF).toByte()
                            byteBuf[ptr++] = ((sR shr 8) and 0xFF).toByte()
                            byteBuf[ptr++] = ((sR shr 16) and 0xFF).toByte()
                        }
                    }
                }
                bos.write(byteBuf, 0, ptr)
                written += ptr
                offset += count
                onProgress(0.45f + (offset.toFloat() / finL.size) * 0.55f, written)
            }
            bos.flush()
        }

        val frameDenom = finL.size.coerceAtLeast(1)
        val rmsDenom = if (config.normalizeToTarget) frameDenom else total
        val rms = sqrt((sumSq / rmsDenom).coerceAtLeast(1e-12)).toFloat()
        val finalLufs = (20f * log10(rms.coerceAtLeast(1e-6f)) - 0.7f).coerceIn(-48f, -2f)
        val finalPeakDb = (20f * log10(peak.coerceAtLeast(1e-6f))).coerceIn(-60f, 0f)
        finalLufs to finalPeakDb
    }

    private fun resampleCubicCatmullRom(
        srcL: FloatArray,
        srcR: FloatArray,
        srcSr: Int,
        dstSr: Int
    ): Pair<FloatArray, FloatArray> {
        val total = srcL.size
        val ratio = srcSr.toDouble() / dstSr.toDouble()
        val newLen = (total / ratio).toInt().coerceAtLeast(1)
        val outL = FloatArray(newLen)
        val outR = FloatArray(newLen)
        val last = (total - 1).coerceAtLeast(0)
        for (i in 0 until newLen) {
            val pos = i * ratio
            val i1 = pos.toInt().coerceIn(0, last)
            val i0 = (i1 - 1).coerceAtLeast(0)
            val i2 = (i1 + 1).coerceAtMost(last)
            val i3 = (i1 + 2).coerceAtMost(last)
            val t = pos - i1
            outL[i] = cubicHermite(srcL[i0].toDouble(), srcL[i1].toDouble(), srcL[i2].toDouble(), srcL[i3].toDouble(), t)
                .coerceIn(-1.0, 1.0).toFloat()
            outR[i] = cubicHermite(srcR[i0].toDouble(), srcR[i1].toDouble(), srcR[i2].toDouble(), srcR[i3].toDouble(), t)
                .coerceIn(-1.0, 1.0).toFloat()
        }
        return outL to outR
    }

    private fun cubicHermite(p0: Double, p1: Double, p2: Double, p3: Double, t: Double): Double {
        val t2 = t * t
        val t3 = t2 * t
        return 0.5 * (
            (2.0 * p1) +
                (-p0 + p2) * t +
                (2.0 * p0 - 5.0 * p1 + 4.0 * p2 - p3) * t2 +
                (-p0 + 3.0 * p1 - 3.0 * p2 + p3) * t3
            )
    }
}
```

---

## `app/src/main/java/com/example/audio/AudioProcessor.kt`

```kotlin
package com.example.audio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import com.example.model.AudioTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Low-level audio I/O & DSP engine built around Android [AudioTrack] and [AudioRecord].
 * Handles local PCM/WAV playback, optional live microphone capture, and a pluggable
 * Float32 block [DspHook] chain.
 */
class AudioProcessor(
    private val scope: CoroutineScope,
    val sampleRate: Int = 48000,
    private val blockSize: Int = 1024
) {
    private val hooks = CopyOnWriteArrayList<DspHook>()
    private val crossfadeSamples = (0.015f * sampleRate).toInt().coerceAtLeast(1)

    @Volatile
    private var leftPcm: FloatArray = FloatArray(0)
    @Volatile
    private var rightPcm: FloatArray = FloatArray(0)
    @Volatile
    private var playCursor: Int = 0
    @Volatile
    private var isPlayingFlag: Boolean = false
    @Volatile
    private var isRecordingFlag: Boolean = false

    @Volatile
    private var targetWetGain: Float = 1f
    private var currentWetMix: Float = 1f

    private var playJob: Job? = null
    private var recordJob: Job? = null

    private val recordedSamples = ArrayList<Float>()
    private val recordLock = Any()

    private val perHookEmaMs = ConcurrentHashMap<String, Float>()
    @Volatile
    private var smoothedChainLatencyMs: Float = 0f
    private var blocksSinceSnapshotPublish: Int = 0

    private val _telemetry = MutableStateFlow(AudioTelemetry())
    val telemetry: StateFlow<AudioTelemetry> = _telemetry.asStateFlow()

    private val _hookSnapshots = MutableStateFlow<List<DspHookSnapshot>>(emptyList())
    val hookSnapshots: StateFlow<List<DspHookSnapshot>> = _hookSnapshots.asStateFlow()

    fun registerHook(hook: DspHook) {
        if (hooks.none { it.id == hook.id }) {
            hooks.add(hook)
            refreshLatencyTelemetry()
        }
    }

    fun unregisterHook(hookId: String) {
        hooks.removeAll { it.id == hookId }
        perHookEmaMs.remove(hookId)
        refreshLatencyTelemetry()
    }

    fun setHookEnabled(hookId: String, enabled: Boolean) {
        hooks.firstOrNull { it.id == hookId }?.let {
            it.isEnabled = enabled
            if (!enabled) perHookEmaMs[hookId] = 0f
            refreshLatencyTelemetry()
        }
    }

    fun clearHooks() {
        hooks.clear()
        perHookEmaMs.clear()
        smoothedChainLatencyMs = 0f
        refreshLatencyTelemetry()
    }

    fun setWetEnabled(enabled: Boolean) {
        targetWetGain = if (enabled) 1f else 0f
        refreshLatencyTelemetry()
    }

    fun refreshLatencyTelemetry() {
        val bypassed = targetWetGain <= 0.01f
        val effectiveLatency = when {
            bypassed -> 0f
            isPlayingFlag && smoothedChainLatencyMs > 0f -> smoothedChainLatencyMs
            else -> computeIdleBaselineLatencyMs()
        }
        _telemetry.value = _telemetry.value.copy(
            dspLatencyMs = effectiveLatency,
            isBypassed = bypassed
        )
        publishHookSnapshots()
    }

    fun loadBuffers(left: FloatArray, right: FloatArray, infoDurationMs: Long? = null, lufs: Float = -16f, peakDb: Float = -2f) {
        leftPcm = left
        rightPcm = right
        playCursor = 0
        hooks.forEach { it.reset() }
        val duration = infoDurationMs ?: ((left.size.toLong() * 1000L) / sampleRate)
        _telemetry.value = _telemetry.value.copy(
            durationMs = duration,
            positionMs = 0L,
            integratedLufs = lufs,
            truePeakDb = peakDb,
            effectiveSampleRate = sampleRate
        )
    }

    /**
     * Parses a local 16-bit LE PCM file (with optional RIFF WAV header) into Float32 stereo buffers
     * ready for [AudioTrack] playback.
     */
    fun loadLocalPcmFile(file: File, fallbackSampleRate: Int = 48000, fallbackChannels: Int = 2): DecodedAudio {
        val bytes = file.readBytes()
        val parsed = parsePcmBytes(bytes, fallbackSampleRate, fallbackChannels)
        val info = AudioDecoder.analyzeBuffer(
            id = "pcm_${file.name}",
            title = file.name,
            sourceLabel = "LOCAL PCM FILE",
            left = parsed.first,
            right = parsed.second,
            sampleRate = sampleRate,
            channels = 2,
            isBundled = false
        )
        loadBuffers(parsed.first, parsed.second, info.durationMs, info.integratedLufs, info.truePeakDb)
        return DecodedAudio(parsed.first, parsed.second, sampleRate, info)
    }

    fun processThroughHooks(left: FloatArray, right: FloatArray, frameCount: Int) {
        var totalNs = 0L
        var anyActive = false
        for (hook in hooks) {
            if (hook.isEnabled) {
                anyActive = true
                val startNs = System.nanoTime()
                hook.processBlock(left, right, frameCount, sampleRate)
                val elapsedNs = (System.nanoTime() - startNs).coerceAtLeast(1L)
                totalNs += elapsedNs

                val rawHookMs = (elapsedNs / 1_000_000f).coerceAtLeast(0.001f)
                val prevHook = perHookEmaMs[hook.id] ?: 0f
                perHookEmaMs[hook.id] = if (prevHook <= 0f) {
                    rawHookMs
                } else {
                    prevHook * (1f - LATENCY_EMA_ALPHA) + rawHookMs * LATENCY_EMA_ALPHA
                }
            } else {
                perHookEmaMs[hook.id] = 0f
            }
        }

        val blockLatencyMs = if (anyActive) (totalNs / 1_000_000f).coerceAtLeast(0.001f) else 0f
        smoothedChainLatencyMs = when {
            !anyActive -> 0f
            smoothedChainLatencyMs <= 0f -> blockLatencyMs
            else -> smoothedChainLatencyMs * (1f - LATENCY_EMA_ALPHA) + blockLatencyMs * LATENCY_EMA_ALPHA
        }

        val bypassed = targetWetGain <= 0.01f
        _telemetry.value = _telemetry.value.copy(
            dspLatencyMs = if (bypassed) 0f else smoothedChainLatencyMs,
            isBypassed = bypassed
        )

        blocksSinceSnapshotPublish++
        if (blocksSinceSnapshotPublish >= 8 || !isPlayingFlag) {
            blocksSinceSnapshotPublish = 0
            publishHookSnapshots()
        }
    }

    fun play() {
        if (isPlayingFlag || leftPcm.isEmpty()) return
        isPlayingFlag = true
        _telemetry.value = _telemetry.value.copy(isPlaying = true)

        playJob = scope.launch(Dispatchers.Default) {
            val minBufBytes = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(blockSize * 4)

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(minBufBytes * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            val dryL = FloatArray(blockSize)
            val dryR = FloatArray(blockSize)
            val wetL = FloatArray(blockSize)
            val wetR = FloatArray(blockSize)
            val pcmOut = ShortArray(blockSize * 2)
            val monoAnalysis = FloatArray(256)
            val scopePreview = FloatArray(96)

            runCatching { audioTrack.play() }

            try {
                while (isActive && isPlayingFlag) {
                    val total = leftPcm.size
                    if (total == 0) break

                    for (i in 0 until blockSize) {
                        val idx = (playCursor + i) % total
                        val dl = leftPcm[idx]
                        val dr = rightPcm[idx]
                        dryL[i] = dl
                        dryR[i] = dr
                        wetL[i] = dl
                        wetR[i] = dr
                    }

                    // Run real-time pluggable Float32 DSP hook chain in place
                    processThroughHooks(wetL, wetR, blockSize)

                    var sumSqL = 0f
                    var sumSqR = 0f
                    var peakBlock = 1e-6f
                    var midEnergy = 0f
                    var sideEnergy = 0f

                    for (i in 0 until blockSize) {
                        val step = 1f / crossfadeSamples.toFloat()
                        if (currentWetMix < targetWetGain) {
                            currentWetMix = (currentWetMix + step).coerceAtMost(1f)
                        } else if (currentWetMix > targetWetGain) {
                            currentWetMix = (currentWetMix - step).coerceAtLeast(0f)
                        }
                        val theta = (currentWetMix * PI * 0.5).toFloat()
                        val dGain = cos(theta)
                        val wGain = sin(theta)

                        val outL = (dryL[i] * dGain + wetL[i] * wGain).coerceIn(-1f, 1f)
                        val outR = (dryR[i] * dGain + wetR[i] * wGain).coerceIn(-1f, 1f)

                        pcmOut[i * 2] = (outL * 32767f).toInt().coerceIn(-32768, 32767).toShort()
                        pcmOut[i * 2 + 1] = (outR * 32767f).toInt().coerceIn(-32768, 32767).toShort()

                        sumSqL += outL * outL
                        sumSqR += outR * outR
                        val p = max(abs(outL), abs(outR))
                        if (p > peakBlock) peakBlock = p

                        val m = 0.5f * (outL + outR)
                        val s = 0.5f * (outL - outR)
                        midEnergy += m * m
                        sideEnergy += s * s

                        if (i < 256) monoAnalysis[i] = m
                        if (i < 96) scopePreview[i] = m
                    }

                    playCursor = (playCursor + blockSize) % total
                    audioTrack.write(pcmOut, 0, pcmOut.size)

                    emitTelemetryBlock(
                        monoAnalysis = monoAnalysis,
                        scopePreview = scopePreview,
                        sumSqL = sumSqL,
                        sumSqR = sumSqR,
                        peakBlock = peakBlock,
                        midEnergy = midEnergy,
                        sideEnergy = sideEnergy,
                        frameCount = blockSize,
                        positionMs = (playCursor.toLong() * 1000L) / sampleRate,
                        durationMs = (total.toLong() * 1000L) / sampleRate,
                        isPlaying = true
                    )
                }
            } finally {
                runCatching {
                    audioTrack.stop()
                    audioTrack.release()
                }
            }
        }
    }

    fun pause() {
        isPlayingFlag = false
        playJob?.cancel()
        playJob = null
        smoothedChainLatencyMs = 0f
        perHookEmaMs.clear()
        val bypassed = targetWetGain <= 0.01f
        _telemetry.value = _telemetry.value.copy(
            isPlaying = false,
            spectrumBins = List(64) { 0.04f },
            glyphZones = List(5) { 0.05f },
            dspLatencyMs = if (bypassed) 0f else computeIdleBaselineLatencyMs(),
            isBypassed = bypassed
        )
        publishHookSnapshots()
    }

    fun togglePlayPause() {
        if (isPlayingFlag) pause() else play()
    }

    fun seekToFraction(fraction: Float) {
        val total = leftPcm.size
        if (total == 0) return
        playCursor = (fraction.coerceIn(0f, 0.99f) * total).toInt()
        _telemetry.value = _telemetry.value.copy(
            positionMs = (playCursor.toLong() * 1000L) / sampleRate
        )
    }

    /**
     * Permission-guarded [AudioRecord] mic capture entry point.
     */
    fun startRecording(
        context: Context,
        onMeterUpdate: (levelNorm: Float, durationMs: Long) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!hasRecordAudioPermission(context)) {
            onError("RECORD_AUDIO permission not granted.")
            return
        }
        startRecording(onMeterUpdate, onError)
    }

    /**
     * Captures live PCM input via [AudioRecord] when RECORD_AUDIO permission is granted.
     * Emits live FFT & input level telemetry during capture.
     */
    @SuppressLint("MissingPermission")
    fun startRecording(
        onMeterUpdate: (levelNorm: Float, durationMs: Long) -> Unit,
        onError: (String) -> Unit
    ) {
        if (isRecordingFlag) return
        pause()

        val minBuf = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(blockSize * 2)

        val recorder = runCatching {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBuf * 2
            )
        }.getOrElse { err ->
            onError(err.message ?: "AudioRecord initialization failed.")
            return
        }

        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            onError("AudioRecord hardware unavailable.")
            return
        }

        synchronized(recordLock) {
            recordedSamples.clear()
        }

        isRecordingFlag = true
        recordJob = scope.launch(Dispatchers.IO) {
            val readBuf = ShortArray(blockSize)
            val floatL = FloatArray(blockSize)
            val floatR = FloatArray(blockSize)
            val monoAnalysis = FloatArray(256)
            val scopePreview = FloatArray(96)
            val maxRecordSamples = sampleRate * 60 // Up to 60s capture buffer

            try {
                recorder.startRecording()
                while (isActive && isRecordingFlag) {
                    val readCount = recorder.read(readBuf, 0, readBuf.size)
                    if (readCount <= 0) continue

                    var sumSq = 0f
                    var peak = 1e-6f
                    for (i in 0 until readCount) {
                        val s = readBuf[i].toFloat() / 32768f
                        floatL[i] = s
                        floatR[i] = s
                        sumSq += s * s
                        val a = abs(s)
                        if (a > peak) peak = a
                        if (i < 256) monoAnalysis[i] = s
                        if (i < 96) scopePreview[i] = s
                    }

                    // Preview through DSP hooks for live telemetry consistency
                    processThroughHooks(floatL, floatR, readCount)

                    var totalCaptured: Int
                    synchronized(recordLock) {
                        if (recordedSamples.size + readCount <= maxRecordSamples) {
                            for (i in 0 until readCount) {
                                recordedSamples.add(floatL[i])
                            }
                        }
                        totalCaptured = recordedSamples.size
                    }

                    val rms = sqrt((sumSq / readCount).coerceAtLeast(1e-12f))
                    val durMs = (totalCaptured.toLong() * 1000L) / sampleRate
                    onMeterUpdate(rms.coerceIn(0f, 1f), durMs)

                    emitTelemetryBlock(
                        monoAnalysis = monoAnalysis,
                        scopePreview = scopePreview,
                        sumSqL = sumSq,
                        sumSqR = sumSq,
                        peakBlock = peak,
                        midEnergy = sumSq,
                        sideEnergy = 0f,
                        frameCount = readCount,
                        positionMs = durMs,
                        durationMs = durMs,
                        isPlaying = false
                    )
                }
            } catch (sec: SecurityException) {
                onError(sec.message ?: "RECORD_AUDIO permission required.")
            } finally {
                runCatching {
                    recorder.stop()
                    recorder.release()
                }
                isRecordingFlag = false
            }
        }
    }

    fun stopRecording(): DecodedAudio? {
        isRecordingFlag = false
        recordJob?.cancel()
        recordJob = null

        val captured: FloatArray
        synchronized(recordLock) {
            if (recordedSamples.isEmpty()) return null
            captured = FloatArray(recordedSamples.size) { recordedSamples[it] }
            recordedSamples.clear()
        }

        val info = AudioDecoder.analyzeBuffer(
            id = "mic_${System.currentTimeMillis()}",
            title = "MIC_CAPTURE_${captured.size / sampleRate}S.wav",
            sourceLabel = "AUDIORECORD MIC",
            left = captured,
            right = captured.copyOf(),
            sampleRate = sampleRate,
            channels = 1,
            isBundled = false
        )
        val rightCopy = captured.copyOf()
        loadBuffers(captured, rightCopy, info.durationMs, info.integratedLufs, info.truePeakDb)
        return DecodedAudio(captured, rightCopy, sampleRate, info)
    }

    fun getCurrentBuffers(): Pair<FloatArray, FloatArray> = leftPcm to rightPcm

    fun release() {
        pause()
        isRecordingFlag = false
        recordJob?.cancel()
        recordJob = null
    }

    private fun computeIdleBaselineLatencyMs(): Float {
        var sum = 0f
        for (hook in hooks) {
            if (hook.isEnabled) {
                sum += hook.baselineLatencyMs
            }
        }
        return sum
    }

    private fun publishHookSnapshots() {
        val bypassed = targetWetGain <= 0.01f
        _hookSnapshots.value = hooks.map { hook ->
            val ms = when {
                bypassed || !hook.isEnabled -> 0f
                isPlayingFlag -> perHookEmaMs[hook.id] ?: hook.baselineLatencyMs
                else -> perHookEmaMs[hook.id]?.takeIf { it > 0f && smoothedChainLatencyMs > 0f }
                    ?: hook.baselineLatencyMs
            }
            DspHookSnapshot(hook.id, hook.name, hook.isEnabled, ms)
        }
    }

    private fun emitTelemetryBlock(
        monoAnalysis: FloatArray,
        scopePreview: FloatArray,
        sumSqL: Float,
        sumSqR: Float,
        peakBlock: Float,
        midEnergy: Float,
        sideEnergy: Float,
        frameCount: Int,
        positionMs: Long,
        durationMs: Long,
        isPlaying: Boolean
    ) {
        val bins64 = computeSpectrum64(monoAnalysis)
        val rmsL = sqrt((sumSqL / frameCount).coerceAtLeast(1e-12f))
        val rmsR = sqrt((sumSqR / frameCount).coerceAtLeast(1e-12f))
        val vuL = (20f * log10(rmsL.coerceAtLeast(1e-5f))).coerceIn(-60f, 0f)
        val vuR = (20f * log10(rmsR.coerceAtLeast(1e-5f))).coerceIn(-60f, 0f)
        val tpDb = (20f * log10(peakBlock.coerceAtLeast(1e-5f))).coerceIn(-60f, 0f)
        val lufs = (((vuL + vuR) * 0.5f) - 0.6f).coerceIn(-48f, -2f)
        val corr = ((midEnergy - sideEnergy) / (midEnergy + sideEnergy).coerceAtLeast(1e-6f))
            .coerceIn(-1f, 1f)

        val glyph = listOf(
            bins64.slice(0..7).average().toFloat().coerceIn(0.05f, 1f),
            bins64.slice(8..19).average().toFloat().coerceIn(0.05f, 1f),
            bins64.slice(20..35).average().toFloat().coerceIn(0.05f, 1f),
            bins64.slice(36..51).average().toFloat().coerceIn(0.05f, 1f),
            peakBlock.coerceIn(0.05f, 1f)
        )

        _telemetry.value = _telemetry.value.copy(
            isPlaying = isPlaying,
            positionMs = positionMs,
            durationMs = durationMs,
            spectrumBins = bins64,
            scopeSamples = scopePreview.toList(),
            leftVuDb = vuL,
            rightVuDb = vuR,
            integratedLufs = lufs,
            truePeakDb = tpDb,
            stereoCorrelation = corr,
            glyphZones = glyph
        )
    }

    private fun computeSpectrum64(samples: FloatArray): List<Float> {
        val n = 256
        val re = FloatArray(n)
        val im = FloatArray(n)
        for (i in 0 until n) {
            val hann = 0.5f * (1f - cos(2f * PI.toFloat() * i / (n - 1)))
            re[i] = samples[i] * hann
        }

        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while ((j and bit) != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j xor bit
            if (i < j) {
                val tr = re[i]; re[i] = re[j]; re[j] = tr
            }
        }

        var len = 2
        while (len <= n) {
            val ang = (-2.0 * PI / len).toFloat()
            val wLenRe = cos(ang)
            val wLenIm = sin(ang)
            for (i in 0 until n step len) {
                var wRe = 1f
                var wIm = 0f
                for (k in 0 until len / 2) {
                    val uRe = re[i + k]
                    val uIm = im[i + k]
                    val vRe = re[i + k + len / 2] * wRe - im[i + k + len / 2] * wIm
                    val vIm = re[i + k + len / 2] * wIm + im[i + k + len / 2] * wRe
                    re[i + k] = uRe + vRe
                    im[i + k] = uIm + vIm
                    re[i + k + len / 2] = uRe - vRe
                    im[i + k + len / 2] = uIm - vIm
                    val nextWRe = wRe * wLenRe - wIm * wLenIm
                    wIm = wRe * wLenIm + wIm * wLenRe
                    wRe = nextWRe
                }
            }
            len = len shl 1
        }

        val half = n / 2
        return List(64) { bar ->
            val binIdx = (1f * (half - 1).toFloat().pow(bar / 63f)).toInt().coerceIn(1, half - 1)
            val mag = sqrt(re[binIdx] * re[binIdx] + im[binIdx] * im[binIdx]) / (n * 0.25f)
            ((20f * log10(mag.coerceAtLeast(1e-4f)) + 52f) / 52f).coerceIn(0.04f, 1f)
        }
    }

    companion object {
        private const val LATENCY_EMA_ALPHA = 0.22f

        fun hasRecordAudioPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun parsePcmBytes(
            bytes: ByteArray,
            fallbackSampleRate: Int = 48000,
            fallbackChannels: Int = 2
        ): Pair<FloatArray, FloatArray> {
            val isRiff = bytes.size >= 44 &&
                bytes[0] == 'R'.code.toByte() &&
                bytes[1] == 'I'.code.toByte() &&
                bytes[2] == 'F'.code.toByte() &&
                bytes[3] == 'F'.code.toByte()

            var dataOffset = 0
            var channels = fallbackChannels.coerceIn(1, 2)
            var bitDepth = 16
            var audioFormat = 1

            if (isRiff) {
                val bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
                audioFormat = bb.getShort(20).toInt() and 0xFFFF
                channels = bb.getShort(22).toInt().coerceIn(1, 2)
                bitDepth = bb.getShort(34).toInt().coerceAtLeast(16)
                dataOffset = 44
            }

            val payloadSize = (bytes.size - dataOffset).coerceAtLeast(0)
            val bytesPerSample = when (bitDepth) {
                32 -> 4
                24 -> 3
                else -> 2
            }
            val frameCount = (payloadSize / (channels * bytesPerSample)).coerceAtLeast(1)
            val left = FloatArray(frameCount)
            val right = FloatArray(frameCount)

            var ptr = dataOffset
            for (f in 0 until frameCount) {
                val l = readNormalizedSample(bytes, ptr, bytesPerSample, audioFormat)
                ptr += bytesPerSample
                val r = if (channels >= 2 && ptr + bytesPerSample <= bytes.size) {
                    val v = readNormalizedSample(bytes, ptr, bytesPerSample, audioFormat)
                    ptr += bytesPerSample
                    v
                } else {
                    l
                }
                left[f] = l
                right[f] = r
            }
            return left to right
        }

        private fun readNormalizedSample(
            bytes: ByteArray,
            offset: Int,
            bytesPerSample: Int,
            audioFormat: Int = 1
        ): Float {
            if (offset + bytesPerSample > bytes.size) return 0f
            return when (bytesPerSample) {
                4 -> {
                    val b0 = bytes[offset].toInt() and 0xFF
                    val b1 = bytes[offset + 1].toInt() and 0xFF
                    val b2 = bytes[offset + 2].toInt() and 0xFF
                    val b3 = bytes[offset + 3].toInt() and 0xFF
                    val bits = b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)
                    if (audioFormat == 3) {
                        java.lang.Float.intBitsToFloat(bits).let { if (it.isFinite()) it.coerceIn(-1f, 1f) else 0f }
                    } else {
                        (bits.toFloat() / 2147483648f).coerceIn(-1f, 1f)
                    }
                }
                3 -> {
                    val b0 = bytes[offset].toInt() and 0xFF
                    val b1 = bytes[offset + 1].toInt() and 0xFF
                    val b2 = bytes[offset + 2].toInt() // signed MSB
                    val raw = (b2 shl 16) or (b1 shl 8) or b0
                    (raw.toFloat() / 8388608f).coerceIn(-1f, 1f)
                }
                else -> {
                    val lo = bytes[offset].toInt() and 0xFF
                    val hi = bytes[offset + 1].toInt()
                    val s = ((hi shl 8) or lo).toShort()
                    (s.toFloat() / 32768f).coerceIn(-1f, 1f)
                }
            }
        }
    }
}
```

---

## `app/src/main/java/com/example/audio/BiquadFilter.kt`

```kotlin
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
```

---

## `app/src/main/java/com/example/audio/DspHook.kt`

```kotlin
package com.example.audio

import androidx.compose.runtime.Immutable
import com.example.model.EqBand
import com.example.model.OversamplingFactor
import com.example.model.StemChannelState
import com.example.model.StemMixerState
import com.example.model.TactileDeckParams
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.tanh

/**
 * Pluggable real-time 64-bit Double internal DSP hook executed sequentially by [AudioProcessor].
 */
interface DspHook {
    val id: String
    val name: String
    var isEnabled: Boolean
    val baselineLatencyMs: Float
        get() = 0.08f

    fun processBlock(
        left: FloatArray,
        right: FloatArray,
        frameCount: Int,
        sampleRate: Int
    )

    fun reset()
}

@Immutable
data class DspHookSnapshot(
    val id: String,
    val name: String,
    val isEnabled: Boolean,
    val latencyMs: Float = 0f
)

class GainTrimHook(
    override val id: String = "gain_trim",
    override val name: String = "Gain Trim Stage",
    override var isEnabled: Boolean = true,
    @Volatile var gainLinear: Float = 1.0f
) : DspHook {
    override val baselineLatencyMs: Float
        get() = 0.03f
    override fun processBlock(
        left: FloatArray,
        right: FloatArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        val g = gainLinear.toDouble()
        if (g == 1.0) return
        for (i in 0 until frameCount) {
            left[i] = (left[i].toDouble() * g).toFloat()
            right[i] = (right[i].toDouble() * g).toFloat()
        }
    }

    override fun reset() = Unit
}

class BiquadEqHook(
    override val id: String = "biquad_eq_5x",
    override val name: String = "5-Band 64-Bit Biquad EQ",
    override var isEnabled: Boolean = true
) : DspHook {
    override val baselineLatencyMs: Float
        get() = 0.20f
    private val filters = List(5) { BiquadFilter(48000.0) }

    fun configure(bands: List<EqBand>, sampleRate: Int) {
        val sr = sampleRate.toFloat()
        bands.forEachIndexed { idx, band ->
            if (idx < filters.size) {
                val type = when (idx) {
                    0 -> FilterType.LOW_SHELF
                    4 -> FilterType.HIGH_SHELF
                    else -> FilterType.PEAKING
                }
                filters[idx].configure(type, band.frequencyHz, band.qFactor, band.gainDb, sr)
            }
        }
    }

    override fun processBlock(
        left: FloatArray,
        right: FloatArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        for (i in 0 until frameCount) {
            var l = left[i].toDouble()
            var r = right[i].toDouble()
            for (f in filters) {
                l = f.processLeftDouble(l)
                r = f.processRightDouble(r)
            }
            left[i] = l.toFloat()
            right[i] = r.toFloat()
        }
    }

    override fun reset() {
        filters.forEach { it.reset() }
    }
}

class TactileDriveHook(
    override val id: String = "tactile_drive",
    override val name: String = "64-Bit Harmonic Drive & M/S Width",
    override var isEnabled: Boolean = true
) : DspHook {
    override val baselineLatencyMs: Float
        get() = 0.26f
    @Volatile
    private var params: TactileDeckParams = TactileDeckParams()
    private val airFilter = BiquadFilter(48000.0)
    private val subBassFilter = BiquadFilter(48000.0)
    private var envFollower = 0.0

    init {
        subBassFilter.configure(FilterType.LOW_PASS, 120f, 0.707f, 0f, 48000f)
    }

    fun configure(deck: TactileDeckParams, sampleRate: Int) {
        params = deck
        val sr = sampleRate.toFloat()
        airFilter.configure(
            FilterType.HIGH_SHELF,
            10000f,
            0.707f,
            (deck.air - 0.35f) * 9f,
            sr
        )
        subBassFilter.configure(FilterType.LOW_PASS, 120f, 0.707f, 0f, sr)
    }

    override fun processBlock(
        left: FloatArray,
        right: FloatArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        val p = params
        val driveGain = 1.0 + p.drive.toDouble() * 2.2
        val driveNorm = if (p.drive > 0.02f) tanh(driveGain) else 1.0
        val widthScale = 0.45 + p.width.toDouble() * 1.35

        for (i in 0 until frameCount) {
            var l = left[i].toDouble()
            var r = right[i].toDouble()

            if (p.drive > 0.02f) {
                l = tanh(l * driveGain) / driveNorm
                r = tanh(r * driveGain) / driveNorm
            }

            val absIn = 0.5 * (abs(l) + abs(r))
            envFollower = envFollower * 0.992 + absIn * 0.008
            val transientDelta = (absIn - envFollower).coerceAtLeast(0.0)
            val punchBoost = 1.0 + transientDelta * p.punch.toDouble() * 1.4
            l *= punchBoost
            r *= punchBoost

            l = airFilter.processLeftDouble(l)
            r = airFilter.processRightDouble(r)

            // Mono-tighten sub-bass (<120Hz) while expanding upper M/S stereo field
            val lowL = subBassFilter.processLeftDouble(l)
            val lowR = subBassFilter.processRightDouble(r)
            val lowMono = 0.5 * (lowL + lowR)

            val hiL = l - lowL
            val hiR = r - lowR
            val midHi = 0.5 * (hiL + hiR)
            val sideHi = 0.5 * (hiL - hiR) * widthScale

            left[i] = (lowMono + midHi + sideHi).toFloat()
            right[i] = (lowMono + midHi - sideHi).toFloat()
        }
    }

    override fun reset() {
        airFilter.reset()
        subBassFilter.reset()
        envFollower = 0.0
    }
}

class StemMatrixHook(
    override val id: String = "stem_matrix_4x",
    override val name: String = "4-Way Stem Crossover",
    override var isEnabled: Boolean = true
) : DspHook {
    override val baselineLatencyMs: Float
        get() = 0.36f
    @Volatile
    private var stemState: StemMixerState = StemMixerState()
    @Volatile
    private var forceActive: Boolean = false

    private val bassLowPass = BiquadFilter(48000.0)
    private val vocalBandPass = BiquadFilter(48000.0)
    private val drumHighPass = BiquadFilter(48000.0)
    private val otherHighPass = BiquadFilter(48000.0)

    init {
        configureCrossovers(48000)
    }

    fun configureCrossovers(sampleRate: Int) {
        val sr = sampleRate.toFloat()
        bassLowPass.configure(FilterType.LOW_PASS, 220f, 0.707f, 0f, sr)
        vocalBandPass.configure(FilterType.BAND_PASS, 1850f, 0.85f, 0f, sr)
        drumHighPass.configure(FilterType.HIGH_PASS, 180f, 0.707f, 0f, sr)
        otherHighPass.configure(FilterType.HIGH_PASS, 5200f, 0.707f, 0f, sr)
    }

    fun updateState(stems: StemMixerState, isStemScreen: Boolean) {
        stemState = stems
        forceActive = isStemScreen
    }

    override fun processBlock(
        left: FloatArray,
        right: FloatArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        val stems = stemState
        val anySolo = stems.asList().any { it.soloed }
        val modified = forceActive || anySolo || stems.asList().any {
            it.muted || abs(it.level - 1f) > 0.03f
        }
        if (!modified) return

        fun effGain(ch: StemChannelState): Double {
            if (ch.muted) return 0.0
            if (anySolo && !ch.soloed) return 0.0
            return ch.level.toDouble()
        }

        val gBass = effGain(stems.bass)
        val gVocal = effGain(stems.vocals)
        val gDrums = effGain(stems.drums)
        val gOther = effGain(stems.other)

        for (i in 0 until frameCount) {
            val l = left[i].toDouble()
            val r = right[i].toDouble()
            val mid = 0.5 * (l + r)
            val side = 0.5 * (l - r)

            val bassL = bassLowPass.processLeftDouble(l)
            val bassR = bassLowPass.processRightDouble(r)
            val vocalM = vocalBandPass.processLeftDouble(mid)
            val drumL = drumHighPass.processLeftDouble(l) * 0.65
            val drumR = drumHighPass.processRightDouble(r) * 0.65
            val otherL = otherHighPass.processLeftDouble(l) + side * 0.75
            val otherR = otherHighPass.processRightDouble(r) - side * 0.75

            left[i] = (bassL * gBass + vocalM * gVocal + drumL * gDrums + otherL * gOther).toFloat()
            right[i] = (bassR * gBass + vocalM * gVocal + drumR * gDrums + otherR * gOther).toFloat()
        }
    }

    override fun reset() {
        bassLowPass.reset()
        vocalBandPass.reset()
        drumHighPass.reset()
        otherHighPass.reset()
    }
}

/**
 * 64-Bit Spectral Band Replication (SBR) & Studio M/S Stereo Shimmer Expander.
 * Synthesizes coherent 2nd/3rd/5th upper harmonics (>4.5kHz) for compressed audio
 * and adds phase-coherent upper-band stereo width while keeping center vocals locked.
 */
class HiResUpsamplingHook(
    override val id: String = "hi_res_upsampler",
    override val name: String = "SBR HF Restoration & Stereo Expander",
    override var isEnabled: Boolean = true,
    @Volatile var restorationAmount: Float = 0.65f,
    @Volatile var targetKhz: Int = 96,
    @Volatile var stereoExpansionAmount: Float = 0.48f
) : DspHook {
    override val baselineLatencyMs: Float
        get() = if (targetKhz >= 192) 0.52f else 0.36f
    private val bandSplitHp = BiquadFilter(48000.0)
    private val harmonicExciterBp = BiquadFilter(48000.0)
    private val airShelf = BiquadFilter(48000.0)
    private val sideShimmerHp = BiquadFilter(48000.0)

    init {
        configure(48000)
    }

    fun configure(sampleRate: Int) {
        val sr = sampleRate.toFloat()
        bandSplitHp.configure(FilterType.HIGH_PASS, 4500f, 0.707f, 0f, sr)
        harmonicExciterBp.configure(FilterType.BAND_PASS, 6400f, 0.9f, 0f, sr)
        airShelf.configure(FilterType.HIGH_SHELF, 11800f, 0.707f, 3.2f, sr)
        sideShimmerHp.configure(FilterType.HIGH_PASS, 5400f, 0.707f, 0f, sr)
    }

    override fun processBlock(
        left: FloatArray,
        right: FloatArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        val amt = restorationAmount.toDouble().coerceIn(0.0, 1.0)
        if (amt <= 0.01) return
        val khzFactor = if (targetKhz >= 192) 1.24 else 1.0
        val blend = 0.16 * amt * khzFactor
        val stereoWidthBoost = stereoExpansionAmount.toDouble().coerceIn(0.0, 1.0) * amt * 0.32

        for (i in 0 until frameCount) {
            val l = left[i].toDouble()
            val r = right[i].toDouble()
            val hiL = bandSplitHp.processLeftDouble(l)
            val hiR = bandSplitHp.processRightDouble(r)

            val excL = harmonicExciterBp.processLeftDouble(hiL)
            val excR = harmonicExciterBp.processRightDouble(hiR)

            // Coherent 2nd (even warmth), 3rd (odd air), and 5th harmonic SBR synthesis
            val harmL = (
                excL * abs(excL) * 2.1 +
                    (excL * excL * 0.65) +
                    (excL * excL * excL * 0.45)
                ).coerceIn(-0.28, 0.28)
            val harmR = (
                excR * abs(excR) * 2.1 +
                    (excR * excR * 0.65) +
                    (excR * excR * excR * 0.45)
                ).coerceIn(-0.28, 0.28)

            val restoredL = airShelf.processLeftDouble(l + harmL * blend)
            val restoredR = airShelf.processRightDouble(r + harmR * blend)

            // Subtle upper-band M/S stereo shimmer keeps low-end & center lead mono-tight
            val mid = 0.5 * (restoredL + restoredR)
            val side = 0.5 * (restoredL - restoredR)
            val hiSide = sideShimmerHp.processLeftDouble(side)
            val expandedSide = side + hiSide * stereoWidthBoost

            left[i] = (mid + expandedSide).toFloat()
            right[i] = (mid - expandedSide).toFloat()
        }
    }

    override fun reset() {
        bandSplitHp.reset()
        harmonicExciterBp.reset()
        airShelf.reset()
        sideShimmerHp.reset()
    }
}

/**
 * 64-Bit Multi-Band (3-Band) True-Peak Oversampled Limiter.
 * Splits Low (<250Hz), Mid (250Hz-4kHz), and High (>4kHz) bands via complementary
 * 64-bit filters so heavy sub-bass hits never pump vocals/air, then enforces a
 * 4x/8x Catmull-Rom cubic Inter-Sample Peak brickwall safety stage.
 */
class TruePeakLimiterHook(
    override val id: String = "true_peak_limiter",
    override val name: String = "3-Band True-Peak ISP Limiter",
    override var isEnabled: Boolean = true,
    @Volatile var ceilingDb: Float = -1.0f,
    @Volatile var oversamplingFactor: OversamplingFactor = OversamplingFactor.X4
) : DspHook {
    override val baselineLatencyMs: Float
        get() = if (oversamplingFactor == OversamplingFactor.X8) 0.64f else 0.42f

    private val lowSplitFilter = BiquadFilter(48000.0)
    private val highSplitFilter = BiquadFilter(48000.0)

    private var lowEnv = 1.0
    private var midEnv = 1.0
    private var highEnv = 1.0
    private var masterEnv = 1.0

    private var l0 = 0.0; private var l1 = 0.0; private var l2 = 0.0
    private var r0 = 0.0; private var r1 = 0.0; private var r2 = 0.0
    private var configuredSampleRate = 48000

    @Volatile
    var lastGainReductionDb: Float = 0f
        private set

    @Volatile
    var lastLowBandGrDb: Float = 0f
        private set

    @Volatile
    var lastMidBandGrDb: Float = 0f
        private set

    @Volatile
    var lastHighBandGrDb: Float = 0f
        private set

    init {
        configureCrossovers(48000)
    }

    private fun configureCrossovers(sampleRate: Int) {
        configuredSampleRate = sampleRate
        val sr = sampleRate.toFloat()
        lowSplitFilter.configure(FilterType.LOW_PASS, 250f, 0.707f, 0f, sr)
        highSplitFilter.configure(FilterType.HIGH_PASS, 4000f, 0.707f, 0f, sr)
    }

    override fun processBlock(
        left: FloatArray,
        right: FloatArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        if (frameCount <= 0) return
        if (sampleRate != configuredSampleRate) {
            configureCrossovers(sampleRate)
        }

        val ceilingAmp = 10.0.pow(ceilingDb.toDouble() / 20.0).coerceIn(0.25, 0.99)
        val osFactor = oversamplingFactor.factor

        val lowThreshold = ceilingAmp * 0.90
        val midThreshold = ceilingAmp * 0.94
        val highThreshold = ceilingAmp * 0.92

        var minLowGain = 1.0
        var minMidGain = 1.0
        var minHighGain = 1.0
        var minMasterGain = 1.0

        for (i in 0 until frameCount) {
            val inL = left[i].toDouble()
            val inR = right[i].toDouble()

            // Complementary 64-bit 3-band crossover (low + mid + high == input when gain=1)
            val lowL = lowSplitFilter.processLeftDouble(inL)
            val lowR = lowSplitFilter.processRightDouble(inR)
            val remL = inL - lowL
            val remR = inR - lowR
            val highL = highSplitFilter.processLeftDouble(remL)
            val highR = highSplitFilter.processRightDouble(remR)
            val midL = remL - highL
            val midR = remR - highR

            val peakLow = max(abs(lowL), abs(lowR))
            val neededLow = if (peakLow > lowThreshold) lowThreshold / peakLow else 1.0
            lowEnv = if (neededLow < lowEnv) neededLow else (lowEnv * 0.9975 + neededLow * 0.0025).coerceAtMost(1.0)
            if (lowEnv < minLowGain) minLowGain = lowEnv

            val peakMid = max(abs(midL), abs(midR))
            val neededMid = if (peakMid > midThreshold) midThreshold / peakMid else 1.0
            midEnv = if (neededMid < midEnv) neededMid else (midEnv * 0.9945 + neededMid * 0.0055).coerceAtMost(1.0)
            if (midEnv < minMidGain) minMidGain = midEnv

            val peakHigh = max(abs(highL), abs(highR))
            val neededHigh = if (peakHigh > highThreshold) highThreshold / peakHigh else 1.0
            highEnv = if (neededHigh < highEnv) neededHigh else (highEnv * 0.991 + neededHigh * 0.009).coerceAtMost(1.0)
            if (highEnv < minHighGain) minHighGain = highEnv

            val l3 = lowL * lowEnv + midL * midEnv + highL * highEnv
            val r3 = lowR * lowEnv + midR * midEnv + highR * highEnv

            // Evaluate 4x or 8x Catmull-Rom cubic inter-sample trajectory to catch ISPs
            var maxIsp = max(abs(l2), abs(r2))
            for (step in 1 until osFactor) {
                val t = step.toDouble() / osFactor.toDouble()
                val ispL = abs(catmullRom(l0, l1, l2, l3, t))
                val ispR = abs(catmullRom(r0, r1, r2, r3, t))
                if (ispL > maxIsp) maxIsp = ispL
                if (ispR > maxIsp) maxIsp = ispR
            }

            val neededMaster = if (maxIsp > ceilingAmp) ceilingAmp / maxIsp else 1.0
            masterEnv = if (neededMaster < masterEnv) {
                neededMaster
            } else {
                (masterEnv * 0.994 + neededMaster * 0.006).coerceAtMost(1.0)
            }
            if (masterEnv < minMasterGain) minMasterGain = masterEnv

            val safeL = tanh((l2 * masterEnv) / ceilingAmp) * ceilingAmp
            val safeR = tanh((r2 * masterEnv) / ceilingAmp) * ceilingAmp
            left[i] = safeL.coerceIn(-ceilingAmp, ceilingAmp).toFloat()
            right[i] = safeR.coerceIn(-ceilingAmp, ceilingAmp).toFloat()

            l0 = l1; l1 = l2; l2 = l3
            r0 = r1; r1 = r2; r2 = r3
        }

        lastLowBandGrDb = (-20.0 * log10(minLowGain.coerceAtLeast(1e-4))).toFloat().coerceIn(0f, 18f)
        lastMidBandGrDb = (-20.0 * log10(minMidGain.coerceAtLeast(1e-4))).toFloat().coerceIn(0f, 18f)
        lastHighBandGrDb = (-20.0 * log10(minHighGain.coerceAtLeast(1e-4))).toFloat().coerceIn(0f, 18f)
        val totalMin = minMasterGain * minOf(minLowGain, minMidGain, minHighGain)
        lastGainReductionDb = (-20.0 * log10(totalMin.coerceAtLeast(1e-4))).toFloat().coerceIn(0f, 18f)
    }

    private fun catmullRom(p0: Double, p1: Double, p2: Double, p3: Double, t: Double): Double {
        val t2 = t * t
        val t3 = t2 * t
        return 0.5 * (
            (2.0 * p1) +
                (-p0 + p2) * t +
                (2.0 * p0 - 5.0 * p1 + 4.0 * p2 - p3) * t2 +
                (-p0 + 3.0 * p1 - 3.0 * p2 + p3) * t3
            )
    }

    override fun reset() {
        lowSplitFilter.reset()
        highSplitFilter.reset()
        lowEnv = 1.0
        midEnv = 1.0
        highEnv = 1.0
        masterEnv = 1.0
        l0 = 0.0; l1 = 0.0; l2 = 0.0
        r0 = 0.0; r1 = 0.0; r2 = 0.0
        lastGainReductionDb = 0f
        lastLowBandGrDb = 0f
        lastMidBandGrDb = 0f
        lastHighBandGrDb = 0f
    }
}
```

---

## `app/src/main/java/com/example/audio/GlyphVUMapper.kt`

```kotlin
package com.example.audio

import androidx.compose.runtime.Immutable
import com.example.model.GlyphPatternMode
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.roundToInt

enum class GlyphPulseType(val label: String, val periodMs: Int) {
    IDLE_HEARTBEAT("IDLE HEARTBEAT", 950),
    RMS_BREATHE_WAVE("RMS BREATHE", 160),
    TRANSIENT_EDGE_STROBE("TRANSIENT STROBE", 55),
    GR_CLAMP_PULSE("GR CLAMP PULSE", 35)
}

@Immutable
data class GlyphZonePulse(
    val zoneId: String,
    val label: String,
    val intensity0To100: Int,
    val pulsePeriodMs: Int,
    val activePulse: Boolean
)

@Immutable
data class GlyphPulsePacket(
    val rmsPercent: Int,
    val rmsLeftPercent: Int,
    val rmsRightPercent: Int,
    val gainReductionPercent: Int,
    val pulseType: GlyphPulseType,
    val zones: List<GlyphZonePulse>
)

/**
 * Stateful visualizer mapper that normalizes real-time DSP RMS loudness and True-Peak
 * Gain Reduction values into calibrated 0-100 envelopes using fast-attack / exponential-decay
 * ballistics, then maps them onto the 5-zone Nothing Phone rear chassis Glyph LED topology.
 */
class GlyphVUMapper(
    val rmsFloorDb: Float = DEFAULT_FLOOR_DB,
    val rmsCeilDb: Float = DEFAULT_CEIL_DB,
    val maxGrDb: Float = DEFAULT_MAX_GR_DB,
    attackAlpha: Float = DEFAULT_ATTACK_ALPHA,
    rmsDecayAlpha: Float = DEFAULT_RMS_DECAY_ALPHA,
    grDecayAlpha: Float = DEFAULT_GR_DECAY_ALPHA
) {
    private val attackCoeff = attackAlpha.coerceIn(0.05f, 1.0f)
    private val rmsDecayCoeff = rmsDecayAlpha.coerceIn(0.02f, 1.0f)
    private val grDecayCoeff = grDecayAlpha.coerceIn(0.02f, 1.0f)

    private val stateLock = Any()
    private var smoothedLeftRms: Float = 0f
    private var smoothedRightRms: Float = 0f
    private var smoothedGr: Float = 0f
    private var smoothedGate: Float = 0f

    fun normalizeRmsDb(rmsDb: Float): Int =
        Companion.normalizeRmsDb(rmsDb, floorDb = rmsFloorDb, ceilDb = rmsCeilDb)

    fun normalizeRmsLinear(rmsLinear: Float): Int =
        Companion.normalizeRmsLinear(rmsLinear, floorDb = rmsFloorDb, ceilDb = rmsCeilDb)

    fun normalizeGainReductionDb(gainReductionDb: Float): Int =
        Companion.normalizeGainReductionDb(gainReductionDb, maxGrDb = maxGrDb)

    fun reset() {
        synchronized(stateLock) {
            smoothedLeftRms = 0f
            smoothedRightRms = 0f
            smoothedGr = 0f
            smoothedGate = 0f
        }
    }

    fun mapToGlyphPulsePattern(
        rmsLeftDb: Float,
        rmsRightDb: Float,
        gainReductionDb: Float,
        neuralGatePct: Float = 0f,
        patternMode: GlyphPatternMode = GlyphPatternMode.STEREO_VU_BRIDGE
    ): GlyphPulsePacket {
        val targetL = normalizeRmsDb(rmsLeftDb).toFloat()
        val targetR = normalizeRmsDb(rmsRightDb).toFloat()
        val targetGr = normalizeGainReductionDb(gainReductionDb).toFloat()
        val targetGate = if (neuralGatePct.isFinite()) neuralGatePct.coerceIn(0f, 100f) else 0f

        val rmsL: Int
        val rmsR: Int
        val grPct: Int
        val gatePct: Int

        synchronized(stateLock) {
            smoothedLeftRms = applyBallistics(targetL, smoothedLeftRms, rmsDecayCoeff)
            smoothedRightRms = applyBallistics(targetR, smoothedRightRms, rmsDecayCoeff)
            smoothedGr = applyBallistics(targetGr, smoothedGr, grDecayCoeff)
            smoothedGate = applyBallistics(targetGate, smoothedGate, grDecayCoeff)

            rmsL = smoothedLeftRms.roundToInt().coerceIn(0, 100)
            rmsR = smoothedRightRms.roundToInt().coerceIn(0, 100)
            grPct = smoothedGr.roundToInt().coerceIn(0, 100)
            gatePct = smoothedGate.roundToInt().coerceIn(0, 100)
        }

        val rmsAvg = ((rmsL + rmsR) * 0.5f).roundToInt().coerceIn(0, 100)
        val combinedGr = maxOf(grPct, (gatePct * 0.75f).roundToInt()).coerceIn(0, 100)

        val pulseType = when {
            grPct >= 25 -> GlyphPulseType.GR_CLAMP_PULSE
            rmsAvg >= 78 -> GlyphPulseType.TRANSIENT_EDGE_STROBE
            rmsAvg >= 10 || gatePct >= 10 || grPct >= 8 -> GlyphPulseType.RMS_BREATHE_WAVE
            else -> GlyphPulseType.IDLE_HEARTBEAT
        }

        // 5-zone Nothing Phone layout: C-Ring (C1) stays anchored to RMS loudness,
        // USB-C Exclamation Bar (D1) stays anchored to limiter Gain Reduction clamp.
        val (a1Level, a2Level, c1Level, c2Level, d1Level) = when (patternMode) {
            GlyphPatternMode.STEREO_VU_BRIDGE -> listOf(
                rmsL,
                rmsR,
                rmsAvg,
                ((rmsAvg * 0.85f) + (gatePct * 0.15f)).roundToInt().coerceIn(0, 100),
                grPct
            )
            GlyphPatternMode.GAIN_REDUCTION_CLAMP -> listOf(
                gatePct,
                grPct,
                rmsAvg,
                ((100 - abs(rmsL - rmsR)) * (rmsAvg / 100f)).roundToInt().coerceIn(0, 100),
                combinedGr
            )
            GlyphPatternMode.GLYPH_SPECTRUM_MATRIX -> listOf(
                maxOf(rmsL, gatePct / 2),
                maxOf(rmsR, grPct / 2),
                rmsAvg,
                ((rmsAvg * 0.65f) + (combinedGr * 0.35f)).roundToInt().coerceIn(0, 100),
                maxOf(grPct, combinedGr / 2)
            )
        }

        val zones = listOf(
            GlyphZonePulse("ZONE_A1", "CAMERA PILL", a1Level, pulseType.periodMs, a1Level >= 12),
            GlyphZonePulse("ZONE_A2", "DIAGONAL STRIP", a2Level, pulseType.periodMs, a2Level >= 12),
            GlyphZonePulse("ZONE_C1", "COIL C-RING", c1Level, pulseType.periodMs, c1Level >= 10),
            GlyphZonePulse("ZONE_C2", "VOCAL CORE", c2Level, pulseType.periodMs * 2, c2Level >= 10),
            GlyphZonePulse("ZONE_D1", "USB-C CLAMP BAR", d1Level, GlyphPulseType.GR_CLAMP_PULSE.periodMs, d1Level >= 10)
        )

        return GlyphPulsePacket(
            rmsPercent = rmsAvg,
            rmsLeftPercent = rmsL,
            rmsRightPercent = rmsR,
            gainReductionPercent = grPct,
            pulseType = pulseType,
            zones = zones
        )
    }

    private fun applyBallistics(target: Float, current: Float, decayAlpha: Float): Float {
        val alpha = if (target >= current) attackCoeff else decayAlpha
        val next = current + alpha * (target - current)
        return if (target == 0f && next < 0.5f) 0f else next.coerceIn(0f, 100f)
    }

    companion object {
        const val DEFAULT_FLOOR_DB = -48f
        const val DEFAULT_CEIL_DB = -3f
        const val DEFAULT_MAX_GR_DB = 6.0f
        const val DEFAULT_ATTACK_ALPHA = 0.90f
        const val DEFAULT_RMS_DECAY_ALPHA = 0.24f
        const val DEFAULT_GR_DECAY_ALPHA = 0.18f

        fun normalizeRmsDb(
            rmsDb: Float,
            floorDb: Float = DEFAULT_FLOOR_DB,
            ceilDb: Float = DEFAULT_CEIL_DB
        ): Int {
            if (!rmsDb.isFinite()) return 0
            val span = (ceilDb - floorDb).coerceAtLeast(1f)
            val norm = ((rmsDb - floorDb) / span).coerceIn(0f, 1f)
            return (norm * 100f).roundToInt().coerceIn(0, 100)
        }

        fun normalizeRmsLinear(
            rmsLinear: Float,
            floorDb: Float = DEFAULT_FLOOR_DB,
            ceilDb: Float = DEFAULT_CEIL_DB
        ): Int {
            if (!rmsLinear.isFinite() || rmsLinear <= 1e-6f) return 0
            val db = 20f * log10(rmsLinear.coerceAtLeast(1e-6f))
            return normalizeRmsDb(db, floorDb = floorDb, ceilDb = ceilDb)
        }

        fun normalizeGainReductionDb(
            gainReductionDb: Float,
            maxGrDb: Float = DEFAULT_MAX_GR_DB
        ): Int {
            if (!gainReductionDb.isFinite() || gainReductionDb <= 0f) return 0
            val norm = (gainReductionDb / maxGrDb.coerceAtLeast(0.5f)).coerceIn(0f, 1f)
            return (norm * 100f).roundToInt().coerceIn(0, 100)
        }
    }
}
```

---

## `app/src/main/java/com/example/audio/GlyphVuTelemetry.kt`

```kotlin
package com.example.audio

import androidx.compose.runtime.Immutable
import com.example.model.GlyphHardwareFrame
import com.example.model.GlyphPatternMode

/**
 * Hardware telemetry interface mapping real-time Float32 DSP gain reduction
 * and stereo RMS loudness levels into Nothing OS rear-chassis Glyph LED PWM patterns.
 */
interface GlyphVuInterface {
    fun mapToHardwareFrame(
        rmsLeftDb: Float,
        rmsRightDb: Float,
        limiterGainReductionDb: Float,
        neuralGateReductionPct: Float,
        peakBlock: Float,
        bins64: List<Float>,
        patternMode: GlyphPatternMode
    ): GlyphHardwareFrame

    fun reset() {}
}

@Immutable
data class GlyphZoneReadout(
    val code: String,
    val label: String,
    val intensity: Float,
    val pwm12Bit: Int
)

class DefaultGlyphVuController(
    private val glyphMapper: GlyphVUMapper = GlyphVUMapper()
) : GlyphVuInterface {

    override fun reset() {
        glyphMapper.reset()
    }

    override fun mapToHardwareFrame(
        rmsLeftDb: Float,
        rmsRightDb: Float,
        limiterGainReductionDb: Float,
        neuralGateReductionPct: Float,
        peakBlock: Float,
        bins64: List<Float>,
        patternMode: GlyphPatternMode
    ): GlyphHardwareFrame {
        val pulsePacket = glyphMapper.mapToGlyphPulsePattern(
            rmsLeftDb = rmsLeftDb,
            rmsRightDb = rmsRightDb,
            gainReductionDb = limiterGainReductionDb,
            neuralGatePct = neuralGateReductionPct,
            patternMode = patternMode
        )

        val z = pulsePacket.zones
        val rmsAvgNorm = (pulsePacket.rmsPercent / 100f).coerceIn(0.04f, 1f)
        val subEnergy = bins64.take(8).averageOrDefault(0.05f)
        val midEnergy = bins64.drop(18).take(16).averageOrDefault(0.05f)
        val airEnergy = bins64.drop(42).take(18).averageOrDefault(0.05f)

        val baseA1 = (z.getOrNull(0)?.intensity0To100 ?: 6) / 100f
        val baseA2 = (z.getOrNull(1)?.intensity0To100 ?: 6) / 100f
        val baseC1 = (z.getOrNull(2)?.intensity0To100 ?: 5) / 100f
        val baseC2 = (z.getOrNull(3)?.intensity0To100 ?: 5) / 100f
        val baseD1 = (z.getOrNull(4)?.intensity0To100 ?: 5) / 100f

        val cameraPill = if (patternMode == GlyphPatternMode.GLYPH_SPECTRUM_MATRIX) {
            (subEnergy * 0.45f + baseA1 * 0.55f).coerceIn(0.06f, 1f)
        } else {
            baseA1.coerceIn(0.06f, 1f)
        }
        val diagonalStrip = if (patternMode == GlyphPatternMode.GLYPH_SPECTRUM_MATRIX) {
            (airEnergy * 0.45f + baseA2 * 0.55f).coerceIn(0.06f, 1f)
        } else {
            baseA2.coerceIn(0.05f, 1f)
        }
        val ringFill = if (patternMode == GlyphPatternMode.STEREO_VU_BRIDGE) {
            (baseC1 * 0.85f + subEnergy * 0.15f).coerceIn(0.05f, 1f)
        } else {
            baseC1.coerceIn(0.06f, 1f)
        }
        val coreDot = if (patternMode == GlyphPatternMode.GLYPH_SPECTRUM_MATRIX) {
            midEnergy.coerceIn(0.05f, 1f)
        } else {
            (baseC2 * 0.7f + midEnergy * 0.3f).coerceIn(0.05f, 1f)
        }
        val usbClamp = maxOf(baseD1, peakBlock * 0.25f).coerceIn(0.06f, 1f)

        // Map continuous fill into 8 addressable wireless-coil C-Ring LEDs
        val ringSegments = List(8) { idx ->
            val segStart = idx / 8f
            val segSpan = 1f / 8f
            ((ringFill - segStart) / segSpan).coerceIn(0.08f, 1f)
        }

        // Map gain reduction & peak clamp into 6 addressable bottom exclamation LEDs
        val exclamationSegments = List(6) { idx ->
            val segStart = idx / 6f
            val segSpan = 1f / 6f
            ((usbClamp - segStart) / segSpan).coerceIn(0.06f, 1f)
        }

        // Convert to Nothing OS 12-bit hardware PWM integers (0..4095)
        val pwmList = listOf(
            toPwm12(cameraPill),
            toPwm12(diagonalStrip),
            toPwm12(ringFill),
            toPwm12(coreDot),
            toPwm12(usbClamp)
        )

        return GlyphHardwareFrame(
            patternMode = patternMode,
            cameraPillLevel = cameraPill,
            diagonalStripLevel = diagonalStrip,
            centerRingSegments = ringSegments,
            coreDotLevel = coreDot,
            usbExclamationSegments = exclamationSegments,
            rmsAvgNorm = rmsAvgNorm,
            gainReductionDb = limiterGainReductionDb,
            rmsPercent = pulsePacket.rmsPercent,
            gainReductionPercent = pulsePacket.gainReductionPercent,
            pulsePatternLabel = pulsePacket.pulseType.label,
            pwm12BitChannels = pwmList
        )
    }

    private fun toPwm12(level: Float): Int = (level.coerceIn(0f, 1f) * 4095f).toInt()

    private fun List<Float>.averageOrDefault(fallback: Float): Float {
        if (isEmpty()) return fallback
        var sum = 0f
        for (v in this) sum += v
        return sum / size
    }
}
```

---

## `app/src/main/java/com/example/audio/TfliteDspHook.kt`

```kotlin
package com.example.audio

import android.content.Context
import android.util.Log
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.Environment
import com.google.ai.edge.litert.TensorBuffer
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * On-device AI denoising and dynamic mastering [DspHook] powered by LiteRT (.tflite).
 * Lazily initializes [CompiledModel] with CPU XNNPACK options and reusable [TensorBuffer]s on
 * the audio processing thread, with a deterministic DSP spectral-gating fallback when native
 * runtime libraries are unavailable.
 */
class TfliteDspHook(
    private val context: Context,
    private val modelAssetName: String = "ai_audio_mastering_model.tflite",
    override val id: String = "tflite_ai_master",
    override val name: String = "On-Device TFLite AI Denoise & Master",
    override var isEnabled: Boolean = true
) : DspHook, AutoCloseable {

    override val baselineLatencyMs: Float
        get() = if (runtimeMode == "LITERT_COMPILED") 0.85f else 0.42f

    private val lock = Any()
    private var initialized = false
    private var compiledModel: CompiledModel? = null
    private var inputBuffers: List<TensorBuffer> = emptyList()
    private var outputBuffers: List<TensorBuffer> = emptyList()

    private val featureArray = FloatArray(4)
    private var smoothNoiseGate = 1.0f
    private var smoothDynGain = 1.0f

    @Volatile
    var runtimeMode: String = "INITIALIZING"
        private set

    @Volatile
    var lastGateReductionPct: Float = 0f
        private set

    @Volatile
    var lastDynGainDb: Float = 0f
        private set

    private fun ensureInitializedOnBackgroundThread() {
        if (initialized) return
        synchronized(lock) {
            if (initialized) return
            initialized = true
            runCatching {
                val env = getOrCreateSharedEnv() ?: error("LiteRT environment unavailable")
                val options = CompiledModel.Options(Accelerator.CPU).apply {
                    cpuOptions = CompiledModel.CpuOptions(numThreads = 2)
                }
                val model = CompiledModel.create(
                    context.assets,
                    modelAssetName,
                    options,
                    env
                )
                val ins = model.createInputBuffers()
                val outs = model.createOutputBuffers()
                // Warm-up inference with pre-allocated reusable buffers
                ins[0].writeFloat(floatArrayOf(0.15f, 0.45f, 0.10f, 0.25f))
                model.run(ins, outs)
                outs[0].readFloat()

                compiledModel = model
                inputBuffers = ins
                outputBuffers = outs
                runtimeMode = "LITERT_COMPILED"
            }.onFailure { err ->
                runCatching {
                    Log.w("TfliteDspHook", "Falling back to DSP spectral gate ($modelAssetName): ${err.message}")
                }
                closeInternal()
                runtimeMode = "DSP_FALLBACK"
            }
        }
    }

    override fun processBlock(
        left: FloatArray,
        right: FloatArray,
        frameCount: Int,
        sampleRate: Int
    ) {
        if (frameCount <= 0) return
        ensureInitializedOnBackgroundThread()

        // Extract compact block telemetry features [rms, peak, zcrNoise, hfEnergy]
        var sumSq = 0f
        var peak = 0f
        var zeroCrossings = 0
        var hfDiffSum = 0f
        var prevMid = 0f

        for (i in 0 until frameCount) {
            val mid = 0.5f * (left[i] + right[i])
            sumSq += mid * mid
            val a = abs(mid)
            if (a > peak) peak = a
            if (i > 0) {
                if ((mid >= 0f && prevMid < 0f) || (mid < 0f && prevMid >= 0f)) {
                    zeroCrossings++
                }
                hfDiffSum += abs(mid - prevMid)
            }
            prevMid = mid
        }

        val rms = sqrt((sumSq / frameCount).coerceAtLeast(1e-12f)).coerceIn(0f, 1f)
        val zcr = (zeroCrossings.toFloat() / frameCount).coerceIn(0f, 1f)
        val hfEnergy = (hfDiffSum / frameCount).coerceIn(0f, 1f)

        val (targetDenoiseStrength, targetDynGain) = runInference(rms, peak, zcr, hfEnergy)

        // Smooth block-to-block parameter trajectories to prevent pumping
        smoothNoiseGate = smoothNoiseGate * 0.75f + targetDenoiseStrength * 0.25f
        smoothDynGain = smoothDynGain * 0.80f + targetDynGain * 0.20f

        val noiseFloorThreshold = 0.008f + smoothNoiseGate * 0.020f
        val gain = smoothDynGain.coerceIn(0.75f, 1.45f)
        var gateSum = 0f

        for (i in 0 until frameCount) {
            var l = left[i]
            var r = right[i]
            val env = 0.5f * (abs(l) + abs(r))
            // Soft downward expander suppresses low-level hiss/noise below threshold
            val gateFactor = if (env < noiseFloorThreshold) {
                val ratio = (env / noiseFloorThreshold).coerceIn(0f, 1f)
                0.35f + 0.65f * (ratio * ratio)
            } else {
                1.0f
            }
            gateSum += gateFactor
            l = tanh(l * gateFactor * gain)
            r = tanh(r * gateFactor * gain)
            left[i] = l
            right[i] = r
        }

        val avgGateFactor = (gateSum / frameCount).coerceIn(0f, 1f)
        lastGateReductionPct = ((1f - avgGateFactor) * 100f).coerceIn(0f, 65f)
        lastDynGainDb = (20f * log10(gain.coerceAtLeast(1e-4f))).coerceIn(-6f, 6f)
    }

    private fun runInference(
        rms: Float,
        peak: Float,
        zcr: Float,
        hfEnergy: Float
    ): Pair<Float, Float> {
        val model = compiledModel
        if (model != null && inputBuffers.isNotEmpty() && outputBuffers.isNotEmpty()) {
            val out = runCatching {
                featureArray[0] = rms
                featureArray[1] = peak
                featureArray[2] = zcr
                featureArray[3] = hfEnergy
                inputBuffers[0].writeFloat(featureArray)
                model.run(inputBuffers, outputBuffers)
                outputBuffers[0].readFloat()
            }.getOrNull()
            if (out != null && out.size >= 2 && out[0].isFinite() && out[1].isFinite()) {
                return out[0].coerceIn(0f, 1f) to out[1].coerceIn(0.75f, 1.45f)
            }
        }
        // Deterministic fallback mirroring the bundled TFLite FC weights
        val denoise = (-0.65f * rms - 0.10f * peak + 1.25f * zcr - 0.25f * hfEnergy + 0.20f)
            .coerceIn(0f, 1f)
        val dynGain = (-0.55f * rms - 0.45f * peak - 0.15f * zcr + 0.20f * hfEnergy + 1.15f)
            .coerceIn(0.75f, 1.45f)
        return denoise to dynGain
    }

    override fun reset() {
        smoothNoiseGate = 1.0f
        smoothDynGain = 1.0f
        lastGateReductionPct = 0f
        lastDynGainDb = 0f
    }

    override fun close() {
        synchronized(lock) {
            closeInternal()
        }
    }

    private fun closeInternal() {
        runCatching {
            (inputBuffers + outputBuffers).forEach { it.close() }
            compiledModel?.close()
        }
        inputBuffers = emptyList()
        outputBuffers = emptyList()
        compiledModel = null
    }

    companion object {
        @Volatile
        private var cachedEnv: Environment? = null

        private fun getOrCreateSharedEnv(): Environment? {
            return cachedEnv ?: synchronized(this) {
                cachedEnv ?: runCatching { Environment.create() }.getOrNull()?.also {
                    cachedEnv = it
                }
            }
        }
    }
}
```

---

## `app/src/main/java/com/example/data/MasterLibraryRepository.kt`

```kotlin
package com.example.data

import kotlinx.coroutines.flow.Flow

class MasterLibraryRepository(private val dao: MasterTrackDao) {
    val tracks: Flow<List<MasterTrackEntity>> = dao.getAllTracks()

    suspend fun saveMaster(track: MasterTrackEntity): Long = dao.insertTrack(track)

    suspend fun deleteMaster(id: Int) = dao.deleteTrackById(id)
}
```

---

## `app/src/main/java/com/example/data/MasterTrackDao.kt`

```kotlin
package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MasterTrackDao {
    @Query("SELECT * FROM master_tracks ORDER BY createdAtMs DESC")
    fun getAllTracks(): Flow<List<MasterTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: MasterTrackEntity): Long

    @Query("SELECT COUNT(*) FROM master_tracks")
    suspend fun getTrackCount(): Int

    @Query("DELETE FROM master_tracks WHERE id = :id")
    suspend fun deleteTrackById(id: Int)
}
```

---

## `app/src/main/java/com/example/data/MasterTrackEntity.kt`

```kotlin
package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "master_tracks")
data class MasterTrackEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val formatLabel: String,
    val sampleRate: Int,
    val bitDepth: Int,
    val durationMs: Long,
    val finalLufs: Float,
    val finalPeakDb: Float,
    val profileName: String,
    val filePath: String,
    val createdAtMs: Long = System.currentTimeMillis()
)
```

---

## `app/src/main/java/com/example/data/WaviqDatabase.kt`

```kotlin
package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MasterTrackEntity::class], version = 1, exportSchema = false)
abstract class WaviqDatabase : RoomDatabase() {
    abstract fun masterTrackDao(): MasterTrackDao

    companion object {
        @Volatile
        private var INSTANCE: WaviqDatabase? = null

        fun getInstance(context: Context): WaviqDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WaviqDatabase::class.java,
                    "waviq_masters.db"
                ).fallbackToDestructiveMigration(dropAllTables = true)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
```

---

## `app/src/main/java/com/example/model/DevMapSchemas.kt`

```kotlin
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
                                     │ #10 4-WAY STEMS │◄───────┼─┤ #07 TACTILE DECK│
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

    const val SCHEMA_10 = """// RoutePayloadSchema.ts — #10 4-Way Stem Separation
export interface Route10StemsPayload {
  routeId: '#10';
  path: '/studio/stems';
  busMode: 'STEM_MATRIX_4X';
  stems: {
    vocals: { gain: number; mute: boolean; solo: boolean };
    drums: { gain: number; mute: boolean; solo: boolean };
    bass: { gain: number; mute: boolean; solo: boolean };
    other: { gain: number; mute: boolean; solo: boolean };
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
  format: 'WAV_24_48' | 'WAV_16_44' | 'WAV_24_96' | 'FLAC_PROFILE';
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
```

---

## `app/src/main/java/com/example/model/WaviqRoute.kt`

```kotlin
package com.example.model

import androidx.compose.runtime.Immutable

@Immutable
data class WaviqRouteSpec(
    val id: Int,
    val code: String,
    val title: String,
    val shortTitle: String,
    val path: String,
    val tab: WaviqTab,
    val busStatus: String,
    val busMode: AudioBusMode,
    val crossfadeSpec: String,
    val inboundTriggers: List<String>,
    val outboundSuccess: String,
    val alternateBypass: String,
    val hapticProfile: String,
    val glyphZoneAssignment: String,
    val peakRamBudget: String,
    val npuStatus: String,
    val tsSchema: String
)

object WaviqRouteRegistry {

    val allRoutes: List<WaviqRouteSpec> = listOf(
        WaviqRouteSpec(
            id = 1,
            code = "#01",
            title = "Audio Track Inspector",
            shortTitle = "INSPECTOR",
            path = "/analyze/inspector",
            tab = WaviqTab.ANALYZE,
            busStatus = "RUNNING (48kHz / 96kHz)",
            busMode = AudioBusMode.SPECTRAL_TAP,
            crossfadeSpec = "15ms constant-power",
            inboundTriggers = listOf("Cold Launch", "SAF OpenDocument", "Route #16 Reload"),
            outboundSuccess = "Route #02 (/analyze/scope)",
            alternateBypass = "Route #05 (/enhance/engine)",
            hapticProfile = "12ms crisp detent on track load",
            glyphZoneAssignment = "ZONE_A1: File Integrity Pulse",
            peakRamBudget = "94 MB / < 180 MB",
            npuStatus = "AIR-GAPPED // READY",
            tsSchema = DevMapSchemas.SCHEMA_01
        ),
        WaviqRouteSpec(
            id = 2,
            code = "#02",
            title = "Live Scope Analyzer",
            shortTitle = "LIVE SCOPE",
            path = "/analyze/scope",
            tab = WaviqTab.ANALYZE,
            busStatus = "RUNNING (96kHz DSP)",
            busMode = AudioBusMode.SPECTRAL_TAP,
            crossfadeSpec = "15ms constant-power",
            inboundTriggers = listOf("Route #01 Inspector", "Tab 1 Sub-Rail"),
            outboundSuccess = "Route #03 (/analyze/eq)",
            alternateBypass = "Route #04 (/analyze/reports)",
            hapticProfile = "10ms micro-tick on scope mode flip",
            glyphZoneAssignment = "ZONE_B2: 64-Bin FFT Crest Bar",
            peakRamBudget = "112 MB / < 180 MB",
            npuStatus = "AIR-GAPPED // FFT_TAP_64",
            tsSchema = DevMapSchemas.SCHEMA_02
        ),
        WaviqRouteSpec(
            id = 3,
            code = "#03",
            title = "Parametric EQ & Filters",
            shortTitle = "PARAM EQ",
            path = "/analyze/eq",
            tab = WaviqTab.ANALYZE,
            busStatus = "RUNNING (96kHz DSP)",
            busMode = AudioBusMode.NPU_DSP_ACTIVE,
            crossfadeSpec = "15ms constant-power",
            inboundTriggers = listOf("Route #02 Scope", "Route #09 Pro Suite"),
            outboundSuccess = "Route #04 (/analyze/reports)",
            alternateBypass = "Route #08 (/enhance/ab)",
            hapticProfile = "10ms–14ms band detent sweep",
            glyphZoneAssignment = "ZONE_C3: 5-Band Gain Arc",
            peakRamBudget = "118 MB / < 180 MB",
            npuStatus = "AIR-GAPPED // BIQUAD_5X",
            tsSchema = DevMapSchemas.SCHEMA_03
        ),
        WaviqRouteSpec(
            id = 4,
            code = "#04",
            title = "Analysis Reports",
            shortTitle = "REPORTS",
            path = "/analyze/reports",
            tab = WaviqTab.ANALYZE,
            busStatus = "RUNNING (48kHz / 96kHz)",
            busMode = AudioBusMode.SPECTRAL_TAP,
            crossfadeSpec = "15ms constant-power",
            inboundTriggers = listOf("Route #03 EQ", "Route #02 Scope"),
            outboundSuccess = "Route #05 (/enhance/engine)",
            alternateBypass = "Route #13 (/library/transcode)",
            hapticProfile = "15ms audit-complete confirm pulse",
            glyphZoneAssignment = "ZONE_D1: Headroom Status Ring",
            peakRamBudget = "106 MB / < 180 MB",
            npuStatus = "AIR-GAPPED // LUFS_AUDIT",
            tsSchema = DevMapSchemas.SCHEMA_04
        ),
        WaviqRouteSpec(
            id = 5,
            code = "#05",
            title = "Autonomous AI Engine",
            shortTitle = "AI ENGINE",
            path = "/enhance/engine",
            tab = WaviqTab.ENHANCE,
            busStatus = "RUNNING (96kHz DSP)",
            busMode = AudioBusMode.NPU_DSP_ACTIVE,
            crossfadeSpec = "15ms constant-power",
            inboundTriggers = listOf("Route #04 Reports", "Tab 2 Nav"),
            outboundSuccess = "Route #06 (/enhance/processing)",
            alternateBypass = "Route #07 (/enhance/deck)",
            hapticProfile = "16ms profile lock thud",
            glyphZoneAssignment = "ZONE_A2: Neural Profile Halo",
            peakRamBudget = "128 MB / < 180 MB",
            npuStatus = "AIR-GAPPED // PROFILE_ARMED",
            tsSchema = DevMapSchemas.SCHEMA_05
        ),
        WaviqRouteSpec(
            id = 6,
            code = "#06",
            title = "Deep 3.5s Processing",
            shortTitle = "3.5S PASS",
            path = "/enhance/processing",
            tab = WaviqTab.ENHANCE,
            busStatus = "RUNNING (96kHz BATCH)",
            busMode = AudioBusMode.NPU_DSP_ACTIVE,
            crossfadeSpec = "15ms constant-power",
            inboundTriggers = listOf("Route #05 AI Engine Dispatch"),
            outboundSuccess = "Route #07 (/enhance/deck)",
            alternateBypass = "Route #08 (/enhance/ab)",
            hapticProfile = "18ms 4-stage cadence pulse",
            glyphZoneAssignment = "ZONE_ALL: Sequential Sweep 0-100%",
            peakRamBudget = "156 MB / < 180 MB",
            npuStatus = "AIR-GAPPED // 4_PASS_ACTIVE",
            tsSchema = DevMapSchemas.SCHEMA_06
        ),
        WaviqRouteSpec(
            id = 7,
            code = "#07",
            title = "Tactile Deck",
            shortTitle = "TACTILE DECK",
            path = "/enhance/deck",
            tab = WaviqTab.ENHANCE,
            busStatus = "RUNNING (96kHz DSP)",
            busMode = AudioBusMode.NPU_DSP_ACTIVE,
            crossfadeSpec = "15ms constant-power",
            inboundTriggers = listOf("Route #06 Processing", "Route #05 Engine"),
            outboundSuccess = "Route #08 (/enhance/ab)",
            alternateBypass = "Route #09 (/studio/pro)",
            hapticProfile = "10ms rotary tick per 5% notch",
            glyphZoneAssignment = "ZONE_E1: Drive & Ceiling Strip",
            peakRamBudget = "132 MB / < 180 MB",
            npuStatus = "AIR-GAPPED // SAT_WIDTH_DSP",
            tsSchema = DevMapSchemas.SCHEMA_07
        ),
        WaviqRouteSpec(
            id = 8,
            code = "#08",
            title = "Original vs Enhanced A/B",
            shortTitle = "A/B COMPARE",
            path = "/enhance/ab",
            tab = WaviqTab.ENHANCE,
            busStatus = "RUNNING (96kHz DUAL)",
            busMode = AudioBusMode.DUAL_BUFFER,
            crossfadeSpec = "15ms constant-power",
            inboundTriggers = listOf("Route #07 Deck", "Route #11 Bypass"),
            outboundSuccess = "Route #09 (/studio/pro)",
            alternateBypass = "Route #13 (/library/transcode)",
            hapticProfile = "14ms crisp A/B relay snap",
            glyphZoneAssignment = "ZONE_B1/C1: Dual Dry/Wet Split",
            peakRamBudget = "142 MB / < 180 MB",
            npuStatus = "AIR-GAPPED // DUAL_BUFFER",
            tsSchema = DevMapSchemas.SCHEMA_08
        ),
        WaviqRouteSpec(
            id = 9,
            code = "#09",
            title = "AI Studio Pro Suite",
            shortTitle = "PRO SUITE",
            path = "/studio/pro",
            tab = WaviqTab.STUDIO,
            busStatus = "RUNNING (96kHz DSP)",
            busMode = AudioBusMode.NPU_DSP_ACTIVE,
            crossfadeSpec = "15ms constant-power",
            inboundTriggers = listOf("Route #08 A/B", "Tab 3 Nav"),
            outboundSuccess = "Route #10 (/studio/stems)",
            alternateBypass = "Route #12 (/studio/glyph)",
            hapticProfile = "12ms bus module engage",
            glyphZoneAssignment = "ZONE_A1-D1: Console Bus Lock",
            peakRamBudget = "148 MB / < 180 MB",
            npuStatus = "AIR-GAPPED // MASTER_CHAIN",
            tsSchema = DevMapSchemas.SCHEMA_09
        ),
        WaviqRouteSpec(
            id = 10,
            code = "#10",
            title = "4-Way Stem Separation",
            shortTitle = "4-STEMS",
            path = "/studio/stems",
            tab = WaviqTab.STUDIO,
            busStatus = "RUNNING (96kHz 4X-BUS)",
            busMode = AudioBusMode.STEM_MATRIX_4X,
            crossfadeSpec = "15ms constant-power",
            inboundTriggers = listOf("Route #09 Pro Suite", "Tab 3 Sub-Rail"),
            outboundSuccess = "Route #11 (/studio/bypass)",
            alternateBypass = "Route #13 (/library/transcode)",
            hapticProfile = "11ms solo/mute latch click",
            glyphZoneAssignment = "ZONE_4X: Vocals/Drums/Bass/Other",
            peakRamBudget = "164 MB / < 180 MB",
            npuStatus = "AIR-GAPPED // CROSSOVER_4X",
            tsSchema = DevMapSchemas.SCHEMA_10
        ),
        WaviqRouteSpec(
            id = 11,
            code = "#11",
            title = "Zero-Latency Bypass vs Engaged",
            shortTitle = "BYPASS MATRIX",
            path = "/studio/bypass",
            tab = WaviqTab.STUDIO,
            busStatus = "RUNNING (96kHz ZERO-LAT)",
            busMode = AudioBusMode.BYPASS_RAW,
            crossfadeSpec = "15ms constant-power",
            inboundTriggers = listOf("Route #10 Stems", "Route #09 Pro"),
            outboundSuccess = "Route #12 (/studio/glyph)",
            alternateBypass = "Route #08 (/enhance/ab)",
            hapticProfile = "16ms hard relay toggle",
            glyphZoneAssignment = "ZONE_C2: Bypass Red / Active Green",
            peakRamBudget = "124 MB / < 180 MB",
            npuStatus = "AIR-GAPPED // ZERO_LAT_RELAY",
            tsSchema = DevMapSchemas.SCHEMA_11
        ),
        WaviqRouteSpec(
            id = 12,
            code = "#12",
            title = "Glyph VU Sync",
            shortTitle = "GLYPH VU",
            path = "/studio/glyph",
            tab = WaviqTab.STUDIO,
            busStatus = "RUNNING (96kHz TELEMETRY)",
            busMode = AudioBusMode.GLYPH_VU_SYNC,
            crossfadeSpec = "15ms constant-power",
            inboundTriggers = listOf("Route #11 Bypass", "Route #09 Pro"),
            outboundSuccess = "Route #13 (/library/transcode)",
            alternateBypass = "Route #16 (/library/player)",
            hapticProfile = "10ms transient peak pulse",
            glyphZoneAssignment = "CHASSIS_5_ZONE: Full Rear Array",
            peakRamBudget = "136 MB / < 180 MB",
            npuStatus = "AIR-GAPPED // GLYPH_SYNC_60HZ",
            tsSchema = DevMapSchemas.SCHEMA_12
        ),
        WaviqRouteSpec(
            id = 13,
            code = "#13",
            title = "Transcode & Encoding Matrix",
            shortTitle = "TRANSCODE",
            path = "/library/transcode",
            tab = WaviqTab.LIBRARY,
            busStatus = "RUNNING (48kHz / 96kHz)",
            busMode = AudioBusMode.OFFLINE_RENDER,
            crossfadeSpec = "15ms constant-power",
            inboundTriggers = listOf("Route #12 Glyph VU", "Route #08 A/B", "Tab 4 Nav"),
            outboundSuccess = "Route #14 (/library/export)",
            alternateBypass = "Route #16 (/library/player)",
            hapticProfile = "12ms codec matrix selection",
            glyphZoneAssignment = "ZONE_D2: Encoder Bitrate Gauge",
            peakRamBudget = "140 MB / < 180 MB",
            npuStatus = "AIR-GAPPED // PCM_ENCODER",
            tsSchema = DevMapSchemas.SCHEMA_13
        ),
        WaviqRouteSpec(
            id = 14,
            code = "#14",
            title = "Export Telemetry Modal",
            shortTitle = "EXPORT MODAL",
            path = "/library/export",
            tab = WaviqTab.LIBRARY,
            busStatus = "OFFLINE RENDER (LOCKED)",
            busMode = AudioBusMode.OFFLINE_RENDER,
            crossfadeSpec = "15ms constant-power",
            inboundTriggers = listOf("Route #13 Transcode Dispatch"),
            outboundSuccess = "Route #15 (/library/done)",
            alternateBypass = "Route #13 (/library/transcode)",
            hapticProfile = "14ms buffer flush tick",
            glyphZoneAssignment = "ZONE_ALL: Linear Export Bar",
            peakRamBudget = "172 MB / < 180 MB",
            npuStatus = "AIR-GAPPED // WAV_WRITE_IO",
            tsSchema = DevMapSchemas.SCHEMA_14
        ),
        WaviqRouteSpec(
            id = 15,
            code = "#15",
            title = "Master Done 100%",
            shortTitle = "DONE 100%",
            path = "/library/done",
            tab = WaviqTab.LIBRARY,
            busStatus = "VERIFIED (100% SEALED)",
            busMode = AudioBusMode.OFFLINE_RENDER,
            crossfadeSpec = "15ms constant-power",
            inboundTriggers = listOf("Route #14 Export Completion"),
            outboundSuccess = "Route #16 (/library/player)",
            alternateBypass = "Route #01 (/analyze/inspector)",
            hapticProfile = "18ms double-pulse master seal",
            glyphZoneAssignment = "ZONE_ALL: Crimson Completion Flash",
            peakRamBudget = "98 MB / < 180 MB",
            npuStatus = "AIR-GAPPED // SHA_VERIFIED",
            tsSchema = DevMapSchemas.SCHEMA_15
        ),
        WaviqRouteSpec(
            id = 16,
            code = "#16",
            title = "Master Audio Player",
            shortTitle = "PLAYER",
            path = "/library/player",
            tab = WaviqTab.LIBRARY,
            busStatus = "RUNNING (48kHz / 96kHz)",
            busMode = AudioBusMode.PLAYER_PLAYBACK,
            crossfadeSpec = "15ms constant-power",
            inboundTriggers = listOf("Route #15 Master Done", "Tab 4 Sub-Rail"),
            outboundSuccess = "Route #01 (/analyze/inspector)",
            alternateBypass = "Route #12 (/studio/glyph)",
            hapticProfile = "12ms transport scrub & A/B tap",
            glyphZoneAssignment = "ZONE_E2: Offline Playback Breath",
            peakRamBudget = "102 MB / < 180 MB",
            npuStatus = "AIR-GAPPED // ROOM_SQLITE",
            tsSchema = DevMapSchemas.SCHEMA_16
        )
    )

    fun byId(id: Int): WaviqRouteSpec = allRoutes.firstOrNull { it.id == id } ?: allRoutes.first()

    fun byTab(tab: WaviqTab): List<WaviqRouteSpec> = allRoutes.filter { it.tab == tab }
}
```

---

## `app/src/main/java/com/example/model/WaviqState.kt`

```kotlin
package com.example.model

import androidx.compose.runtime.Immutable

enum class WaviqTab(
    val indexLabel: String,
    val title: String,
    val routePrefix: String
) {
    ANALYZE("1", "ANALYZE", "/analyze"),
    ENHANCE("2", "ENHANCE", "/enhance"),
    STUDIO("3", "STUDIO", "/studio"),
    LIBRARY("4", "LIBRARY", "/library")
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
    val vocals: StemChannelState = StemChannelState("vocals", "VOCALS", "1.2kHz–5.5kHz Mid"),
    val drums: StemChannelState = StemChannelState("drums", "DRUMS", "Transient + 2.5k–8k"),
    val bass: StemChannelState = StemChannelState("bass", "BASS", "20Hz–220Hz Sub/Low"),
    val other: StemChannelState = StemChannelState("other", "OTHER", "Side Stereo & 8k+ Air")
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
    WAV_32F_192("WAV 32-BIT FLOAT / 192kHz", 32, 192000, "wav", "64->32f IEEE Float Studio Master"),
    FLAC_PROFILE("WAV-PCM LOSSLESS 24B", 24, 48000, "wav", "Archival Lossless Container")
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
    val autoAppliedSummary: String = "TFLITE DENOISE + HF RESTORE + -1.0 dBTP LIMITER",
    val detectedGenre: MusicGenre = MusicGenre.POP,
    val genreConfidencePct: Int = 88,
    val genreRationale: String = "Balanced spectral energy with transient punch"
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
    val isDevMapOpen: Boolean = false,
    val devMapFilter: WaviqTab? = null,
    val inspectedRouteId: Int = 1,
    val lastDispatchedLog: String = "BUS READY // ROUTE #01 INITIALIZED",
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
    val assistantResponse: String = "Ready. Tell WAVIQ what you want changed, in Arabic or English."
)

fun defaultEqBands(): List<EqBand> = listOf(
    EqBand("sub", "SUB", 60f, 1.2f, 0.8f),
    EqBand("bass", "LOW", 180f, 0.6f, 0.9f),
    EqBand("mid", "MID", 900f, -0.4f, 1.1f),
    EqBand("pres", "PRES", 3600f, 1.4f, 1.0f),
    EqBand("air", "AIR", 12000f, 2.0f, 0.7f)
)
```

---

## `app/src/main/java/com/example/ui/components/InteractiveSpectralCurve.kt`

```kotlin
package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.MusicGenre
import com.example.ui.theme.BusGreen
import com.example.ui.theme.ConsolePanel
import com.example.ui.theme.CyanScope
import com.example.ui.theme.GlyphCrimson
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.MutedGrid
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.PeakAmber
import com.example.ui.theme.SignalWhite
import com.example.ui.theme.TechSlate
import java.util.Locale

/**
 * Interactive Real-Time Spectral Distribution Curve Canvas.
 * Renders the frequency distribution (20Hz - 20kHz) mapped to the detected/selected
 * MusicGenre DSP calibration with touch inspection and target LUFS indicators.
 */
@Composable
fun InteractiveSpectralCurve(
    genre: MusicGenre,
    bandwidthKhz: Float = 18.5f,
    modifier: Modifier = Modifier,
    isRealTime: Boolean = false
) {
    val genreAccent = when (genre) {
        MusicGenre.CLASSICAL, MusicGenre.ACOUSTIC -> BusGreen
        MusicGenre.POP, MusicGenre.ELECTRONIC -> CyanScope
        MusicGenre.ROCK, MusicGenre.HIP_HOP -> PeakAmber
    }

    var inspectFraction by remember { mutableFloatStateOf(0.5f) }

    val bandLabels = listOf("60Hz", "250Hz", "1kHz", "4kHz", "12kHz")
    val offsets = genre.eqOffsetsDb.ifEmpty { listOf(0f, 0f, 0f, 0f, 0f) }

    Surface(
        color = ConsolePanel,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, HairlineBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_spectral_curve")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(genreAccent, CircleShape)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "SPECTRAL DISTRIBUTION // ${genre.title}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SignalWhite
                    )
                }

                Surface(
                    color = genreAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, genreAccent.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "TARGET ${genre.targetLufs} LUFS",
                        style = MaterialTheme.typography.labelSmall,
                        color = genreAccent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Main Interactive Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ObsidianVoid)
                    .border(1.dp, HairlineBorder, RoundedCornerShape(6.dp))
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            inspectFraction = (offset.x / size.width).coerceIn(0.02f, 0.98f)
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            inspectFraction = (change.position.x / size.width).coerceIn(0.02f, 0.98f)
                            change.consume()
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val midY = h * 0.52f

                    // Gridlines
                    val gridXSteps = 5
                    for (i in 1..gridXSteps) {
                        val gx = w * (i.toFloat() / (gridXSteps + 1))
                        drawLine(
                            color = MutedGrid.copy(alpha = 0.4f),
                            start = Offset(gx, 0f),
                            end = Offset(gx, h),
                            strokeWidth = 1f
                        )
                    }

                    // Horizontal zero-dB baseline
                    drawLine(
                        color = MutedGrid.copy(alpha = 0.6f),
                        start = Offset(0f, midY),
                        end = Offset(w, midY),
                        strokeWidth = 1f
                    )

                    // Control points across the 5 EQ bands
                    val points = mutableListOf<Offset>()
                    val marginX = w * 0.08f
                    val spanX = w - 2f * marginX

                    for (i in 0 until 5) {
                        val px = marginX + (spanX * (i.toFloat() / 4f))
                        val gainDb = offsets.getOrElse(i) { 0f }
                        // Scale: +6dB is near top, -6dB is near bottom
                        val py = (midY - (gainDb * (h * 0.065f))).coerceIn(h * 0.12f, h * 0.88f)
                        points.add(Offset(px, py))
                    }

                    // Build smooth cubic Bézier curve path
                    val curvePath = Path().apply {
                        moveTo(0f, midY - (offsets[0] * (h * 0.04f)))
                        lineTo(points[0].x, points[0].y)

                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val controlX = (p0.x + p1.x) / 2f
                            cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                        }

                        // Air rolloff past bandwidth
                        val endY = if (bandwidthKhz < 15f) h * 0.82f else midY - (offsets.last() * (h * 0.04f))
                        lineTo(w, endY)
                    }

                    // Gradient fill under the curve
                    val fillPath = Path().apply {
                        addPath(curvePath)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                genreAccent.copy(alpha = 0.30f),
                                genreAccent.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = h
                        )
                    )

                    // Draw main spectral contour line
                    drawPath(
                        path = curvePath,
                        color = genreAccent,
                        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                    )

                    // Draw node circles on the 5 bands
                    points.forEachIndexed { idx, pt ->
                        drawCircle(
                            color = ObsidianVoid,
                            radius = 5.5f,
                            center = pt
                        )
                        drawCircle(
                            color = genreAccent,
                            radius = 3.5f,
                            center = pt
                        )
                    }

                    // Interactive touch cursor
                    val cursorX = w * inspectFraction
                    drawLine(
                        color = SignalWhite.copy(alpha = 0.75f),
                        start = Offset(cursorX, 0f),
                        end = Offset(cursorX, h),
                        strokeWidth = 1.5f
                    )
                    drawCircle(
                        color = SignalWhite,
                        radius = 4f,
                        center = Offset(cursorX, midY)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Frequency band labels footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                bandLabels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TechSlate
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Inspection Telemetry Footer
            val inspectHz = (20.0 * Math.pow(1000.0, inspectFraction.toDouble())).toInt().coerceIn(20, 20000)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CURSOR: ${inspectHz}Hz",
                    style = MaterialTheme.typography.labelSmall,
                    color = SignalWhite
                )
                Text(
                    text = "CEILING: ${genre.ceilingDb} dBTP • ${genre.oversampling.factor}X",
                    style = MaterialTheme.typography.labelSmall,
                    color = TechSlate
                )
            }
        }
    }
}
```

---

## `app/src/main/java/com/example/ui/components/WaviqChrome.kt`

```kotlin
package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AudioBusMode
import com.example.model.WaviqRouteSpec
import com.example.model.WaviqTab
import com.example.ui.theme.BusGreen
import com.example.ui.theme.BusGreenMuted
import com.example.ui.theme.ConsolePanel
import com.example.ui.theme.GlyphCrimson
import com.example.ui.theme.GlyphCrimsonGlow
import com.example.ui.theme.GlyphCrimsonMuted
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.HardwareSurface
import com.example.ui.theme.MutedGrid
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.PeakAmber
import com.example.ui.theme.SignalWhite
import com.example.ui.theme.TechSlate
import java.util.Locale

@Composable
fun StudioTopBar(
    activeRoute: WaviqRouteSpec,
    busModeOverride: AudioBusMode?,
    isPlaying: Boolean,
    dspLatencyMs: Float,
    isBypassed: Boolean,
    isDevMapOpen: Boolean,
    onTogglePlay: () -> Unit,
    onToggleDevMap: () -> Unit
) {
    val activeBus = busModeOverride ?: activeRoute.busMode
    val effectiveBypassed = isBypassed || activeBus == AudioBusMode.BYPASS_RAW
    val effectiveLatencyMs = if (effectiveBypassed) 0f else dspLatencyMs
    val latencyColor = when {
        effectiveBypassed -> TechSlate
        effectiveLatencyMs > 15.0f -> GlyphCrimson
        effectiveLatencyMs > 8.0f -> PeakAmber
        else -> BusGreen
    }
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ObsidianVoid)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) GlyphCrimson else BusGreen)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "WAVIQ",
                            style = MaterialTheme.typography.titleLarge,
                            color = SignalWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = ConsolePanel,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, HairlineBorder)
                        ) {
                            Text(
                                text = "${activeRoute.code} ${activeBus.code}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (effectiveBypassed) TechSlate else BusGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = ConsolePanel,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, latencyColor.copy(alpha = 0.55f)),
                            modifier = Modifier.testTag("top_bus_latency_badge")
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.2f ms", effectiveLatencyMs),
                                style = MaterialTheme.typography.labelSmall,
                                color = latencyColor,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = activeRoute.path,
                        style = MaterialTheme.typography.labelSmall,
                        color = TechSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onTogglePlay()
                    },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("top_transport_play_button")
                        .clip(CircleShape)
                        .background(if (isPlaying) GlyphCrimson else ConsolePanel)
                        .border(1.dp, if (isPlaying) GlyphCrimson else HairlineBorder, CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause audio playback" else "Play audio playback",
                        tint = SignalWhite
                    )
                }

                Spacer(Modifier.width(8.dp))

                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleDevMap()
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDevMapOpen) GlyphCrimson else ConsolePanel,
                    border = BorderStroke(1.dp, if (isDevMapOpen) GlyphCrimson else HairlineBorder),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("dev_map_toggle_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountTree,
                            contentDescription = "Open Developer Navigation Map",
                            tint = SignalWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isDevMapOpen) "STUDIO" else "DEV MAP",
                            style = MaterialTheme.typography.labelMedium,
                            color = SignalWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubScreenSelectorRail(
    routes: List<WaviqRouteSpec>,
    activeRouteId: Int,
    onSelectRoute: (Int) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ObsidianVoid)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        routes.forEach { route ->
            val selected = route.id == activeRouteId
            val bg by animateColorAsState(
                if (selected) GlyphCrimsonMuted else HardwareSurface,
                label = "pillBg"
            )
            val border by animateColorAsState(
                if (selected) GlyphCrimson else HairlineBorder,
                label = "pillBorder"
            )
            Surface(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSelectRoute(route.id)
                },
                shape = RoundedCornerShape(8.dp),
                color = bg,
                border = BorderStroke(1.dp, border),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("subroute_pill_${route.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = route.code,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) GlyphCrimson else TechSlate
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = route.shortTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) SignalWhite else TechSlate
                    )
                }
            }
        }
    }
}

@Composable
fun HardwarePanel(
    modifier: Modifier = Modifier,
    title: String? = null,
    badge: String? = null,
    accentBorder: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = HardwareSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (accentBorder) GlyphCrimson else HairlineBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (title != null || badge != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = SignalWhite
                        )
                    }
                    if (badge != null) {
                        Surface(
                            color = if (accentBorder) GlyphCrimsonMuted else ConsolePanel,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (accentBorder) GlyphCrimson else HairlineBorder)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (accentBorder) SignalWhite else TechSlate,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            content()
        }
    }
}

@Composable
fun TelemetryStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accentColor: Color = SignalWhite
) {
    Surface(
        modifier = modifier,
        color = ConsolePanel,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, HairlineBorder)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TechSlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WaveformScrubBar(
    peaks: List<Float>,
    progressFraction: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val safePeaks = if (peaks.isEmpty()) List(64) { 0.25f } else peaks
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ConsolePanel)
            .border(1.dp, HairlineBorder, RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val frac = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    onSeek(frac)
                }
            }
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val count = safePeaks.size
            val slotW = size.width / count
            val barW = (slotW * 0.65f).coerceAtLeast(2f)
            val midY = size.height / 2f
            val playX = progressFraction.coerceIn(0f, 1f) * size.width

            for (i in 0 until count) {
                val x = i * slotW + (slotW - barW) / 2f
                val amp = safePeaks[i].coerceIn(0.06f, 1f)
                val h = amp * size.height * 0.9f
                val isPlayed = x <= playX
                drawRoundRect(
                    color = if (isPlayed) GlyphCrimson else MutedGrid,
                    topLeft = Offset(x, midY - h / 2f),
                    size = Size(barW, h),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }
            // Playhead needle
            drawLine(
                color = SignalWhite,
                start = Offset(playX, 0f),
                end = Offset(playX, size.height),
                strokeWidth = 2.5f
            )
        }
    }
}

@Composable
fun TactileRotarySlider(
    label: String,
    value: Float,
    valueDisplay: String,
    onValueChange: (Float) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    val norm = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start))
        .coerceIn(0f, 1f)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ConsolePanel)
            .border(1.dp, HairlineBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(28.dp)) {
                    drawArc(
                        color = MutedGrid,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = GlyphCrimson,
                        startAngle = 135f,
                        sweepAngle = 270f * norm,
                        useCenter = false,
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = SignalWhite
                )
            }
            Text(
                text = valueDisplay,
                style = MaterialTheme.typography.labelLarge,
                color = GlyphCrimson
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = SignalWhite,
                activeTrackColor = GlyphCrimson,
                inactiveTrackColor = HairlineBorder
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
fun WaviqBottomNavBar(
    activeTab: WaviqTab,
    onSelectTab: (WaviqTab) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    NavigationBar(
        containerColor = HardwareSurface,
        contentColor = SignalWhite,
        tonalElevation = 0.dp,
        modifier = Modifier
            .border(BorderStroke(1.dp, HairlineBorder))
            .navigationBarsPadding()
    ) {
        WaviqTab.entries.forEach { tab ->
            val selected = tab == activeTab
            NavigationBarItem(
                selected = selected,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSelectTab(tab)
                },
                icon = {
                    Icon(
                        imageVector = tabIcon(tab),
                        contentDescription = tab.title
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SignalWhite,
                    selectedTextColor = SignalWhite,
                    indicatorColor = GlyphCrimson,
                    unselectedIconColor = TechSlate,
                    unselectedTextColor = TechSlate
                ),
                modifier = Modifier.testTag("bottom_tab_${tab.name.lowercase()}")
            )
        }
    }
}

@Composable
fun WaviqLeftNavRail(
    activeTab: WaviqTab,
    onSelectTab: (WaviqTab) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    NavigationRail(
        containerColor = HardwareSurface,
        contentColor = SignalWhite
    ) {
        Spacer(Modifier.height(16.dp))
        WaviqTab.entries.forEach { tab ->
            val selected = tab == activeTab
            NavigationRailItem(
                selected = selected,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSelectTab(tab)
                },
                icon = {
                    Icon(
                        imageVector = tabIcon(tab),
                        contentDescription = tab.title
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = SignalWhite,
                    selectedTextColor = SignalWhite,
                    indicatorColor = GlyphCrimson,
                    unselectedIconColor = TechSlate,
                    unselectedTextColor = TechSlate
                ),
                modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}")
            )
        }
    }
}

private fun tabIcon(tab: WaviqTab) = when (tab) {
    WaviqTab.ANALYZE -> Icons.Filled.Analytics
    WaviqTab.ENHANCE -> Icons.Filled.AutoAwesome
    WaviqTab.STUDIO -> Icons.Filled.GraphicEq
    WaviqTab.LIBRARY -> Icons.Filled.LibraryMusic
}

fun formatMs(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val m = totalSec / 60L
    val s = totalSec % 60L
    return String.format(Locale.US, "%02d:%02d", m, s)
}
```

---

## `app/src/main/java/com/example/ui/devmap/DeveloperMapScreen.kt`

```kotlin
package com.example.ui.devmap

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioTelemetry
import com.example.model.DevMapSchemas
import com.example.model.StartupCheckStatus
import com.example.model.StartupGuardReport
import com.example.model.StartupHealthState
import com.example.model.WaviqRouteRegistry
import com.example.model.WaviqRouteSpec
import com.example.model.WaviqTab
import com.example.model.WaviqUiState
import com.example.ui.components.HardwarePanel
import com.example.ui.components.TelemetryStatTile
import com.example.ui.theme.BusGreen
import com.example.ui.theme.ConsolePanel
import com.example.ui.theme.CyanScope
import com.example.ui.theme.GlyphCrimson
import com.example.ui.theme.GlyphCrimsonMuted
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.PeakAmber
import com.example.ui.theme.SignalWhite
import com.example.ui.theme.TechSlate
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun DeveloperMapScreen(
    state: WaviqUiState,
    onSelectFilter: (WaviqTab?) -> Unit,
    onInspectRoute: (Int) -> Unit,
    onSimulateDispatch: (Int, Boolean) -> Unit,
    onRerunStartupGuard: () -> Unit = {}
) {
    val inspected = WaviqRouteRegistry.byId(state.inspectedRouteId)
    val filteredRoutes = remember(state.devMapFilter) {
        val f = state.devMapFilter
        if (f == null) WaviqRouteRegistry.allRoutes else WaviqRouteRegistry.byTab(f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Dispatch telemetry banner
        Surface(
            color = ConsolePanel,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, GlyphCrimson)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "WAVIQ (1) // DEVELOPER NAVIGATION MAP",
                        style = MaterialTheme.typography.labelLarge,
                        color = SignalWhite
                    )
                    Text(
                        state.lastDispatchedLog,
                        style = MaterialTheme.typography.labelSmall,
                        color = BusGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Module Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipButton(
                label = "ALL (16)",
                selected = state.devMapFilter == null,
                onClick = { onSelectFilter(null) },
                testTag = "devmap_filter_all"
            )
            WaviqTab.entries.forEach { tab ->
                FilterChipButton(
                    label = "${tab.indexLabel}. ${tab.title}",
                    selected = state.devMapFilter == tab,
                    onClick = { onSelectFilter(tab) },
                    testTag = "devmap_filter_${tab.name.lowercase()}"
                )
            }
        }

        // 4x4 Screen Architecture Matrix
        HardwarePanel(
            title = "4×4 SCREEN ARCHITECTURE MATRIX",
            badge = "TAP NODE TO AUDIT"
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val columns = if (maxWidth >= 600.dp) 4 else 2
                val rows = filteredRoutes.chunked(columns)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { spec ->
                                MatrixNodeCard(
                                    spec = spec,
                                    isInspected = spec.id == inspected.id,
                                    isActiveLive = spec.id == state.activeRouteId,
                                    onClick = { onInspectRoute(spec.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(columns - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // StartupGuard Pre-Flight Diagnostics
        StartupGuardDiagnosticsPanel(
            report = state.startupGuard,
            onRerunStartupGuard = onRerunStartupGuard
        )

        // Live Interactive Route Inspector
        LiveRouteInspectorPanel(
            spec = inspected,
            telemetry = state.telemetry,
            onSimulateDispatch = { jump -> onSimulateDispatch(inspected.id, jump) }
        )

        // Global 16-Screen State Machine Flow ASCII Diagram
        HardwarePanel(
            title = "GLOBAL 16-SCREEN STATE MACHINE FLOW",
            badge = "BIDIRECTIONAL LOOP"
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF060607))
                    .border(1.dp, HairlineBorder, RoundedCornerShape(8.dp))
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = DevMapSchemas.GLOBAL_STATE_MACHINE_ASCII.trimIndent(),
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    color = SignalWhite
                )
            }
        }
    }
}

@Composable
private fun FilterChipButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        color = if (selected) GlyphCrimson else ConsolePanel,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (selected) GlyphCrimson else HairlineBorder),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = SignalWhite)
        }
    }
}

@Composable
private fun MatrixNodeCard(
    spec: WaviqRouteSpec,
    isInspected: Boolean,
    isActiveLive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    Surface(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        color = if (isInspected) GlyphCrimsonMuted else ConsolePanel,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = if (isInspected) 1.5.dp else 1.dp,
            color = when {
                isInspected -> GlyphCrimson
                isActiveLive -> BusGreen
                else -> HairlineBorder
            }
        ),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag("devmap_node_${spec.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = spec.code,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isInspected) SignalWhite else GlyphCrimson,
                    fontWeight = FontWeight.Bold
                )
                if (isActiveLive) {
                    Text("LIVE", style = MaterialTheme.typography.labelSmall, color = BusGreen)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = spec.title,
                style = MaterialTheme.typography.labelMedium,
                color = SignalWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = spec.path,
                style = MaterialTheme.typography.labelSmall,
                color = TechSlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LiveRouteInspectorPanel(
    spec: WaviqRouteSpec,
    telemetry: AudioTelemetry,
    onSimulateDispatch: (jumpToScreen: Boolean) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    var copiedRecently by remember(spec.id) { mutableStateOf(false) }

    LaunchedEffect(copiedRecently) {
        if (copiedRecently) {
            delay(1800)
            copiedRecently = false
        }
    }

    val effLatencyMs = if (telemetry.isBypassed) 0f else telemetry.dspLatencyMs
    val latencyAccent = when {
        telemetry.isBypassed -> TechSlate
        effLatencyMs > 15.0f -> GlyphCrimson
        effLatencyMs > 8.0f -> PeakAmber
        else -> BusGreen
    }

    HardwarePanel(
        title = "ROUTE INSPECTOR // ${spec.code} ${spec.title.uppercase()}",
        badge = spec.tab.title,
        accentBorder = true
    ) {
        // 1. Audio Core Bus State
        Text("AUDIO CORE BUS STATE", style = MaterialTheme.typography.labelSmall, color = TechSlate)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryStatTile("STATUS", spec.busStatus, Modifier.weight(1.2f), BusGreen)
            TelemetryStatTile("BUS MODE", spec.busMode.code, Modifier.weight(1f), GlyphCrimson)
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryStatTile(
                label = "DSP CHAIN LATENCY",
                value = String.format(Locale.US, "%.2f ms", effLatencyMs),
                accentColor = latencyAccent,
                modifier = Modifier
                    .weight(1f)
                    .testTag("devmap_bus_latency_tile")
            )
            TelemetryStatTile(
                label = "CROSSFADE SMOOTHING",
                value = spec.crossfadeSpec,
                accentColor = CyanScope,
                modifier = Modifier.weight(1.2f)
            )
        }

        Spacer(Modifier.height(14.dp))

        // 2. Transition Triggers
        Text("TRANSITION TRIGGERS", style = MaterialTheme.typography.labelSmall, color = TechSlate)
        Spacer(Modifier.height(6.dp))
        Surface(
            color = ConsolePanel,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, HairlineBorder)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("INBOUND: ${spec.inboundTriggers.joinToString(" | ")}", style = MaterialTheme.typography.labelSmall, color = SignalWhite)
                Text("OUTBOUND: ${spec.outboundSuccess}", style = MaterialTheme.typography.labelSmall, color = BusGreen)
                Text("BYPASS ALT: ${spec.alternateBypass}", style = MaterialTheme.typography.labelSmall, color = PeakAmber)
            }
        }

        Spacer(Modifier.height(14.dp))

        // 3. Hardware & Glyph Telemetry
        Text("HARDWARE & GLYPH TELEMETRY", style = MaterialTheme.typography.labelSmall, color = TechSlate)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryStatTile("RAM BUDGET", spec.peakRamBudget, Modifier.weight(1f))
            TelemetryStatTile("NPU ENGINE", spec.npuStatus, Modifier.weight(1f), BusGreen)
        }
        Spacer(Modifier.height(8.dp))
        Surface(
            color = ConsolePanel,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, HairlineBorder)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("HAPTIC: ${spec.hapticProfile}", style = MaterialTheme.typography.labelSmall, color = SignalWhite)
                Text("GLYPH LED: ${spec.glyphZoneAssignment}", style = MaterialTheme.typography.labelSmall, color = GlyphCrimson)
            }
        }

        Spacer(Modifier.height(14.dp))

        // 4. TypeScript Payload Schema + One-Click Copy
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("RoutePayloadSchema.ts", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
            OutlinedButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    clipboard.setText(AnnotatedString(spec.tsSchema))
                    copiedRecently = true
                },
                border = BorderStroke(1.dp, if (copiedRecently) BusGreen else HairlineBorder),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("copy_ts_type_button")
            ) {
                Icon(
                    imageVector = if (copiedRecently) Icons.Filled.Check else Icons.Filled.ContentCopy,
                    contentDescription = "Copy TypeScript type",
                    tint = if (copiedRecently) BusGreen else SignalWhite,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (copiedRecently) "COPIED" else "COPY TS TYPE",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (copiedRecently) BusGreen else SignalWhite
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF060607))
                .border(1.dp, HairlineBorder, RoundedCornerShape(8.dp))
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = spec.tsSchema,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = CyanScope
            )
        }

        Spacer(Modifier.height(14.dp))

        // 5. Simulate Dispatch & Jump Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSimulateDispatch(false)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ConsolePanel, contentColor = SignalWhite),
                border = BorderStroke(1.dp, GlyphCrimson),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("simulate_dispatch_button")
            ) {
                Icon(Icons.Filled.PlayCircleOutline, contentDescription = null, tint = GlyphCrimson, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("SIMULATE DISPATCH", style = MaterialTheme.typography.labelSmall)
            }

            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSimulateDispatch(true)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("jump_to_screen_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Launch, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("OPEN ${spec.code}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun StartupGuardDiagnosticsPanel(
    report: StartupGuardReport,
    onRerunStartupGuard: () -> Unit
) {
    val badgeColor = when (report.healthState) {
        StartupHealthState.HEALTHY -> BusGreen
        StartupHealthState.PERMISSION_ADVISORY, StartupHealthState.VALIDATING -> PeakAmber
        StartupHealthState.DEGRADED_FALLBACK -> GlyphCrimson
    }
    HardwarePanel(
        title = "STARTUP GUARD // PRE-FLIGHT DIAGNOSTICS",
        badge = report.healthState.label,
        accentBorder = report.isDegraded,
        modifier = Modifier.testTag("devmap_startup_guard_panel")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(report.tfliteCheck, report.databaseCheck, report.permissionCheck).forEach { check ->
                val color = when (check.status) {
                    StartupCheckStatus.PASS -> BusGreen
                    StartupCheckStatus.ADVISORY -> PeakAmber
                    StartupCheckStatus.FALLBACK -> GlyphCrimson
                }
                TelemetryStatTile(
                    label = check.title,
                    value = check.status.label,
                    accentColor = color,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Surface(
            color = ConsolePanel,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, HairlineBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = report.fallbackSummary,
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeColor
                )
                Text(
                    text = "TFLITE: ${report.tfliteCheck.detail}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TechSlate
                )
                Text(
                    text = "ROOM: ${report.databaseCheck.detail}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TechSlate
                )
                Text(
                    text = "HW PERM: ${report.permissionCheck.detail}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TechSlate
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onRerunStartupGuard,
            border = BorderStroke(1.dp, badgeColor),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("devmap_startup_guard_rerun_button")
        ) {
            Text("RE-RUN STARTUP GUARD", style = MaterialTheme.typography.labelSmall, color = SignalWhite)
        }
    }
}
```

---

## `app/src/main/java/com/example/ui/home/WaviqHomeScreen.kt`

```kotlin
package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.MasterTrackEntity
import com.example.model.EnhanceProfile
import com.example.model.ExportFormat
import com.example.model.WaviqUiState
import com.example.ui.theme.BusGreen
import com.example.ui.theme.ConsolePanel
import com.example.ui.theme.ElevatedModule
import com.example.ui.theme.GlyphCrimson
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.PeakAmber
import com.example.ui.theme.SignalWhite
import com.example.ui.theme.TechSlate
import java.util.Locale
import kotlin.math.max

@Composable
fun WaviqHomeScreen(
    state: WaviqUiState,
    savedMasters: List<MasterTrackEntity>,
    onImportAudio: () -> Unit,
    onRunCommand: (String) -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onSaveToDevice: (String) -> Unit,
    onLoadMaster: (MasterTrackEntity) -> Unit,
    onDeleteMaster: (Int) -> Unit,
    onSelectProfile: (EnhanceProfile) -> Unit,
    onUpdateDeck: (drive: Float?, width: Float?, punch: Float?, air: Float?) -> Unit,
    onSelectFormat: (ExportFormat) -> Unit
) {
    var command by rememberSaveable { mutableStateOf("") }
    var showFineControls by rememberSaveable { mutableStateOf(false) }
    var profileMenuExpanded by remember { mutableStateOf(false) }
    var formatMenuExpanded by remember { mutableStateOf(false) }

    val busy = state.isLoadingFile || state.exportJob.isExporting
    val exportComplete = state.exportJob.lastExportedPath != null
    val progress = if (state.exportJob.isExporting) state.exportJob.progress else state.processing.progress
    val localMode = when {
        state.tfliteRuntimeMode.contains("FALLBACK", ignoreCase = true) -> "LOCAL DSP FALLBACK"
        state.tfliteRuntimeMode.contains("LITERT", ignoreCase = true) -> "LITERT MODEL ACTIVE"
        else -> "LOCAL ENGINE READY"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Header(localMode = localMode, isPlaying = state.telemetry.isPlaying)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "YOUR SOUND.\nREFINED.",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = SignalWhite,
                lineHeight = MaterialTheme.typography.headlineLarge.lineHeight
            )
            Text(
                text = "One local audio intelligence engine. Import, describe, master.",
                style = MaterialTheme.typography.bodyMedium,
                color = TechSlate
            )
        }

        TrackCard(
            state = state,
            onImportAudio = onImportAudio,
            onPlayPause = onPlayPause,
            onSeek = onSeek
        )

        AssistantCard(
            command = command,
            onCommandChange = { command = it },
            response = state.assistantResponse,
            busy = busy,
            progress = progress,
            progressLabel = if (state.exportJob.isExporting) state.exportJob.stageLabel else state.processing.currentStage,
            onRun = {
                onRunCommand(command.ifBlank { "Master this track automatically" })
            },
            onQuickCommand = { value ->
                command = value
                onRunCommand(value)
            }
        )

        if (state.exportJob.errorMessage != null || state.fileErrorMessage != null) {
            StatusMessage(
                text = state.exportJob.errorMessage ?: state.fileErrorMessage.orEmpty(),
                accent = PeakAmber
            )
        }

        if (exportComplete) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ElevatedModule),
                border = androidx.compose.foundation.BorderStroke(1.dp, BusGreen.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = BusGreen)
                        Spacer(Modifier.width(9.dp))
                        Text("MASTER READY", color = BusGreen, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = state.exportJob.lastExportedTitle ?: "WAVIQ_MASTER.wav",
                        color = SignalWhite,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Metric(label = "LOUDNESS", value = String.format(Locale.US, "%.1f LUFS", state.exportJob.lastFinalLufs))
                        Metric(label = "TRUE PEAK", value = String.format(Locale.US, "%.1f dBTP", state.exportJob.lastFinalPeakDb))
                    }
                    Button(
                        onClick = { onSaveToDevice(state.exportJob.lastExportedTitle ?: "WAVIQ_MASTER.wav") },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BusGreen, contentColor = ObsidianVoid),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.SaveAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("SAVE MASTER TO DEVICE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                SectionHeading("QUICK CONTROL", "Keep the detail. Skip the clutter.")
                TextButton(onClick = { showFineControls = !showFineControls }) {
                    Icon(Icons.Filled.Tune, contentDescription = null, tint = GlyphCrimson, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (showFineControls) "HIDE" else "ADJUST", color = GlyphCrimson)
                }
            }
            AnimatedVisibility(
                visible = showFineControls,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ConsolePanel),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HairlineBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("MASTER PROFILE", color = TechSlate, style = MaterialTheme.typography.labelSmall)
                            Box {
                                OutlinedButton(onClick = { profileMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text(state.selectedProfile.title, color = SignalWhite, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                DropdownMenu(expanded = profileMenuExpanded, onDismissRequest = { profileMenuExpanded = false }) {
                                    EnhanceProfile.entries.forEach { profile ->
                                        DropdownMenuItem(
                                            text = { Text(profile.title) },
                                            onClick = {
                                                profileMenuExpanded = false
                                                onSelectProfile(profile)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        AdjustableSlider("DRIVE", state.deckParams.drive, "Saturation amount") { onUpdateDeck(it, null, null, null) }
                        AdjustableSlider("STEREO WIDTH", state.deckParams.width, "Spatial width") { onUpdateDeck(null, it, null, null) }
                        AdjustableSlider("PUNCH", state.deckParams.punch, "Transient impact") { onUpdateDeck(null, null, it, null) }
                        AdjustableSlider("AIR", state.deckParams.air, "High-frequency openness") { onUpdateDeck(null, null, null, it) }
                        Divider(color = HairlineBorder)
                        Text("EXPORT FORMAT", color = TechSlate, style = MaterialTheme.typography.labelSmall)
                        Box {
                            OutlinedButton(onClick = { formatMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(state.transcodeConfig.format.label, color = SignalWhite)
                            }
                            DropdownMenu(expanded = formatMenuExpanded, onDismissRequest = { formatMenuExpanded = false }) {
                                ExportFormat.entries.filter { it != ExportFormat.FLAC_PROFILE }.forEach { format ->
                                    DropdownMenuItem(
                                        text = { Text(format.label) },
                                        onClick = {
                                            formatMenuExpanded = false
                                            onSelectFormat(format)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        LibrarySection(
            masters = savedMasters,
            onLoadMaster = onLoadMaster,
            onDeleteMaster = onDeleteMaster
        )

        Footer()
    }
}

@Composable
private fun Header(localMode: String, isPlaying: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(SignalWhite),
                contentAlignment = Alignment.Center
            ) {
                Text("W", color = ObsidianVoid, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("WAVIQ", color = SignalWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text("AUDIO INTELLIGENCE", color = TechSlate, style = MaterialTheme.typography.labelSmall)
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(if (isPlaying) GlyphCrimson else BusGreen))
                Spacer(Modifier.width(6.dp))
                Text("OFFLINE READY", color = BusGreen, style = MaterialTheme.typography.labelSmall)
            }
            Text(localMode, color = TechSlate, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun TrackCard(
    state: WaviqUiState,
    onImportAudio: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ConsolePanel),
        border = androidx.compose.foundation.BorderStroke(1.dp, HairlineBorder),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("CURRENT TRACK", color = TechSlate, style = MaterialTheme.typography.labelSmall)
                Text("${state.trackInfo.sampleRate / 1000} kHz  •  ${state.trackInfo.channels} CH", color = TechSlate, style = MaterialTheme.typography.labelSmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(ElevatedModule),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.MusicNote, contentDescription = null, tint = GlyphCrimson, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        state.trackInfo.title,
                        color = SignalWhite,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(state.trackInfo.sourceLabel, color = TechSlate, style = MaterialTheme.typography.labelSmall)
                }
                IconButton(onClick = onPlayPause, modifier = Modifier.minimumInteractiveComponentSize().testTag("home_play_pause")) {
                    Icon(
                        if (state.telemetry.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (state.telemetry.isPlaying) "Pause preview" else "Play preview",
                        tint = SignalWhite
                    )
                }
            }
            WaveformStrip(
                values = state.trackInfo.waveformPeaks.ifEmpty { state.telemetry.spectrumBins },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            )
            Slider(
                value = if (state.telemetry.durationMs > 0L) {
                    (state.telemetry.positionMs.toFloat() / state.telemetry.durationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f,
                onValueChange = onSeek,
                colors = androidx.compose.material3.SliderDefaults.colors(
                    thumbColor = GlyphCrimson,
                    activeTrackColor = GlyphCrimson,
                    inactiveTrackColor = HairlineBorder
                ),
                modifier = Modifier.height(22.dp).testTag("home_track_seek")
            )
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(formatTime(state.telemetry.positionMs), color = TechSlate, style = MaterialTheme.typography.labelSmall)
                Text(formatTime(state.trackInfo.durationMs), color = TechSlate, style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(
                onClick = onImportAudio,
                enabled = !state.isLoadingFile,
                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("home_import_audio"),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isLoadingFile) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = GlyphCrimson, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.FolderOpen, contentDescription = null)
                }
                Spacer(Modifier.width(8.dp))
                Text(if (state.isLoadingFile) "LOADING AUDIO..." else "IMPORT AUDIO FILE")
            }
        }
    }
}

@Composable
private fun AssistantCard(
    command: String,
    onCommandChange: (String) -> Unit,
    response: String,
    busy: Boolean,
    progress: Float,
    progressLabel: String,
    onRun: () -> Unit,
    onQuickCommand: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ElevatedModule),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlyphCrimson.copy(alpha = 0.42f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(GlyphCrimson.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = GlyphCrimson)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("TALK TO WAVIQ", color = SignalWhite, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Arabic or English · processed on this device", color = TechSlate, style = MaterialTheme.typography.labelSmall)
                }
                Surface(color = ObsidianVoid, shape = RoundedCornerShape(50), border = androidx.compose.foundation.BorderStroke(1.dp, HairlineBorder)) {
                    Text("LOCAL", color = BusGreen, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp))
                }
            }
            OutlinedTextField(
                value = command,
                onValueChange = onCommandChange,
                modifier = Modifier.fillMaxWidth().testTag("local_assistant_command"),
                enabled = !busy,
                placeholder = { Text("e.g. Clean the noise and bring vocals forward…", color = TechSlate) },
                singleLine = false,
                maxLines = 3,
                shape = RoundedCornerShape(13.dp)
            )
            Text("QUICK ACTIONS", color = TechSlate, style = MaterialTheme.typography.labelSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                QuickChip("Clean", "Remove background noise", onQuickCommand)
                QuickChip("Vocals", "Make vocals clearer", onQuickCommand)
                QuickChip("Club", "Give it a club punch", onQuickCommand)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                QuickChip("Warm", "Make it warmer and analog", onQuickCommand)
                QuickChip("Streaming", "Prepare a balanced streaming master", onQuickCommand)
            }
            Button(
                onClick = onRun,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("run_local_auto_master"),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite, disabledContainerColor = GlyphCrimson.copy(alpha = 0.35f))
            ) {
                if (busy) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = SignalWhite, strokeWidth = 2.dp)
                    Spacer(Modifier.width(9.dp))
                    Text("WORKING LOCALLY…", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Filled.GraphicEq, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("RUN WAVIQ AUTO MASTER", fontWeight = FontWeight.Bold)
                }
            }
            if (busy) {
                LinearProgressIndicator(
                    progress = progress.coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = GlyphCrimson,
                    trackColor = HairlineBorder,
                    strokeCap = StrokeCap.Round
                )
                Text(progressLabel, color = TechSlate, style = MaterialTheme.typography.labelSmall)
            }
            if (response.isNotBlank()) {
                Divider(color = HairlineBorder)
                Row(verticalAlignment = Alignment.Top) {
                    Box(Modifier.padding(top = 5.dp).size(6.dp).clip(CircleShape).background(BusGreen))
                    Spacer(Modifier.width(9.dp))
                    Text(response, color = SignalWhite, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun QuickChip(label: String, command: String, onClick: (String) -> Unit) {
    AssistChip(
        onClick = { onClick(command) },
        label = { Text(label, maxLines = 1) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = ConsolePanel,
            labelColor = SignalWhite
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, HairlineBorder),
        modifier = Modifier.height(34.dp)
    )
}

@Composable
private fun AdjustableSlider(
    label: String,
    value: Float,
    hint: String,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(label, color = SignalWhite, style = MaterialTheme.typography.labelMedium)
                Text(hint, color = TechSlate, style = MaterialTheme.typography.labelSmall)
            }
            Text("${(value * 100).toInt()}%", color = GlyphCrimson, style = MaterialTheme.typography.labelMedium)
        }
        Slider(
            value = value.coerceIn(0f, 1f),
            onValueChange = onValueChange,
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = GlyphCrimson,
                activeTrackColor = GlyphCrimson,
                inactiveTrackColor = HairlineBorder
            )
        )
    }
}

@Composable
private fun LibrarySection(
    masters: List<MasterTrackEntity>,
    onLoadMaster: (MasterTrackEntity) -> Unit,
    onDeleteMaster: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeading("YOUR MASTERS", "Saved locally on this device")
        if (masters.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = ConsolePanel), shape = RoundedCornerShape(14.dp)) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.MusicNote, contentDescription = null, tint = TechSlate)
                    Spacer(Modifier.width(10.dp))
                    Text("Your finished masters will appear here.", color = TechSlate, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            masters.take(4).forEach { track ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onLoadMaster(track) },
                    colors = CardDefaults.cardColors(containerColor = ConsolePanel),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HairlineBorder),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Row(Modifier.padding(horizontal = 13.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = GlyphCrimson)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(track.title, color = SignalWhite, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${track.formatLabel}  •  ${track.sampleRate / 1000} kHz", color = TechSlate, style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = { onDeleteMaster(track.id) }) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete master", tint = TechSlate)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, color = SignalWhite, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(subtitle, color = TechSlate, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, color = TechSlate, style = MaterialTheme.typography.labelSmall)
        Text(value, color = SignalWhite, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun WaveformStrip(values: List<Float>, modifier: Modifier = Modifier) {
    val bars = values.take(72).ifEmpty { List(48) { 0.15f } }
    Canvas(modifier = modifier) {
        val gap = 3.dp.toPx()
        val barWidth = ((size.width - gap * (bars.size - 1)) / bars.size).coerceAtLeast(1f)
        val centerY = size.height / 2f
        bars.forEachIndexed { index, raw ->
            val strength = max(0.08f, raw.coerceIn(0f, 1f))
            val height = (size.height * (0.18f + strength * 0.78f)).coerceAtMost(size.height)
            val x = index * (barWidth + gap)
            drawRoundRect(
                color = if (index % 7 == 0) GlyphCrimson else TechSlate.copy(alpha = 0.72f),
                topLeft = Offset(x, centerY - height / 2f),
                size = Size(barWidth, height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f)
            )
        }
    }
}

@Composable
private fun StatusMessage(text: String, accent: Color) {
    Surface(color = accent.copy(alpha = 0.10f), shape = RoundedCornerShape(10.dp), border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.35f))) {
        Text(text, color = accent, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
    }
}

@Composable
private fun Footer() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("WAVIQ  /  BUILT FOR SOUND", color = TechSlate, style = MaterialTheme.typography.labelSmall)
        Text("No account · No cloud upload · Local processing", color = TechSlate.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000L).coerceAtLeast(0L)
    return String.format(Locale.US, "%d:%02d", totalSeconds / 60L, totalSeconds % 60L)
}
```

---

## `app/src/main/java/com/example/ui/screens/AnalyzeScreens.kt`

```kotlin
package com.example.ui.screens

import com.example.ui.components.InteractiveSpectralCurve
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.audio.BiquadFilter
import com.example.audio.FilterType
import com.example.model.MusicGenre
import com.example.model.StartupCheckStatus
import com.example.model.StartupHealthState
import com.example.model.WaviqUiState
import com.example.ui.components.HardwarePanel
import com.example.ui.components.TelemetryStatTile
import com.example.ui.components.WaveformScrubBar
import com.example.ui.components.formatMs
import com.example.ui.theme.BusGreen
import com.example.ui.theme.ConsolePanel
import com.example.ui.theme.CyanScope
import com.example.ui.theme.GlyphCrimson
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.MutedGrid
import com.example.ui.theme.PeakAmber
import com.example.ui.theme.SignalWhite
import com.example.ui.theme.TechSlate
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

@Composable
fun Screen01TrackInspector(
    state: WaviqUiState,
    onImportAudioClick: () -> Unit,
    onLoadReferenceClick: () -> Unit,
    onStartMicCaptureClick: () -> Unit,
    onStopMicCaptureClick: () -> Unit,
    onSeek: (Float) -> Unit,
    onGoToScope: () -> Unit,
    onRerunStartupGuard: () -> Unit = {},
    onDismissStartupBanner: () -> Unit = {}
) {
    val track = state.trackInfo
    val tel = state.telemetry
    val progress = if (tel.durationMs > 0L) (tel.positionMs.toFloat() / tel.durationMs).coerceIn(0f, 1f) else 0f
    val guard = state.startupGuard
    val showFallbackCard = !state.isStartupBannerDismissed &&
        (guard.healthState == StartupHealthState.DEGRADED_FALLBACK ||
            guard.healthState == StartupHealthState.PERMISSION_ADVISORY)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (showFallbackCard) {
            val isDegraded = guard.isDegraded
            val accent = if (isDegraded) GlyphCrimson else PeakAmber
            HardwarePanel(
                title = if (isDegraded) "STARTUP GUARD // FALLBACK MODE" else "STARTUP GUARD // HARDWARE ADVISORY",
                badge = guard.healthState.label,
                accentBorder = isDegraded,
                modifier = Modifier.testTag("startup_guard_fallback_card")
            ) {
                Text(
                    text = guard.fallbackSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SignalWhite
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(guard.tfliteCheck, guard.databaseCheck, guard.permissionCheck).forEach { check ->
                        val cColor = when (check.status) {
                            StartupCheckStatus.PASS -> BusGreen
                            StartupCheckStatus.ADVISORY -> PeakAmber
                            StartupCheckStatus.FALLBACK -> GlyphCrimson
                        }
                        TelemetryStatTile(
                            label = check.title,
                            value = check.status.label,
                            accentColor = cColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRerunStartupGuard,
                        border = BorderStroke(1.dp, accent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("startup_guard_rerun_button")
                    ) {
                        Icon(Icons.Filled.RestartAlt, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("RE-RUN AUDIT", style = MaterialTheme.typography.labelSmall, color = SignalWhite)
                    }
                    OutlinedButton(
                        onClick = onDismissStartupBanner,
                        border = BorderStroke(1.dp, HairlineBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("startup_guard_dismiss_button")
                    ) {
                        Text("DISMISS", style = MaterialTheme.typography.labelSmall, color = TechSlate)
                    }
                }
            }
        }

        HardwarePanel(
            title = "#01 // AUDIO TRACK INSPECTOR",
            badge = track.sourceLabel,
            accentBorder = true
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AudioFile,
                    contentDescription = "Active audio track",
                    tint = GlyphCrimson,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = SignalWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${track.sampleRate / 1000f} kHz  •  ${track.channels}CH STEREO  •  ${track.bitDepthLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TechSlate
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            WaveformScrubBar(
                peaks = track.waveformPeaks,
                progressFraction = progress,
                onSeek = onSeek
            )

            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatMs(tel.positionMs), style = MaterialTheme.typography.labelSmall, color = SignalWhite)
                Text("TAP WAVEFORM TO SCRUB", style = MaterialTheme.typography.labelSmall, color = TechSlate)
                Text(formatMs(tel.durationMs), style = MaterialTheme.typography.labelSmall, color = TechSlate)
            }

            if (state.isLoadingFile) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        color = GlyphCrimson,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("DECODING PCM STREAM...", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                }
            }

            state.fileErrorMessage?.let { err ->
                Spacer(Modifier.height(8.dp))
                Text(err, style = MaterialTheme.typography.bodyMedium, color = GlyphCrimson)
            }

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onImportAudioClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("import_audio_button")
                ) {
                    Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("IMPORT AUDIO", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onLoadReferenceClick,
                    border = BorderStroke(1.dp, HairlineBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("load_ref_button")
                ) {
                    Text("STUDIO REF", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                }
            }
        }

        val mic = state.micCapture
        HardwarePanel(
            title = "AUDIORECORD // LIVE MIC CAPTURE",
            badge = if (mic.isRecording) "REC ${formatMs(mic.durationMs)}" else "OPTIONAL INPUT",
            accentBorder = mic.isRecording
        ) {
            if (mic.isRecording) {
                LinearProgressIndicator(
                    progress = { (mic.inputLevel * 3.5f).coerceIn(0.05f, 1f) },
                    color = GlyphCrimson,
                    trackColor = ConsolePanel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onStopMicCaptureClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("stop_mic_capture_button")
                ) {
                    Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("STOP & LOAD MIC TAKE", style = MaterialTheme.typography.labelMedium)
                }
            } else {
                Text(
                    text = "Record live mono PCM directly into WAVIQ's 64-Bit DSP bus via AudioRecord.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TechSlate
                )
                mic.errorMessage?.let { msg ->
                    Spacer(Modifier.height(6.dp))
                    Text(msg, style = MaterialTheme.typography.labelSmall, color = PeakAmber)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onStartMicCaptureClick,
                    border = BorderStroke(1.dp, HairlineBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("start_mic_capture_button")
                ) {
                    Icon(Icons.Filled.Mic, contentDescription = null, tint = GlyphCrimson, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("CAPTURE LIVE MIC (AUDIORECORD)", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryStatTile(
                label = "INTEGRATED LUFS",
                value = String.format(Locale.US, "%.1f LUFS", track.integratedLufs),
                accentColor = BusGreen,
                modifier = Modifier.weight(1f)
            )
            TelemetryStatTile(
                label = "TRUE PEAK",
                value = String.format(Locale.US, "%.1f dBTP", track.truePeakDb),
                accentColor = if (track.truePeakDb > -0.5f) GlyphCrimson else SignalWhite,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryStatTile(
                label = "DYNAMIC RANGE",
                value = String.format(Locale.US, "%.1f dB", track.dynamicRangeDb),
                modifier = Modifier.weight(1f)
            )
            TelemetryStatTile(
                label = "STEREO WIDTH",
                value = "${track.stereoWidthPct}%",
                accentColor = CyanScope,
                modifier = Modifier.weight(1f)
            )
        }

        val diag = track.diagnostic
        val genreAccent = when (diag.detectedGenre) {
            MusicGenre.CLASSICAL, MusicGenre.ACOUSTIC -> BusGreen
            MusicGenre.POP, MusicGenre.ELECTRONIC -> CyanScope
            MusicGenre.ROCK, MusicGenre.HIP_HOP -> PeakAmber
        }

        HardwarePanel(
            title = "AUTO-AUDIT // SMART TRACK DIAGNOSTIC",
            badge = "AUTO-GENRE // ${diag.detectedGenre.title}",
            accentBorder = true,
            modifier = Modifier.testTag("smart_diagnostic_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryStatTile(
                    label = "DETECTED GENRE",
                    value = "${diag.detectedGenre.title} (${diag.genreConfidencePct}%)",
                    accentColor = genreAccent,
                    modifier = Modifier.weight(1.2f).testTag("smart_genre_tile")
                )
                TelemetryStatTile(
                    label = "AUTO HI-RES TARGET",
                    value = "${diag.detectedGenre.targetKhz}k • ${diag.detectedGenre.oversampling.factor}X ISP",
                    accentColor = BusGreen,
                    modifier = Modifier.weight(1f).testTag("smart_hires_target_tile")
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryStatTile(
                    label = "RECORDING TYPE",
                    value = diag.recordingTypeLabel,
                    accentColor = SignalWhite,
                    modifier = Modifier.weight(1.2f)
                )
                TelemetryStatTile(
                    label = "CLARITY NEED",
                    value = diag.clarityNeedLabel,
                    accentColor = when (diag.clarityNeedLabel) {
                        "HIGH" -> GlyphCrimson
                        "MODERATE" -> PeakAmber
                        else -> BusGreen
                    },
                    modifier = Modifier.weight(0.8f)
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryStatTile(
                    label = "HF BANDWIDTH",
                    value = String.format(Locale.US, "%.1f kHz (-%d%%)", diag.estimatedBandwidthKhz, diag.hfDeficitPct),
                    accentColor = CyanScope,
                    modifier = Modifier.weight(1.15f)
                )
                TelemetryStatTile(
                    label = "PEAK GUARD",
                    value = String.format(Locale.US, "%.1f dBTP (%dX)", state.deckParams.ceilingDb, state.oversamplingFactor.factor),
                    accentColor = BusGreen,
                    modifier = Modifier.weight(0.85f)
                )
            }
            Spacer(Modifier.height(10.dp))
            Surface(
                color = ConsolePanel,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, HairlineBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = diag.autoAppliedSummary,
                        style = MaterialTheme.typography.labelSmall,
                        color = BusGreen
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "RATIONALE: ${diag.genreRationale}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TechSlate
                    )
                }
            }
        }

        InteractiveSpectralCurve(
            genre = diag.detectedGenre,
            bandwidthKhz = diag.estimatedBandwidthKhz,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        Button(
            onClick = onGoToScope,
            colors = ButtonDefaults.buttonColors(containerColor = ConsolePanel, contentColor = SignalWhite),
            border = BorderStroke(1.dp, HairlineBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("go_to_scope_button")
        ) {
            Text("NEXT: #02 LIVE SCOPE ANALYZER", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun Screen02LiveScope(
    state: WaviqUiState,
    onTogglePlay: () -> Unit,
    onGoToEq: () -> Unit
) {
    val tel = state.telemetry
    // 0 = 64-BIN FFT, 1 = 3D VOLUMETRIC, 2 = WAVE SCOPE
    var scopeMode by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Nothing OS 8K / Hi-Res Master Telemetry Badges
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HiResBadge("8K / HI-RES MASTER", GlyphCrimson)
            HiResBadge("${state.hiResTargetKhz}KHZ HF RESTORE", BusGreen)
            HiResBadge(state.oversamplingFactor.label, CyanScope)
            HiResBadge(state.tfliteRuntimeMode, PeakAmber)
        }

        HardwarePanel(
            title = "#02 // LIVE SCOPE ANALYZER",
            badge = if (tel.isPlaying) "60HZ VOLUMETRIC" else "PAUSED — TAP PLAY",
            accentBorder = tel.isPlaying
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    onClick = { scopeMode = 0 },
                    color = if (scopeMode == 0) GlyphCrimson else ConsolePanel,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("scope_mode_fft")
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                        Text("64-BIN FFT", style = MaterialTheme.typography.labelSmall, color = SignalWhite)
                    }
                }
                Surface(
                    onClick = { scopeMode = 1 },
                    color = if (scopeMode == 1) GlyphCrimson else ConsolePanel,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("scope_mode_volumetric")
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                        Text("3D VOLUMETRIC", style = MaterialTheme.typography.labelSmall, color = SignalWhite)
                    }
                }
                Surface(
                    onClick = { scopeMode = 2 },
                    color = if (scopeMode == 2) GlyphCrimson else ConsolePanel,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("scope_mode_osc")
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                        Text("WAVE SCOPE", style = MaterialTheme.typography.labelSmall, color = SignalWhite)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF070708))
                    .border(1.dp, HairlineBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    for (g in 1..3) {
                        val y = size.height * (g / 4f)
                        drawLine(
                            color = Color(0xFF1F1F24),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                    }

                    when (scopeMode) {
                        0 -> {
                            val bins = tel.spectrumBins
                            val slot = size.width / bins.size.coerceAtLeast(1)
                            val barW = (slot * 0.72f).coerceAtLeast(2f)
                            bins.forEachIndexed { idx, v ->
                                val norm = v.coerceIn(0.03f, 1f)
                                val barH = norm * size.height
                                val color = when {
                                    norm > 0.85f -> GlyphCrimson
                                    norm > 0.62f -> PeakAmber
                                    else -> SignalWhite.copy(alpha = 0.85f)
                                }
                                drawRoundRect(
                                    color = color,
                                    topLeft = Offset(idx * slot, size.height - barH),
                                    size = Size(barW, barH),
                                    cornerRadius = CornerRadius(2f, 2f)
                                )
                            }
                        }
                        1 -> {
                            // 3D Volumetric Dual-Mirrored Spectrum with perspective depth shadow bars
                            val bins = tel.spectrumBins
                            val horizonY = size.height * 0.62f
                            val slot = size.width / bins.size.coerceAtLeast(1)
                            val barW = (slot * 0.68f).coerceAtLeast(2f)

                            // Horizon line
                            drawLine(
                                color = GlyphCrimson.copy(alpha = 0.45f),
                                start = Offset(0f, horizonY),
                                end = Offset(size.width, horizonY),
                                strokeWidth = 1.5f
                            )

                            bins.forEachIndexed { idx, v ->
                                val norm = v.coerceIn(0.04f, 1f)
                                val upH = norm * horizonY * 0.92f
                                val x = idx * slot

                                // Depth shadow layer offset for 3D volumetric illusion
                                drawRoundRect(
                                    color = GlyphCrimson.copy(alpha = 0.25f),
                                    topLeft = Offset(x + 3f, horizonY - upH - 4f),
                                    size = Size(barW, upH),
                                    cornerRadius = CornerRadius(2f, 2f)
                                )
                                // Primary upper volumetric crest
                                val crestColor = when {
                                    norm > 0.82f -> GlyphCrimson
                                    norm > 0.58f -> SignalWhite
                                    else -> CyanScope.copy(alpha = 0.85f)
                                }
                                drawRoundRect(
                                    color = crestColor,
                                    topLeft = Offset(x, horizonY - upH),
                                    size = Size(barW, upH),
                                    cornerRadius = CornerRadius(2f, 2f)
                                )
                                // Lower volumetric floor reflection
                                val downH = norm * (size.height - horizonY) * 0.88f
                                drawRoundRect(
                                    color = GlyphCrimson.copy(alpha = 0.42f),
                                    topLeft = Offset(x, horizonY + 2f),
                                    size = Size(barW, downH),
                                    cornerRadius = CornerRadius(2f, 2f)
                                )
                            }
                        }
                        else -> {
                            val samples = tel.scopeSamples
                            if (samples.size > 1) {
                                val path = Path()
                                val stepX = size.width / (samples.size - 1)
                                val midY = size.height / 2f
                                samples.forEachIndexed { i, s ->
                                    val x = i * stepX
                                    val y = midY - (s.coerceIn(-1f, 1f) * midY * 0.85f)
                                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                }
                                drawPath(
                                    path = path,
                                    color = GlyphCrimson,
                                    style = Stroke(width = 3f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("20Hz", style = MaterialTheme.typography.labelSmall, color = TechSlate)
                Text("250Hz", style = MaterialTheme.typography.labelSmall, color = TechSlate)
                Text("1kHz", style = MaterialTheme.typography.labelSmall, color = TechSlate)
                Text("6kHz", style = MaterialTheme.typography.labelSmall, color = TechSlate)
                Text("20kHz", style = MaterialTheme.typography.labelSmall, color = TechSlate)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryStatTile(
                label = "LIVE LUFS",
                value = String.format(Locale.US, "%.1f", tel.integratedLufs),
                accentColor = BusGreen,
                modifier = Modifier.weight(1f)
            )
            TelemetryStatTile(
                label = "CORRELATION",
                value = String.format(Locale.US, "%+.2f", tel.stereoCorrelation),
                accentColor = if (tel.stereoCorrelation < 0f) GlyphCrimson else CyanScope,
                modifier = Modifier.weight(1f)
            )
            TelemetryStatTile(
                label = "L / R VU",
                value = "${tel.leftVuDb.toInt()} / ${tel.rightVuDb.toInt()} dB",
                modifier = Modifier.weight(1.3f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryStatTile(
                label = "NEURAL GATE",
                value = String.format(Locale.US, "-%.0f%%", tel.neuralGateReductionPct),
                accentColor = if (tel.neuralGateReductionPct > 1f) BusGreen else TechSlate,
                modifier = Modifier
                    .weight(1f)
                    .testTag("scope_neural_gate_tile")
            )
            TelemetryStatTile(
                label = "DYN GAIN",
                value = String.format(Locale.US, "%+.1f dB", tel.neuralDynGainDb),
                accentColor = if (kotlin.math.abs(tel.neuralDynGainDb) > 0.1f) PeakAmber else SignalWhite,
                modifier = Modifier
                    .weight(1f)
                    .testTag("scope_neural_gain_tile")
            )
        }

        if (!tel.isPlaying) {
            Button(
                onClick = onTogglePlay,
                colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("scope_start_playback_button")
            ) {
                Text("START REAL-TIME AUDIO TAP", style = MaterialTheme.typography.labelLarge)
            }
        }

        Button(
            onClick = onGoToEq,
            colors = ButtonDefaults.buttonColors(containerColor = ConsolePanel, contentColor = SignalWhite),
            border = BorderStroke(1.dp, HairlineBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("go_to_eq_button")
        ) {
            Text("NEXT: #03 PARAMETRIC EQ & FILTERS", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun Screen03ParametricEq(
    state: WaviqUiState,
    onUpdateBand: (String, Float) -> Unit,
    onResetEq: () -> Unit,
    onGoToReports: () -> Unit
) {
    val bands = state.eqBands

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#03 // PARAMETRIC EQ & BIQUAD FILTERS",
            badge = "5-BAND ACTIVE",
            accentBorder = true
        ) {
            // Live calculated EQ frequency response curve
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF070708))
                    .border(1.dp, HairlineBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val midY = size.height / 2f
                    // 0 dB center line
                    drawLine(
                        color = MutedGrid,
                        start = Offset(0f, midY),
                        end = Offset(size.width, midY),
                        strokeWidth = 1.5f
                    )

                    val tempFilters = bands.mapIndexed { idx, band ->
                        BiquadFilter(48000f).apply {
                            val type = when (idx) {
                                0 -> FilterType.LOW_SHELF
                                4 -> FilterType.HIGH_SHELF
                                else -> FilterType.PEAKING
                            }
                            configure(type, band.frequencyHz, band.qFactor, band.gainDb, 48000f)
                        }
                    }

                    val points = 80
                    val path = Path()
                    val minLog = ln(20f)
                    val maxLog = ln(20000f)
                    for (i in 0 until points) {
                        val frac = i.toFloat() / (points - 1)
                        val freq = kotlin.math.exp(minLog + frac * (maxLog - minLog))
                        var totalDb = 0f
                        for (f in tempFilters) {
                            totalDb += f.magnitudeDbAt(freq)
                        }
                        val clampedDb = totalDb.coerceIn(-14f, 14f)
                        val x = frac * size.width
                        val y = midY - (clampedDb / 14f) * (size.height * 0.45f)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path = path, color = GlyphCrimson, style = Stroke(width = 3.5f))

                    // Band control nodes
                    bands.forEach { band ->
                        val frac = ((ln(band.frequencyHz) - minLog) / (maxLog - minLog)).coerceIn(0f, 1f)
                        val cx = frac * size.width
                        val cy = midY - (band.gainDb / 14f) * (size.height * 0.45f)
                        drawCircle(color = SignalWhite, radius = 7f, center = Offset(cx, cy))
                        drawCircle(color = GlyphCrimson, radius = 4f, center = Offset(cx, cy))
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("REAL-TIME BIQUAD RESPONSE (±12 dB)", style = MaterialTheme.typography.labelSmall, color = TechSlate)
                OutlinedButton(
                    onClick = onResetEq,
                    border = BorderStroke(1.dp, HairlineBorder),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("eq_reset_button")
                ) {
                    Icon(Icons.Filled.RestartAlt, contentDescription = null, tint = SignalWhite, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("FLAT", style = MaterialTheme.typography.labelSmall, color = SignalWhite)
                }
            }

            Spacer(Modifier.height(8.dp))

            bands.forEach { band ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${band.label} // ${band.frequencyHz.toInt()} Hz (Q ${band.qFactor})",
                            style = MaterialTheme.typography.labelMedium,
                            color = SignalWhite
                        )
                        Text(
                            text = String.format(Locale.US, "%+.1f dB", band.gainDb),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (band.gainDb == 0f) TechSlate else GlyphCrimson
                        )
                    }
                    Slider(
                        value = band.gainDb,
                        onValueChange = { onUpdateBand(band.id, it) },
                        valueRange = -12f..12f,
                        colors = SliderDefaults.colors(
                            thumbColor = SignalWhite,
                            activeTrackColor = GlyphCrimson,
                            inactiveTrackColor = HairlineBorder
                        ),
                        modifier = Modifier.testTag("eq_slider_${band.id}")
                    )
                }
            }
        }

        Button(
            onClick = onGoToReports,
            colors = ButtonDefaults.buttonColors(containerColor = ConsolePanel, contentColor = SignalWhite),
            border = BorderStroke(1.dp, HairlineBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("go_to_reports_button")
        ) {
            Text("NEXT: #04 ANALYSIS REPORTS", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun Screen04AnalysisReports(
    state: WaviqUiState,
    onGoToEnhanceEngine: () -> Unit
) {
    val track = state.trackInfo
    val lufsDelta = -14.0f - track.integratedLufs
    val headroomOk = track.truePeakDb <= -1.0f
    val dynamicHealthy = track.dynamicRangeDb >= 7.0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#04 // MASTERING AUDIT REPORT",
            badge = "AIR-GAPPED DIAGNOSTIC",
            accentBorder = true
        ) {
            AuditRow(
                label = "STREAMING TARGET (-14.0 LUFS)",
                detail = String.format(Locale.US, "Current %.1f LUFS (Delta %+.1f dB)", track.integratedLufs, lufsDelta),
                passed = kotlin.math.abs(lufsDelta) < 4f
            )
            Spacer(Modifier.height(10.dp))
            AuditRow(
                label = "TRUE-PEAK HEADROOM (<= -1.0 dBTP)",
                detail = String.format(Locale.US, "Measured %.1f dBTP inter-sample peak", track.truePeakDb),
                passed = headroomOk
            )
            Spacer(Modifier.height(10.dp))
            AuditRow(
                label = "CREST FACTOR / DYNAMIC RANGE",
                detail = String.format(Locale.US, "%.1f dB DR — ${if (dynamicHealthy) "Punch Preserved" else "Over-Compressed"}", track.dynamicRangeDb),
                passed = dynamicHealthy
            )
            Spacer(Modifier.height(10.dp))
            AuditRow(
                label = "STEREO PHASE & PROFILE (${track.diagnostic.recordingTypeLabel})",
                detail = "${track.stereoWidthPct}% width — ${track.diagnostic.clarityNeedLabel} restoration applied",
                passed = true
            )
            Spacer(Modifier.height(10.dp))
            AuditRow(
                label = "HIGH-FREQUENCY BANDWIDTH (${String.format(Locale.US, "%.1f", track.diagnostic.estimatedBandwidthKhz)} kHz)",
                detail = "${track.diagnostic.hfDeficitPct}% upper-band deficit — Hi-Res upsampler auto-calibrated",
                passed = track.diagnostic.hfDeficitPct < 50
            )
        }

        HardwarePanel(title = "RECOMMENDED DSP ACTION") {
            Text(
                text = if (lufsDelta > 1.5f) {
                    "Apply +${String.format(Locale.US, "%.1f", lufsDelta)} dB gain staging with True-Peak ceiling at -1.0 dBTP and subtle 12kHz air lift."
                } else {
                    "Track loudness is near streaming target. Engage Autonomous AI Engine (#05) for harmonic glue and stereo polish."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = SignalWhite
            )
        }

        Button(
            onClick = onGoToEnhanceEngine,
            colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("go_to_enhance_button")
        ) {
            Text("ENGAGE TAB 2: #05 AUTONOMOUS AI ENGINE", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun AuditRow(
    label: String,
    detail: String,
    passed: Boolean
) {
    Surface(
        color = ConsolePanel,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, HairlineBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (passed) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                contentDescription = if (passed) "Audit passed" else "Audit warning",
                tint = if (passed) BusGreen else PeakAmber,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                Text(detail, style = MaterialTheme.typography.bodyMedium, color = TechSlate)
            }
        }
    }
}

@Composable
fun HiResBadge(
    label: String,
    accent: Color
) {
    Surface(
        color = ConsolePanel,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, accent)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
```

---

## `app/src/main/java/com/example/ui/screens/EnhanceScreens.kt`

```kotlin
package com.example.ui.screens

import com.example.ui.components.InteractiveSpectralCurve
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.EnhanceProfile
import com.example.model.MusicGenre
import com.example.model.TactileDeckParams
import com.example.model.WaviqUiState
import com.example.ui.components.HardwarePanel
import com.example.ui.components.TactileRotarySlider
import com.example.ui.components.TelemetryStatTile
import com.example.ui.theme.BusGreen
import com.example.ui.theme.ConsolePanel
import com.example.ui.theme.CyanScope
import com.example.ui.theme.GlyphCrimson
import com.example.ui.theme.GlyphCrimsonMuted
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.MutedGrid
import com.example.ui.theme.PeakAmber
import com.example.ui.theme.SignalWhite
import com.example.ui.theme.TechSlate
import java.util.Locale

@Composable
fun Screen05AutonomousEngine(
    state: WaviqUiState,
    onSelectProfile: (EnhanceProfile) -> Unit,
    onSelectGenre: (MusicGenre) -> Unit = {},
    onDispatchDeepProcessing: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val diag = state.trackInfo.diagnostic
    val activeGenre = state.selectedGenre

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#05 // SMART GENRE ADAPTIVE MASTERING",
            badge = if (state.isGenreAutoDetected && activeGenre == diag.detectedGenre) {
                "AUTO-DETECTED (${diag.genreConfidencePct}%)"
            } else {
                "MANUAL OVERRIDE"
            },
            accentBorder = true
        ) {
            Text(
                text = "Adaptive DSP presets tuned by real-time spectral FFT and transient crest analysis. Selecting a genre auto-calibrates EQ, Harmonic Drive, Hi-Res Target, and True-Peak limiting.",
                style = MaterialTheme.typography.bodyMedium,
                color = TechSlate
            )
            Spacer(Modifier.height(12.dp))

            MusicGenre.entries.forEach { genre ->
                val selected = genre == activeGenre
                val isDetected = genre == diag.detectedGenre
                val genreBorder = when (genre) {
                    MusicGenre.CLASSICAL, MusicGenre.ACOUSTIC -> BusGreen
                    MusicGenre.POP, MusicGenre.ELECTRONIC -> CyanScope
                    MusicGenre.ROCK, MusicGenre.HIP_HOP -> PeakAmber
                }

                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectGenre(genre)
                    },
                    color = if (selected) GlyphCrimsonMuted else ConsolePanel,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = if (selected) 1.5.dp else 1.dp,
                        color = if (selected) GlyphCrimson else if (isDetected) genreBorder.copy(alpha = 0.7f) else HairlineBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("genre_card_${genre.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(genre.title, style = MaterialTheme.typography.labelLarge, color = SignalWhite)
                                if (isDetected) {
                                    Spacer(Modifier.width(8.dp))
                                    Surface(
                                        color = genreBorder.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(1.dp, genreBorder)
                                    ) {
                                        Text(
                                            text = "DETECTED",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = genreBorder,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(genre.subtitle, style = MaterialTheme.typography.bodyMedium, color = TechSlate)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${genre.targetKhz}k • ${genre.oversampling.factor}X",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) SignalWhite else BusGreen
                            )
                            Text(
                                text = "${genre.ceilingDb} dBTP • ${genre.targetLufs} LUFS",
                                style = MaterialTheme.typography.labelSmall,
                                color = TechSlate
                            )
                        }
                    }
                }
            }
        }

        InteractiveSpectralCurve(
            genre = activeGenre,
            bandwidthKhz = diag.estimatedBandwidthKhz,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        HardwarePanel(
            title = "TARGET STREAMING PROFILES",
            badge = "AIR-GAPPED NPU"
        ) {
            Text(
                text = "Manual override profiles for fine-tuning dynamic range, target LUFS, and brickwall saturation.",
                style = MaterialTheme.typography.bodyMedium,
                color = TechSlate
            )
            Spacer(Modifier.height(12.dp))

            EnhanceProfile.entries.forEach { profile ->
                val selected = profile == state.selectedProfile
                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectProfile(profile)
                    },
                    color = if (selected) GlyphCrimsonMuted else ConsolePanel,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (selected) GlyphCrimson else HairlineBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("profile_card_${profile.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(profile.title, style = MaterialTheme.typography.labelLarge, color = SignalWhite)
                            Spacer(Modifier.height(2.dp))
                            Text(profile.subtitle, style = MaterialTheme.typography.bodyMedium, color = TechSlate)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${profile.ceilingDb} dBTP",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) SignalWhite else TechSlate
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onDispatchDeepProcessing()
            },
            colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("run_deep_processing_button")
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("DISPATCH #06 DEEP 3.5S PROCESSING", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun Screen06DeepProcessing(
    state: WaviqUiState,
    onRunPass: () -> Unit,
    onGoToDeck: () -> Unit
) {
    val proc = state.processing
    val passes = listOf(
        "PASS 1 // SPECTRAL FFT AUDIT",
        "PASS 2 // HARMONIC TUBE CALIBRATION",
        "PASS 3 // MULTIBAND DYNAMICS ALIGN",
        "PASS 4 // TRUE-PEAK LIMITER SEAL"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#06 // DEEP 3.5S PROCESSING",
            badge = if (proc.isRunning) "NPU_DSP_ACTIVE" else if (proc.hasCompletedOnce) "LOCKED 100%" else "STANDBY",
            accentBorder = true
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(144.dp)) {
                    drawArc(
                        color = MutedGrid,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 12f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = if (proc.progress >= 0.99f) BusGreen else GlyphCrimson,
                        startAngle = -90f,
                        sweepAngle = 360f * proc.progress.coerceIn(0f, 1f),
                        useCenter = false,
                        style = Stroke(width = 12f, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(proc.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.displaySmall,
                        color = SignalWhite
                    )
                    Text(
                        text = state.selectedProfile.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = TechSlate
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = proc.currentStage,
                style = MaterialTheme.typography.labelLarge,
                color = if (proc.hasCompletedOnce && !proc.isRunning) BusGreen else GlyphCrimson
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { proc.progress.coerceIn(0f, 1f) },
                color = GlyphCrimson,
                trackColor = ConsolePanel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(Modifier.height(14.dp))
            passes.forEachIndexed { idx, label ->
                val done = proc.completedPasses > idx
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = if (done) BusGreen else MutedGrid,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (done) SignalWhite else TechSlate
                    )
                }
            }
        }

        if (!proc.isRunning) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onRunPass,
                    colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("rerun_35s_button")
                ) {
                    Text("RUN 3.5S PASS", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick = onGoToDeck,
                    colors = ButtonDefaults.buttonColors(containerColor = ConsolePanel, contentColor = SignalWhite),
                    border = BorderStroke(1.dp, HairlineBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("go_to_deck_button")
                ) {
                    Text("#07 TACTILE DECK", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun Screen07TactileDeck(
    state: WaviqUiState,
    onUpdateDeck: ((TactileDeckParams) -> TactileDeckParams) -> Unit,
    onGoToAb: () -> Unit
) {
    val d = state.deckParams
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HardwarePanel(
            title = "#07 // TACTILE MASTERING DECK",
            badge = "10MS HAPTIC DETENTS",
            accentBorder = true
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TactileRotarySlider(
                    label = "HARMONIC DRIVE",
                    value = d.drive,
                    valueDisplay = "${(d.drive * 100).toInt()}%",
                    onValueChange = { v -> onUpdateDeck { it.copy(drive = v) } },
                    testTag = "deck_drive_slider"
                )
                TactileRotarySlider(
                    label = "STEREO WIDTH (M/S)",
                    value = d.width,
                    valueDisplay = "${(d.width * 150).toInt()}%",
                    onValueChange = { v -> onUpdateDeck { it.copy(width = v) } },
                    testTag = "deck_width_slider"
                )
                TactileRotarySlider(
                    label = "TRANSIENT PUNCH",
                    value = d.punch,
                    valueDisplay = "${(d.punch * 100).toInt()}%",
                    onValueChange = { v -> onUpdateDeck { it.copy(punch = v) } },
                    testTag = "deck_punch_slider"
                )
                TactileRotarySlider(
                    label = "AIR EXCITER (10kHz)",
                    value = d.air,
                    valueDisplay = String.format(Locale.US, "%+.1f dB", (d.air - 0.35f) * 9f),
                    onValueChange = { v -> onUpdateDeck { it.copy(air = v) } },
                    testTag = "deck_air_slider"
                )
                TactileRotarySlider(
                    label = "LIMITER CEILING",
                    value = d.ceilingDb,
                    valueDisplay = String.format(Locale.US, "%.1f dBTP", d.ceilingDb),
                    onValueChange = { v -> onUpdateDeck { it.copy(ceilingDb = v) } },
                    valueRange = -3.0f..-0.1f,
                    testTag = "deck_ceiling_slider"
                )
            }
        }

        Button(
            onClick = onGoToAb,
            colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("go_to_ab_button")
        ) {
            Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("NEXT: #08 ORIGINAL VS ENHANCED A/B", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun Screen08AbCompare(
    state: WaviqUiState,
    onToggleAb: (Boolean) -> Unit,
    onGoToStudioPro: () -> Unit
) {
    val enhanced = state.abEnhancedEngaged
    val haptics = LocalHapticFeedback.current
    val tel = state.telemetry

    val cardBorder by animateColorAsState(
        if (enhanced) GlyphCrimson else HairlineBorder,
        label = "abBorder"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#08 // ORIGINAL VS ENHANCED A/B",
            badge = "15MS CONSTANT-POWER",
            accentBorder = enhanced
        ) {
            Text(
                text = "Flip seamlessly between raw bypassed input (A) and the full NPU/DSP mastering chain (B) with 15ms constant-power crossfade smoothing.",
                style = MaterialTheme.typography.bodyMedium,
                color = TechSlate
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleAb(false)
                    },
                    color = if (!enhanced) SignalWhite else ConsolePanel,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (!enhanced) SignalWhite else HairlineBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .testTag("ab_button_original")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "A // ORIGINAL",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (!enhanced) com.example.ui.theme.ObsidianVoid else SignalWhite
                        )
                        Text(
                            "DRY BYPASS",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (!enhanced) com.example.ui.theme.ObsidianVoid else TechSlate
                        )
                    }
                }

                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleAb(true)
                    },
                    color = if (enhanced) GlyphCrimson else ConsolePanel,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .testTag("ab_button_enhanced")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "B // ENHANCED",
                            style = MaterialTheme.typography.labelLarge,
                            color = SignalWhite
                        )
                        Text(
                            state.selectedProfile.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (enhanced) SignalWhite else TechSlate
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryStatTile(
                    label = "ACTIVE BUS",
                    value = if (enhanced) "ENHANCED WET" else "ORIGINAL DRY",
                    accentColor = if (enhanced) GlyphCrimson else SignalWhite,
                    modifier = Modifier.weight(1f)
                )
                TelemetryStatTile(
                    label = "REAL-TIME LUFS",
                    value = String.format(Locale.US, "%.1f LUFS", tel.integratedLufs),
                    accentColor = BusGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Button(
            onClick = onGoToStudioPro,
            colors = ButtonDefaults.buttonColors(containerColor = ConsolePanel, contentColor = SignalWhite),
            border = BorderStroke(1.dp, HairlineBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("go_to_studio_pro_button")
        ) {
            Icon(Icons.Filled.CompareArrows, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("NEXT TAB 3: #09 AI STUDIO PRO SUITE", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}
```

---

## `app/src/main/java/com/example/ui/screens/LibraryScreens.kt`

```kotlin
package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.MasterTrackEntity
import com.example.model.ExportFormat
import com.example.model.WaviqUiState
import com.example.ui.components.HardwarePanel
import com.example.ui.components.TelemetryStatTile
import com.example.ui.components.WaveformScrubBar
import com.example.ui.components.formatMs
import com.example.ui.theme.BusGreen
import com.example.ui.theme.ConsolePanel
import com.example.ui.theme.GlyphCrimson
import com.example.ui.theme.GlyphCrimsonMuted
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.SignalWhite
import com.example.ui.theme.TechSlate
import java.util.Locale

@Composable
fun Screen13TranscodeMatrix(
    state: WaviqUiState,
    onUpdateConfig: (ExportFormat?, Boolean?, Boolean?) -> Unit,
    onStartExport: () -> Unit
) {
    val cfg = state.transcodeConfig
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#13 // TRANSCODE & ENCODING MATRIX",
            badge = "OFFLINE PCM RENDER",
            accentBorder = true
        ) {
            ExportFormat.entries.forEach { fmt ->
                val selected = cfg.format == fmt
                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onUpdateConfig(fmt, null, null)
                    },
                    color = if (selected) GlyphCrimsonMuted else ConsolePanel,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (selected) GlyphCrimson else HairlineBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("export_format_${fmt.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(fmt.label, style = MaterialTheme.typography.labelLarge, color = SignalWhite)
                            Text(fmt.codecDesc, style = MaterialTheme.typography.bodyMedium, color = TechSlate)
                        }
                        Text(
                            text = if (fmt.bitDepth == 32) "32F // 192K" else "${fmt.bitDepth}-BIT",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) SignalWhite else TechSlate
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("TPDF SHAPED DITHER", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                    Text("Triangular noise-shaping for 16-bit & 24-bit PCM masters", style = MaterialTheme.typography.bodyMedium, color = TechSlate)
                }
                Switch(
                    checked = cfg.ditherEnabled,
                    onCheckedChange = { onUpdateConfig(null, it, null) },
                    colors = SwitchDefaults.colors(checkedTrackColor = GlyphCrimson, checkedThumbColor = SignalWhite),
                    modifier = Modifier.testTag("transcode_dither_switch")
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("NORMALIZE CEILING (-1.0 dBTP)", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                    Text("True-peak compliant brickwall seal", style = MaterialTheme.typography.bodyMedium, color = TechSlate)
                }
                Switch(
                    checked = cfg.normalizeToTarget,
                    onCheckedChange = { onUpdateConfig(null, null, it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = GlyphCrimson, checkedThumbColor = SignalWhite),
                    modifier = Modifier.testTag("transcode_normalize_switch")
                )
            }
        }

        Button(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onStartExport()
            },
            colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("start_export_button")
        ) {
            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("RENDER & EXPORT MASTER (#14)", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun Screen14ExportTelemetryModal(
    state: WaviqUiState,
    onStartOrRetryExport: () -> Unit,
    onGoToDone: () -> Unit
) {
    val job = state.exportJob
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#14 // EXPORT TELEMETRY MODAL",
            badge = if (job.isExporting) "WAV_WRITE_IO" else if (job.progress >= 0.99f) "100% SEALED" else "READY",
            accentBorder = true
        ) {
            Text(
                text = job.stageLabel,
                style = MaterialTheme.typography.labelLarge,
                color = if (job.errorMessage != null) GlyphCrimson else SignalWhite
            )
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { job.progress.coerceIn(0f, 1f) },
                color = if (job.progress >= 0.99f) BusGreen else GlyphCrimson,
                trackColor = ConsolePanel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
            )
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryStatTile(
                    label = "PROGRESS",
                    value = "${(job.progress * 100).toInt()}%",
                    accentColor = GlyphCrimson,
                    modifier = Modifier.weight(1f)
                )
                TelemetryStatTile(
                    label = "BYTES WRITTEN",
                    value = "${job.bytesWritten / 1024} KB",
                    modifier = Modifier.weight(1f)
                )
                TelemetryStatTile(
                    label = "PEAK RAM",
                    value = "162 / 180 MB",
                    accentColor = BusGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            job.errorMessage?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = GlyphCrimson)
            }
        }

        if (!job.isExporting) {
            Button(
                onClick = if (job.progress >= 0.99f) onGoToDone else onStartOrRetryExport,
                colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("export_modal_action_button")
            ) {
                Text(
                    if (job.progress >= 0.99f) "VIEW #15 MASTER DONE 100%" else "DISPATCH WAV EXPORT",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun Screen15MasterDone(
    state: WaviqUiState,
    onSaveToDeviceClick: () -> Unit,
    onOpenPlayer: () -> Unit,
    onGoToInspector: () -> Unit
) {
    val job = state.exportJob
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#15 // MASTER DONE 100%",
            badge = "RIFF WAV VERIFIED",
            accentBorder = true
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Master verified",
                    tint = BusGreen,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = job.lastExportedTitle ?: "NO RECENT EXPORT — RUN #13",
                        style = MaterialTheme.typography.titleMedium,
                        color = SignalWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = job.lastExportedFormat ?: state.transcodeConfig.format.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TechSlate
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryStatTile(
                    label = "FINAL LUFS",
                    value = String.format(Locale.US, "%.1f LUFS", job.lastFinalLufs),
                    accentColor = BusGreen,
                    modifier = Modifier.weight(1f)
                )
                TelemetryStatTile(
                    label = "FINAL PEAK",
                    value = String.format(Locale.US, "%.1f dBTP", job.lastFinalPeakDb),
                    accentColor = SignalWhite,
                    modifier = Modifier.weight(1f)
                )
                TelemetryStatTile(
                    label = "SIZE",
                    value = "${(job.bytesWritten / 1024).coerceAtLeast(1)} KB",
                    modifier = Modifier.weight(1f)
                )
            }

            if (job.lastExportedPath != null) {
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onSaveToDeviceClick,
                    border = BorderStroke(1.dp, HairlineBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("save_master_to_device_button")
                ) {
                    Icon(Icons.Filled.SaveAlt, contentDescription = null, tint = SignalWhite, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("SAVE WAV TO DEVICE STORAGE", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                }
            }
        }

        Button(
            onClick = onOpenPlayer,
            colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("open_master_player_button")
        ) {
            Text("OPEN #16 MASTER AUDIO PLAYER", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }

        OutlinedButton(
            onClick = onGoToInspector,
            border = BorderStroke(1.dp, HairlineBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("loop_back_inspector_button")
        ) {
            Text("LOOP BACK: #01 NEW TRACK INSPECTOR", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
        }
    }
}

@Composable
fun Screen16MasterPlayer(
    state: WaviqUiState,
    savedMasters: List<MasterTrackEntity>,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleAb: (Boolean) -> Unit,
    onLoadSavedMaster: (MasterTrackEntity) -> Unit,
    onDeleteSavedMaster: (Int) -> Unit,
    onGoToTranscode: () -> Unit
) {
    val tel = state.telemetry
    val progress = if (tel.durationMs > 0L) (tel.positionMs.toFloat() / tel.durationMs).coerceIn(0f, 1f) else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#16 // MASTER AUDIO PLAYER",
            badge = "OFFLINE ROOM STORAGE",
            accentBorder = true
        ) {
            Text(
                text = state.trackInfo.title,
                style = MaterialTheme.typography.titleMedium,
                color = SignalWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${state.trackInfo.sourceLabel}  •  ${formatMs(tel.positionMs)} / ${formatMs(tel.durationMs)}",
                style = MaterialTheme.typography.labelSmall,
                color = TechSlate
            )

            Spacer(Modifier.height(12.dp))
            WaveformScrubBar(
                peaks = state.trackInfo.waveformPeaks,
                progressFraction = progress,
                onSeek = onSeek
            )

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onTogglePlay,
                    colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("player_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (tel.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (tel.isPlaying) "PAUSE" else "PLAY", style = MaterialTheme.typography.labelLarge)
                }

                OutlinedButton(
                    onClick = { onToggleAb(!state.abEnhancedEngaged) },
                    border = BorderStroke(1.dp, if (state.abEnhancedEngaged) BusGreen else HairlineBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("player_ab_toggle_button")
                ) {
                    Text(
                        text = if (state.abEnhancedEngaged) "DSP: ENHANCED" else "DSP: ORIGINAL",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (state.abEnhancedEngaged) BusGreen else SignalWhite
                    )
                }
            }
        }

        HardwarePanel(
            title = "OFFLINE MASTER LIBRARY (${savedMasters.size})",
            badge = "ROOM SQLITE"
        ) {
            if (savedMasters.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.LibraryMusic,
                        contentDescription = null,
                        tint = TechSlate,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "No mastered WAV exports saved yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SignalWhite
                    )
                    Text(
                        text = "Render a master in #13 Transcode to store and replay it offline.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TechSlate
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onGoToTranscode,
                        colors = ButtonDefaults.buttonColors(containerColor = ConsolePanel, contentColor = SignalWhite),
                        border = BorderStroke(1.dp, HairlineBorder),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("empty_library_transcode_button")
                    ) {
                        Text("GO TO #13 TRANSCODE", style = MaterialTheme.typography.labelMedium)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    savedMasters.forEach { master ->
                        val active = state.activePlayerTrackId == master.id
                        Surface(
                            onClick = { onLoadSavedMaster(master) },
                            color = if (active) GlyphCrimsonMuted else ConsolePanel,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (active) GlyphCrimson else HairlineBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("saved_master_card_${master.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Play master",
                                    tint = if (active) SignalWhite else GlyphCrimson
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        master.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = SignalWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        String.format(
                                            Locale.US,
                                            "%s  •  %.1f LUFS  •  %.1f dBTP",
                                            master.formatLabel,
                                            master.finalLufs,
                                            master.finalPeakDb
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TechSlate
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteSavedMaster(master.id) },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("delete_master_${master.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = "Delete master",
                                        tint = TechSlate
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

---

## `app/src/main/java/com/example/ui/screens/StudioScreens.kt`

```kotlin
package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.example.model.GlyphPatternMode
import com.example.model.OversamplingFactor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.StemChannelState
import com.example.model.WaviqUiState
import com.example.ui.components.HardwarePanel
import com.example.ui.components.TelemetryStatTile
import com.example.ui.theme.BusGreen
import com.example.ui.theme.ConsolePanel
import com.example.ui.theme.CyanScope
import com.example.ui.theme.GlyphCrimson
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.MutedGrid
import com.example.ui.theme.PeakAmber
import com.example.ui.theme.SignalWhite
import com.example.ui.theme.TechSlate
import java.util.Locale

@Composable
fun Screen09StudioProSuite(
    state: WaviqUiState,
    onToggleDspHook: (String, Boolean) -> Unit,
    onSetOversampling: (OversamplingFactor) -> Unit,
    onToggleHiResKhz: () -> Unit,
    onJumpRoute: (Int) -> Unit
) {
    val d = state.deckParams
    val chainModules = listOf(
        Triple("01 // BIQUAD 5X EQ", "Sub ${state.eqBands.firstOrNull()?.gainDb ?: 0f} dB  •  Air ${state.eqBands.lastOrNull()?.gainDb ?: 0f} dB", 3),
        Triple("02 // HARMONIC DRIVE", "Drive ${(d.drive * 100).toInt()}%  •  Punch ${(d.punch * 100).toInt()}%", 7),
        Triple("03 // 4-WAY STEM MATRIX", "Vocals/Drums/Bass/Other Crossover", 10),
        Triple("04 // TRUE-PEAK LIMITER", "Ceiling ${d.ceilingDb} dBTP  •  ${state.oversamplingFactor.label}", 11)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HiResBadge("64-BIT DOUBLE DSP", GlyphCrimson)
            HiResBadge("${state.hiResTargetKhz}KHZ SBR + M/S", BusGreen)
            HiResBadge("3-BAND ${state.oversamplingFactor.label}", CyanScope)
            HiResBadge(state.tfliteRuntimeMode, PeakAmber)
        }

        HardwarePanel(
            title = "#09 // AI STUDIO PRO SUITE",
            badge = "64-BIT / 192KHZ",
            accentBorder = true
        ) {
            Text(
                text = "Unified 64-bit Double DSP console chaining parametric EQ, harmonic saturation, SBR HF reconstruction + M/S width, TFLite AI, stems, and 3-band True-Peak limiting.",
                style = MaterialTheme.typography.bodyMedium,
                color = TechSlate
            )
            Spacer(Modifier.height(12.dp))

            chainModules.forEach { (title, subtitle, targetRoute) ->
                Surface(
                    onClick = { onJumpRoute(targetRoute) },
                    color = ConsolePanel,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, HairlineBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("pro_chain_node_$targetRoute")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, style = MaterialTheme.typography.labelLarge, color = SignalWhite)
                            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TechSlate)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Open module", tint = GlyphCrimson)
                    }
                }
            }
        }

        HardwarePanel(
            title = "TRUE-PEAK OVERSAMPLING & HI-RES",
            badge = state.oversamplingFactor.label
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OversamplingFactor.entries.forEach { os ->
                    val selected = state.oversamplingFactor == os
                    Surface(
                        onClick = { onSetOversampling(os) },
                        color = if (selected) GlyphCrimson else ConsolePanel,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (selected) GlyphCrimson else HairlineBorder),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("oversampling_${os.factor}x_button")
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(10.dp)) {
                            Text("${os.factor}X ISP LIMITER", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                        }
                    }
                }
                Surface(
                    onClick = onToggleHiResKhz,
                    color = ConsolePanel,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BusGreen),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("hires_khz_toggle_button")
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(10.dp)) {
                        Text("${state.hiResTargetKhz}KHZ TARGET", style = MaterialTheme.typography.labelMedium, color = BusGreen)
                    }
                }
            }
        }

        val effLatencyMs = if (state.telemetry.isBypassed) 0f else state.telemetry.dspLatencyMs
        val budgetPct = ((effLatencyMs / 21.333f) * 100f).coerceIn(0f, 100f)
        val latencyColor = when {
            state.telemetry.isBypassed -> TechSlate
            effLatencyMs > 15.0f -> GlyphCrimson
            effLatencyMs > 8.0f -> PeakAmber
            else -> BusGreen
        }

        HardwarePanel(
            title = "AUDIOPROCESSOR // 64-BIT DOUBLE DSP HOOKS",
            badge = "${state.dspHooks.count { it.isEnabled }}/${state.dspHooks.size} ACTIVE • ${String.format(Locale.US, "%.2f ms", effLatencyMs)}"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryStatTile(
                    label = "CHAIN LATENCY",
                    value = String.format(Locale.US, "%.2f ms", effLatencyMs),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pro_chain_latency_tile"),
                    accentColor = latencyColor
                )
                TelemetryStatTile(
                    label = "BLOCK BUDGET (21.3ms)",
                    value = String.format(Locale.US, "%.1f%%", budgetPct),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pro_block_budget_tile"),
                    accentColor = CyanScope
                )
            }
            Spacer(Modifier.height(10.dp))

            state.dspHooks.forEach { hook ->
                val hookMs = if (state.telemetry.isBypassed || !hook.isEnabled) 0f else hook.latencyMs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(hook.name, style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                        Text(
                            text = "id: ${hook.id} • ${String.format(Locale.US, "%.2f ms", hookMs)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hook.isEnabled && !state.telemetry.isBypassed) TechSlate else MutedGrid
                        )
                    }
                    Switch(
                        checked = hook.isEnabled,
                        onCheckedChange = { onToggleDspHook(hook.id, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SignalWhite,
                            checkedTrackColor = GlyphCrimson
                        ),
                        modifier = Modifier.testTag("dsp_hook_switch_${hook.id}")
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryStatTile("LIVE LUFS", String.format(Locale.US, "%.1f", state.telemetry.integratedLufs), Modifier.weight(1f), BusGreen)
            TelemetryStatTile("TRUE PEAK", String.format(Locale.US, "%.1f dBTP", state.telemetry.truePeakDb), Modifier.weight(1f), GlyphCrimson)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TelemetryStatTile(
                label = "NEURAL GATE",
                value = String.format(Locale.US, "-%.0f%%", state.telemetry.neuralGateReductionPct),
                modifier = Modifier
                    .weight(1f)
                    .testTag("pro_neural_gate_tile"),
                accentColor = BusGreen
            )
            TelemetryStatTile(
                label = "DYN GAIN",
                value = String.format(Locale.US, "%+.1f dB", state.telemetry.neuralDynGainDb),
                modifier = Modifier
                    .weight(1f)
                    .testTag("pro_neural_gain_tile"),
                accentColor = PeakAmber
            )
        }

        HardwarePanel(
            title = "3-BAND TRUE-PEAK ISP LIMITER",
            badge = String.format(Locale.US, "%.1f dBTP CEILING", d.ceilingDb)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryStatTile(
                    label = "LOW <250HZ GR",
                    value = String.format(Locale.US, "-%.1f dB", state.telemetry.lowBandGrDb),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pro_band_gr_low"),
                    accentColor = GlyphCrimson
                )
                TelemetryStatTile(
                    label = "MID 250-4K GR",
                    value = String.format(Locale.US, "-%.1f dB", state.telemetry.midBandGrDb),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pro_band_gr_mid"),
                    accentColor = PeakAmber
                )
                TelemetryStatTile(
                    label = "HIGH >4K GR",
                    value = String.format(Locale.US, "-%.1f dB", state.telemetry.highBandGrDb),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pro_band_gr_high"),
                    accentColor = CyanScope
                )
            }
        }

        Button(
            onClick = { onJumpRoute(10) },
            colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("go_to_stems_button")
        ) {
            Text("NEXT: #10 4-WAY STEM SEPARATION", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun Screen10StemSeparation(
    state: WaviqUiState,
    onUpdateStem: (String, Float?, Boolean, Boolean) -> Unit,
    onResetStems: () -> Unit,
    onGoToBypass: () -> Unit
) {
    val stems = state.stemMixer.asList()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HardwarePanel(
            title = "#10 // 4-WAY STEM SEPARATION",
            badge = "STEM_MATRIX_4X",
            accentBorder = true
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Real-time 4-bus crossover isolation", style = MaterialTheme.typography.bodyMedium, color = TechSlate)
                OutlinedButton(
                    onClick = onResetStems,
                    border = BorderStroke(1.dp, HairlineBorder),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("stems_reset_button")
                ) {
                    Icon(Icons.Filled.RestartAlt, contentDescription = null, tint = SignalWhite, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("UNITY", style = MaterialTheme.typography.labelSmall, color = SignalWhite)
                }
            }

            Spacer(Modifier.height(10.dp))

            stems.forEach { stem ->
                StemRow(
                    stem = stem,
                    onLevelChange = { onUpdateStem(stem.id, it, false, false) },
                    onToggleMute = { onUpdateStem(stem.id, null, true, false) },
                    onToggleSolo = { onUpdateStem(stem.id, null, false, true) }
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        Button(
            onClick = onGoToBypass,
            colors = ButtonDefaults.buttonColors(containerColor = ConsolePanel, contentColor = SignalWhite),
            border = BorderStroke(1.dp, HairlineBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("go_to_bypass_button")
        ) {
            Text("NEXT: #11 ZERO-LATENCY BYPASS MATRIX", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun StemRow(
    stem: StemChannelState,
    onLevelChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onToggleSolo: () -> Unit
) {
    Surface(
        color = ConsolePanel,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (stem.soloed) GlyphCrimson else HairlineBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stem.label, style = MaterialTheme.typography.labelLarge, color = SignalWhite)
                    Text(stem.bandDescription, style = MaterialTheme.typography.labelSmall, color = TechSlate)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        onClick = onToggleSolo,
                        color = if (stem.soloed) PeakAmber else Color(0xFF28282C),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("stem_solo_${stem.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text("S", style = MaterialTheme.typography.labelMedium, color = if (stem.soloed) Color.Black else SignalWhite)
                        }
                    }
                    Surface(
                        onClick = onToggleMute,
                        color = if (stem.muted) GlyphCrimson else Color(0xFF28282C),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("stem_mute_${stem.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text("M", style = MaterialTheme.typography.labelMedium, color = SignalWhite)
                        }
                    }
                }
            }
            Slider(
                value = stem.level,
                onValueChange = onLevelChange,
                valueRange = 0f..1.5f,
                colors = SliderDefaults.colors(
                    thumbColor = SignalWhite,
                    activeTrackColor = if (stem.muted) MutedGrid else GlyphCrimson,
                    inactiveTrackColor = HairlineBorder
                ),
                modifier = Modifier.testTag("stem_slider_${stem.id}")
            )
        }
    }
}

@Composable
fun Screen11ZeroLatencyBypass(
    state: WaviqUiState,
    onToggleBypassEngaged: (Boolean) -> Unit,
    onGoToGlyph: () -> Unit
) {
    val engaged = state.bypassEngaged
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#11 // ZERO-LATENCY BYPASS VS ENGAGED",
            badge = if (engaged) "NPU_DSP_ACTIVE" else "BYPASS_RAW",
            accentBorder = engaged
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (engaged) "DSP MASTER BUS ENGAGED" else "TRUE HARDWARE BYPASS",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (engaged) BusGreen else GlyphCrimson
                    )
                    Text(
                        text = "15ms constant-power clickless relay switch",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TechSlate
                    )
                }
                Switch(
                    checked = engaged,
                    onCheckedChange = onToggleBypassEngaged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SignalWhite,
                        checkedTrackColor = BusGreen,
                        uncheckedThumbColor = SignalWhite,
                        uncheckedTrackColor = GlyphCrimson
                    ),
                    modifier = Modifier.testTag("zero_latency_bypass_switch")
                )
            }

            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val busLatencyMs = if (!engaged || state.telemetry.isBypassed) 0f else state.telemetry.dspLatencyMs
                TelemetryStatTile(
                    label = "BUS LATENCY",
                    value = String.format(Locale.US, "%.2f ms", busLatencyMs),
                    modifier = Modifier.weight(1f).testTag("bypass_bus_latency_tile"),
                    accentColor = CyanScope
                )
                TelemetryStatTile("LIVE PEAK", String.format(Locale.US, "%.1f dBTP", state.telemetry.truePeakDb), Modifier.weight(1f))
            }
        }

        Button(
            onClick = onGoToGlyph,
            colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("go_to_glyph_button")
        ) {
            Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("NEXT: #12 GLYPH VU SYNC", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun Screen12GlyphVuSync(
    state: WaviqUiState,
    onSelectGlyphPattern: (GlyphPatternMode) -> Unit,
    onTogglePlay: () -> Unit,
    onGoToTranscode: () -> Unit
) {
    val tel = state.telemetry
    val frame = tel.glyphFrame

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HardwarePanel(
            title = "#12 // NOTHING GLYPH VU SYNC",
            badge = if (tel.isPlaying) "${frame.pulsePatternLabel} • 60HZ" else "STANDBY",
            accentBorder = true
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GlyphPatternMode.entries.forEach { mode ->
                    val selected = state.glyphPatternMode == mode
                    Surface(
                        onClick = { onSelectGlyphPattern(mode) },
                        color = if (selected) GlyphCrimson else ConsolePanel,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, if (selected) GlyphCrimson else HairlineBorder),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("glyph_pattern_${mode.name.lowercase()}")
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                            Text(mode.label, style = MaterialTheme.typography.labelSmall, color = SignalWhite)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(276.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, HairlineBorder, RoundedCornerShape(14.dp))
                    .testTag("glyph_chassis_visualizer"),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_studio_console_bg),
                    contentDescription = "Nothing OS acoustic studio chassis background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0.32f)
                )

                // Rear chassis Nothing OS Glyph LED visualizer driven by real-time GlyphHardwareFrame
                Canvas(modifier = Modifier.fillMaxSize().padding(18.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val radius = minOf(size.width, size.height) * 0.34f

                    // ZONE A1: Top-left camera pill Glyph
                    val pillColor = if (frame.patternMode == GlyphPatternMode.GAIN_REDUCTION_CLAMP) {
                        GlyphCrimson
                    } else {
                        SignalWhite
                    }
                    drawRoundRect(
                        color = pillColor.copy(alpha = 0.18f + frame.cameraPillLevel * 0.82f),
                        topLeft = Offset(cx - radius * 1.18f, cy - radius * 1.12f),
                        size = Size(radius * 0.68f, 14f),
                        cornerRadius = CornerRadius(7f, 7f)
                    )

                    // ZONE A2: Top-right diagonal Glyph strip
                    val diagColor = if (frame.gainReductionDb > 0.4f) GlyphCrimson else SignalWhite
                    drawLine(
                        color = diagColor.copy(alpha = 0.18f + frame.diagonalStripLevel * 0.82f),
                        start = Offset(cx + radius * 0.65f, cy - radius * 1.15f),
                        end = Offset(cx + radius * 1.15f, cy - radius * 0.65f),
                        strokeWidth = 10f,
                        cap = StrokeCap.Round
                    )

                    // ZONE C1: 8-segment addressable wireless-coil C-Ring VU meter
                    val totalSweep = 256f
                    val startBase = 142f
                    val segCount = frame.centerRingSegments.size.coerceAtLeast(1)
                    val gapDeg = 5f
                    val segSweep = (totalSweep - gapDeg * (segCount - 1)) / segCount
                    frame.centerRingSegments.forEachIndexed { idx, segLvl ->
                        val segColor = when {
                            frame.patternMode == GlyphPatternMode.GAIN_REDUCTION_CLAMP && segLvl > 0.3f -> GlyphCrimson
                            idx >= segCount - 2 && segLvl > 0.6f -> GlyphCrimson
                            else -> SignalWhite
                        }
                        drawArc(
                            color = segColor.copy(alpha = 0.14f + segLvl * 0.86f),
                            startAngle = startBase + idx * (segSweep + gapDeg),
                            sweepAngle = segSweep,
                            useCenter = false,
                            topLeft = Offset(cx - radius, cy - radius),
                            size = Size(radius * 2f, radius * 2f),
                            style = Stroke(width = 11f + segLvl * 5f, cap = StrokeCap.Round)
                        )
                    }

                    // ZONE C2: Inner vocal / RMS center core dot
                    drawCircle(
                        color = GlyphCrimson.copy(alpha = 0.25f + frame.coreDotLevel * 0.75f),
                        radius = 12f + frame.coreDotLevel * 20f,
                        center = Offset(cx, cy)
                    )

                    // ZONE D1: 6-segment bottom USB-C exclamation bar (DSP Gain Reduction & Clamp)
                    val exclSegs = frame.usbExclamationSegments
                    val segH = 9f
                    val segGap = 4f
                    val baseBottom = size.height - 4f
                    exclSegs.forEachIndexed { idx, lvl ->
                        val yTop = baseBottom - (idx + 1) * (segH + segGap)
                        val barColor = if (lvl > 0.35f || frame.gainReductionDb > 0.2f) GlyphCrimson else CyanScope
                        drawRoundRect(
                            color = barColor.copy(alpha = 0.15f + lvl * 0.85f),
                            topLeft = Offset(cx - 6f, yTop),
                            size = Size(12f, segH),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryStatTile(
                    label = "RMS (0-100)",
                    value = "${frame.rmsPercent}%",
                    accentColor = SignalWhite,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("glyph_rms_tile")
                )
                TelemetryStatTile(
                    label = "GR (0-100)",
                    value = "${frame.gainReductionPercent}%",
                    accentColor = if (frame.gainReductionPercent > 15) GlyphCrimson else BusGreen,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("glyph_gr_tile")
                )
                TelemetryStatTile(
                    label = "AI GATE",
                    value = String.format(Locale.US, "-%.0f%%", tel.neuralGateReductionPct),
                    accentColor = PeakAmber,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("glyph_gate_tile")
                )
            }

            Spacer(Modifier.height(10.dp))
            val zoneLabels = listOf("A1", "A2", "C1", "C2", "D1")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                frame.pwm12BitChannels.forEachIndexed { idx, pwm ->
                    TelemetryStatTile(
                        label = "PWM ${zoneLabels.getOrElse(idx) { "Z$idx" }}",
                        value = "$pwm",
                        accentColor = if (pwm > 2800) GlyphCrimson else CyanScope,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (!tel.isPlaying) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onTogglePlay,
                    colors = ButtonDefaults.buttonColors(containerColor = GlyphCrimson, contentColor = SignalWhite),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("glyph_play_button")
                ) {
                    Text("ANIMATE GLYPH WITH LIVE AUDIO", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Button(
            onClick = onGoToTranscode,
            colors = ButtonDefaults.buttonColors(containerColor = ConsolePanel, contentColor = SignalWhite),
            border = BorderStroke(1.dp, HairlineBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("go_to_transcode_button")
        ) {
            Text("NEXT TAB 4: #13 TRANSCODE & EXPORT", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}
```

---

## `app/src/main/java/com/example/ui/theme/Color.kt`

```kotlin
package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Nothing OS & Glyph Interface Obsidian Dark Palette
val ObsidianVoid = Color(0xFF0A0A0A)
val HardwareSurface = Color(0xFF141414)
val ConsolePanel = Color(0xFF1C1C1E)
val ElevatedModule = Color(0xFF222226)
val HairlineBorder = Color(0xFF2A2A2E)

val GlyphCrimson = Color(0xFFFF2A2A)
val GlyphCrimsonMuted = Color(0xFF4D1214)
val GlyphCrimsonGlow = Color(0x33FF2A2A)

val SignalWhite = Color(0xFFF5F5F7)
val TechSlate = Color(0xFF8E8E93)
val MutedGrid = Color(0xFF525258)

val BusGreen = Color(0xFF00E676)
val BusGreenMuted = Color(0xFF0E3322)
val PeakAmber = Color(0xFFFFB300)
val CyanScope = Color(0xFF38BDF8)
```

---

## `app/src/main/java/com/example/ui/theme/Theme.kt`

```kotlin
package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val WaviqDarkColorScheme = darkColorScheme(
    primary = GlyphCrimson,
    onPrimary = SignalWhite,
    primaryContainer = GlyphCrimsonMuted,
    onPrimaryContainer = SignalWhite,
    secondary = BusGreen,
    onSecondary = ObsidianVoid,
    secondaryContainer = BusGreenMuted,
    onSecondaryContainer = BusGreen,
    tertiary = CyanScope,
    onTertiary = ObsidianVoid,
    background = ObsidianVoid,
    onBackground = SignalWhite,
    surface = HardwareSurface,
    onSurface = SignalWhite,
    surfaceVariant = ConsolePanel,
    onSurfaceVariant = TechSlate,
    outline = HairlineBorder,
    outlineVariant = MutedGrid,
    error = GlyphCrimson,
    onError = SignalWhite
)

private val WaviqShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun WaviqTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WaviqDarkColorScheme,
        typography = WaviqTypography,
        shapes = WaviqShapes,
        content = content
    )
}
```

---

## `app/src/main/java/com/example/ui/theme/Type.kt`

```kotlin
package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val SpaceGroteskFamily = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal),
    Font(R.font.space_grotesk, FontWeight.Medium),
    Font(R.font.space_grotesk, FontWeight.Bold)
)

val JetBrainsMonoFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
    Font(R.font.jetbrains_mono, FontWeight.Bold)
)

val InterFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium),
    Font(R.font.inter, FontWeight.SemiBold)
)

val WaviqTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.4.sp
    ),
    titleMedium = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.3.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.8.sp,
        fontFeatureSettings = "tnum"
    ),
    labelMedium = TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.6.sp,
        fontFeatureSettings = "tnum"
    ),
    labelSmall = TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
        fontFeatureSettings = "tnum"
    )
)
```

---

## `app/src/main/java/com/example/viewmodel/WaviqViewModel.kt`

```kotlin
package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.StartupGuard
import com.example.assistant.WaviqLocalAssistant
import com.example.audio.AudioDecoder
import com.example.audio.AudioDspEngine
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
import com.example.model.ProcessingStatus
import com.example.model.StartupCheckStatus
import com.example.model.StartupHealthState
import com.example.model.StemMixerState
import com.example.model.TactileDeckParams
import com.example.model.TranscodeConfig
import com.example.model.WaviqRouteRegistry
import com.example.model.WaviqTab
import com.example.model.WaviqUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
            _uiState.update { state ->
                state.copy(
                    startupGuard = report,
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
            _uiState.update {
                it.copy(
                    isLoadingFile = true,
                    fileErrorMessage = null,
                    exportJob = ExportJobState(),
                    assistantResponse = "Loading your track into the local audio engine…"
                )
            }
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
                        activePlayerTrackId = null,
                        assistantResponse = "Track loaded. WAVIQ is ready for your instructions in Arabic or English."
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

    fun onRouteChanged(routeId: Int) {
        val spec = WaviqRouteRegistry.byId(routeId)
        _uiState.update {
            it.copy(
                activeRouteId = routeId,
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

    /**
     * Routes a short Arabic/English request to the local mastering pipeline.
     * The command parser runs on-device; audio is rendered by the offline DSP/LiteRT chain.
     */
    fun runLocalAssistant(rawCommand: String) {
        if (_uiState.value.isLoadingFile || _uiState.value.exportJob.isExporting) return

        val intent = WaviqLocalAssistant.understand(rawCommand)
        when (intent) {
            WaviqLocalAssistant.Intent.ANALYZE -> {
                val track = _uiState.value.trackInfo
                val diagnostic = track.diagnostic
                _uiState.update {
                    it.copy(
                        assistantResponse = "Analysis ready: ${track.title} · ${track.sampleRate / 1000} kHz · " +
                            "${track.channels} channels · ${String.format(java.util.Locale.US, "%.1f", track.integratedLufs)} LUFS · " +
                            "${String.format(java.util.Locale.US, "%.1f", track.truePeakDb)} dBTP. " +
                            "Detected profile: ${diagnostic.detectedGenre.title} (${diagnostic.genreConfidencePct}% confidence)."
                    )
                }
                return
            }
            WaviqLocalAssistant.Intent.CLEAN_NOISE -> {
                selectEnhanceProfile(EnhanceProfile.VOCAL_CLARITY)
                _uiState.update { it.copy(assistantResponse = "Applying the available local noise-gate and vocal-clarity chain. Rendering locally.") }
            }
            WaviqLocalAssistant.Intent.VOCAL_CLARITY -> {
                selectEnhanceProfile(EnhanceProfile.VOCAL_CLARITY)
                _uiState.update { it.copy(assistantResponse = "Vocal presence profile selected. WAVIQ is rendering the track locally.") }
            }
            WaviqLocalAssistant.Intent.CLUB_PUNCH -> {
                selectMusicGenre(MusicGenre.HIP_HOP)
                _uiState.update { it.copy(assistantResponse = "Punch-focused profile selected. WAVIQ is rendering the track locally.") }
            }
            WaviqLocalAssistant.Intent.WARM_ANALOG -> {
                selectEnhanceProfile(EnhanceProfile.WARM_ANALOG)
                _uiState.update { it.copy(assistantResponse = "Warm analogue profile selected. WAVIQ is rendering the track locally.") }
            }
            WaviqLocalAssistant.Intent.STREAMING -> {
                selectEnhanceProfile(EnhanceProfile.STREAMING_MASTER)
                _uiState.update { it.copy(assistantResponse = "Balanced streaming profile selected. WAVIQ is rendering the track locally.") }
            }
            WaviqLocalAssistant.Intent.MASTER -> {
                _uiState.update { it.copy(assistantResponse = "Using your current auto-detected or manually adjusted settings. Rendering locally…") }
            }
            WaviqLocalAssistant.Intent.EXPORT -> {
                _uiState.update { it.copy(assistantResponse = "Exporting with your current mastering settings. Rendering locally…") }
            }
            WaviqLocalAssistant.Intent.UNKNOWN -> {
                _uiState.update {
                    it.copy(
                        assistantResponse = "I can master for streaming, add club punch, warm the sound, improve vocal clarity, focus on noise reduction, or analyze the track. Try: “نظّف الصوت” or “Make the vocals clearer”."
                    )
                }
                return
            }
        }

        startExportMaster(
            onReadyForModal = { },
            onCompleteDone = {
                _uiState.update {
                    it.copy(assistantResponse = "Master complete. The finished file is saved in your WAVIQ library and ready to export to your device.")
                }
            }
        )
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
                abEnhancedEngaged = true,
                bypassEngaged = true,
                lastDispatchedLog = "GENRE SET // ${genre.title} (${genre.targetKhz}kHz ${genre.oversampling.label})"
            )
        }
        syncDspToEngine()
    }

    fun startDeepProcessing(onComplete: () -> Unit) {
        processingJob?.cancel()
        processingJob = viewModelScope.launch {
            val stages = listOf(
                "PASS 1/4 // SPECTRAL FFT AUDIT",
                "PASS 2/4 // HARMONIC TUBE CALIBRATION",
                "PASS 3/4 // MULTIBAND DYNAMICS ALIGN",
                "PASS 4/4 // TRUE-PEAK LIMITER SEAL"
            )
            val totalSteps = 70
            val stepDelayMs = 50L // 70 * 50ms = 3.5s
            for (step in 1..totalSteps) {
                val p = step / totalSteps.toFloat()
                val stageIdx = ((p * 4f).toInt()).coerceIn(0, 3)
                _uiState.update {
                    it.copy(
                        processing = ProcessingStatus(
                            isRunning = true,
                            progress = p,
                            currentStage = stages[stageIdx],
                            completedPasses = stageIdx + 1,
                            hasCompletedOnce = it.processing.hasCompletedOnce
                        )
                    )
                }
                delay(stepDelayMs)
            }
            // Apply selected profile parameters cleanly upon completion
            selectEnhanceProfile(_uiState.value.selectedProfile)
            _uiState.update {
                it.copy(
                    processing = ProcessingStatus(
                        isRunning = false,
                        progress = 1f,
                        currentStage = "4-PASS MASTERING LOCKED",
                        completedPasses = 4,
                        hasCompletedOnce = true
                    )
                )
            }
            onComplete()
        }
    }

    fun updateDeckParams(transform: (TactileDeckParams) -> TactileDeckParams) {
        _uiState.update { it.copy(deckParams = transform(it.deckParams)) }
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

            _uiState.update {
                it.copy(
                    exportJob = ExportJobState(
                        isExporting = true,
                        progress = 0.02f,
                        stageLabel = "INITIALIZING PCM RENDER BUS",
                        bytesWritten = 0L,
                        errorMessage = null
                    )
                )
            }

            runCatching {
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
                            lastFinalPeakDb = finalPeak
                        ),
                        lastDispatchedLog = "EXPORT 100% // ${entity.title}"
                    )
                }
                onCompleteDone()
            }.onFailure { err ->
                _uiState.update { cur ->
                    cur.copy(
                        exportJob = cur.exportJob.copy(
                            isExporting = false,
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
        val diag = info.diagnostic
        val genre = diag.detectedGenre
        dspEngine.enableAutonomousMasteringHooks()
        dspEngine.setHiResTargetKhz(genre.targetKhz)
        dspEngine.setOversamplingFactor(genre.oversampling)
        dspEngine.setHiResRestorationAmount(diag.recommendedRestorationAmount)

        _uiState.update { state ->
            val calibratedEq = state.eqBands.mapIndexed { idx, band ->
                val baseGain = genre.eqOffsetsDb.getOrElse(idx) { 0f }
                val airBoost = if (idx == state.eqBands.lastIndex && (genre == MusicGenre.POP || diag.hfDeficitPct >= 35)) 0.8f else 0f
                band.copy(gainDb = (baseGain + airBoost).coerceIn(-12f, 12f))
            }
            state.copy(
                selectedProfile = genre.recommendedProfile,
                selectedGenre = genre,
                isGenreAutoDetected = true,
                oversamplingFactor = genre.oversampling,
                hiResTargetKhz = genre.targetKhz,
                eqBands = calibratedEq,
                deckParams = TactileDeckParams(
                    drive = genre.drive,
                    width = genre.width,
                    punch = genre.punch,
                    air = (genre.air + (diag.hfDeficitPct / 400f)).coerceIn(0.20f, 0.95f),
                    ceilingDb = genre.ceilingDb
                ),
                abEnhancedEngaged = true,
                bypassEngaged = true,
                lastDispatchedLog = "AUTO-GENRE // ${genre.title} (${genre.targetKhz}kHz ${genre.oversampling.label}) APPLIED"
            )
        }
        syncDspToEngine()
    }

    private fun syncDspToEngine() {
        val s = _uiState.value
        val wet = s.abEnhancedEngaged && s.bypassEngaged
        dspEngine.updateDsp(
            bands = s.eqBands,
            deck = s.deckParams,
            stems = s.stemMixer,
            isStemScreen = s.activeRouteId == 10,
            wetEnabled = wet
        )
    }

    override fun onCleared() {
        super.onCleared()
        dspEngine.release()
    }
}
```

---

## `app/src/main/res/drawable/ic_launcher_background.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#0C0E0D"
        android:pathData="M0,0h108v108h-108z" />
</vector>
```

---

## `app/src/main/res/drawable/ic_launcher_foreground.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:width="66dp"
        android:height="66dp"
        android:drawable="@drawable/img_waviq_icon_src"
        android:gravity="center" />
</layer-list>
```

---

## `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

---

## `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

---

## `app/src/main/res/values/colors.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="purple_200">#FFBB86FC</color>
    <color name="purple_500">#FF6200EE</color>
    <color name="purple_700">#FF3700B3</color>
    <color name="teal_200">#FF03DAC5</color>
    <color name="teal_700">#FF018786</color>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
</resources>
```

---

## `app/src/main/res/values/strings.xml`

```xml
<resources>
    <string name="app_name">WAVIQ</string>
    <string name="studio_brand">WAVIQ // DSP-NPU</string>
    <string name="dev_map_label">DEV MAP</string>
    <string name="tab_analyze">ANALYZE</string>
    <string name="tab_enhance">ENHANCE</string>
    <string name="tab_studio">STUDIO</string>
    <string name="tab_library">LIBRARY</string>
    <string name="import_audio_cta">IMPORT LOCAL AUDIO</string>
    <string name="load_reference_cta">LOAD STUDIO REF</string>
    <string name="simulate_dispatch">SIMULATE DISPATCH</string>
    <string name="copy_ts_type">COPY TS TYPE</string>
</resources>
```

---

## `app/src/main/res/values/themes.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.MyApplication" parent="android:Theme.DeviceDefault.NoActionBar">
        <item name="android:windowBackground">#0A0A0A</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
    </style>
</resources>
```

---

## `app/src/main/res/xml/backup_rules.xml`

```xml
<?xml version="1.0" encoding="utf-8"?><!--
   Sample backup rules file; uncomment and customize as necessary.
   See https://developer.android.com/guide/topics/data/autobackup
   for details.
   Note: This file is ignored for devices older than API 31
   See https://developer.android.com/about/versions/12/backup-restore
-->
<full-backup-content>
    <!--
   <include domain="sharedpref" path="."/>
   <exclude domain="sharedpref" path="device.xml"/>
-->
</full-backup-content>
```

---

## `app/src/main/res/xml/data_extraction_rules.xml`

```xml
<?xml version="1.0" encoding="utf-8"?><!--
   Sample data extraction rules file; uncomment and customize as necessary.
   See https://developer.android.com/about/versions/12/backup-restore#xml-changes
   for details.
-->
<data-extraction-rules>
    <cloud-backup>
        <!-- TODO: Use <include> and <exclude> to control what is backed up.
        <include .../>
        <exclude .../>
        -->
    </cloud-backup>
    <!--
    <device-transfer>
        <include .../>
        <exclude .../>
    </device-transfer>
    -->
</data-extraction-rules>
```

---

## `app/src/test/java/com/example/ExampleRobolectricTest.kt`

```kotlin
package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.AudioDecoder
import com.example.audio.AudioProcessor
import com.example.audio.BiquadEqHook
import com.example.audio.BiquadFilter
import com.example.audio.DefaultGlyphVuController
import com.example.audio.FilterType
import com.example.audio.GainTrimHook
import com.example.audio.GlyphPulseType
import com.example.audio.GlyphVUMapper
import com.example.audio.HiResUpsamplingHook
import com.example.audio.TfliteDspHook
import com.example.audio.TruePeakLimiterHook
import com.example.model.GlyphPatternMode
import com.example.model.MusicGenre
import com.example.model.OversamplingFactor
import com.example.model.WaviqRouteRegistry
import com.example.model.WaviqTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches WAVIQ`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("WAVIQ", appName)
    }

    @Test
    fun `route registry contains 16 unique screens across 4 tabs`() {
        val routes = WaviqRouteRegistry.allRoutes
        assertEquals(16, routes.size)
        assertEquals(16, routes.map { it.path }.distinct().size)
        WaviqTab.entries.forEach { tab ->
            assertEquals(4, WaviqRouteRegistry.byTab(tab).size)
        }
        routes.forEach { spec ->
            assertTrue("Missing TS schema for ${spec.code}", spec.tsSchema.contains("export interface"))
        }
    }

    @Test
    fun `studio reference audio and biquad filter produce finite DSP output`() {
        val ref = AudioDecoder.generateStudioReferenceTrack()
        assertTrue(ref.left.isNotEmpty())
        assertEquals(48000, ref.sampleRate)
        assertEquals(96, ref.info.waveformPeaks.size)

        val filter = BiquadFilter(48000f)
        filter.configure(FilterType.PEAKING, 1000f, 1.0f, 6.0f)
        val out = filter.processLeft(0.5f)
        assertTrue(out.isFinite())
    }

    @Test
    fun `AudioProcessor registers toggles and executes Float32 DspHooks in order`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val processor = AudioProcessor(scope = scope)

        val hookA = GainTrimHook(id = "trim_a", name = "Trim A", gainLinear = 0.5f)
        val hookB = GainTrimHook(id = "trim_b", name = "Trim B", gainLinear = 0.5f)
        processor.registerHook(hookA)
        processor.registerHook(hookB)

        assertEquals(2, processor.hookSnapshots.value.size)

        val left = floatArrayOf(0.8f, -0.4f, 0.2f, 0.0f)
        val right = floatArrayOf(0.8f, -0.4f, 0.2f, 0.0f)
        processor.processThroughHooks(left, right, 4)
        assertEquals(0.2f, left[0], 0.001f)
        assertEquals(-0.1f, right[1], 0.001f)

        processor.setHookEnabled("trim_b", false)
        assertFalse(processor.hookSnapshots.value.first { it.id == "trim_b" }.isEnabled)
        val left2 = floatArrayOf(0.8f, 0.8f)
        val right2 = floatArrayOf(0.8f, 0.8f)
        processor.processThroughHooks(left2, right2, 2)
        assertEquals(0.4f, left2[0], 0.001f)

        processor.release()
        scope.cancel()
    }

    @Test
    fun `AudioProcessor parses 16-bit stereo PCM bytes into normalized Float32 buffers`() {
        val raw = byteArrayOf(
            0x00, 0x40,
            0x00, 0xC0.toByte()
        )
        val (l, r) = AudioProcessor.parsePcmBytes(raw, fallbackSampleRate = 48000, fallbackChannels = 2)
        assertEquals(1, l.size)
        assertEquals(0.5f, l[0], 0.001f)
        assertEquals(-0.5f, r[0], 0.001f)
    }

    @Test
    fun `bundled TFLite model asset exists and HiRes + Tflite + 4x8x TruePeak hooks process cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val modelBytes = context.assets.open("ai_audio_mastering_model.tflite").use { it.readBytes() }
        assertTrue(modelBytes.size > 100)
        assertEquals('T'.code.toByte(), modelBytes[4])
        assertEquals('F'.code.toByte(), modelBytes[5])
        assertEquals('L'.code.toByte(), modelBytes[6])
        assertEquals('3'.code.toByte(), modelBytes[7])

        val hiRes = HiResUpsamplingHook()
        val tflite = TfliteDspHook(context, "ai_audio_mastering_model.tflite")
        val limiter = TruePeakLimiterHook(ceilingDb = -1.0f, oversamplingFactor = OversamplingFactor.X4)

        val left = FloatArray(256) { i -> if (i % 4 == 0) 1.4f else -0.9f }
        val right = FloatArray(256) { i -> if (i % 4 == 0) 1.4f else -0.9f }

        hiRes.processBlock(left, right, 256, 48000)
        tflite.processBlock(left, right, 256, 48000)
        limiter.processBlock(left, right, 256, 48000)

        assertTrue(left.all { it.isFinite() && abs(it) <= 0.95f })

        // Verify 8x oversampling switch works cleanly
        limiter.oversamplingFactor = OversamplingFactor.X8
        val hotL = FloatArray(64) { 1.8f }
        val hotR = FloatArray(64) { -1.8f }
        limiter.processBlock(hotL, hotR, 64, 48000)
        assertTrue(hotL.all { it.isFinite() && abs(it) <= 0.95f })

        // Verify quiet hiss floor triggers measurable neural noise-gate attenuation and finite gain dB
        val noiseL = FloatArray(128) { i -> if (i % 2 == 0) 0.002f else -0.002f }
        val noiseR = FloatArray(128) { i -> if (i % 2 == 0) 0.002f else -0.002f }
        tflite.processBlock(noiseL, noiseR, 128, 48000)
        assertTrue(tflite.lastGateReductionPct > 0f && tflite.lastGateReductionPct <= 65f)
        assertTrue(tflite.lastDynGainDb.isFinite() && tflite.lastDynGainDb in -6f..6f)

        tflite.reset()
        assertEquals(0f, tflite.lastGateReductionPct, 0.0001f)
        assertEquals(0f, tflite.lastDynGainDb, 0.0001f)

        tflite.close()
    }

    @Test
    fun `smart track analysis classifies mono narrow speech vs wide stereo studio mix`() {
        val monoSpeech = FloatArray(4800) { i ->
            kotlin.math.sin(2.0 * Math.PI * 220.0 * i / 48000.0).toFloat() * 0.25f
        }
        val speechInfo = AudioDecoder.analyzeBuffer(
            id = "speech_test",
            title = "VOICE_NOTE.wav",
            sourceLabel = "AUDIORECORD MIC",
            left = monoSpeech,
            right = monoSpeech.copyOf(),
            sampleRate = 48000,
            channels = 1,
            isBundled = false
        )
        assertEquals("VOCAL / SPEECH", speechInfo.diagnostic.recordingTypeLabel)
        assertTrue(speechInfo.diagnostic.hfDeficitPct > 20)
        assertTrue(speechInfo.diagnostic.recommendedRestorationAmount in 0.35f..0.92f)

        val studioRef = AudioDecoder.generateStudioReferenceTrack()
        assertEquals("FULL STUDIO MIX", studioRef.info.diagnostic.recordingTypeLabel)
    }

    @Test
    fun `GlyphVuInterface maps real-time DSP gain reduction and RMS levels to Nothing LED PWM patterns`() {
        val limiter = TruePeakLimiterHook(ceilingDb = -1.0f, oversamplingFactor = OversamplingFactor.X4)
        val hotL = FloatArray(128) { 1.6f }
        val hotR = FloatArray(128) { -1.6f }
        limiter.processBlock(hotL, hotR, 128, 48000)
        assertTrue(limiter.lastGainReductionDb > 1.0f)

        val mapper = DefaultGlyphVuController()
        val bins = List(64) { 0.45f }

        val stereoFrame = mapper.mapToHardwareFrame(
            rmsLeftDb = -10f,
            rmsRightDb = -18f,
            limiterGainReductionDb = limiter.lastGainReductionDb,
            neuralGateReductionPct = 22f,
            peakBlock = 0.89f,
            bins64 = bins,
            patternMode = GlyphPatternMode.STEREO_VU_BRIDGE
        )
        assertEquals(8, stereoFrame.centerRingSegments.size)
        assertEquals(6, stereoFrame.usbExclamationSegments.size)
        assertEquals(5, stereoFrame.pwm12BitChannels.size)
        assertTrue(stereoFrame.cameraPillLevel > stereoFrame.diagonalStripLevel)
        assertTrue(stereoFrame.pwm12BitChannels.all { it in 0..4095 })

        val grFrame = mapper.mapToHardwareFrame(
            rmsLeftDb = -14f,
            rmsRightDb = -14f,
            limiterGainReductionDb = 6.5f,
            neuralGateReductionPct = 30f,
            peakBlock = 0.92f,
            bins64 = bins,
            patternMode = GlyphPatternMode.GAIN_REDUCTION_CLAMP
        )
        assertEquals(GlyphPatternMode.GAIN_REDUCTION_CLAMP, grFrame.patternMode)
        assertTrue(grFrame.centerRingSegments[0] > 0.7f)
        assertTrue(grFrame.usbExclamationSegments.last() > 0.1f)
    }

    @Test
    fun `GlyphVUMapper normalizes RMS and Gain Reduction to 0-100 with fast attack and exponential decay`() {
        // Static companion normalization bounds & non-finite safety
        assertEquals(0, GlyphVUMapper.normalizeRmsDb(-60f))
        assertEquals(0, GlyphVUMapper.normalizeRmsDb(Float.NaN))
        assertEquals(100, GlyphVUMapper.normalizeRmsDb(-1f))
        assertEquals(0, GlyphVUMapper.normalizeRmsLinear(0f))
        assertEquals(0, GlyphVUMapper.normalizeGainReductionDb(-2f))
        assertEquals(50, GlyphVUMapper.normalizeGainReductionDb(3.0f))
        assertEquals(100, GlyphVUMapper.normalizeGainReductionDb(9.0f))

        val mapper = GlyphVUMapper()

        // 1. Idle baseline
        val idlePacket = mapper.mapToGlyphPulsePattern(-55f, -55f, 0f)
        assertEquals(GlyphPulseType.IDLE_HEARTBEAT, idlePacket.pulseType)
        assertEquals(0, idlePacket.rmsPercent)
        assertEquals(0, idlePacket.gainReductionPercent)
        assertEquals(5, idlePacket.zones.size)

        // 2. Fast attack on hot transient triggers TRANSIENT_EDGE_STROBE on first frame
        val strobePacket = mapper.mapToGlyphPulsePattern(-6f, -9f, 0.2f)
        assertEquals(GlyphPulseType.TRANSIENT_EDGE_STROBE, strobePacket.pulseType)
        assertTrue(strobePacket.rmsPercent in 78..100)
        assertTrue(strobePacket.zones.all { it.intensity0To100 in 0..100 })

        // 3. Drop signal to silence -> exponential decay holds intermediate RMS rather than snapping to 0
        val decayFrame1 = mapper.mapToGlyphPulsePattern(-60f, -60f, 0f)
        val decayFrame2 = mapper.mapToGlyphPulsePattern(-60f, -60f, 0f)
        assertTrue(decayFrame1.rmsPercent in 40 until strobePacket.rmsPercent)
        assertTrue(decayFrame2.rmsPercent in 20 until decayFrame1.rmsPercent)

        // 4. Reset clears smoothed envelopes back to 0 / IDLE_HEARTBEAT
        mapper.reset()
        val afterReset = mapper.mapToGlyphPulsePattern(-60f, -60f, 0f)
        assertEquals(0, afterReset.rmsPercent)
        assertEquals(0, afterReset.gainReductionPercent)
        assertEquals(GlyphPulseType.IDLE_HEARTBEAT, afterReset.pulseType)

        // 5. Dual-zone routing: C-Ring (C1) tracks RMS loudness, USB-C Clamp Bar (D1) tracks Gain Reduction
        val clampPacket = mapper.mapToGlyphPulsePattern(-14f, -14f, 4.2f)
        assertEquals(GlyphPulseType.GR_CLAMP_PULSE, clampPacket.pulseType)
        val cRingZone = clampPacket.zones.first { it.zoneId == "ZONE_C1" }
        val usbClampZone = clampPacket.zones.first { it.zoneId == "ZONE_D1" }
        assertEquals(clampPacket.rmsPercent, cRingZone.intensity0To100)
        assertEquals(clampPacket.gainReductionPercent, usbClampZone.intensity0To100)
        assertTrue(usbClampZone.activePulse)
        assertTrue(clampPacket.gainReductionPercent in 55..75)

        // 6. Gain Reduction decays smoothly over subsequent quiet limiter frames
        val grDecay1 = mapper.mapToGlyphPulsePattern(-14f, -14f, 0f)
        assertTrue(grDecay1.gainReductionPercent in 35 until clampPacket.gainReductionPercent)
    }

    @Test
    fun `Audio Core Bus latency monitor tracks System nanoTime EMA, idle baseline, and 0ms bypass`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val processor = AudioProcessor(scope = scope)

        val eq = BiquadEqHook()
        val limiter = TruePeakLimiterHook(oversamplingFactor = OversamplingFactor.X4)
        processor.registerHook(eq)
        processor.registerHook(limiter)

        // 1. Paused baseline sums active hook baseline estimates
        val initialLatency = processor.telemetry.value.dspLatencyMs
        assertEquals(eq.baselineLatencyMs + limiter.baselineLatencyMs, initialLatency, 0.001f)
        assertFalse(processor.telemetry.value.isBypassed)

        // 2. Disabling a hook lowers paused baseline immediately and zeros its snapshot latency
        processor.setHookEnabled(eq.id, false)
        assertEquals(limiter.baselineLatencyMs, processor.telemetry.value.dspLatencyMs, 0.001f)
        assertEquals(0f, processor.hookSnapshots.value.first { it.id == eq.id }.latencyMs, 0.0001f)

        // Re-enable both hooks and verify System.nanoTime() EMA measurement on processThroughHooks
        processor.setHookEnabled(eq.id, true)
        val l = FloatArray(1024) { 0.4f }
        val r = FloatArray(1024) { -0.4f }
        processor.processThroughHooks(l, r, 1024)
        val measured1 = processor.telemetry.value.dspLatencyMs
        assertTrue(measured1.isFinite() && measured1 > 0f)
        assertTrue(processor.hookSnapshots.value.all { it.latencyMs > 0f })

        processor.processThroughHooks(l, r, 1024)
        val measured2 = processor.telemetry.value.dspLatencyMs
        assertTrue(measured2.isFinite() && measured2 > 0f)

        // 3. Bypass mode forces 0.00 ms telemetry & 0.00 ms hook snapshots
        processor.setWetEnabled(false)
        assertTrue(processor.telemetry.value.isBypassed)
        assertEquals(0f, processor.telemetry.value.dspLatencyMs, 0.0001f)
        assertTrue(processor.hookSnapshots.value.all { it.latencyMs == 0f })

        // 4. Re-enabling wet DSP and pausing restores cumulative active baseline estimate
        processor.setWetEnabled(true)
        processor.pause()
        assertFalse(processor.telemetry.value.isBypassed)
        assertEquals(eq.baselineLatencyMs + limiter.baselineLatencyMs, processor.telemetry.value.dspLatencyMs, 0.001f)

        processor.release()
        scope.cancel()
    }

    @Test
    fun `smart genre recognition classifies audio styles and provisions adaptive DSP presets`() {
        // 1. Studio reference classifies as ELECTRONIC with wide stereo and 96kHz 4x oversampling
        val studioRef = AudioDecoder.generateStudioReferenceTrack()
        assertEquals(MusicGenre.ELECTRONIC, studioRef.info.diagnostic.detectedGenre)
        assertEquals(96, studioRef.info.diagnostic.detectedGenre.targetKhz)
        assertEquals(OversamplingFactor.X4, studioRef.info.diagnostic.detectedGenre.oversampling)
        assertTrue(studioRef.info.diagnostic.genreConfidencePct >= 85)

        // 2. High-dynamic-range classical signal with high crest factor
        val classicalPcm = FloatArray(4800) { i ->
            if (i % 600 == 0) 0.95f else (kotlin.math.sin(2.0 * Math.PI * 440.0 * i / 48000.0).toFloat() * 0.05f)
        }
        val classicalInfo = AudioDecoder.analyzeBuffer(
            id = "symphony_01",
            title = "ORCHESTRAL_CONCERTO.wav",
            sourceLabel = "LOCAL SAF IMPORT",
            left = classicalPcm,
            right = classicalPcm.copyOf(),
            sampleRate = 48000,
            channels = 2,
            isBundled = false
        )
        assertEquals(MusicGenre.CLASSICAL, classicalInfo.diagnostic.detectedGenre)
        assertEquals(192, classicalInfo.diagnostic.detectedGenre.targetKhz)
        assertEquals(OversamplingFactor.X8, classicalInfo.diagnostic.detectedGenre.oversampling)
        assertEquals(-1.2f, classicalInfo.diagnostic.detectedGenre.ceilingDb, 0.001f)
        assertTrue(classicalInfo.diagnostic.genreConfidencePct >= 85)

        // 3. Acoustic speech/vocal signal
        val acousticPcm = FloatArray(4800) { i ->
            kotlin.math.sin(2.0 * Math.PI * 330.0 * i / 48000.0).toFloat() * 0.22f
        }
        val speechInfo = AudioDecoder.analyzeBuffer(
            id = "acoustic_guitar",
            title = "ACOUSTIC_SESSION.wav",
            sourceLabel = "AUDIORECORD MIC",
            left = acousticPcm,
            right = acousticPcm.copyOf(),
            sampleRate = 48000,
            channels = 1,
            isBundled = false
        )
        assertEquals(MusicGenre.ACOUSTIC, speechInfo.diagnostic.detectedGenre)
        assertEquals(192, speechInfo.diagnostic.detectedGenre.targetKhz)
        assertEquals(OversamplingFactor.X8, speechInfo.diagnostic.detectedGenre.oversampling)
        assertEquals(-1.0f, speechInfo.diagnostic.detectedGenre.ceilingDb, 0.001f)

        // 4. Verify all 6 genres have complete tuning parameters and -14 LUFS streaming targets
        MusicGenre.entries.forEach { genre ->
            assertTrue(genre.targetKhz in listOf(96, 192))
            assertTrue(genre.oversampling in listOf(OversamplingFactor.X4, OversamplingFactor.X8))
            assertTrue(genre.ceilingDb in -1.5f..-0.3f)
            assertTrue(genre.drive in 0.1f..0.8f)
            assertTrue(genre.punch in 0.3f..0.95f)
            assertTrue(genre.air in 0.2f..0.9f)
            assertEquals(5, genre.eqOffsetsDb.size)
            assertTrue(genre.targetLufs in -19.0f..-13.0f)
            if (genre in listOf(MusicGenre.POP, MusicGenre.ELECTRONIC, MusicGenre.ROCK, MusicGenre.HIP_HOP)) {
                assertEquals(-14.0f, genre.targetLufs, 0.001f)
            }
        }
    }
}
```

---

## `app/src/test/java/com/example/ExampleUnitTest.kt`

```kotlin
package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }
}
```

---

## `app/src/test/java/com/example/MainActivityStartupTest.kt`

```kotlin
package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.audio.TfliteDspHook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MainActivityStartupTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `MainActivity opens the single-screen local assistant workspace`() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("home_import_audio").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_play_pause").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("local_assistant_command").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("run_local_auto_master").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `TfliteDspHook falls back gracefully when model asset is missing`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val hook = TfliteDspHook(context, modelAssetName = "non_existent_model.tflite")
        val left = FloatArray(512) { 0.01f }
        val right = FloatArray(512) { 0.01f }

        hook.processBlock(left, right, 512, 48000)

        assertEquals("DSP_FALLBACK", hook.runtimeMode)
        assertTrue(left.all { it.isFinite() })
        hook.close()
    }
}
```

---

## `app/src/test/java/com/example/ProAudioDspTest.kt`

```kotlin
package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.audio.AudioDspEngine
import com.example.audio.AudioProcessor
import com.example.audio.BiquadFilter
import com.example.audio.DecodedAudio
import com.example.audio.FilterType
import com.example.audio.HiResUpsamplingHook
import com.example.audio.TruePeakLimiterHook
import com.example.model.AudioTelemetry
import com.example.model.AudioTrackInfo
import com.example.model.ExportFormat
import com.example.model.OversamplingFactor
import com.example.model.TranscodeConfig
import com.example.model.WaviqUiState
import com.example.ui.screens.Screen09StudioProSuite
import com.example.ui.screens.Screen13TranscodeMatrix
import com.example.ui.theme.WaviqTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProAudioDspTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `64-bit BiquadFilter stays numerically stable on sub-bass 35Hz shelf at 192kHz`() {
        val filter = BiquadFilter(192000.0)
        filter.configure(FilterType.LOW_SHELF, 35f, 0.707f, 6.0f, 192000f)

        var lastOut = 0.0
        for (i in 0 until 4096) {
            val x = 0.1 * sin(2.0 * PI * 35.0 * i / 192000.0)
            lastOut = filter.processLeftDouble(x)
            assertTrue("Filter output became non-finite at $i", lastOut.isFinite())
        }
        assertTrue(abs(lastOut) > 0.001)
    }

    @Test
    fun `HiResUpsamplingHook adds coherent HF harmonic brightness and stereo expansion`() {
        val hook = HiResUpsamplingHook(restorationAmount = 0.85f, targetKhz = 192, stereoExpansionAmount = 0.75f)
        hook.configure(48000)

        // Generate 6kHz tone with slight stereo imbalance
        val n = 1024
        val left = FloatArray(n) { i -> (0.35 * sin(2.0 * PI * 6000.0 * i / 48000.0)).toFloat() }
        val right = FloatArray(n) { i -> (0.20 * sin(2.0 * PI * 6000.0 * i / 48000.0)).toFloat() }
        val drySideEnergy = left.indices.sumOf { i ->
            val s = 0.5 * (left[i] - right[i])
            s * s
        }

        hook.processBlock(left, right, n, 48000)

        val wetSideEnergy = left.indices.sumOf { i ->
            val s = 0.5 * (left[i] - right[i])
            s * s
        }
        assertTrue(left.all { it.isFinite() } && right.all { it.isFinite() })
        assertTrue("Expected stereo expansion on upper band ($wetSideEnergy vs $drySideEnergy)", wetSideEnergy > drySideEnergy)
    }

    @Test
    fun `3-Band TruePeakLimiterHook clamps hot sub-bass independently and enforces ISP ceiling`() {
        val limiter = TruePeakLimiterHook(ceilingDb = -1.0f, oversamplingFactor = OversamplingFactor.X8)
        val n = 1024
        val left = FloatArray(n) { i -> (1.45 * sin(2.0 * PI * 60.0 * i / 48000.0)).toFloat() }
        val right = FloatArray(n) { i -> (1.45 * sin(2.0 * PI * 60.0 * i / 48000.0)).toFloat() }

        limiter.processBlock(left, right, n, 48000)

        assertTrue("Expected low-band gain reduction on 60Hz hot signal", limiter.lastLowBandGrDb > 0.5f)
        assertTrue("Expected overall GR > 0", limiter.lastGainReductionDb > 0.5f)
        assertTrue("Peak exceeded -1.0 dBTP ceiling", left.all { it.isFinite() && abs(it) <= 0.892f })
    }

    @Test
    fun `exportMasterWav renders 192kHz 32-bit IEEE Float WAV and applies TPDF dither to 16-bit PCM`() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val engine = AudioDspEngine(context, scope)

        try {
            val sr = 48000
            val len = 2048
            val srcL = FloatArray(len) { i -> (0.25 * sin(2.0 * PI * 1000.0 * i / sr)).toFloat() }
            val srcR = FloatArray(len) { i -> (0.25 * sin(2.0 * PI * 1000.0 * i / sr)).toFloat() }
            engine.loadDecodedAudio(DecodedAudio(srcL, srcR, sr, AudioTrackInfo()))

            // 1. Export 32-bit IEEE Float @ 192kHz
            val float192File = File(context.cacheDir, "test_master_192k_32f.wav")
            engine.exportMasterWav(
                outFile = float192File,
                config = TranscodeConfig(
                    format = ExportFormat.WAV_32F_192,
                    ditherEnabled = false,
                    normalizeToTarget = true,
                    targetTruePeakDb = -1.0f
                )
            ) { _, _ -> }

            val bytes32f = float192File.readBytes()
            val hdr = ByteBuffer.wrap(bytes32f, 0, 44).order(ByteOrder.LITTLE_ENDIAN)
            assertEquals("RIFF", String(bytes32f, 0, 4))
            assertEquals("WAVE", String(bytes32f, 8, 4))
            assertEquals(3, hdr.getShort(20).toInt() and 0xFFFF) // IEEE_FLOAT format tag = 3
            assertEquals(2, hdr.getShort(22).toInt() and 0xFFFF)
            assertEquals(192000, hdr.getInt(24))
            assertEquals(32, hdr.getShort(34).toInt() and 0xFFFF)

            // 192kHz / 48kHz = 4x frame count
            val dataBytes32f = hdr.getInt(40)
            assertEquals(len * 4 * 2 * 4, dataBytes32f)

            val (parsedL, parsedR) = AudioProcessor.parsePcmBytes(bytes32f)
            assertEquals(len * 4, parsedL.size)
            assertTrue(parsedL.all { it.isFinite() } && parsedR.all { it.isFinite() })

            // 2. Export 24-bit PCM @ 192kHz
            val pcm24File = File(context.cacheDir, "test_master_192k_24b.wav")
            engine.exportMasterWav(
                outFile = pcm24File,
                config = TranscodeConfig(
                    format = ExportFormat.WAV_24_192,
                    ditherEnabled = true,
                    normalizeToTarget = true,
                    targetTruePeakDb = -1.0f
                )
            ) { _, _ -> }

            val bytes24 = pcm24File.readBytes()
            val hdr24 = ByteBuffer.wrap(bytes24, 0, 44).order(ByteOrder.LITTLE_ENDIAN)
            assertEquals(1, hdr24.getShort(20).toInt() and 0xFFFF)
            assertEquals(192000, hdr24.getInt(24))
            assertEquals(24, hdr24.getShort(34).toInt() and 0xFFFF)

            // 3. Compare TPDF dither ON vs OFF on 16-bit PCM export of quiet signal
            val quietL = FloatArray(512) { 0.00001f }
            val quietR = FloatArray(512) { -0.00001f }
            engine.loadDecodedAudio(DecodedAudio(quietL, quietR, sr, AudioTrackInfo()))

            val ditherOffFile = File(context.cacheDir, "test_dither_off.wav")
            val ditherOnFile = File(context.cacheDir, "test_dither_on.wav")
            engine.exportMasterWav(
                outFile = ditherOffFile,
                config = TranscodeConfig(
                    format = ExportFormat.WAV_16_44,
                    ditherEnabled = false,
                    normalizeToTarget = false
                )
            ) { _, _ -> }
            engine.exportMasterWav(
                outFile = ditherOnFile,
                config = TranscodeConfig(
                    format = ExportFormat.WAV_16_44,
                    ditherEnabled = true,
                    normalizeToTarget = false
                )
            ) { _, _ -> }

            assertFalse(
                "TPDF dither must measurably change quantized LSB output compared to undithered truncation",
                ditherOffFile.readBytes().contentEquals(ditherOnFile.readBytes())
            )

            float192File.delete()
            pcm24File.delete()
            ditherOffFile.delete()
            ditherOnFile.delete()
            } finally {
                engine.release()
                scope.cancel()
            }
        }
    }

    @Test
    fun `StudioProSuite renders 3-band limiter telemetry tiles`() {
        composeTestRule.setContent {
            WaviqTheme {
                Screen09StudioProSuite(
                    state = WaviqUiState(
                        telemetry = AudioTelemetry(lowBandGrDb = 1.4f, midBandGrDb = 0.6f, highBandGrDb = 0.3f)
                    ),
                    onToggleDspHook = { _, _ -> },
                    onSetOversampling = {},
                    onToggleHiResKhz = {},
                    onJumpRoute = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("pro_band_gr_low").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("pro_band_gr_mid").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("pro_band_gr_high").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `TranscodeMatrix exposes 192kHz 24-bit and 32-bit IEEE float formats`() {
        var selectedFormat: ExportFormat? = null
        composeTestRule.setContent {
            WaviqTheme {
                Screen13TranscodeMatrix(
                    state = WaviqUiState(),
                    onUpdateConfig = { fmt, _, _ -> if (fmt != null) selectedFormat = fmt },
                    onStartExport = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("export_format_wav_24_192").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("export_format_wav_32f_192").performScrollTo().performClick()
        assertEquals(ExportFormat.WAV_32F_192, selectedFormat)
    }
}
```

---

## `app/src/test/java/com/example/StartupGuardTest.kt`

```kotlin
package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.model.StartupCheckStatus
import com.example.model.StartupGuardReport
import com.example.model.StartupHealthState
import com.example.model.StartupSubsystemCheck
import com.example.model.WaviqUiState
import com.example.ui.devmap.DeveloperMapScreen
import com.example.ui.screens.Screen01TrackInspector
import com.example.ui.theme.WaviqTheme
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StartupGuardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `validate returns HEALTHY when TFLite model, permission, and Room DB pass`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val guard = StartupGuard(
            context = context,
            permissionGrantedProvider = { true },
            databasePing = { 3 }
        )

        val report = guard.validate()

        assertEquals(StartupHealthState.HEALTHY, report.healthState)
        assertFalse(report.isDegraded)
        assertEquals(StartupCheckStatus.PASS, report.tfliteCheck.status)
        assertEquals(StartupCheckStatus.PASS, report.permissionCheck.status)
        assertEquals(StartupCheckStatus.PASS, report.databaseCheck.status)
        assertTrue(report.databaseCheck.detail.contains("3 saved masters"))
    }

    @Test
    fun `validate returns PERMISSION_ADVISORY when only RECORD_AUDIO is ungranted`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val guard = StartupGuard(
            context = context,
            permissionGrantedProvider = { false },
            databasePing = { 0 }
        )

        val report = guard.validate()

        assertEquals(StartupHealthState.PERMISSION_ADVISORY, report.healthState)
        assertFalse(report.isDegraded)
        assertEquals(StartupCheckStatus.PASS, report.tfliteCheck.status)
        assertEquals(StartupCheckStatus.ADVISORY, report.permissionCheck.status)
        assertEquals(StartupCheckStatus.PASS, report.databaseCheck.status)
    }

    @Test
    fun `validate enters DEGRADED_FALLBACK when TFLite asset is missing or corrupt`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val missingGuard = StartupGuard(
            context = context,
            assetOpener = { throw FileNotFoundException("missing.tflite") },
            permissionGrantedProvider = { true },
            databasePing = { 1 }
        )
        val missingReport = missingGuard.validate()

        assertEquals(StartupHealthState.DEGRADED_FALLBACK, missingReport.healthState)
        assertTrue(missingReport.isDegraded)
        assertEquals(StartupCheckStatus.FALLBACK, missingReport.tfliteCheck.status)
        assertEquals(StartupCheckStatus.PASS, missingReport.databaseCheck.status)

        val corruptGuard = StartupGuard(
            context = context,
            assetOpener = { ByteArrayInputStream(ByteArray(32) { 0x01 }) },
            permissionGrantedProvider = { true },
            databasePing = { 1 }
        )
        val corruptReport = corruptGuard.validate()

        assertEquals(StartupHealthState.DEGRADED_FALLBACK, corruptReport.healthState)
        assertEquals(StartupCheckStatus.FALLBACK, corruptReport.tfliteCheck.status)
    }

    @Test
    fun `validate enters DEGRADED_FALLBACK when Room DB ping fails`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val guard = StartupGuard(
            context = context,
            permissionGrantedProvider = { true },
            databasePing = { throw IllegalStateException("SQLite locked") }
        )

        val report = guard.validate()

        assertEquals(StartupHealthState.DEGRADED_FALLBACK, report.healthState)
        assertTrue(report.isDegraded)
        assertEquals(StartupCheckStatus.PASS, report.tfliteCheck.status)
        assertEquals(StartupCheckStatus.FALLBACK, report.databaseCheck.status)
        assertTrue(report.fallbackSummary.contains("SQLite locked"))
    }

    @Test
    fun `Screen01 fallback card and Dev Map diagnostics display StartupGuard status`() {
        val degradedReport = StartupGuardReport(
            healthState = StartupHealthState.DEGRADED_FALLBACK,
            tfliteCheck = StartupSubsystemCheck("TFLITE MODEL", StartupCheckStatus.FALLBACK, "Using DSP gate"),
            permissionCheck = StartupSubsystemCheck("RECORD_AUDIO", StartupCheckStatus.ADVISORY, "Optional"),
            databaseCheck = StartupSubsystemCheck("ROOM SQLITE", StartupCheckStatus.PASS, "Connected (0 saved masters)"),
            fallbackSummary = "Degraded mode active: Using DSP gate",
            checkedAtMs = 1L
        )
        var rerunCount = 0
        var dismissCount = 0

        composeTestRule.setContent {
            WaviqTheme {
                Screen01TrackInspector(
                    state = WaviqUiState(startupGuard = degradedReport, isStartupBannerDismissed = false),
                    onImportAudioClick = {},
                    onLoadReferenceClick = {},
                    onStartMicCaptureClick = {},
                    onStopMicCaptureClick = {},
                    onSeek = {},
                    onGoToScope = {},
                    onRerunStartupGuard = { rerunCount++ },
                    onDismissStartupBanner = { dismissCount++ }
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("startup_guard_fallback_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("startup_guard_rerun_button").performClick()
        composeTestRule.onNodeWithTag("startup_guard_dismiss_button").performClick()
        assertEquals(1, rerunCount)
        assertEquals(1, dismissCount)
    }

    @Test
    fun `DeveloperMapScreen displays StartupGuard diagnostics panel and triggers audit`() {
        var rerunCount = 0
        composeTestRule.setContent {
            WaviqTheme {
                DeveloperMapScreen(
                    state = WaviqUiState(
                        startupGuard = StartupGuardReport(
                            healthState = StartupHealthState.HEALTHY,
                            fallbackSummary = "All checks passed."
                        )
                    ),
                    onSelectFilter = {},
                    onInspectRoute = {},
                    onSimulateDispatch = { _, _ -> },
                    onRerunStartupGuard = { rerunCount++ }
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("devmap_startup_guard_panel").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("devmap_startup_guard_rerun_button").performScrollTo().performClick()
        assertEquals(1, rerunCount)
    }
}
```

---

## `app/src/test/java/com/example/assistant/WaviqLocalAssistantTest.kt`

```kotlin
package com.example.assistant

import com.example.assistant.WaviqLocalAssistant.Intent
import org.junit.Assert.assertEquals
import org.junit.Test

class WaviqLocalAssistantTest {

    @Test
    fun `understands Arabic cleaning request`() {
        assertEquals(Intent.CLEAN_NOISE, WaviqLocalAssistant.understand("نظّف الصوت من الضوضاء"))
    }

    @Test
    fun `understands Arabic noise removal request`() {
        assertEquals(Intent.CLEAN_NOISE, WaviqLocalAssistant.understand("ازالة الضوضاء من التسجيل"))
    }

    @Test
    fun `understands Arabic master request`() {
        assertEquals(Intent.MASTER, WaviqLocalAssistant.understand("حسن جودة هذا المقطع"))
    }

    @Test
    fun `understands English vocal request`() {
        assertEquals(Intent.VOCAL_CLARITY, WaviqLocalAssistant.understand("Make the vocals clearer"))
    }

    @Test
    fun `understands club bass request`() {
        assertEquals(Intent.CLUB_PUNCH, WaviqLocalAssistant.understand("Give it a strong 808 club punch"))
    }

    @Test
    fun `understands streaming request`() {
        assertEquals(Intent.STREAMING, WaviqLocalAssistant.understand("Prepare a Spotify streaming master"))
    }

    @Test
    fun `unknown request does not accidentally trigger mastering`() {
        assertEquals(Intent.UNKNOWN, WaviqLocalAssistant.understand("open settings and show themes"))
    }
}
```

---

## `build.gradle.kts`

```kotlin
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
}
```

---

## `gradle/libs.versions.toml`

```toml
[versions]
agp = "9.1.1"
coreKtx = "1.18.0"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
lifecycleRuntimeKtx = "2.8.7"
lifecycleViewmodelCompose = "2.8.7"
lifecycleRuntimeCompose = "2.8.7"
activityCompose = "1.10.1"
kotlin = "2.2.10"
composeBom = "2024.09.00"
googleDevtoolsKsp = "2.3.5"
navigationCompose = "2.8.9"
roomRuntime = "2.7.0"
roomKtx = "2.7.0"
roomCompiler = "2.7.0"
kotlinxCoroutinesTest = "1.10.2"
core = "1.6.1"
runner = "1.6.2"
coilCompose = "2.7.0"
retrofit = "2.12.0"
converterMoshi = "2.12.0"
kotlinxCoroutinesAndroid = "1.10.2"
kotlinxCoroutinesCore = "1.10.2"
accompanistPermissions = "0.37.3"
playServicesLocation = "21.3.0"
cameraCamera2 = "1.5.0"
cameraLifecycle = "1.5.0"
cameraView = "1.5.0"
cameraCore = "1.5.0"
loggingInterceptor = "4.10.0"
okhttp = "4.10.0"
moshiKotlin = "1.15.2"
moshiKotlinCodegen = "1.15.2"
datastorePreferences = "1.1.7"
robolectric = "4.16.1"
firebaseBom = "34.17.0"
secretsGradlePlugin = "2.0.1"
googleServices = "4.5.0"
credentials = "1.5.0"
googleid = "1.1.1"
litert = "2.3.0"


[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewmodelCompose" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycleRuntimeCompose" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "roomRuntime" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "roomKtx" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "roomCompiler" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "kotlinxCoroutinesTest" }
androidx-core = { group = "androidx.test", name = "core", version.ref = "core" }
androidx-runner = { group = "androidx.test", name = "runner", version.ref = "runner" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-core = { group = "androidx.compose.material", name = "material-icons-core" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coilCompose" }
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
converter-moshi = { group = "com.squareup.retrofit2", name = "converter-moshi", version.ref = "converterMoshi" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "kotlinxCoroutinesAndroid" }
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "kotlinxCoroutinesCore" }
accompanist-permissions = { group = "com.google.accompanist", name = "accompanist-permissions", version.ref = "accompanistPermissions" }
play-services-location = { group = "com.google.android.gms", name = "play-services-location", version.ref = "playServicesLocation" }
androidx-camera-camera2 = { group = "androidx.camera", name = "camera-camera2", version.ref = "cameraCamera2" }
androidx-camera-lifecycle = { group = "androidx.camera", name = "camera-lifecycle", version.ref = "cameraLifecycle" }
androidx-camera-view = { group = "androidx.camera", name = "camera-view", version.ref = "cameraView" }
androidx-camera-core = { group = "androidx.camera", name = "camera-core", version.ref = "cameraCore" }
logging-interceptor = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "loggingInterceptor" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
moshi-kotlin = { group = "com.squareup.moshi", name = "moshi-kotlin", version.ref = "moshiKotlin" }
moshi-kotlin-codegen = { group = "com.squareup.moshi", name = "moshi-kotlin-codegen", version.ref = "moshiKotlinCodegen" }
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastorePreferences" }
robolectric = { group = "org.robolectric", name = "robolectric", version.ref = "robolectric" }
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-ai = { group = "com.google.firebase", name = "firebase-ai" }
firebase-appcheck-recaptcha = { group = "com.google.firebase", name = "firebase-appcheck-recaptcha" }
firebase-appcheck-debug = { group = "com.google.firebase", name = "firebase-appcheck-debug" }
firebase-firestore = { group = "com.google.firebase", name = "firebase-firestore" }
firebase-auth = { group = "com.google.firebase", name = "firebase-auth" }
androidx-credentials = { group = "androidx.credentials", name = "credentials", version.ref = "credentials" }
androidx-credentials-play-services = { group = "androidx.credentials", name = "credentials-play-services-auth", version.ref = "credentials" }
googleid = { group = "com.google.android.libraries.identity.googleid", name = "googleid", version.ref = "googleid" }
litert = { group = "com.google.ai.edge.litert", name = "litert", version.ref = "litert" }


[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
google-devtools-ksp = { id = "com.google.devtools.ksp", version.ref = "googleDevtoolsKsp" }
secrets = { id = "com.google.android.libraries.mapsplatform.secrets-gradle-plugin", version.ref = "secretsGradlePlugin" }
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
```

---

## `gradle/wrapper/gradle-wrapper.properties`

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-9.3.1-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

---

## `gradle.properties`

```properties
# Project-wide Gradle settings.
# IDE (e.g. Android Studio) users:
# Gradle settings configured through the IDE *will override*
# any settings specified in this file.
# For more details on how to configure your build environment visit
# http://www.gradle.org/docs/current/userguide/build_environment.html
# Specifies the JVM arguments used for the daemon process.
# The setting is particularly useful for tweaking memory settings.
org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8
# When configured, Gradle will run in incubating parallel mode.
# This option should only be used with decoupled projects. For more details, visit
# https://developer.android.com/r/tools/gradle-multi-project-decoupled-projects
org.gradle.parallel=true
# Kotlin code style for this project: "official" or "obsolete":
kotlin.code.style=official
# Enables namespacing of each library's R class so that its R class includes only the
# resources declared in the library itself and none from the library's dependencies,
# thereby reducing the size of the R class for that library
android.nonTransitiveRClass=true
org.gradle.caching=true
org.gradle.configuration-cache=true
# Set the maximum number of workers to 4 to avoid overloading the machine.
org.gradle.workers.max=4
# Set the Kotlin compiler execution strategy to in-process to avoid "Could not
# connect to Kotlin compile daemon" error.
kotlin.compiler.execution.strategy=in-process
# Allow the google-services plugin to work even if the google-services.json is not present.
googleServices.missing.passthrough=true
```

---

## `metadata.json`

```json
{
  "name": "WAVIQ",
  "description": "16-screen offline audio mastering studio with real-time PCM DSP preview, Nothing OS Glyph VU telemetry, and an interactive Developer Navigation Map & State Flow Engine.",
  "requestFramePermissions": [],
  "majorCapabilities": ["MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API"]
}
```

---

## `settings.gradle.kts`

```kotlin
pluginManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "WAVIQ"

include(":app")
```

---

## Binary assets not embedded in this document

- `.env.example` — 125 bytes
- `app/src/main/assets/ai_audio_mastering_model.tflite` — 704 bytes
- `app/src/main/res/drawable/img_studio_console_bg.jpg` — 575,099 bytes
- `app/src/main/res/drawable/img_waviq_icon_src.jpg` — 443,075 bytes
- `app/src/main/res/font/inter.ttf` — 66,592 bytes
- `app/src/main/res/font/jetbrains_mono.ttf` — 57,404 bytes
- `app/src/main/res/font/space_grotesk.ttf` — 31,964 bytes
- `app/src/main/res/mipmap-hdpi/ic_launcher.png` — 10,644 bytes
- `app/src/main/res/mipmap-hdpi/ic_launcher_round.png` — 11,058 bytes
- `app/src/main/res/mipmap-mdpi/ic_launcher.png` — 7,631 bytes
- `app/src/main/res/mipmap-mdpi/ic_launcher_round.png` — 7,909 bytes
- `app/src/main/res/mipmap-xhdpi/ic_launcher.png` — 15,061 bytes
- `app/src/main/res/mipmap-xhdpi/ic_launcher_round.png` — 15,502 bytes
- `app/src/main/res/mipmap-xxhdpi/ic_launcher.png` — 27,293 bytes
- `app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png` — 27,126 bytes
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png` — 44,526 bytes
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png` — 43,224 bytes

Upload these binary files separately only when the Android project needs the exact resource. Keep the original paths.
