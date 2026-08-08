package com.docai.kabelkopf.util

import android.util.Log

object Logger {
    private const val TAG = "KabelKopf"

    fun d(message: String) {
        if (Constants.DEBUG) Log.d(TAG, message)
    }

    fun i(message: String) {
        Log.i(TAG, message)
    }

    fun w(message: String, throwable: Throwable? = null) {
        Log.w(TAG, message, throwable)
    }

    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
}