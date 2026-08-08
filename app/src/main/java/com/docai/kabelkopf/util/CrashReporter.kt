package com.docai.kabelkopf.util

import android.content.Context
import com.docai.kabelkopf.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dünne Crash-Fang-Schicht. Optional kann hier später Firebase Crashlytics
 * oder Sentry angeschlossen werden. Standard: lokales File-Crash-Log.
 */
interface CrashReporter {
    fun log(throwable: Throwable)
    fun log(message: String)
}

@Singleton
class FileCrashReporter @Inject constructor(
    @ApplicationContext private val context: Context
) : CrashReporter {

    private val crashDir: File
        get() = File(context.filesDir, "crash_logs").apply { mkdirs() }

    override fun log(throwable: Throwable) {
        val text = throwable.stackTraceToString()
        Logger.e("Crash: ${throwable.message}", throwable)
        write(text)
    }

    override fun log(message: String) {
        Logger.e(message)
        write(message)
    }

    private fun write(text: String) {
        try {
            val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY).format(Date())
            File(crashDir, "crash_${System.currentTimeMillis()}.log")
                .writeText("[$stamp] $text")
        } catch (_: Exception) {
            // nothing more we can do
        }
    }
}