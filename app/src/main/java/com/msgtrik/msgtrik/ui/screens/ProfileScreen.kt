package com.msgtrik.msgtrik.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.msgtrik.msgtrik.R
import com.msgtrik.msgtrik.models.auth.ProfileUpdateFields
import com.msgtrik.msgtrik.models.auth.User
import java.util.Calendar

@Composable
fun ProfileScreen(
    user: User,
    onLogout: () -> Unit,
    onSave: (ProfileUpdateFields) -> Unit
) {
    val context = LocalContext.current

    var editMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(TextFieldValue(user.profile.name ?: "")) }
    var gender by remember { mutableStateOf(user.profile.gender ?: "") }
    var dob by remember { mutableStateOf(user.profile.dob ?: "") }
    val avatarUrl by remember { mutableStateOf(user.profile.avatarUrl ?: "") }
    val avatarColor by remember {
        mutableStateOf(user.profile.avatarColor ?: "bg-purple-500")
    }

    fun mapTailwindColorToRes(colorClass: String): Int {
        return when (colorClass) {
            "bg-purple-500" -> R.color.bg_purple_500
            "bg-blue-500" -> R.color.bg_blue_500
            "bg-green-500" -> R.color.bg_green_500
            "bg-yellow-500" -> R.color.bg_yellow_500
            "bg-pink-500" -> R.color.bg_pink_500
            "bg-indigo-500" -> R.color.bg_indigo_500
            else -> R.color.bg_purple_500 // default fallback
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(bottom = 24.dp)
        )


        // Avatar
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(
                    Color(context.getColor(mapTailwindColorToRes(avatarColor))),
                    CircleShape
                )
                .clickable(enabled = editMode) {
                    // TODO: Implement avatar color/image picker
                },
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl.isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(avatarUrl),
                    contentDescription = "Avatar",
                    modifier = Modifier.size(96.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = user.profile.name.take(1).uppercase(),
                    style = MaterialTheme.typography.h3,
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Name
        if (editMode) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(text = "Name: ${user.profile.name}", style = MaterialTheme.typography.body1)
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Gender
        if (editMode) {
            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (gender.isNotBlank()) gender.replaceFirstChar { it.uppercase() } else "Select Gender")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(onClick = {
                        gender = "male"; expanded = false
                    }) { Text("Male") }
                    DropdownMenuItem(onClick = {
                        gender = "female"; expanded = false
                    }) { Text("Female") }
                    DropdownMenuItem(onClick = {
                        gender = "other"; expanded = false
                    }) { Text("Other") }
                }
            }
        } else {
            Text(
                text = "Gender: ${user.profile.gender ?: "N/A"}",
                style = MaterialTheme.typography.body1
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Date of Birth
        if (editMode) {
            val calendar = Calendar.getInstance()
            val year = dob.take(4).toIntOrNull() ?: calendar.get(Calendar.YEAR)
            val month = dob.drop(5).take(2).toIntOrNull()?.minus(1) ?: calendar.get(Calendar.MONTH)
            val day = dob.takeLast(2).toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)
            OutlinedButton(onClick = {
                DatePickerDialog(context, { _, y, m, d ->
                    dob = String.format("%04d-%02d-%02d", y, m + 1, d)
                }, year, month, day).show()
            }, modifier = Modifier.fillMaxWidth()) {
                Text(if (dob.isNotBlank()) dob else "Select Date of Birth")
            }
        } else {
            Text(
                text = "Date of Birth: ${user.profile.dob ?: "N/A"}",
                style = MaterialTheme.typography.body1
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Edit/Save/Cancel buttons
        if (editMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    onSave(
                        ProfileUpdateFields(
                            name = name.text,
                            gender = gender,
                            dob = dob,
                            avatarUrl = avatarUrl.takeIf { it.isNotBlank() },
                            avatarColor = avatarColor
                        )
                    )
                    editMode = false
                }) { Text("Save") }
                Button(onClick = { editMode = false }) { Text("Cancel") }
            }
        } else {
            Button(
                onClick = { editMode = true },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Edit Profile") }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Logout")
        }
    }
} 