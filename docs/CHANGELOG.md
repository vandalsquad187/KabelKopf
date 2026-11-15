# Changelog

Alle nennenswerten Änderungen. Format nach
[Keep a Changelog](https://keepachangelog.com/).

## [Unreleased]

(noch nichts)

---

## [1.1.1] – 2026-11-15 (versionCode 13)

### Changed
- `isShrinkResources = true`: R8 entfernt jetzt auch ungenutzte
  Ressourcen → kleinere Download-Größe, geringerer Arbeitsspeicher-
  verbrauch (Play-Release-Dashboard-Empfehlung "R8-Optimierung")
- Keine Funktionsänderungen

---

## [1.1.0] – 2026-10-31 (versionCode 12)

### Added
- In-App-Bewertungs-Flow (`AppReviewer`): Google Play In-App-Review wird
  kontextsensitiv im Betrieb angestoßen
- Diagnose-Berichte im Profil: lokal gespeicherte Crash-Logs können vom
  Nutzer geteilt werden (nur technische Fehlermeldungen, keine Arbeits-
  oder Kundendaten; kein automatisches Senden)
- CI-Workflow (`.github/workflows/ci.yml`): Build + Tests bei jedem Push

### Changed
- `targetSdk`/`compileSdk` 36 (Android 16) – Google-Play-Anforderung
  seit November 2026

---

## [1.0.x] – vor Umbenennung / Kabel-Kopf-Public-Release

### Changed
- Vollständige Umbenennung auf Kabel-Kopf / `com.docai.kabelkopf`
  (applicationId, namespace, alle Packages, ProGuard-Keep-Rules)
- App-Klasse `ElektrikerApp` → `KabelKopfApp`, Theme `Theme.KabelKopf`

### Added
- `README.md`, `docs/` (ARCHITECTURE, DATABASE, ROADMAP, FEATURES, CHANGELOG, RELEASE, STYLEGUIDE)

---

## [frühere Version – 1.0.0] (vor Umbenennung)

Siehe Git-History.

### Added / Changed (historisch)
- Fotos zoombar + Assets bearbeitbar
- Datenpersistenz + Migration + Fullscreen-Fotos
- UX-Rework + Baustellen-Assets (UV/Dokumente)
- Phase 2: Globale Suche, Skill-Radar, Checklisten
- Phase 1: Timer pro Task, ErrorCauses, Task↔Wissen
- Azubi-Basiswissen (33 KnowledgeBase, 14 Workflow-Templates, 15 ErrorCauses, Skills)
- Gamification & Erfolge
- Backup/Export