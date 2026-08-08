package com.docai.kabelkopf.util

import com.docai.kabelkopf.BuildConfig

object Constants {
    val DEBUG = BuildConfig.DEBUG
    const val DB_NAME = "kabelkopf.db"
    const val PHOTO_DIR = "task_photos"
    const val VOICE_DIR = "voice_notes"
    const val PDF_DIR = "pdf_reports"
    const val WARNING_SEVERITY_THRESHOLD = 3
    const val WARNING_LOOKBACK_DAYS = 30
    const val REPEATED_ERROR_THRESHOLD = 2

    object Categories {
        const val UV = "Unterverteilung"
        const val MESSUNG = "Messung"
        const val SICHERHEIT = "Sicherheit"
        const val VERDRAHTUNG = "Verdrahtung"
        const val RCD = "RCD / FI-Schalter"
        const val SCHALTER = "Schalter / Steckdosen"
        const val BELEUCHTUNG = "Beleuchtung"
        const val ZAEHLER = "Zähleranlage"
        const val SONSTIGES = "Sonstiges"
        const val NETZWERK = "Netzwerk / Datenverkabelung"
        const val STEUERUNG = "Steuerung / Automatisierung"
        const val INSTALLATION = "Installation"

        val all = listOf(UV, MESSUNG, SICHERHEIT, VERDRAHTUNG, RCD, SCHALTER, BELEUCHTUNG, ZAEHLER, SONSTIGES, NETZWERK, STEUERUNG, INSTALLATION)
    }

    object BuiltInTemplates {
        val uvVerdrahten = listOf(
            "Hauptschalter prüfen und abschalten",
            "Spannungsfreiheit messen (allpolig)",
            "RCD/FI-Schalter einbauen und anschließen",
            "LS-Schalter setzen und verdrahten",
            "PE-Schiene montieren und verdrahten",
            "N-Schiene montieren und verdrahten",
            "Phasenschienen (Brücken) einlegen",
            "Leitungen kennzeichnen (Aderendhülsen)",
            "Messung durchführen (RCD, Schleifenimpedanz)",
            "Dokumentation ausfüllen"
        )

        val rcdPruefen = listOf(
            "Prüfgerät vorbereiten",
            "RCD-Taster (Funktion) testen",
            "Auslösestrom messen (IΔN)",
            "Auslösezeit messen (tΔ)",
            "Ergebnis dokumentieren"
        )
    }
}
