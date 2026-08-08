# Kabel-Kopf

Das persönliche Erfahrungs- und Wissensgedächtnis für Elektriker.

> Deine App ist dein digitales Gehirn: Arbeitsjournal, Fehlerdatenbank, Checklisten,
> Wissensdatenbank und Projekte – sie funktionieren komplett offline und gehören dir.

---

## Produktbeschreibung

**Kabel-Kopf** erfasst, organisiert und vernetzt das täglich wachsende Erfahrungswissen
eines Elektrikers. Die App ist *offline-first* und lokal gespeichert – deine Daten bleiben
auf deinem Gerät, solange du es willst.

Der Kern (Core) ist komplett nutzbar und kostenlos. Zukünftige Module wie KI-Assistent,
Cloud-Synchronisation und Team-Features liegen als optionale Schichten darüber.

## Kernfunktionen (FREE)

- 📘 **Arbeitsjournal** – Arbeiten erfassen, Schritte abhaken, Bewertung, PDF-Export
- ❌ **Fehlerdatenbank** – Fehler dokumentieren, Ursachen (ErrorCauses), Warnungen
- ✅ **Checklisten** – wiederkehrende Abläufe als vorbereitete Listen
- 🧠 **Wissensdatenbank** – persönliche Einträge, Workflow-Templates, Wissen↔Task
- 📷 **Fotos & Baustellen-Assets** – Schaltschrankfotos, UV-Assets, Dokumente
- 🔍 **Globale Suche** – über alle Einträge
- 📁 **Projekte** – Aufgaben und Assets je Baustelle
- 🏆 **Gamification / Skills / Erfolge** – Erfahrungs-Punkte, Radar, Auszeichnungen
- 📈 **Insights** – Häufigkeit, Trends
- 💾 **Backup & Export** – auf Gerät (PDF, JSON-Export/Import)
- 🎙 **Sprachaufnahme**, 📷 **Kamera**
- 🌍 **Offline-Betrieb** – Room-Datenbank, kein Netzwerk nötig

Die Kernfunktionen bleiben **kostenlos**. Pro ist rein **optional** und nur für neue,
erneuernde Module (KI, Cloud, Team) gedacht – keine bestehende Funktion wird gesperrt.

---

## Technologien

- **Kotlin** + **Jetpack Compose** (Material 3)
- **MVVM** + **Repository-Pattern** (ViewModel → Repository → DAO)
- **Room** (SQLite) – Offline-first Persistenz mit Schema-Migrationen
- **Hilt** (Dependency Injection)
- **Navigation Compose**
- **CameraX** (Foto/Aufnahme), **Coil** (Bilder)
- **iText** (PDF)
- Backup/Export via **JSON**

---

## Architektur

```
KabelKopf
├── core/   (offline, komplett frei – das Funktionsmodule)
├── ai/     (optional – KI-Assistent, Fehleranalyse)
├── cloud/  (optional – Synchronisation, Backup)
└── pro/    (optional – Monetarisierung, erweitertes Analyse)
```

Dokumentation: siehe [docs/](docs/).

---

## Roadmap

Siehe [docs/ROADMAP.md](docs/ROADMAP.md).

---

## Download

> Kommt bald – Interne Testversion via Google Play (Interner Test-Track).

---

## Screenshots

*(werden ergänzt)*

---

## Lizenz

Privat / Doc.Ai. Alle Rechte vorbehalten.