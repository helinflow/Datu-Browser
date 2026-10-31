#!/usr/bin/env bash
set -Eeuo pipefail

# Builds the official Chromium Content Shell for Android as a toolchain/runtime
# proof. This is NOT the Datu Browser APK and does not embed Chromium in Datu.

WORKSPACE="${CHROMIUM_WORKSPACE:-${RUNNER_TEMP:-$HOME}/datu-chromium}"
SRC_DIR="$WORKSPACE/src"
DEPOT_TOOLS="$WORKSPACE/depot_tools"
OUT_DIR="out/DatuAndroid"
MIN_DISK_GB=100

command -v git >/dev/null || { echo "ERROR: git is required."; exit 1; }
command -v python3 >/dev/null || { echo "ERROR: Python 3 is required."; exit 1; }

mkdir -p "$WORKSPACE"
AVAILABLE_GB=$(df -Pk "$WORKSPACE" | awk 'NR==2 {print int($4/1024/1024)}')
if (( AVAILABLE_GB < MIN_DISK_GB )); then
  echo "ERROR: Chromium needs at least ${MIN_DISK_GB} GB free for this build; found ${AVAILABLE_GB} GB."
  exit 1
fi

if [[ ! -d "$DEPOT_TOOLS/.git" ]]; then
  git clone https://chromium.googlesource.com/chromium/tools/depot_tools.git "$DEPOT_TOOLS"
fi
export PATH="$DEPOT_TOOLS:$PATH"
export DEPOT_TOOLS_UPDATE=0

cd "$WORKSPACE"
if [[ ! -d "$SRC_DIR/.git" ]]; then
  fetch --nohooks --no-history android
fi

cd "$SRC_DIR"
if [[ -n "${CHROMIUM_REVISION:-}" ]]; then
  echo "Syncing Chromium revision: $CHROMIUM_REVISION"
  gclient sync --revision="src@$CHROMIUM_REVISION"
else
  echo "No CHROMIUM_REVISION supplied; using the current Chromium Android checkout."
  gclient sync
fi

if [[ $EUID -eq 0 ]]; then
  ./build/install-build-deps.sh --android
elif command -v sudo >/dev/null; then
  sudo ./build/install-build-deps.sh --android
else
  echo "ERROR: install Android build dependencies first (sudo is unavailable)."
  exit 1
fi

gclient runhooks

gn gen "$OUT_DIR" --args='target_os="android" target_cpu="arm64" is_debug=false'
autoninja -C "$OUT_DIR" content_shell_apk

APK="$OUT_DIR/apks/ContentShell.apk"
if [[ ! -f "$APK" ]]; then
  echo "ERROR: Chromium build completed but expected APK was not found at $APK."
  exit 1
fi

DEST="${GITHUB_WORKSPACE:-$WORKSPACE}/artifacts/Datu-Chromium-ContentShell-arm64.apk"
mkdir -p "$(dirname "$DEST")"
cp "$APK" "$DEST"
echo "Chromium Content Shell APK created: $DEST"
echo "IMPORTANT: This is a standalone Chromium test shell, not Datu Browser.";
