# Roadmap

## Phase A – Marke & Grundlage ✅
- [x] Umbenennung auf `com.docai.kabelkopf` (applicationId / namespace / package)
- [x] Name **Kabel-Kopf**, rootProject
- [x] `README.md`, `docs/`
- [x] Datenbank-Software (kein Destructive-Migration-Fallback)
- [x] Tag `v0.1`

## Phase B — Kernwandel (größtenteils erledigt)
- [x] Dashboard
- [x] Arbeitsjournal (Tasks/Schritte, Bewertung)
- [x] Fehlerdatenbank (ErrorLog + ErrorCauses)
- [x] Checklisten
- [x] Wissensdatenbank + Workflow-Templates
- [x] Projekte & Baustellen-Assets
- [x] Globale Suche
- [x] Skills, Gamification, Achievements, Insights
- [x] Fotos, Sprachaufnahme

## Phase C — Absicherung & Vertrieb ✅
- [x] Backup/Export/Import (JSON, PDF)
- [x] PDF-Export
- [ ] **Interne Testversion** auf Google Play
- [ ] Migration-Smoke-Tests

## Phase D — Beobachtbarkeit (empfohlen, nicht-blockierend)
- [ ] `Logger.kt` + `Log` ersetzen
- [ ] Crash-Reporting (Interface, optional Firebase Crashlytics / Sentry), datenschutzfreundlich (opt-in)

## Phase E — Pro / Monetarisierung (NICHT jetzt)
- [x] `PremiumRepository` (Fake, `isPro=false`) – Struktur nur
- [ ] Echt: Google Play Billing
- [ ] KI-Assistent, Cloud-Sync, Team-Features als Pro-Module
- [ ] **Keine** bestehende Funktion sperren / keine künstlichen Limits