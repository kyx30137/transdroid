#!/usr/bin/env bash
# Render the real Compose screens to PNG on the JVM - no emulator, no device, no KVM.
#
# Usage:
#   tools/screenshots.sh                # render everything, into app/build/reports/screenshots
#   tools/screenshots.sh '*RssScreenshot*'   # only tests matching a filter
#
# Requirements
#   * A JDK for the host CPU. Robolectric's native graphics runtime ships for macOS arm64/x86_64,
#     linux x86_64 and Windows x86_64; on an arm64 Linux host run this under an x86_64 JDK with
#     amd64 libc (Rosetta/qemu). Paparazzi cannot help there: its layoutlib bridge is x86_64-only.
#   * ANDROID_HOME pointing at an SDK with platform 36 and build-tools 36+.
#   * Network access to Maven on first run (Robolectric pulls its android-all runtime).
#     Set ROBOLECTRIC_MIRROR (or pass it through) when Maven Central is unreachable, e.g.
#     ROBOLECTRIC_MIRROR=https://maven.aliyun.com/repository/public tools/screenshots.sh
set -euo pipefail

cd "$(dirname "$0")/.."

FILTER="${1:-*ScreenshotTest}"
OUT="app/build/reports/screenshots"

ARGS=(--tests "$FILTER" --console=plain)
if [[ -n "${ROBOLECTRIC_MIRROR:-}" ]]; then
    ARGS+=("-Probolectric.repo.url=${ROBOLECTRIC_MIRROR}")
fi

# GRADLE_BIN lets a sandbox without access to services.gradle.org use an already installed
# distribution instead of the wrapper's download.
GRADLE="${GRADLE_BIN:-./gradlew}"
$GRADLE :app:testFullDebugUnitTest "${ARGS[@]}"

echo
echo "Screenshots in ${PWD}/${OUT}:"
ls -1 "$OUT"/*.png 2>/dev/null || echo "  (none - check the test output above)"
