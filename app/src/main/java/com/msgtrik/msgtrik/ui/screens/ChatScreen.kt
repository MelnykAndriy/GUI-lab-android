package com.msgtrik.msgtrik.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.msgtrik.msgtrik.models.Message
import com.msgtrik.msgtrik.models.chat.*
import com.msgtrik.msgtrik.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    selectedUser: ChatUser,
    currentUserId: Int
) {
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var messageText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Load messages when the screen is created
    LaunchedEffect(selectedUser.id) {
        RetrofitClient.chatService.getChatMessages(selectedUser.id).enqueue(object : Callback<ChatMessagesResponse> {
            override fun onResponse(call: Call<ChatMessagesResponse>, response: Response<ChatMessagesResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    messages = response.body()!!.messages.map {
                        Message(it.content, it.senderId == currentUserId)
                    }
                    isLoading = false
                } else {
                    error = "Failed to load messages"
                    isLoading = false
                }
            }
            override fun onFailure(call: Call<ChatMessagesResponse>, t: Throwable) {
                error = "Network error: ${t.message}"
                isLoading = false
            }
        })
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        TopAppBar(
            title = { Text(selectedUser.profile.name) },
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 4.dp
        )

        // Messages
        Box(modifier = Modifier.weight(1f)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                messages.isEmpty() -> {
                    Text(
                        text = "No messages yet. Start the conversation!",
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        reverseLayout = true
                    ) {
                        items(messages) { message ->
                            MessageItem(message = message)
                        }
                    }
                }
            }
        }

        // Message input
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            val request = NewMessageRequest(receiverId = selectedUser.id, content = messageText)
                            RetrofitClient.chatService.sendMessage(request).enqueue(object : Callback<ChatMessage> {
                                override fun onResponse(call: Call<ChatMessage>, response: Response<ChatMessage>) {
                                    if (response.isSuccessful) {
                                        // Add the new message to the list
                                        messages = messages + Message(messageText, true)
                                        messageText = "" // Clear input
                                    } else {
                                        error = "Failed to send message"
                                    }
                                }
                                override fun onFailure(call: Call<ChatMessage>, t: Throwable) {
                                    error = "Network error: ${t.message}"
                                }
                            })
                        }
                    },
                    enabled = messageText.isNotBlank()
                ) {
                    Text("Send")
                }
            }
        }
    }
}

@Composable
private fun MessageItem(message: Message) {
    val alignment = if (message.isSentByUser) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (message.isSentByUser)
        MaterialTheme.colors.primary
    else
        MaterialTheme.colors.surface
    val contentColor = if (message.isSentByUser)
        MaterialTheme.colors.onPrimary
    else
        MaterialTheme.colors.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        contentAlignment = alignment
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            backgroundColor = backgroundColor,
            elevation = 2.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                color = contentColor,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
} 