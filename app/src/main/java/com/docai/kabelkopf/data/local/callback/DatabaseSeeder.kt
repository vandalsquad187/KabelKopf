package com.docai.kabelkopf.data.local.callback

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.docai.kabelkopf.data.local.AppDatabase
import com.docai.kabelkopf.data.local.entity.WorkflowTemplateEntity
import com.docai.kabelkopf.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class DatabaseSeeder(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        scope.launch {
            seedData(db)
        }
    }

    private suspend fun seedData(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()
        seedErrorCauses(db)
        seedSkills(db)
        seedAchievements(db)
        seedTemplates(db, now)
        seedKnowledgeBase(db, now)
    }

    private fun insertKnowledge(
        db: SupportSQLiteDatabase, title: String, content: String,
        tags: String, category: String, now: Long
    ) {
        db.execSQL(
            """INSERT OR IGNORE INTO knowledge_base (id, title, content, tags, category, isFavorite, sourceTaskId, createdAt, updatedAt)
               VALUES ('${uuid()}','${esc(title)}','${esc(content)}','${esc(tags)}','${esc(category)}',1,NULL,$now,$now)"""
        )
    }

    private fun insertTemplate(
        db: SupportSQLiteDatabase, name: String, category: String,
        steps: List<String>, now: Long
    ) {
        val stepsStr = steps.joinToString("\n---\n")
        db.execSQL(
            """INSERT OR IGNORE INTO workflow_templates (id, name, category, stepsJson, isBuiltIn, usageCount, createdAt)
               VALUES ('${uuid()}','${esc(name)}','${esc(category)}','${esc(stepsStr)}',1,0,$now)"""
        )
    }

    private fun insertCause(db: SupportSQLiteDatabase, label: String, description: String) {
        db.execSQL(
            """INSERT OR IGNORE INTO error_causes (id, label, description, category)
               VALUES ('${uuid()}','${esc(label)}','${esc(description)}','')"""
        )
    }

    private fun esc(s: String) = s.replace("'", "''")

    private suspend fun seedTemplates(db: SupportSQLiteDatabase, now: Long) {
        insertTemplate(db, "Unterverteilung verdrahten", Constants.Categories.UV,
            Constants.BuiltInTemplates.uvVerdrahten, now)
        insertTemplate(db, "RCD/FI-Schalter prüfen", Constants.Categories.RCD,
            Constants.BuiltInTemplates.rcdPruefen, now)
        insertTemplate(db, "UV nach VDE 0603 planen", Constants.Categories.UV, listOf(
            "Platzkapazität ermitteln (Anzahl Stromkreise + 30% Reserve)",
            "Feldaufteilung festlegen (Zähler, Vorsicherung, Verteiler, Steuerung)",
            "Sammelschienensystem auswählen (3-Phasen oder Einzel)",
            "Reihenklemmen für PE und N vorsehen",
            "RCD/LS-Kombination planen (je nach Stromkreis)",
            "Überspannungsschutz Typ 2 (SPD) vorsehen",
            "Kabeleinführungen und Leerrohre einplanen",
            "Beschriftungssystem vorbereiten",
            "Platz für Smart-Meter und Steuerung freihalten",
            "Prüfung und Dokumentation einplanen"
        ), now)
        insertTemplate(db, "Wechselschaltung installieren", Constants.Categories.SCHALTER, listOf(
            "Spannungsfreiheit prüfen",
            "Kabel und Querschnitt wählen (NYM 3x1,5mm²)",
            "Schalterdosen setzen (Einbauhöhe 105cm)",
            "Leitung verlegen und kennzeichnen",
            "Ersten Wechselschalter anschließen (L auf Klemme L, korrespondierende Ader auf 1/2)",
            "Zweiten Wechselschalter anschließen (korrespondierende Ader auf 1/2, geschalteter L auf Klemme L)",
            "Leuchte anschließen (N und geschalteter L)",
            "Spannung prüfen und Funktion testen",
            "Dokumentieren"
        ), now)
        insertTemplate(db, "Kreuzschaltung installieren", Constants.Categories.SCHALTER, listOf(
            "Spannungsfreiheit prüfen",
            "Kabel wählen (NYM 5x1,5mm² für 2 Wechsel + 1 Kreuz)",
            "Wechselschalter an den Enden, Kreuzschalter in der Mitte positionieren",
            "Korrespondierende Adern (2 Adern zwischen den Schaltern) durchschleifen",
            "Kreuzschalter anschließen (Eingang 1/2, Ausgang 3/4)",
            "Leuchte anschließen",
            "Durchgang prüfen (alle Schalterstellungen testen)",
            "Spannung aufschalten und Funktion prüfen",
            "Dokumentieren"
        ), now)
        insertTemplate(db, "Tasterschaltung mit Stromstoßrelais", Constants.Categories.SCHALTER, listOf(
            "Spannungsfreiheit prüfen",
            "Stromstoßrelais im Verteilerschrank vorsehen (Hutschiene)",
            "Tasterleitungen (NYM 3x1,5mm²) verlegen",
            "Taster parallel auf A1/A2 des Relais schalten",
            "Arbeitsstromkreis vorbereiten",
            "Leuchte über Schließerkontakt des Relais anschließen",
            "Spannung prüfen",
            "Funktion prüfen (jeder Taster schaltet Ein/Aus)",
            "Dokumentieren"
        ), now)
        insertTemplate(db, "Isolationsmessung nach VDE 0100-600", Constants.Categories.MESSUNG, listOf(
            "Anlage freischalten und Spannungsfreiheit allpolig prüfen",
            "Alle Verbraucher trennen (Gerätestecker, Leuchtmittel)",
            "Messgerät auf 500V DC einstellen",
            "Messung L gegen PE (Grenzwert ≥ 1 MΩ)",
            "Messung N gegen PE",
            "Messung L gegen N",
            "Messung zwischen Außenleitern (L1-L2, L2-L3, L1-L3)",
            "Ergebnisse notieren und mit Grenzwert vergleichen",
            "Prüfprotokoll ausfüllen",
            "Anlage wieder in Betrieb nehmen"
        ), now)
        insertTemplate(db, "Anlage nach VDE 0100 prüfen (Erstprüfung)", Constants.Categories.MESSUNG, listOf(
            "Besichtigung: Schutzmaßnahmen, Brandabschnitte, Mindestabstände prüfen",
            "Durchgangsprüfung: PE und PA (Potentialausgleich)",
            "Isolationswiderstand messen (≥ 1 MΩ bei 500V)",
            "Schleifenimpedanz messen (L-PE, L-N)",
            "RCD-Prüfung: Auslösestrom und Auslösezeit",
            "Spannungsfall berechnen / messen",
            "Funktionsprüfung: Schalten, Trennen, Kennzeichnung",
            "Prüfprotokoll erstellen und unterschreiben",
            "Anlage übergeben und einweisen"
        ), now)
        insertTemplate(db, "SAT-/DASM-Anlage installieren", Constants.Categories.SONSTIGES, listOf(
            "Standort für Antenne wählen (freie Sicht nach Süden, Befestigung prüfen)",
            "Multischalter / Verteiler positionieren (wettergeschützt)",
            "Koaxialkabel verlegen (Dämpfung beachten, Biegeradius einhalten)",
            "F-Stecker konfektionieren (korrekt absetzen, Schirmung)",
            "Dosen anklemmen (Durchgangsverdrahtung beachten)",
            "Antenne ausrichten (Pegel und Qualität optimieren)",
            "Pegelmessung durchführen (Frequenzbereich, Kanal)",
            "Receiver-Suchlauf durchführen",
            "Dokumentation (Pegelprotokoll)"
        ), now)
        insertTemplate(db, "Netzwerkdose (RJ45) anschließen", Constants.Categories.NETZWERK, listOf(
            "Kabeltyp wählen (Cat6 SF/UTP für feste Installation)",
            "Kabel abmanteln (Mantel nicht zu weit entfernen, Schirmung erhalten)",
            "Adernpaare entdrillen (max. 13mm), Schirmfolie zurückschneiden",
            "Belegung nach T568A oder T568B festlegen (durchziehen)",
            "Einzeladern in Schneidklemme (LSA+) legen und durchdrücken",
            "Zugentlastung schließen",
            "Patchfeld belegen (gleiche Belegung)",
            "Durchgang prüfen (Kabeltester, alle 8 Adern)",
            "Beschriften (Dose und Patchfeld)"
        ), now)
        insertTemplate(db, "EASY-Kleinststeuerung programmieren und verdrahten", Constants.Categories.STEUERUNG, listOf(
            "Anforderungen aufschreiben (Eingänge/Ausgänge definieren)",
            "EASY-Spannungsversorgung anschließen (24V DC/AC)",
            "Sensoren (Taster, Schalter) an Eingänge anschließen",
            "Aktoren (Schütze, Lampen) an Ausgänge anschließen",
            "Grundprogramm erstellen (Kontaktplan / FBD)",
            "Selbsthaltung programmieren",
            "Verriegelungen und Sicherheitsbedingungen einbauen",
            "Programm testen (simulieren, Einzelabschritt)",
            "Beschriften und Dokumentieren"
        ), now)
        insertTemplate(db, "Elektroherd / Kochfeld anschließen", Constants.Categories.VERDRAHTUNG, listOf(
            "Spannungsfreiheit prüfen",
            "Herdanschlussdose prüfen (5-adrig vorhanden?)",
            "Kabelquerschnitt wählen (mind. 2,5mm², bei 11kW+ ≥ 4mm²)",
            "Adern abmanteln und abisolieren",
            "Anschließen: PE (grün-gelb), N (blau), L1 (braun), L2 (schwarz), L3 (grau)",
            "Brücken je nach Netzform (400V oder 230V) setzen",
            "Zugentlastung anbringen",
            "Spannung prüfen und Funktion testen (alle Kochzonen, Backofen)",
            "Dokumentieren: Herdanschluss mit Datum"
        ), now)
        insertTemplate(db, "Durchlauferhitzer installieren", Constants.Categories.VERDRAHTUNG, listOf(
            "Leistung und Anschlussart prüfen (400V Drehstrom oder 230V)",
            "Leitungsquerschnitt berechnen (bei 21kW: 4mm², bei 27kW: 6mm²)",
            "Absicherung prüfen (3-polig, entsprechend Leistung)",
            "Wasser abstellen und Leitungen ablassen",
            "Gerät montieren (Wandhalterung, hydraulische Anschlüsse)",
            "Elektrischer Anschluss (Klemmen laut Plan) ",
            "Wasserleitungen auf Dichtheit prüfen",
            "Gerät in Betrieb nehmen (entlüften, Temperatur einstellen)",
            "Leistungseinstellung anpassen (je nach Netzform)"
        ), now)
        insertTemplate(db, "Brandmelder (Rauchwarnmelder) installieren", Constants.Categories.SICHERHEIT, listOf(
            "Anzahl und Position nach Landesbauordnung ermitteln",
            "Montageort: Flure, Schlafzimmer, Kinderzimmer (Deckenmitte)",
            "Abstand zu Wänden und Hindernissen ≥ 50cm einhalten",
            "Montageplatte an Decke schrauben (ggf. dübeln)",
            "Melder auf Montageplatte drehen (Rastung hörbar)",
            "Vernetzung: Funkmelder oder 3-adrige Steuerleitung",
            "Inbetriebnahme: Testknopf drücken",
            "Dokumentation: Melder-Typ, Position, Datum",
            "Wartung: jährliche Prüfung, alle 10 Jahre ersetzen"
        ), now)
        insertTemplate(db, "Kabelverlegung (Unterputz / Aufputz)", Constants.Categories.VERDRAHTUNG, listOf(
            "Verlegeweg einzeichnen (nur waagerecht und senkrecht)",
            "Fräsen / Schlitzen nach VDE: Tiefe und Breite einhalten",
            "Leerrohr / Kanal einlegen (Biegeradius beachten)",
            "Kabel einziehen mit Zugentlastung",
            "Befestigungsabstand einhalten (alle 40cm waagerecht)",
            "Abstand zu Wasser- und Gasleitungen ≥ 10cm",
            "Brandschutz: Durchführung abschotten",
            "Nach Putzarbeiten Durchgang prüfen",
            "Kennzeichnen und Dokumentieren"
        ), now)
    }

    private suspend fun seedKnowledgeBase(db: SupportSQLiteDatabase, now: Long) {
        insertKnowledge(db, "Die 5 Sicherheitsregeln",
            "1. Freischalten\n2. Gegen Wiedereinschalten sichern\n3. Spannungsfreiheit feststellen (allpolig)\n4. Erden und Kurzschließen\n5. Benachbarte unter Spannung stehende Teile abdecken oder abschranken\n\nDiese Reihenfolge ist zwingend einzuhalten!",
            "Sicherheit, 5 Sicherheitsregeln, VDE, Freischalten", Constants.Categories.SICHERHEIT, now)
        insertKnowledge(db, "PSA für Elektriker",
            "Persönliche Schutzausrüstung:\n• Schutzhelm mit Stirnlampenhalter\n• Schutzbrille (bei Staub, Fräsen, Bohren)\n• Isolierte Handschuhe (bis 1000V) bei Arbeiten an aktiven Teilen\n• Sicherheitsschuhe S3 mit Stahlkappe und Durchtrittschutz\n• Arbeitskleidung aus schwer entflammbarem Material (EN 61482)\n• Gehörschutz bei Lärm > 85 dB",
            "PSA, Arbeitssicherheit, Schutzausrüstung, Helm, Handschuhe", Constants.Categories.SICHERHEIT, now)
        insertKnowledge(db, "Erste Hilfe bei Stromunfällen",
            "1. Stromkreis unterbrechen (Sicherung aus, Hauptschalter)\n2. Eigene Sicherheit beachten (isolierte Kleidung, trockener Stand)\n3. Retten: Verunglückten mit nichtleitendem Gegenstand (Besenstiel) von der Spannungsquelle trennen\n4. Notruf 112 wählen\n5. Bewusstlosigkeit: Stabile Seitenlage\n6. Keine Atmung: Wiederbelebung (30x drücken, 2x beatmen)\n7. Verbrennungen: Kühlen (max. 10 Min), steril abdecken",
            "Erste Hilfe, Notfall, Stromunfall, Wiederbelebung", Constants.Categories.SICHERHEIT, now)
        insertKnowledge(db, "Inbetriebnahme elektrischer Anlagen",
            "Ablauf vor Inbetriebnahme:\n1. Sichtprüfung: Schutzmaßnahmen, Kennzeichnung, Mindestabstände, Beschädigungen\n2. Messung: Durchgang PE/PA, Isolationswiderstand, Schleifenimpedanz\n3. RCD-Prüfung: Auslösestrom und Auslösezeit\n4. Funktionsprüfung: Schalten, Trennen, Einstellungen\n5. Dokumentation: Prüfprotokoll, Stromlaufplan, Einstellwerte\n→ Erst nach bestandener Prüfung darf die Anlage in Betrieb genommen werden",
            "Inbetriebnahme, Prüfung, VDE 0100, Erstprüfung", Constants.Categories.SICHERHEIT, now)
        insertKnowledge(db, "Brand in elektrischen Anlagen",
            "Verhalten bei Elektrobrand:\n• Nie mit Wasser löschen (Stromschlag-Gefahr!)\n• Geeignete Löschmittel: CO2-Löscher (Kohlendioxid) oder Pulverlöscher (Brandklasse B/C)\n• Vor Löschen: Anlage freischalten (wenn möglich)   \n• Abstand zum Brandherd ≥ 1m (bei CO2)   \n• Nach Brand: Anlage von Elektrofachkraft prüfen lassen, bevor sie wieder eingeschaltet wird\n• Brandschutzklappen und Kabelabschottungen regelmäßig prüfen",
            "Brand, Löschen, CO2, Brandschutz, Sicherheit", Constants.Categories.SICHERHEIT, now)
        insertKnowledge(db, "Isolationswiderstand Grenzwerte",
            "Grenzwerte nach VDE 0100-600:\n• 230V-Anlagen: ≥ 1,0 MΩ (gemessen mit 500V DC)\n• 400V-Anlagen: ≥ 1,0 MΩ (gemessen mit 1000V DC)\n• SELV/PELV: ≥ 250kΩ (gemessen mit 250V DC)\n\nMessung zwischen:\n• L gegen PE\n• N gegen PE\n• L gegen N\n• L1-L2, L2-L3, L1-L3\n\nWichtig: Verbraucher vor Messung trennen!",
            "Isolationswiderstand, Messung, Grenzwerte, VDE 0100", Constants.Categories.MESSUNG, now)
        insertKnowledge(db, "Schleifenimpedanz Grenzwerte (erweitert)",
            "Maximale Schleifenimpedanz Zs für TN-System (230V):\n• 6A LS: ≤ 7,67 Ω\n• 10A LS: ≤ 4,60 Ω\n• 13A LS: ≤ 3,54 Ω\n• 16A LS: ≤ 2,87 Ω\n• 20A LS: ≤ 2,19 Ω\n• 25A LS: ≤ 1,75 Ω\n• 32A LS: ≤ 1,37 Ω\n• 40A LS: ≤ 1,10 Ω\n• 50A LS: ≤ 0,88 Ω\n• 63A LS: ≤ 0,69 Ω\n\nFormel: Zs ≤ (Uo x 0,95) / (Ia x 1,5)",
            "Schleifenimpedanz, Messung, Grenzwerte, LS, Formel", Constants.Categories.MESSUNG, now)
        insertKnowledge(db, "RCD-Prüfung detailliert",
            "RCD-Typen:\n• AC: Wechselfehlerströme (ältere Bauart)\n• A: Wechsel- + pulsierende Gleichfehlerströme (Standard)\n• F: Wie A + hochfrequente Fehlerströme (für Wärmepumpen, PV)\n• B: Wie F + glatte Gleichfehlerströme (für E-Autos, USV)\n\nPrüfwerte:\n• IΔN 30mA: Auslösestrom 15-30mA, Auslösezeit ≤ 300ms (≤ 40ms bei selektiv)\n• IΔN 100mA: Auslösestrom 50-100mA, Auslösezeit ≤ 500ms\n• IΔN 300mA: Auslösestrom 150-300mA, Auslösezeit ≤ 200ms (brandschutz)\n\nPrüfung: 1x Funktionstaster, 1x Messung mit Prüfgerät",
            "RCD, FI-Schalter, Prüfung, Auslösestrom, Auslösezeit", Constants.Categories.MESSUNG, now)
        insertKnowledge(db, "Durchgangsprüfung",
            "Durchgangsprüfung nach VDE 0100-600:\n• Wann: Bei Erstprüfung und nach Änderungen\n• Prüfstrom: ≥ 200mA (um Korrosion zu durchbrechen)\n• Leerlaufspannung: 4-24V (Schutz Kleinspannung)\n• Grenzwert: ≤ 0,3 Ω für PE/PA-Verbindungen\n\nGeprüft werden:\n• PE-Leiter aller Steckdosen und Geräte\n• Potentialausgleichsleiter\n• Schutzleiter in Kabeln\n• Erdungsanlagen\n\nAchtung: Vor Messung Anlage freischalten!",
            "Durchgang, PE, PA, Messung, VDE 0100", Constants.Categories.MESSUNG, now)
        insertKnowledge(db, "Prüffristen nach DGUV V3",
            "Wiederholungsprüfungen elektrischer Anlagen und Betriebsmittel:\n• Ortsfeste Anlagen: alle 4 Jahre\n• Ortsveränderliche Geräte: alle 12 Monate (Baustelle: alle 6 Monate)\n• Mess- und Prüfgeräte: alle 12 Monate (bzw. nach Herstellerangabe)\n• Fi-Schalter (RCD): halbjährlich Funktionstaster (durch Nutzer)\n• fi-Schalter (RCD): jährlich fachgerechte Prüfung\n\nDokumentation: Prüfprotokoll mit Datum, Prüfer, Ergebnis",
            "DGUV V3, Prüffrist, Wiederholungsprüfung, Betriebsmittel", Constants.Categories.SICHERHEIT, now)
        insertKnowledge(db, "Spannungsfall berechnen",
            "Maximaler Spannungsfall nach VDE 0100-520:\n• Allgemein: ≤ 3% vom Hausanschluss bis zur Endstromkreis-Abzweigdose\n• Insgesamt (incl. Hausanschluss): ≤ 5%\n\nFormel: ΔU = (2 x L x I x cos φ) / (κ x A)\n• L = Leitungslänge in m\n• I = Strom in A\n• cos φ = Leistungsfaktor (0,9 für allgemeine Anlagen)\n• κ = Leitfähigkeit (56 für Kupfer, 36 für Aluminium)\n• A = Querschnitt in mm²\n\nFaustformel für 16A, 230V, Kupfer:\n• 1,5mm² → max. ca. 20m\n• 2,5mm² → max. ca. 33m",
            "Spannungsfall, Formel, Berechnung, VDE 0100, Leitungslänge", Constants.Categories.MESSUNG, now)
        insertKnowledge(db, "Kabeltypen und Verwendung",
            "Gängige Kabeltypen im Elektroinstallationsbereich:\n• NYM 3/5x1,5/2,5: Standard-Installationskabel (Innen, trocken/feucht)\n• NYM-O: ohne Mantel (nur in Kabelkanälen)\n• NYY: Erdkabel (direkte Erdverlegung)\n• H05VV-F: Gummischlauchleitung (Geräte, Verlängerungen)\n• H07RN-F: Gummischlauchleitung (Baustelle, schwer)\n• Cat6/7: Netzwerkkabel (Datenverkabelung)\n• Koax: SAT-/Antennenkabel\n• J-Y(St)Y: Telefon-/BUS-Leitung\n• LYY: Steuerleitung (EASY, SPS)\n\nWichtig: Verwendungszweck und Verlegeart beachten!",
            "Kabeltypen, NYM, NYY, Leitung, Querschnitt", Constants.Categories.VERDRAHTUNG, now)
        insertKnowledge(db, "Aderfarben – neu und alt",
            "Aktuelle Farbcodierung (VDE 0293-308):\n• PE: grün-gelb (immer)\n• N: blau (früher grau)\n• L1: braun (früher schwarz)\n• L2: schwarz (früher braun)\n• L3: grau (früher schwarz)\n\nAlte Farben (vor 2003):\n• N: grau\n• L1: schwarz\n• L2: braun\n• L3: schwarz\n\nGemischte Alt-/Neuanlagen: Besondere Vorsicht! Adern kennzeichnen.",
            "Farbcodierung, Aderfarben, L1, L2, L3, PE, N, VDE", Constants.Categories.VERDRAHTUNG, now)
        insertKnowledge(db, "LS-Charakteristik B/C/D",
            "Leitungsschutzschalter Charakteristiken:\n\nTyp B (allgemeine Installation):\n• Auslösebereich: 3-5x In\n• Verwendung: Steckdosen, Licht, Geräte ohne Einschaltstrom\n\nTyp C (Motorische Lasten):\n• Auslösebereich: 5-10x In\n• Verwendung: Leuchtstofflampen, kleine Motoren, Transformatoren\n\nTyp D (Hohe Einschaltströme):\n• Auslösebereich: 10-20x In\n• Verwendung: Schweißgeräte, große Motoren, USV-Anlagen\n\nFaustregel: Bei Unsicherheit Typ C wählen (deckt 90% der Fälle ab)",
            "LS-Automat, B-Charakteristik, C-Charakteristik, D-Charakteristik, Auslösebereich", Constants.Categories.VERDRAHTUNG, now)
        insertKnowledge(db, "Verbindungstechnik (Wago, Klemmen)",
            "Gängige Verbindungstechniken:\n• Wago 221 (Hebelklemme): 0,2-4mm², mehrfach nutzbar, für alle Leiterarten\n• Wago 2273 (Steckklemme): 0,5-2,5mm², eindrähtig, günstig\n• Wago 222 (Hebelklemme): 0,08-4mm², flexibel, für Prüfzwecke\n• Durchgangsklemme (Reihenklemme): für Hutschiene, 0,5-6mm²\n• Schraubklemme: klassisch, für starre/flexible Leiter, Drehmoment beachten\n• Aderendhülsen: bei flexiblen Leitern in Schraub-/Zugfederklemmen zwingend!\n\nTipp: Wago 221 ist der Allrounder für fast alle Situationen.",
            "Wago, Klemmen, Verbindungstechnik, Aderendhülse", Constants.Categories.VERDRAHTUNG, now)
        insertKnowledge(db, "Schaltungsarten Übersicht",
            "Die wichtigsten Schaltungsarten:\n\n1. Ausschaltung: Ein Schalter schaltet eine Leuchte Ein/Aus\n2. Wechselschaltung: Zwei Schalter schalten eine Leuchte (Treppenhaus)\n3. Kreuzschaltung: Drei oder mehr Schalter schalten eine Leuchte (langer Flur)\n4. Serienschaltung: Ein Schalter schaltet zwei Leuchtengruppen (Bad)\n5. Tasterschaltung: Taster steuern Stromstoßrelais (große Räume, viele Schalter)\n\nLeitungsbedarf:\n• Ausschaltung: NYM 2x1,5 + PE\n• Wechselschaltung: NYM 3x1,5 + PE\n• Kreuzschaltung: NYM 5x1,5 + PE\n• Tasterschaltung: NYM 3x1,5 + PE (pro Taster)",
            "Schaltungen, Ausschaltung, Wechselschaltung, Kreuzschaltung, Serienschaltung", Constants.Categories.SCHALTER, now)
        insertKnowledge(db, "Tasterschaltung mit Stromstoßrelais",
            "Stromstoßrelais (auch Treppenlicht-Zeitschalter):\n• Relais wird durch Tasterimpuls geschaltet (Schließt und öffnet wechselnd)\n• Anschluss: A1/A2 sind Spulenanschlüsse (Taster parallel auf A1/A2)\n• 13/14 (oder 1/2) sind Arbeitskontakte für die Leuchte\n\nWichtige Punkte:\n• Relais auf Hutschiene im Verteiler\n• Taster parallel (wie viele man möchte)\n• Jeder kurze Tastendruck schaltet den Zustand um\n• Taster haben Öffnerkontakt (Arbeitsstromprinzip)\n• Achtung: Taster nicht mit Schaltern verwechseln (Taster federn zurück!)",
            "Stromstoßrelais, Taster, Treppenlicht, Schaltung", Constants.Categories.SCHALTER, now)
        insertKnowledge(db, "Stromlaufplan lesen",
            "Stromlaufplan – Grundsymbole:\n• Schütze/Relais: Spule als Rechteck, Kontakte als Schalter (Öffner/Schließer)\n• Schließer (NO): normal offen, schließt bei Ansteuerung\n• Öffner (NC): normal geschlossen, öffnet bei Ansteuerung   \n• Wechsler: ein gemeinsamer Kontakt, der umschaltet\n• Taster: Schließer (Öffner mit Federrückstellung)\n• Leuchtmittel: Kreis mit Kreuz\n• PE: Erde-Symbol (drei Striche)\n\nLeserichtung: Strompfade von links (Potential) nach rechts (Verbraucher)",
            "Stromlaufplan, Schaltplan, Symbol, Schließer, Öffner", Constants.Categories.SONSTIGES, now)
        insertKnowledge(db, "Einbauhöhen nach Norm",
            "Empfohlene Einbauhöhen (Oberkante Unterkante Fertigfußboden OKFF):\n• Lichtschalter: 105 cm\n• Steckdosen allgemein: 30 cm (Wohnen), 105 cm (Küche über Arbeitsplatte)\n• Steckdosen Bad: 30 cm (Abstand zu Dusche/Badewanne ≥ 60cm)   \n• TV/Telefon/Netzwerk: 30 cm oder 105 cm je nach Raum\n• Herdanschlussdose: 40-60 cm (hinter Herd)\n• FI-Schalter im Zählerschrank: Mitte 140-170 cm\n• Briefkasten: 70-170 cm (je nach Post- Vorgabe)\n• Außensteckdose: 30-50 cm (über Gelände)\n\nNach DIN 18015 (Planungsgrundlagen für Elektroanlagen in Wohnungen)",
            "Einbauhöhe, Norm, DIN 18015, Schalter, Steckdose, OKFF", Constants.Categories.INSTALLATION, now)
        insertKnowledge(db, "Schutzbereiche Badezimmer (VDE 0100-701)",
            "Schutzbereiche im Bad nach VDE 0100-701:\n\nBereich 0: Innenbereich der Badewanne/Dusche\n• Nur SELV (12V), IPX7\n• Keine Steckdosen, keine Schalter\n\nBereich 1: Oberhalb der Wanne/Dusche bis 2,25m Höhe\n• Nur SELV (12V) oder Schutztrennung\n• Nur fest angeschlossene Geräte (Durchlauferhitzer, Lüfter)\n\nBereich 2: 60cm um Bereich 1, bis 2,25m Höhe\n• Schutz durch Abstand oder Trennung\n• Steckdosen ≥ 60cm von Wanne/Dusche\n\nAußerhalb der Bereiche:\n• Normale Installation erlaubt\n• RCD 30mA für alle Stromkreise im Bad zwingend!",
            "Badezimmer, VDE 0100-701, Schutzbereiche, IPX7", Constants.Categories.SICHERHEIT, now)
        insertKnowledge(db, "Leitungslängen und Spannungsfall",
            "Maximale Leitungslängen bei 230V, 3% Spannungsfall, Kupfer:\n\n1,5mm²:\n• 10A: ca. 45m\n• 16A: ca. 28m\n\n2,5mm²:\n• 16A: ca. 47m\n• 20A: ca. 37m\n\n4mm²:\n• 25A: ca. 40m\n• 32A: ca. 31m\n\n6mm²:\n• 40A: ca. 33m\n• 50A: ca. 26m\n\nFormel: Lmax = (κ x A x ΔU) / (2 x I x cos φ)\nκ=56 (Kupfer), ΔU=6,9V (3% von 230V)",
            "Leitungslänge, Spannungsfall, Querschnitt, Formel", Constants.Categories.MESSUNG, now)
        insertKnowledge(db, "Verlegearten nach VDE 0298",
            "Verlegearten (A1 bis F) und ihre Strombelastbarkeit:\n\n• A1: Wärmeisolierte Wand, Kabel direkt eingeputzt\n• A2: Wärmeisolierte Wand, Kabel in Leerrohr eingeputzt\n• B1: In Holz-/Metallwand, auf Putz oder in Kabelkanal\n• B2: Wie B1, aber in Leerrohr\n• C: Direkt an der Wand / Decke (Kabeltrasse)\n• E: Im Boden (Kabelkanal) frei\n• F: In Luft (Kabelpritsche), freie Belüftung\n\nStrombelastbarkeit: A1 am schlechtesten (Kabel eingeputzt isoliert), F am besten (freie Luft)\n→ Bei Verlegeart A1 nur ca. 60% der Werte von Verlegeart C!",
            "Verlegeart, VDE 0298, Strombelastbarkeit, Kabelverlegung", Constants.Categories.VERDRAHTUNG, now)
        insertKnowledge(db, "Brandschutz bei Elektroinstallation",
            "Brandschutz bei Kabeldurchführungen:\n• Kabelabschottung: Bei Brandabschnitten (Wände/Decken) zwingend\n• Zugelassene Systeme: Putz, Mörtel, Brandschutzschaum, Kissen\n• Einzelleitungen: Vergussmasse oder Brandschutzmanschette\n• Abstand brennbarer Materialien: ≥ 10cm zu Kabeln\n• Brandlast: Kabelbündel minimieren (max. 5% Füllgrad bei Kabeltrassen)\n• Notwendige Brandschutzklappen vor elektrischer Auslösung schützen\n\nPrüfung: Alle 2 Jahre durch Brandschutzbeauftragten",
            "Brandschutz, Abschottung, Brandlast, Kabeldurchführung", Constants.Categories.SICHERHEIT, now)
        insertKnowledge(db, "Überspannungsschutz (SPD) Typ 1/2/3",
            "Überspannungsschutzgeräte:\n\nTyp 1 (Blitzschutz):\n• Ableitstoßstrom ≥ 12,5kA (10/350 μs)\n• Primärschutz am Gebäudeeintritt\n• Bei Blitzschutzsystem oder Freileitung zwingend\n\nTyp 2 (Hauptverteilung):\n• Ableitstoßstrom ≥ 15kA (8/20 μs)\n• Sekundärschutz in Unterverteilung\n• Seit 2018 bei Neuanlagen nach TAB (VDE-AR-N 4100) gefordert\n\nTyp 3 (Geräteschutz):\n• Ableitstoßstrom ≥ 3kA\n• Tertiärschutz direkt am Endgerät (Steckdosenleiste)\n• Feinschutz für empfindliche Geräte (PC, TV)\n\nKoordination: Typ 1→Typ 2→Typ 3, Abstand ≥ 10m Leitung oder Entkopplung",
            "Überspannungsschutz, SPD, Blitzschutz, Typ 1, Typ 2, Typ 3", Constants.Categories.VERDRAHTUNG, now)
        insertKnowledge(db, "Zählerplatz nach TAB 2019",
            "Zähleranlagen nach TAB 2019 & VDE-AR-N 4100:\n• Zählerschrank: min. 700mm breit (3-Punkt-Befestigung)\n• Vorsicherung: NH-Sicherung 63A-100A (je nach Netzbetreiber)\n• Zählerplatz: 250mm breit, für Smart Meter vorbereitet\n• APZ-Feld: Allgemeiner Platz für Zusatzgeräte (Steuerung, Kommunikation)\n• Verteilerfeld: min. 12 TE, für RCD, LS, Überspannungsschutz\n• Netzbetreiberauflagen vor Planung einholen!\n• Nach VDE-AR-N 4100 ist Typ-2-SPD ab 2018 Pflicht",
            "TAB, Zählerplatz, VDE-AR-N 4100, Zählerschrank", Constants.Categories.ZAEHLER, now)
        insertKnowledge(db, "Potentialausgleich (Haupt-, Schutz-, Erdung)",
            "Potentialausgleich nach VDE 0100-540:\n\nHauptpotentialausgleich:\n• Verbindet: Erdung, PE, Wasser, Gas, Heizung, Blitzschutz\n• Querschnitt: ≥ 10mm² Cu (oder 16mm² Al)\n• Anschluss: Hauptpotentialausgleichsschiene (HPA) im Keller/Hausanschlussraum\n\nSchutzpotentialausgleich:\n• In Feuchträumen (Bad, Dusche) alle leitfähigen Teile verbinden\n• Querschnitt: ≥ 4mm² Cu\n• Bad: Wanne, Dusche, Heizkörper, Wasserleitungen\n\nErdung:\n• Tiefenerder: 9m (3x3m) oder Ringerdung\n• Fundamenterder: im Beton der Bodenplatte, ≥ 10mm² rund\n• Erdungswiderstand: ≤ 10Ω (bei Blitzschutz ≤ 2Ω)",
            "Potentialausgleich, Erdung, HPA, VDE 0100", Constants.Categories.VERDRAHTUNG, now)
        insertKnowledge(db, "Kabelabmantelung und Abisolierlängen",
            "Praktische Hinweise zur Kabelbearbeitung:\n• NYM-Mantel: 15-20cm abmanteln (Kabelmesser längs, nicht quer!)\n• Innenleiter: ca. 10mm abisolieren (für Wago, Schraubklemmen)\n• Aderendhülsen: flexible Leiter bei Schraubklemmen zwingend\n• Hülsenlänge auf Klemme abstimmen (8mm für Wago, 10mm für Schraube)\n• Koax: 5-8mm Innenleiter, Schirmung vollständig bedeckt\n• Cat-Kabel: Mantel max. 13mm entfernen, Drall erhalten\n\nWichtig: Keine Isolierung beschädigen, Kabelmesser parallel zum Leiter führen!",
            "Abisolieren, Kabel, Aderendhülsen, Werkzeug", Constants.Categories.VERDRAHTUNG, now)
        insertKnowledge(db, "Kabelquerschnitt nach Strombelastbarkeit",
            "Richtwerte für Kupferkabel (Verlegeart B1, 30°C Umgebung):\n• 1,5mm²: max. 16A (Licht, Steckdosen)\n• 2,5mm²: max. 22A (Steckdosen, Herd 1-phasig)\n• 4mm²: max. 30A (Herd, Durchlauferhitzer 11kW)\n• 6mm²: max. 37A (Durchlauferhitzer 21kW)\n• 10mm²: max. 50A (Zuleitung, Wallbox 11kW)\n• 16mm²: max. 67A (Hausanschluss, E-Auto 22kW)\n\nReduktionsfaktoren bei:   \n• Mehreren Leitungen gebündelt: -20%   \n• Hoher Umgebungstemperatur: -10% pro 10°C über 30°C\n• Verlegeart A1 (eingeputzt): -40% von B1",
            "Querschnitt, Strombelastbarkeit, Kupfer, Tabelle, VDE 0298", Constants.Categories.VERDRAHTUNG, now)
        insertKnowledge(db, "SAT-/DASM-Anlage Grundlagen",
            "Grundlagen SAT/DASM:\n• Frequenzbereiche: Sat 10,7-12,75 GHz, DVB-T 174-230 MHz\n• Multischalter: Verteilt Signal auf mehrere Teilnehmer (4/8/12 Ausgänge)\n• Koaxialkabel: Je länger, desto mehr Dämpfung (ca. 0,2 dB/m bei 2,4 GHz)\n• Dämpfung bei 20m Kabel: ca. 4dB → ggf. Verstärker nötig\n• F-Stecker korrekt crimpen: Schirmung vollständig, Innenleiter 3mm überstehend\n• Durchgangsdosen: Erste Dose hat geringste Dämpfung, letzte die höchste\n• Pegel: 45-75 dBμV für guten Empfang\n• Ausrichtung: Azimut und Elevation (je nach Standort in Deutschland)",
            "SAT, DASM, Antenne, Kabel, Dämpfung, Multischalter", Constants.Categories.NETZWERK, now)
        insertKnowledge(db, "Netzwerk Grundlagen (T568A/B, Cat-Kategorien)",
            "Netzwerk-Grundlagen für den Elektriker:\n\nT568A vs T568B:\n• T568A: Grün/Weiß - Grün - Orange/Weiß - Blau - Blau/Weiß - Orange - Braun/Weiß - Braun\n• T568B: Orange/Weiß - Orange - Grün/Weiß - Blau - Blau/Weiß - Grün - Braun/Weiß - Braun\n• Wichtig: In einer Anlage konsequent durchziehen (meist T568B)\n\nCat-Kategorien:\n• Cat5e: 1 Gbit/s, 100 MHz (Bestand, nicht mehr neu verlegen)\n• Cat6: 1 Gbit/s, 250 MHz (Standard für Wohnungsbau)\n• Cat6A: 10 Gbit/s, 500 MHz (Empfehlung für Neubau)\n• Cat7: 10 Gbit/s, 600 MHz (FTP, nicht mehr empfohlen)\n• Cat8: 40 Gbit/s, 2000 MHz (Rechenzentren)\n\nLängenlimit: max. 100m (90m feste Verlegung + 10m Patchkabel)",
            "Netzwerk, Cat6, RJ45, T568A, T568B, LSA", Constants.Categories.NETZWERK, now)
        insertKnowledge(db, "EASY-Kleinststeuerung Grundlagen",
            "Eaton/Moeller EASY-Steuerung:\n• EASY 500/700: 8/12 Eingänge, 4/8 Ausgänge, Relais oder Transistor\n• Erweiterung über EASY-Link (max. 8 Geräte koppelbar)\n• Spannungsversorgung: 24V DC oder AC, 230V AC\n\nGrundbefehle (Kontaktplan / FBD):\n• I1-I8: Digitale Eingänge (Taster, Schalter, Sensoren)\n• Q1-Q4: Digitale Ausgänge (Schütze, Lampen, Ventile)\n• M1-M32: Merker (interne Variablen)\n• T1-T8: Zeiten (Ein-/Ausschaltverzögerung, Impuls)\n• C1-C8: Zähler (Vor-/Rückwärts)\n\nStandardübungen: Selbsthaltung, Treppenlicht-Zeitschalter, Stern-Dreieck-Schaltung, Verriegelung von Schützen\n\nProgrammierung: Mit PC-Software (easySoft) oder direkt am Gerät",
            "EASY, Steuerung, Eaton, Moeller, SPS, Programmierung", Constants.Categories.STEUERUNG, now)
        insertKnowledge(db, "Dokumentation und Protokollierung",
            "Pflicht zur Dokumentation nach VDE 0100-600:\n• Prüfprotokoll: Erstellt bei Erstprüfung und nach Änderungen\n• Enthalten muss: Prüfer, Datum, Prüfgerät, Messwerte, Grenzwerte, Ergebnis\n• Stromlaufplan: Bestandteil der Dokumentation, alle Änderungen eintragen\n• Einstellwerte: RCD-Typ, LS-Charakteristik, Schwellwerte notieren\n• Fotos: Vorher-Nachher bei Änderungen, Kabelverlegung dokumentieren" ,
            "Dokumentation, Protokoll, VDE 0100, Prüfprotokoll", Constants.Categories.SONSTIGES, now)
        insertKnowledge(db, "Querschnittsberechnung für Drehstrom",
            "Berechnung von Drehstrom (400V):\n• Formel: P = √3 x U x I x cos φ\n• P = Leistung in Watt, U = 400V, I = Strom je Außenleiter\n• Vereinfacht: P ≈ 692 x I (bei cos φ ≈ 1)\n\nBeispiele:\n• 11kW Herd: I = 11000 / (1,732 x 400 x 0,95) ≈ 16,7A → 5x2,5mm²\n• 22kW Durchlauferhitzer: I = 22000 / (1,732 x 400 x 1) ≈ 31,8A → 5x6mm²\n• 11kW Wallbox: I = 11000 / (1,732 x 400 x 1) ≈ 15,9A → 5x2,5mm²\n\nQuerschnitt für Drehstrom nach VDE 0298 (B1, 30°C):\n• 2,5mm²: max. 22A (max. 15kW)\n• 4mm²: max. 30A (max. 20kW)\n• 6mm²: max. 37A (max. 25kW)\n• 10mm²: max. 50A (max. 34kW)",
            "Drehstrom, 400V, Berechnung, Querschnitt, Leistung", Constants.Categories.MESSUNG, now)
        insertKnowledge(db, "Verteilerschrank Aufbau (VDE 0603)",
            "Aufbau eines Verteiler-/Zählerschranks nach VDE 0603:\n\n• Zählerschrank: 700mm breit, geteilte Bauweise (Zähler + Verteiler)\n• Felder von links nach rechts: Vorsicherung, Zähler, APZ, Verteiler\n• Vorsicherung: NH00-Sicherungslasttrennschalter 63A\n• Zählerfeld: für eHZ (Smart Meter) vorbereitet, 250mm breit\n• APZ (Allgemeiner Platz für Zusatzanwendungen): Steuerung, KNX, Rundsteuerung\n• Verteilerfeld: Hutschiene TH35, 12-16 TE üblich\n• PE-Schiene: für Schutzleiter (min. 10mm² Cu)\n• N-Schiene: für Neutralleiter (min. 10mm² Cu)\n• Reihenklemmen: für Zuleitungen, bis 16mm²\n• Beschriftung: Alle Stromkreise eindeutig kennzeichnen (DIN VDE 0100-510)",
            "Verteilerschrank, Zählerschrank, VDE 0603, Hutschiene, APZ", Constants.Categories.UV, now)
        insertKnowledge(db, "Prüfung von E-Ladesäulen / Wallboxen",
            "Besonderheiten bei der Installation von Wallboxen:\n\nAnschluss: 400V Drehstrom, 5-adrig\n• 11kW Wallbox: 16A → 5x2,5mm²\n• 22kW Wallbox: 32A → 5x6mm²\n\nVorschriften:\n• RCD Typ A oder B (bei Wallbox grundsätzlich)\n• Überspannungsschutz Typ 2 (nach TAB 2023)\n• Energiezähler bei öffentlich zugänglichen Ladepunkten\n• Erdung: Niedriger Erdungswiderstand (< 10Ω)\n\nPrüfung nach Errichtung:\n• Schutzmaßnahmen (Durchgang PE)\n• Isolationswiderstand\n• Schleifenimpedanz (L-PE)\n• RCD-Prüfung (Typ B oder A)\n• Funktion: Laden starten/stoppen",
            "Wallbox, E-Auto, Ladesäule, Installation, Prüfung", Constants.Categories.VERDRAHTUNG, now)
        insertKnowledge(db, "Steckdosenarten im Überblick",
            "Steckdosenarten nach Ländern und Anwendungen:\n\n• Typ F (Schuko): 230V/16A, deutscher Standard\n• Typ E (Französisch): 230V/16A, mit Erdungsstift\n• Typ E+F (Kombi): Hybrid, akzeptiert beide Stecker (Neubau Standard)\n• Typ J (Schweiz): 230V/10A, mit Erdungsbohrung\n• CEE 16A (blaue Camping): 230V/16A, einphasig, 3-polig\n• CEE 32A/63A (rote CEE): 400V, Drehstrom, 5-polig\n• USB-Steckdosen: 230V + USB-C/A Ladebuchse (bis 30W)\n• Steckdoseneinsätze: Unterputz (UP), Aufputz (AP), 45mm Einbauloch",
            "Steckdosen, Schuko, CEE, USB, UP, AP, Camping", Constants.Categories.SCHALTER, now)
    }

    private suspend fun seedErrorCauses(db: SupportSQLiteDatabase) {
        val causes = listOf(
            "Spannung nicht geprüft" to "Vor Arbeitsbeginn Spannungsfreiheit messen",
            "Kabelquerschnitt falsch gewählt" to "Querschnitt an Last und Absicherung anpassen",
            "Adern vertauscht (N/PE/L)" to "Farbcodierung beachten: PE grün-gelb, N blau",
            "Sicherung nicht ausgeschaltet" to "Vor Arbeiten allpolig freischalten",
            "Werkzeug falsch verwendet" to "Geeignetes und geprüftes Werkzeug nutzen",
            "Vorschrift nicht beachtet" to "Aktuelle Normen und TAB einhalten",
            "Messung fehlerhaft durchgeführt" to "Messgerät prüfen und korrekt anschließen",
            "Bauteilbeschriftung übersehen" to "Typenschild und Anschlussplan lesen",
            "Dokumentation nicht gelesen" to "Bestandspläne und Herstellerangaben prüfen",
            "Zeitdruck / Hektik" to "Lieber einmal mehr kontrollieren",
            "Schutzart nicht beachtet" to "IP-Schutzart an Umgebung anpassen",
            "Leitungsschutz falsch dimensioniert" to "Auslösecharakteristik und Last prüfen",
            "Kabel beim Einziehen beschädigt" to "Leerrohr und Kabelziehstrumpf verwenden",
            "Anschlussklemme überdreht" to "Drehmoment nach Herstellerangabe beachten",
            "Zu wenig Reserve im Verteiler" to "30% Reserve für Nachbelegung einplanen",
            "PE-Leiter vergessen" to "Schutzleiter immer mitführen und anklemmen",
            "Neutralleiter nicht markiert" to "N-Leiter farblich kennzeichnen",
            "Schirmung nicht kontaktiert" to "Kabelschirm großflächig auflegen",
            "Aderendhülse fehlt" to "Flexible Leiter in Schraubklemmen mit Hülse versehen",
            "Leitung nicht entdrillt" to "Adern vor dem Anklemmen gerade ziehen",
            "Biegeradius unterschritten" to "Kabel nicht knicken – Mindestradius einhalten",
            "Kabelkanal überfüllt" to "Füllgrad nach VDE 0604 beachten (max. 60%)",
            "Abisolierung beschädigt" to "Isolation sauber absetzen – Litzen nicht verletzen",
            "RCD-Typ falsch gewählt" to "Verwendung prüfen: Typ A für PV/Wallbox, Typ F für Wärmepumpe",
            "Überspannungsschutz vergessen" to "Seit TAB 2019 in Zählerschränken Pflicht",
            "Prüfprotokoll nicht erstellt" to "Dokumentation ist Teil der Inbetriebnahme",
            "Sicherung zu früh eingeschaltet" to "Vor Wiedereinschalten alle Leiter prüfen",
            "Steckdose falsch gepolt" to "Phasenlage prüfen (L links, N rechts)",
            "LS-Schalter ohne Prüfung getauscht" to "Selektivität und Type prüfen",
            "Potentialausgleich nicht angeschlossen" to "HPA im Hausanschlussraum zwingend",
            "Durchgangsprüfung vergessen" to "PE-Durchgang vor Einschalten messen",
            "Leitung mit Heizungsrohr verlegt" to "Abstand ≥ 10cm zu Heizungs-/Wasserleitungen",
            "Kabelschacht nicht abgeschottet" to "Brandschutz bei Durchführung einhalten",
            "Beschriftung fehlt" to "Stromkreise nach VDE 0100-510 kennzeichnen",
            "Messung ohne Verbraucher getrennt" to "Vor Isolationsmessung alle Geräte trennen",
            "Prüfgerät nicht kalibriert" to "Nur mit gültigem Prüfdatum messen",
            "Schalter nicht zugentlastet" to "Kabel in Schalterdose fixieren",
            "Leitung in der Wand nicht befestigt" to "Kabel alle 40cm fixieren",
            "Dose zu tief gesetzt" to "Einbaudosen bündig mit Putz setzen",
            "Kabelmantel nicht abgesetzt" to "Mantel bis 10mm in die Dose führen",
            "Erdungswiderstand zu hoch" to "Fundamenterder oder Tiefenerder prüfen",
            "Blitzschutz nicht einbezogen" to "Potentialausgleich mit Blitzschutz verbinden",
            "Anschlussbild nicht gelesen" to "Herstellerangabe vor Verdrahtung prüfen",
            "Drehfeld nicht geprüft" to "Phasenfolge bei Drehstrom zwingend prüfen",
            "Motor falsch angeschlossen" to "Bei falschem Drehsinn zwei Phasen tauschen",
            "FI löst durch Ableitstrom aus" to "Gesamtableitstrom der Verbraucher prüfen",
            "Kabeltyp falsch gewählt" to "NYM nicht im Freien (UV-beständig?), NYY für Erdreich",
            "Leitungslänge zu groß" to "Spannungsfall >3%? Größeren Querschnitt wählen",
            "Wago-Klemme falsch beschaltet" to "Hebel ganz öffnen, Leiter bis zum Anschlag",
            "Steckdose überlastet" to "Max. 3680W (16A) pro Stromkreis einhalten",
            "Lüsterklemme nicht angezogen" to "Schraubklemmen mit Gefühl – nicht zu fest",
            "Phasenschiene falsch gesetzt" to "Schienen auf APS-Position abstimmen",
            "Erdungsleitung korrodiert" to "Korrosionsschutz an Erdungsanlagen prüfen",
            "Zählerplatz nicht normgerecht" to "TAB und VDE-AR-N 4100 einhalten",
            "Netzform nicht beachtet" to "TN-C, TN-S, TT: Erdungsart prüfen",
            "Kabelziehfett an Klemmen" to "Fett von Adern entfernen vor Klemmen",
            "Reserveleitung nicht gekennzeichnet" to "Unbelegte Adern kennzeichnen und isolieren"
        )
        causes.forEach { (label, desc) -> insertCause(db, label, desc) }
    }

    private suspend fun seedSkills(db: SupportSQLiteDatabase) {
        val skills = listOf(
            Triple("UV-Verdrahtung", Constants.Categories.UV, "Unterverteilungen fachgerecht verdrahten und prüfen"),
            Triple("RCD-Prüfung", Constants.Categories.RCD, "FI-Schalter korrekt prüfen und dokumentieren"),
            Triple("Messung & Prüfung", Constants.Categories.MESSUNG, "Schleifenimpedanz, Isolationswiderstand und Co."),
            Triple("Schalter & Steckdosen", Constants.Categories.SCHALTER, "Installation und Prüfung von Schaltern und Steckdosen"),
            Triple("Sicherheitstechnik", Constants.Categories.SICHERHEIT, "Brandmelder, Alarmanlagen, Sicherheitsbeleuchtung"),
            Triple("Verdrahtung & Anschluss", Constants.Categories.VERDRAHTUNG, "Korrekte Verdrahtung von Geräten und Anlagen"),
            Triple("Fehlersuche & Diagnose", "", "Systematische Fehlersuche in elektrischen Anlagen"),
            Triple("Sicherheit & Vorschriften", "", "Arbeitssicherheit, TAB, DIN VDE Normen"),
            Triple("Beleuchtung", Constants.Categories.BELEUCHTUNG, "Beleuchtungsanlagen und Klimasteuerung"),
            Triple("Zähler & Netz", Constants.Categories.ZAEHLER, "Zähleranlagen und Netzanschluss"),
            Triple("Sonstiges", Constants.Categories.SONSTIGES, "Allgemeine Elektroarbeiten"),
            Triple("Netzwerk", Constants.Categories.NETZWERK, "Datenverkabelung, RJ45, Patchfelder"),
            Triple("Steuerungstechnik", Constants.Categories.STEUERUNG, "EASY, SPS, Schützschaltungen")
        )
        skills.forEach { (name, category, description) ->
            db.execSQL(
                """INSERT OR IGNORE INTO skills (id, name, category, description, iconName, currentXp, level, maxLevel, nextLevelXp)
                   VALUES ('${uuid()}','${esc(name)}','$category','${esc(description)}','',15,1,10,100)"""
            )
        }
    }

    private suspend fun seedAchievements(db: SupportSQLiteDatabase) {
        val achievements = listOf(
            listOf("first_task", "Erste Arbeit", "Erstelle deine erste Arbeit", "", 0),
            listOf("five_tasks", "Fleißig", "Erstelle 5 Arbeiten", "", 0),
            listOf("error_free", "Fehlerfrei", "Schließe eine Arbeit ohne Fehler ab", "", 0),
            listOf("first_error", "Aus Fehlern lernt man", "Dokumentiere deinen ersten Fehler", "", 0),
            listOf("solutions_5", "Lösungsfinder", "Dokumentiere 5 Fehler mit Lösung", "", 1),
            listOf("skill_level_5", "Aufsteiger", "Erreiche Level 5 in einem Skill", "", 1),
            listOf("rating_5", "Perfektionist", "Gib eine 5-Sterne-Bewertung", "", 0),
            listOf("all_categories", "Alleskönner", "Nutze alle Kategorien", "", 1),
            listOf("streak_7", "Woche voll", "7 Tage am Stück gearbeitet", "", 1),
            listOf("streak_30", "Eisern", "30 Tage am Stück gearbeitet", "", 2),
            listOf("ten_projects", "Projektleiter", "Erstelle 10 Projekte", "", 1),
            listOf("skill_max", "Meister", "Erreiche Max-Level in einem Skill", "", 2),
            listOf("hundred_tasks", "100. Arbeit", "100 Arbeiten erstellt", "", 2),
            listOf("all_max_levels", "Alles-Meister", "Alle Skills auf Max-Level", "", 3),
            listOf("master_electrician", "Elektromeister", "Alle Erfolge freigeschaltet", "", 3)
        )
        achievements.forEach { a ->
            val id = a[0] as String
            val name = (a[1] as String).replace("'", "''")
            val desc = (a[2] as String).replace("'", "''")
            val tier = a[4] as Int
            db.execSQL(
                """INSERT OR IGNORE INTO achievements (id, name, description, iconName, tier, isUnlocked, unlockedAt, isBuiltIn)
                   VALUES ('$id','$name','$desc','',$tier,0,NULL,1)"""
            )
        }
    }

    private fun uuid() = UUID.randomUUID().toString()
}
