package com.docai.kabelkopf.pro

import kotlinx.coroutines.flow.StateFlow

/**
 * Prüft, ob ein optionales Pro-Modul für den Nutzer freigeschaltet ist.
 * Der Kern (offline) ist davon unabhängig und immer frei.
 */
interface PremiumRepository {
    val isPro: StateFlow<Boolean>

    /** true, wenn das optionale Modul verfügbar ist. */
    fun isEnabled(module: ProModule): Boolean
}