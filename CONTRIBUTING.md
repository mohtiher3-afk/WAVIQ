# Contributing to WAVIQ

## Set up

1. Install Android Studio with Android SDK Platform 36.
2. Use JDK 17 to build and JDK 21 to run the unit tests (see the README's Requirements section for why).
3. Open the repository root in Android Studio, or work from the command line with the Gradle Wrapper.

## Before opening a pull request

Run these from the repository root and make sure they pass:

```sh
./gradlew ktlintCheck
./gradlew :app:assembleDebug
./gradlew test   # needs JDK 21
```

`./gradlew ktlintFormat` fixes most style findings automatically.

## Conventions

- Kotlin code style is enforced by ktlint (`ktlint_official`) through `.editorconfig`.
- Keep behavior changes separate from formatting or reorganization changes.
- Keep product claims in documentation honest: the command router is a fixed-intent rule-based router, and the bundled LiteRT asset is not a general language model. See the README.
- Never commit release signing material. The committed `debug.keystore` is the standard public debug certificate and signs debug builds only.
