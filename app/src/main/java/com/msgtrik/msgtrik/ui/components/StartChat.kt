package com.msgtrik.msgtrik.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msgtrik.msgtrik.models.chat.ChatUser
import com.msgtrik.msgtrik.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun StartChat(
    onStartChat: (ChatUser) -> Unit,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "New Chat",
                style = MaterialTheme.typography.subtitle1
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }

        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                error = null
            },
            label = { Text("Enter user email") },
            modifier = Modifier.fillMaxWidth(),
            isError = error != null,
            singleLine = true,
            enabled = !isLoading
        )

        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption
            )
        }

        Button(
            onClick = {
                if (email.isBlank()) return@Button
                isLoading = true
                error = null

                RetrofitClient.chatService.getUserByEmail(email)
                    .enqueue(object : Callback<ChatUser> {
                        override fun onResponse(
                            call: Call<ChatUser>,
                            response: Response<ChatUser>
                        ) {
                            isLoading = false
                            if (response.isSuccessful && response.body() != null) {
                                onStartChat(response.body()!!)
                                onDismiss()
                            } else {
                                error = "User not found"
                            }
                        }

                        override fun onFailure(call: Call<ChatUser>, t: Throwable) {
                            isLoading = false
                            error = "Failed to search user: ${t.message}"
                        }
                    })
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = email.isNotBlank() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colors.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Searching...")
            } else {
                Text("Start Chat")
            }
        }
    }
} 