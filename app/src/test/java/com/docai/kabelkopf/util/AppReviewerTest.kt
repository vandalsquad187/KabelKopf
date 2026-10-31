package com.docai.kabelkopf.util

import android.app.Activity
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Regressionstest für die In-App-Review-Zähl-Logik:
 * genau einmaliges Fragen ab der 5. gespeicherten Arbeit,
 * nie öfter, auch nicht bei weiteren Speicher-Vorgängen.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppReviewerTest {

    private fun prefsOf(activity: Activity) =
        activity.getSharedPreferences("review_prefs", Context.MODE_PRIVATE)

    @Test
    fun asksExactlyOnce_afterFifthSave() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val prefs = prefsOf(activity)

        // Noch nicht fragen: erste 4 Speicher-Vorgänge
        repeat(4) { AppReviewer.recordSavedTask(activity) }
        assertFalse("Vor der 5. Arbeit darf nicht gefragt werden",
            prefs.getBoolean("review_asked", false))
        assertEquals(4, prefs.getInt("saved_task_count", 0))

        // 5. Speichern: Markierung wird gesetzt
        AppReviewer.recordSavedTask(activity)
        assertTrue("Ab der 5. Arbeit muss die Marke gesetzt sein",
            prefs.getBoolean("review_asked", false))

        // Danach nie wieder nerven, egal wie oft weitergespeichert wird
        repeat(10) { AppReviewer.recordSavedTask(activity) }
        assertTrue("Marke muss bleiben", prefs.getBoolean("review_asked", false))
        assertEquals("Zähler friert ein, sobald gefragt wurde",
            5, prefs.getInt("saved_task_count", 0))
    }

    @Test
    fun nullActivity_isIgnored_withoutCrash() {
        AppReviewer.recordSavedTask(null) // darf nichts werfen
    }

    @Test
    fun freshInstall_hasCleanPrefs() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val prefs = prefsOf(activity)
        assertFalse(prefs.getBoolean("review_asked", false))
        assertEquals(0, prefs.getInt("saved_task_count", 0))
    }
}
