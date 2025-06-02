package com.msgtrik.msgtrik.ui.components

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.msgtrik.msgtrik.R
import com.msgtrik.msgtrik.utils.DateUtils
import java.util.Calendar

private const val TAG = "DatePickerDropdown"

@Composable
fun DatePickerDropdown(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Log.d(TAG, "DatePickerDropdown composing with selectedDate: $selectedDate, enabled: $enabled")
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    // Set maximum date as today
    val maxDate = calendar.timeInMillis

    // Store today's values for initial display
    val todayYear = calendar.get(Calendar.YEAR)
    val todayMonth = calendar.get(Calendar.MONTH)
    val todayDay = calendar.get(Calendar.DAY_OF_MONTH)
    Log.d(TAG, "Today's date: $todayDay-$todayMonth-$todayYear")

    // Set minimum date to 100 years ago
    calendar.add(Calendar.YEAR, -100)
    val minDate = calendar.timeInMillis

    // Get the date to display (either selected date or today)
    val (year, month, day) = if (selectedDate.isNotEmpty()) {
        try {
            Log.d(TAG, "Attempting to parse selected date: $selectedDate")
            val result = DateUtils.parseDisplayDate(selectedDate)
            Log.d(TAG, "Successfully parsed selected date: year=${result.first}, month=${result.second}, day=${result.third}")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse selected date, using today's date", e)
            Triple(todayYear, todayMonth, todayDay)
        }
    } else {
        Log.d(TAG, "No date selected, using today's date")
        Triple(todayYear, todayMonth, todayDay)
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = if (selectedDate.isNotEmpty()) {
                try {
                    Log.d(TAG, "Formatting selected date for display: $selectedDate")
                    val formattedDate = DateUtils.formatDateForDisplay(selectedDate)
                    Log.d(TAG, "Formatted date: $formattedDate")
                    formattedDate
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to format date for display", e)
                    selectedDate
                }
            } else "",
            onValueChange = { },
            label = { Text("Date of Birth") },
            readOnly = true,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Select Date of Birth") },
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (enabled) {
                            Log.d(TAG, "Opening DatePickerDialog with date: $day-$month-$year")
                            DatePickerDialog(context, R.style.PurpleDatePicker, { _, selectedYear, selectedMonth, selectedDay ->
                                Log.d(TAG, "Date selected from picker: $selectedDay-$selectedMonth-$selectedYear")
                                // First format to display format (DD-MM-YYYY)
                                val displayDate = DateUtils.formatCalendarToDisplayDate(selectedYear, selectedMonth, selectedDay)
                                Log.d(TAG, "Formatted to display format: $displayDate")
                                // Then convert to API format (YYYY-MM-DD)
                                val apiDate = DateUtils.formatDateForApi(displayDate)
                                Log.d(TAG, "Converted to API format: $apiDate")
                                onDateSelected(apiDate)
                            }, year, month, day).apply {
                                datePicker.maxDate = maxDate
                                datePicker.minDate = minDate
                            }.show()
                        } else {
                            Log.d(TAG, "DatePicker click ignored - disabled state")
                        }
                    },
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select date",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )
    }
} 