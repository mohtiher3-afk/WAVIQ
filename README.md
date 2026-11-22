# WAVIQ

WAVIQ is an offline-first Android audio studio for analyzing and mastering tracks on-device. Its current workflow lets you import audio, inspect available analysis, apply local audio-processing profiles, save masters to a local library, and export WAV files.

## What it does today

- Kotlin Android app with Jetpack Compose and Material 3.
- Studio and Library screens, with audio metadata stored locally in Room.
- Local audio decoding, measurements, DSP processing, and WAV export.
- A small Arabic/English command router for a fixed set of supported requests.
- LiteRT integration with a local DSP fallback path.

The command router is rule-based, not a general-purpose conversational AI model. The bundled LiteRT asset is not a general language model or a dedicated neural audio-restoration model. Noise-cleanup requests use the processing paths currently implemented in the app, including vocal-clarity/noise-gate behavior. The current frequency-band mixer does not separate vocals, drums, bass, or other sources into true stems. See the source and tests before making product claims about audio quality or model capabilities.

Audio decoding, processing, and export are designed to run locally. The app does not declare the Android `INTERNET` permission in its manifest.

## Requirements

- Android Studio with Android SDK Platform 36 installed.
- An Android device or emulator running Android 7.0 (API 24) or newer. `minSdk` is 24; the project targets API 36.
- A compatible JDK selected as the Gradle JDK in Android Studio. The Gradle Wrapper downloads the project's configured Gradle distribution on first use.

## Open and build

1. Clone this repository and open its root directory in Android Studio.
2. Install Android SDK Platform 36 if Android Studio prompts you, then sync the Gradle project.
3. Select the `app` run configuration and run it on an API 24+ device or emulator.

From a terminal, build the debug APK with:

```sh
./gradlew :app:assembleDebug
```

On Windows, use `gradlew.bat :app:assembleDebug`.

## Tests

Run the local unit and Robolectric tests with:

```sh
./gradlew test
```

Run the instrumentation tests on a connected API 24+ device or running emulator with:

```sh
./gradlew connectedAndroidTest
```

Android Studio also provides the standard Run Test action for individual tests.

## First device smoke test

After installing the app, verify the core user path on a real device:

1. Import a supported audio file from the system file picker.
2. Review the analysis information the app provides.
3. Apply a mastering profile and listen to the result.
4. Save it to the local library and export a WAV file.

Compare the source and processed audio by listening. A successful build or passing unit test does not by itself establish audio quality or prove that every device/format works.

## Project structure

- `app/src/main/java/com/example/audio/` — audio decoding, analysis, DSP, and LiteRT integration.
- `app/src/main/java/com/example/data/` — Room-backed master library.
- `app/src/main/java/com/example/ui/` — Compose screens, components, and theme.
- `app/src/test/` — unit and Robolectric tests.
- `app/src/androidTest/` — device/emulator instrumentation tests.

## Notes

The project currently uses a placeholder Android namespace and application ID in `app/build.gradle.kts`. Do not change either as part of a README or build-wrapper update. A release build also needs the signing material and environment variables configured by the project; never commit keystores or signing secrets.
