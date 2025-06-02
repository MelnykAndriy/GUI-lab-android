package com.msgtrik.msgtrik.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateUtils {
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    fun formatDateForDisplay(apiDate: String): String {
        return try {
            val date = apiDateFormat.parse(apiDate)
            displayDateFormat.format(date!!)
        } catch (e: Exception) {
            apiDate
        }
    }

    fun formatDateForApi(displayDate: String): String {
        return try {
            val date = displayDateFormat.parse(displayDate)
            apiDateFormat.format(date!!)
        } catch (e: Exception) {
            displayDate
        }
    }

    fun formatCalendarToDisplayDate(year: Int, month: Int, day: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        return displayDateFormat.format(calendar.time)
    }

    fun parseDisplayDate(displayDate: String): Triple<Int, Int, Int> {
        return try {
            val date = displayDateFormat.parse(displayDate)
            val calendar = Calendar.getInstance()
            calendar.time = date!!
            Triple(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        } catch (e: Exception) {
            val calendar = Calendar.getInstance()
            Triple(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
} 