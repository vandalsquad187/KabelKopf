# Architektur

## Prinzip: Core zuerst, optionale Module darüber

Die App ist als modulares System ausgelegt, dessen **Kern komplett offline** und
vollständig funktional ist. KI, Cloud und Pro liegen als optionale Schichten darüber
und sperren keine bestehende Funktion.

```
KabelKopf
├── core        (@Singleton, offline, alles frei)
│     ├── data/local        Room + DAO + Entities + Migrationen
│     ├── data/repository   Repository-Pattern
│     └── service           Backup, PDF, Logging, Voice
├── ai          (optional – geplant: Assistent, Fehleranalyse, OCR)
├── cloud       (optional – geplant: Sync, Gerätewechsel)
└── pro         (optional – Monetarisierung)
```

## Schichtenaufbau (grobe Richtung)

```
UI (Compose Screen / ViewModel)
   ↓
Repository (Data-Logik, Aggregation)
   ↓
DAO (Room)
   ↓
SQLite (lokal)
```

Regeln:
- **ViewModel → Repository → DAO**: UI-Schichten sprechen nie direkt ein DAO an.
- Datenfluss über **StateFlow** + `viewModelScope` (Koroutinen).
- DI über **Hilt** (`@Singleton` für DB/Repositories).

## Modulgrenzen

Neue Funktionen werden hinter ein schmales Interface gelegt, damit der Kern unabhängig
bleibt. Aktuell: `PremiumRepository` (Fake) – siehe Phase E. Echte Billing-Lib kommt erst,
wenn Nutzer existieren (siehe ROADMAP).

→ siehe auch [STYLEGUIDE.md](STYLEGUIDE.md) und [DATABASE.md](DATABASE.md).