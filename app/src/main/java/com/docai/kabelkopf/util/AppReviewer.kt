package com.docai.kabelkopf.util

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * Sanfter In-App-Bewertungs-Flow (Play In-App Review API).
 *
 * Wird nach einer positiven Aktion (hier: gespeicherte Arbeit) getriggert
 * und fragt maximal EINMAL pro Installation. Die Anfrage läuft über den
 * Play-Store-Client selbst – die App braucht dafür KEINE Internet-
 * Berechtigung und keine eigenen Server.
 */
object AppReviewer {

    private const val PREFS = "review_prefs"
    private const val KEY_SAVED_COUNT = "saved_task_count"
    private const val KEY_ASKED = "review_asked"

    /** Ab wie vielen gespeicherten Arbeiten gefragt wird. */
    private const val THRESHOLD = 5

    /**
     * Nach erfolgreichem Speichern einer Arbeit aufrufen.
     * [activity] darf null sein (z. B. Prozess-Hintergrund) – dann passiert nichts.
     */
    fun recordSavedTask(activity: Activity?) {
        val act = activity ?: return
        val prefs = act.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_ASKED, false)) return

        val count = prefs.getInt(KEY_SAVED_COUNT, 0) + 1
        prefs.edit().putInt(KEY_SAVED_COUNT, count).apply()
        if (count < THRESHOLD) return

        // Einmal-Markierung sofort setzen – auch bei Fehler nie nerven
        prefs.edit().putBoolean(KEY_ASKED, true).apply()
        requestReview(act)
    }

    private fun requestReview(activity: Activity) {
        try {
            val manager = ReviewManagerFactory.create(activity)
            manager.requestReviewFlow().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    manager.launchReviewFlow(activity, task.result)
                } else {
                    Logger.w("In-App-Review nicht verfügbar", task.exception)
                }
            }
        } catch (t: Throwable) {
            // z. B. Geräte ohne Play Services – still ignorieren
            Logger.w("In-App-Review fehlgeschlagen", t)
        }
    }
}
