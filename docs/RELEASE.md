# Release-Prozess

## Versionierung

- **Git-Tags** ab jetzt: `v0.1`, `v0.2`, … pro Meilenstein (siehe ROADMAP).
- **Android versionCode/versionName**: siehe `app/build.gradle.kts`.
  - `versionCode`: monoton steigende Int (1, 2, 3, …)
  - `versionName`: menschenlesbar ("1.0.0", …)

## Signierung

Release-Builds brauchen einen Keystore. **Kein Keystore wird committet**:

| Datei | Zweck | Im Git? |
|---|---|---|
| `kabelkopf-release.jks` | Keystore | **Nein** |
| `keystore.properties` | Passwort/Alias | **Nein** |

`keystore.properties` wird von Gradle beim Release eingelesen
(siehe `app/build.gradle.kts`).

> ⚠️ Keystore sicher verwahren (jede Cloud/Backup). Verlust = keine Update-Möglichkeit
> für veröffentlichte App.

## Interne Testversion (Google Play)

1. `./gradlew bundleRelease`
2. AAB in Play Console: **Test → Interner Test** (Track)
3. Tester (Google-Konto) freigeben
4. Daten/Screenshots (später) ergänzen

## Before-Release-Checklist

- [ ] `./gradlew test` grün / Smoke-Tests (Room, Hilt)
- [ ] `bundleRelease` baut ohne ProGuard-Fehlschlag (Room/Hilt/iText gehen)
- [ ] keine `com.elektriker` / "Erfahrungsgedaechtnis" Verweise mehr
- [ ] Backup-Einleitung + Migration verifiziert
- [ ] Keystore vorhanden, VersionINCREMENTE

## Nach Release

- Git-Tag vergeben
- CHANGELOG ergänzen