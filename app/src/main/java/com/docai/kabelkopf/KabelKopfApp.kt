package com.docai.kabelkopf

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class KabelKopfApp : Application() {

    @Inject
    lateinit var crashReporter: com.docai.kabelkopf.util.FileCrashReporter

    override fun onCreate() {
        super.onCreate()
        installCrashHandler()
    }

    private fun installCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                crashReporter.log(throwable)
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}