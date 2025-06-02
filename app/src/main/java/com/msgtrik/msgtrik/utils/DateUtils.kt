package com.msgtrik.msgtrik.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateUtils {
    private const val TAG = "DateUtils"
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        isLenient = false
    }
    private val displayDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).apply {
        isLenient = false
    }

    fun formatDateForDisplay(apiDate: String): String {
        Log.d(TAG, "formatDateForDisplay - Input date: $apiDate")
        return try {
            val date = apiDateFormat.parse(apiDate)
            val result = displayDateFormat.format(date!!)
            Log.d(TAG, "formatDateForDisplay - Successfully converted to display format: $result")
            result
        } catch (e: Exception) {
            Log.w(TAG, "formatDateForDisplay - Failed to parse API date, checking if already in display format", e)
            try {
                // Verify it's a valid display format by parsing and reformatting
                val date = displayDateFormat.parse(apiDate)
                val result = displayDateFormat.format(date!!)
                Log.d(TAG, "formatDateForDisplay - Already in valid display format: $result")
                result
            } catch (e: Exception) {
                Log.e(TAG, "formatDateForDisplay - Invalid date format", e)
                apiDate
            }
        }
    }

    fun formatDateForApi(displayDate: String): String {
        Log.d(TAG, "formatDateForApi - Input date: $displayDate")
        return try {
            val date = displayDateFormat.parse(displayDate)
            val result = apiDateFormat.format(date!!)
            Log.d(TAG, "formatDateForApi - Successfully converted to API format: $result")
            result
        } catch (e: Exception) {
            Log.w(TAG, "formatDateForApi - Failed to parse display date, checking if already in API format", e)
            try {
                // Verify it's a valid API format by parsing and reformatting
                val date = apiDateFormat.parse(displayDate)
                val result = apiDateFormat.format(date!!)
                Log.d(TAG, "formatDateForApi - Already in valid API format: $result")
                result
            } catch (e: Exception) {
                Log.e(TAG, "formatDateForApi - Invalid date format", e)
                displayDate
            }
        }
    }

    fun formatCalendarToDisplayDate(year: Int, month: Int, day: Int): String {
        Log.d(TAG, "formatCalendarToDisplayDate - Input: year=$year, month=$month, day=$day")
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        val result = displayDateFormat.format(calendar.time)
        Log.d(TAG, "formatCalendarToDisplayDate - Result: $result")
        return result
    }

    fun parseDisplayDate(date: String): Triple<Int, Int, Int> {
        Log.d(TAG, "parseDisplayDate - Input date: $date")
        return try {
            // Try API format first
            val parsedDate = try {
                apiDateFormat.parse(date)
            } catch (e: Exception) {
                // If API format fails, try display format
                displayDateFormat.parse(date)
            }

            if (parsedDate != null) {
                val calendar = Calendar.getInstance()
                calendar.time = parsedDate
                val result = Triple(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                Log.d(TAG, "parseDisplayDate - Successfully parsed date: year=${result.first}, month=${result.second}, day=${result.third}")
                result
            } else {
                throw Exception("Failed to parse date")
            }
        } catch (e: Exception) {
            Log.e(TAG, "parseDisplayDate - Failed to parse date, using current date", e)
            val calendar = Calendar.getInstance()
            Triple(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
} 