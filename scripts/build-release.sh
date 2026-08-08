#!/usr/bin/env bash
#
# Kabel-Kopf Release-Build
# -------------------------
# Baut ein signiertes Release-AAB für die Google Play Interne Testversion
# und legt es versioniert unter build-output/ ab.
#
# Voraussetzungen:
#   - keystore.properties (lokale Datei, gitignored) mit Release-Keystore
#   - JAVA verfügbar via ./gradlew
#
# Nutzung:
#   ./scripts/build-release.sh           # nur AAB
#   ./scripts/build-release.sh --apk     # AAB + signiertes APK
#
set -euo pipefail

cd "$(dirname "$0")/.."
APP_DIR="app/build/outputs"
OUT_DIR="build-output"
KEYSTORE_PROP="keystore.properties"

if [[ ! -f "$KEYSTORE_PROP" ]]; then
  echo "FEHLER: '$KEYSTORE_PROP' fehlt. Kein Release-Signing möglich." >&2
  echo "Lege die Datei an (siehe docs/RELEASE.md) und versuche es erneut." >&2
  exit 1
fi

mkdir -p "$OUT_DIR"

echo "==> versionCode/Name lesen ..."
VERSION=$(grep -m1 'versionName' app/build.gradle.kts | sed -E 's/.*"([0-9.]+)".*/\1/')
CODE=$(grep -m1 'versionCode' app/build.gradle.kts | sed -E 's/.*(versionCode = )([0-9]+).*/\2/')
echo "    versionName=$VERSION  versionCode=$CODE"

echo "==> bundleRelease bauen ..."
./gradlew bundleRelease

AAB="$APP_DIR/bundle/release/app-release.aab"
DEST="$OUT_DIR/kabelkopf-release-$VERSION-$CODE.aab"
cp "$AAB" "$DEST"
echo "OK  AAB: $DEST"

if [[ "${1:-}" == "--apk" ]]; then
  echo "==> assembleRelease (APK) bauen ..."
  ./gradlew assembleRelease
  APK="$APP_DIR/apk/release/app-release.apk"
  DEST_APK="$OUT_DIR/kabelkopf-release-$VERSION-$CODE.apk"
  cp "$APK" "$DEST_APK"
  echo "OK  APK: $DEST_APK"
fi

echo
echo ">>> Veröffentlichen: Obige AAB-Datei in Play Console > Internes Testprogramm hochladen."