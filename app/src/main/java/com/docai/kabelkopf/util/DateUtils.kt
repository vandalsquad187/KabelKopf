package com.docai.kabelkopf.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val displayFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY)
    private val dateOnlyFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)

    fun formatDateTime(timestamp: Long): String = displayFormat.format(Date(timestamp))

    fun formatDate(timestamp: Long): String = dateOnlyFormat.format(Date(timestamp))

    fun getRelativeTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / 60000
        val hours = minutes / 60
        val days = hours / 24
        return when {
            minutes < 1 -> "Gerade eben"
            minutes < 60 -> "vor ${minutes}min"
            hours < 24 -> "vor ${hours}h"
            days < 7 -> "vor ${days}Tagen"
            else -> formatDate(timestamp)
        }
    }
}
