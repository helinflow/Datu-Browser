# Datu Browser — Chromium engine migration

## Current status

The `chromium-engine` branch is an isolated experiment based on `main`. The stable `main` branch is not changed.

The current app is a Java/Sketchware-generated Android app. Its browser screen is built around `android.webkit.WebView`. `MainActivity` directly uses WebView APIs for navigation, settings, JavaScript, request interception, cookies, and downloads.

The current Android project targets compile SDK 35, target SDK 34, and min SDK 23. The supported Chromium Android build configuration and ABI must be selected deliberately; do not assume that the current app's min SDK or every ABI is compatible with a future Chromium build.

## Important upstream constraint

Chromium does not provide a supported, drop-in Android AAR/Gradle SDK that turns an arbitrary app into a Chromium Content embedder. Chromium's official Android instructions use its own source checkout and build tools (GN plus Ninja/Siso).

The official `content_shell_apk` target is useful as a minimal Content-layer test application. It is not a reusable browser view or a production-ready embeddable SDK. Building Content Shell proves that the Chromium source/toolchain can produce and launch a Chromium-based Android app; it does **not** prove that Datu can embed that engine.

References:
- Chromium Android build instructions: https://chromium.googlesource.com/chromium/src/+/main/docs/android_build_instructions.md
- Chromium WebView architecture: https://github.com/chromium/chromium/blob/main/android_webview/docs/architecture.md
- Chromium Content module overview: https://www.chromium.org/developers/content-module/

## Implemented build prototype

The branch now contains:
- `scripts/build-chromium-content-shell.sh`: checks the build workspace, obtains `depot_tools`, checks out/synchronizes Chromium, installs Android build dependencies, and builds the official `content_shell_apk` target for Android ARM64.
- `.github/workflows/chromium-prototype.yml`: a manually triggered workflow that runs on a dedicated self-hosted Linux x86_64 runner labelled `chromium-builder` and uploads the test APK as a short-lived workflow artifact.

The runner needs at least 100 GB free disk space. At least 16 GB RAM is recommended by Chromium; 8 GB is the documented minimum. GitHub-hosted runners are deliberately not used for this job because their available disk space is not suitable for a full Chromium checkout/build.

To run it, register a self-hosted Linux x86_64 GitHub Actions runner with the additional label `chromium-builder`, then open Actions → Chromium Android prototype → Run workflow. An optional Chromium commit hash can be supplied to pin the source revision.

**Artifact naming is explicit:** `Datu-Chromium-ContentShell-arm64.apk` is a standalone Chromium test shell. It is not the Datu Browser APK and must not be installed or presented as a Datu release.

## Staged prototype plan

### Stage 0 — preserve the working browser

- Keep the existing WebView implementation untouched as the fallback.
- Keep all Chromium experiments on `chromium-engine`.
- Do not change the stable `main` branch or replace the existing APK.

### Stage 1 — prove the official Chromium Android build

- Use a dedicated Linux build machine with the disk space, memory, Android SDK/NDK toolchain, and build dependencies required by Chromium.
- Pin a specific Chromium source revision rather than building an unpinned moving branch for release work.
- Configure GN for Android and the intended device ABI.
- Build the official `content_shell_apk` target and launch it on a test device.
- Verify that an HTTPS page loads and that the test app is using its packaged Chromium runtime, not Android's WebView provider.

This stage is a toolchain and runtime proof only. The Content Shell APK is not the Datu APK.

### Stage 2 — assess embedding feasibility

Only after Stage 1 succeeds, investigate a minimal Content embedder host and the Java/JNI boundary required to host it inside an Android application. Document the upstream APIs and required native libraries before writing integration code. Do not invent a Maven dependency or copy Chromium binaries from unrelated apps.

### Stage 3 — integrate behind an engine boundary

If a maintainable embedder is feasible, isolate engine-specific code behind a Datu interface. Port navigation, lifecycle, permissions, cookies, downloads, file chooser, SSL/error handling, and renderer crash recovery incrementally. Keep WebView selectable as fallback until parity and stability tests pass.

### Stage 4 — port Datu Shields and release checks

Reimplement request filtering using supported Chromium networking hooks. WebView callbacks and `evaluateJavascript` are not drop-in replacements. Test HTTPS, redirects, downloads, privacy behavior, renderer recovery, ABI packaging, APK size, and installation on physical devices.

## Stop conditions

Pause the migration rather than pretending it is complete if:
- Chromium cannot be built reproducibly with the available build machine.
- The only working result is the standalone Content Shell test APK.
- Embedding requires unsupported/private APIs or an unmaintainable patch set.
- The resulting APK cannot be tested on the target Android versions and ABIs.

## Completion rule

Do not remove the current WebView implementation or change the stable `main` branch until a genuine Datu-hosted Chromium prototype can build, launch, load HTTPS pages, navigate back/forward, survive basic lifecycle events, and pass security and stability tests. Never label a build as Chromium-integrated unless the Datu APK itself contains and launches the Chromium runtime without relying on the device's WebView provider.
