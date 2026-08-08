package com.docai.kabelkopf.pro

/**
 * Beschreibt die verfügbaren Monetarisierungs-Module.
 * Nur optionale, neue Features landen hier – der Kern (offline) bleibt frei.
 */
enum class ProModule {
    AI_ASSISTANT,     // KI-Assistent, Fehleranalyse, OCR
    CLOUD_SYNC,       // Cloud-Synchronisation, Gerätewechsel
    TEAM,             // gemeinsame Projekte, Azubi↔Meister
    PREMIUM_ANALYTICS // erweiterte Auswertung
}