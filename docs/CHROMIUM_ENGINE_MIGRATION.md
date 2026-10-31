# Datu Browser — Chromium engine migration

## Current status

The `chromium-engine` branch is an isolated experiment based on `main`. The stable `main` branch is not changed.

The current app is a Java/Sketchware-generated Android app that embeds pages through `android.webkit.WebView`. `MainActivity` directly depends on WebView APIs for navigation, settings, JavaScript injection, request interception, cookies, and downloads.

## Important upstream constraint

The Chromium project does not publish a supported Android AAR/Gradle SDK for embedding the Chromium Content layer into an arbitrary Android app. Chromium's own guidance says that a direct embedder must be built around Chromium's GN/Ninja build system and that Content Shell is a testing/example target, not a production-grade browser. Existing code written against `android.webkit.WebView` cannot simply be redirected to a standalone Chromium engine.

References:
- Chromium WebView architecture: https://github.com/chromium/chromium/blob/main/android_webview/docs/architecture.md
- Chromium Android build targets (Chrome APK and Content Shell): https://chromium.googlesource.com/chromium/src/+/HEAD/docs/android_build_instructions.md
- Chromium developer discussion on embedding Chromium in Android apps: https://groups.google.com/a/chromium.org/g/chromium-dev/c/7oa3OoEt-7w

## Migration requirements

A genuine standalone Chromium embedder would require:

1. Build and pin a Chromium source revision with the Android toolchain and GN/Ninja.
2. Create a native Java/JNI bridge and a rendering host based on Chromium's Content APIs (not `android.webkit.WebView`).
3. Reimplement navigation, back/forward, lifecycle, permissions, cookies, downloads, file chooser, SSL/error handling, and renderer crash recovery.
4. Rebuild Datu Shields around the new engine's request interception/network hooks; WebView callbacks and `evaluateJavascript` cannot be reused as-is.
5. Package and test ABI-specific native libraries, renderer processes, sandboxing, updates, and APK size.
6. Add automated smoke tests and verify a release APK on physical Android devices before considering migration complete.

## Safety rule for this experiment

Do not remove the current WebView implementation or change the stable `main` branch until a standalone Chromium prototype can build, launch, load HTTPS pages, navigate back/forward, and pass basic security tests.

## First milestone

Produce a minimal Chromium Content embedder prototype and a reproducible build pipeline. Do not claim the engine is integrated until the APK actually contains and launches the Chromium runtime without relying on the device's WebView provider.
