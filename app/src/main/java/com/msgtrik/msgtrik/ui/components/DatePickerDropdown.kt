package com.msgtrik.msgtrik.ui.components

import android.app.DatePickerDialog
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
import com.msgtrik.msgtrik.utils.DateUtils
import java.util.Calendar

@Composable
fun DatePickerDropdown(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    val (year, month, day) = if (selectedDate.isNotEmpty()) {
        DateUtils.parseDisplayDate(selectedDate)
    } else {
        Triple(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = if (selectedDate.isNotEmpty()) DateUtils.formatDateForDisplay(selectedDate) else "",
            onValueChange = { },
            label = { Text("Date of Birth") },
            readOnly = true,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Select Date of Birth") },
            trailingIcon = {
                IconButton(
                    onClick = {
                        DatePickerDialog(context, { _, y, m, d ->
                            onDateSelected(String.format("%04d-%02d-%02d", y, m + 1, d))
                        }, year, month, day).show()
                    }
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