# Datenbank (Room, SQLite)

## Grundsatz

**Datenverlust ist der Super-GAU.** Die App ist das Wissensgedächtnis des Nutzers.
Aktuell: Schema-Version **7**.

- `fallbackToDestructiveMigration` ist **NICHT** aktiviert → ein Schema-Update löscht
  niemals Nutzerdaten.
- `exportSchema = true`: gespeicherte Schemata liegen unter `app/schemas` und werden
  mitversioniert.
- Jede Schemaänderung braucht eine explizite `Migration` in `AppDatabase` und einen
  Schema-`Commit`.

## Entities (Version 7)

- `WorkTaskEntity` – Arbeitsjournaleinträge
- `WorkStepEntity` – Schritte je Aufgabe
- `ErrorLogEntity` – Fehlerdokumentation
- `ErrorCauseEntity` – Ursachen-Katalog
- `KnowledgeBaseEntity` – persönliche Wissenseinträge
- `WorkflowTemplateEntity` – Vorlagen
- `PhotoEntity` – Fotos
- `ProjectEntity`, `ProjectAssetEntity` – Baustellen-Assets
- `SkillEntity`, `AchievementEntity` – Gamification
- `ChecklistEntity`, `ChecklistItemEntity` – Checklisten

## Schema exportieren

Build-Gradle hat `exportSchema = true` gesetzt. Das generierte Schema wird bei
`androidx.room` in `app/schemas/` abgelegt und muss mitcommittet werden, damit
Migrationstests das historische Schema kennen.

## Neue Version hinzufügen

1. Version in `AppDatabase.version` erhöhen.
2. `Migration(altVersion, neuVersion)` im `companion object` implementieren.
3. Migration in `DatabaseModule` registrieren (`.addMigrations(...)`).
4. Schema generieren und committen.
5. (Empfohlen) Migrations-Smoke-Test.

## Seeder / Seed-Daten

`DatabaseSeeder` legt einmalig Basisdaten an (Wissen, Templates, ErrorCauses, Skills).
Wird nur bei leerem DB-Stand ausgeführt – nie bei Migrationen.