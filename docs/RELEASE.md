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

### 1. Release bauen (Skript)

```bash
./scripts/build-release.sh            # nur AAB
./scripts/build-release.sh --apk     # AAB + signiertes APK
```

- Das Skript prüft, ob `keystore.properties` existiert (sonst Abbruch mit Fehler).
- Ergebnis (versioniert): `build-output/kabelkopf-release-<version>-<code>.aab`
  (bzw. `.apk` bei `--apk`).
- Beispiel: `build-output/kabelkopf-release-1.0.0-1.aab`

> `build-output/` ist in `.gitignore` (kein Upload ins Repo).
> `app/build/outputs/...` ist der Gradle-Standardpfad und bleibt unangetastet.

### 2. In Play Console hochladen

1. Play Console → **App auswählen** → **Release-Management → Internes Testprogramm**
2. **Neue Releaseversion erstellen** → das AAB hochladen
3. Release-Notizen + App-Highlight (kurzes Update-Log)
4. Release speichern, Review durchlaufen, **“Rollout starten”**

### 3. Tester freigeben (Geräte/Team)

1. Im selben Track: **Testers → Test-Geräte/Testnutzer** öffnen
2. Bis zu 100 **Google-Konto-E-Mail-Adressen** einpflegen
   (z.B. eigene, Kollegen, Azubis)
3. Play generiert einen **Opt-in-Link** (per E-Mail oder kopieren)
4. Jeder Tester öffnet den Link, akzeptiert → App direkt im **Play Store**
   herunterladbar (kein APK-Sideload nötig)

### 4. Feinheiten

- Daten/Screenshots (Store-Listing) später ergänzen
- Bei Update: einfach ein neues AAB (höherer `versionCode`) hochladen

## Before-Release-Checklist

- [ ] `./gradlew test` grün / Smoke-Tests (Room, Hilt)
- [ ] `bundleRelease` baut ohne ProGuard-Fehlschlag (Room/Hilt/iText gehen)
- [ ] keine `com.elektriker` / "Erfahrungsgedaechtnis" Verweise mehr
- [ ] Backup-Einleitung + Migration verifiziert
- [ ] Keystore vorhanden, `versionCode` erhöht

## Nach Release

- Git-Tag vergeben
- CHANGELOG ergänzen