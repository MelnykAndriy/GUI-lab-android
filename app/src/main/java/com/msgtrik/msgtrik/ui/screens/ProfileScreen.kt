package com.msgtrik.msgtrik.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.msgtrik.msgtrik.models.auth.ProfileUpdateFields
import com.msgtrik.msgtrik.models.auth.User
import com.msgtrik.msgtrik.ui.components.DatePickerDropdown
import com.msgtrik.msgtrik.ui.components.GenderDropdown
import com.msgtrik.msgtrik.ui.components.UserAvatar

@Composable
fun ProfileScreen(
    user: User,
    onLogout: () -> Unit,
    onSave: (ProfileUpdateFields) -> Unit
) {
    var editMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(TextFieldValue(user.profile.name ?: "")) }
    var gender by remember { mutableStateOf(user.profile.gender ?: "") }
    var dob by remember { mutableStateOf(user.profile.dob ?: "") }

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
        UserAvatar(
            userProfile = user.profile,
            size = 240.dp,
            email = user.email,
            modifier = Modifier.clickable(enabled = editMode) {
                // TODO: Implement avatar color/image picker
            }
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Email field (non-editable)
        OutlinedTextField(
            value = user.email,
            onValueChange = { },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            readOnly = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Name field
        OutlinedTextField(
            value = name,
            onValueChange = { if (editMode) name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = editMode,
            readOnly = !editMode
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Gender field
        GenderDropdown(
            selectedGender = gender,
            onGenderSelected = { if (editMode) gender = it },
            modifier = Modifier.fillMaxWidth(),
            enabled = editMode
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Date of Birth field
        DatePickerDropdown(
            selectedDate = dob,
            onDateSelected = { if (editMode) dob = it },
            modifier = Modifier.fillMaxWidth(),
            enabled = editMode
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Edit/Save buttons
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
                            dob = dob
                        )
                    )
                    editMode = false
                }) {
                    Text("Save")
                }
                Button(onClick = { editMode = false }) {
                    Text("Cancel")
                }
            }
        } else {
            Button(
                onClick = { editMode = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Profile")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Logout button
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
} 