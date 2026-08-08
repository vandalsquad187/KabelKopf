package com.docai.kabelkopf.pro

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Platzhalter, bis echter Google Play Billing angebunden wird.
 * Liefert aktuell immer isPro=false. Exakt so wird die Pro-Logik getestet.
 */
@Singleton
class FakePremiumRepository @Inject constructor() : PremiumRepository {

    private val _isPro = MutableStateFlow(false)
    override val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    override fun isEnabled(module: ProModule): Boolean = _isPro.value
}