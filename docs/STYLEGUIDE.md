# Styling / Konventionen

## Architektur
- **ViewModel → Repository → DAO**: UI greift **nie** direkt auf ein `Dao` zu.
- Datenfluss über `StateFlow` + `viewModelScope`.
- DI zentral via Hilt (`@Singleton` für DB, Repositories).

## Kotlin
- Package-Format: `com.docai.kabelkopf.*` (keine altren Namen mehr).
- Kein Hardcode von `android.util.Log` – stattdessen `Logger.i/w/e` (Phase D) verwenden.
- Kompakt, idiomatisch, dekliziert.
- State as `data class XxxUiState` im jeweiligen `ViewModel`.

## UI / Compose
- Material 3, typisierte Theme (`Theme.KabelKopf`) im `ui/theme`.
- Navigation über `Screen.kt` (Routen) + `NavGraph.kt`.
- Komponenten wiederverwendbar in `ui/components`.

## Datenbank
- Jede Schemaänderung → echte `Migration` (nie destructive).
- `exportSchema = true`, Schema committen.

## Fehlerbehandlung
- Keine `printStackTrace()` im Produktivcode → `Logger` / CrashReporter.
- Fehler sauber an UI-State binden, nicht schlucken.

## Naming
- Englische Code-Identifiers, deutsche UI-Texte.
- Entities: `<Name>Entity`, DAOs `<Name>Dao`, Repos `<Name>Repository`.