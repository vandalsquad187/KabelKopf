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
- [x] **Auf Google Play** (intern → closed → open → Production, seit 10/2026 live)
- [x] Migration-Smoke-Tests
- [x] In-App-Review (sanfter Flow nach 5. gespeicherter Arbeit, max. 1×)
- [x] Unit-Tests + GitHub-Actions-CI (Tests blockierend, Lint informativ)

## Phase D — Beobachtbarkeit ✅ (lokal, ohne Internet-Permission)
- [x] `Logger.kt` + `Log` ersetzt
- [x] Crash-Reporting: `FileCrashReporter` (lokale Logs) + „Diagnose-Details senden" im Profil
- [ ] Optional später: Cloud-Crashlytics/Sentry (erst mit Server-Feature/INTERNET)

## Phase E — Pro / Monetarisierung (NICHT jetzt)
- [x] `PremiumRepository` (Fake, `isPro=false`) – Struktur nur
- [ ] 1.2.0: Play Billing, `isPro` an echten Purchase (ohne App-Login, Play-Identität genügt)
- [ ] 1.3.0: KI-Assistent (Supabase Free/EU + LLM-Proxy, Play-Kauf-Verifikation statt Account)
- [ ] 1.4.0: Cloud-Sync (bringt Google-Login via Supabase Auth mit)
- [ ] 1.5.0: Team-Features + Abo-Verkauf
- [ ] **Keine** bestehende Funktion sperren / keine künstlichen Limits