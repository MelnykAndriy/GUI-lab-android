package com.msgtrik.msgtrik.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.msgtrik.msgtrik.models.Message
import com.msgtrik.msgtrik.models.chat.ChatMessage
import com.msgtrik.msgtrik.models.chat.ChatMessagesResponse
import com.msgtrik.msgtrik.models.chat.ChatUser
import com.msgtrik.msgtrik.models.chat.NewMessageRequest
import com.msgtrik.msgtrik.network.RetrofitClient
import com.msgtrik.msgtrik.ui.components.UserAvatar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    selectedUser: ChatUser,
    currentUserId: Int,
    onBackClick: () -> Unit
) {
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var messageText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(1) }
    var hasMoreMessages by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Function to load messages
    fun loadMessages(page: Int = 1, loadingMore: Boolean = false) {
        if (loadingMore) {
            if (!hasMoreMessages || isLoading) return
        }

        if (loadingMore) {
            isLoadingMore = true
        } else {
            isLoading = true
        }

        RetrofitClient.chatService.getChatMessages(selectedUser.id, page)
            .enqueue(object : Callback<ChatMessagesResponse> {
                override fun onResponse(
                    call: Call<ChatMessagesResponse>,
                    response: Response<ChatMessagesResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val newMessages = response.body()!!.messages.map {
                            Message(
                                text = it.content,
                                isSentByUser = it.senderId == currentUserId,
                                timestamp = it.timestamp
                            )
                        }

                        // Update pagination state
                        hasMoreMessages =
                            newMessages.isNotEmpty() && page < response.body()!!.pagination.pages

                        // Merge and sort messages
                        messages = if (loadingMore) {
                            (messages + newMessages)
                                .distinctBy { "${it.text}${it.timestamp}" }
                                .sortedByDescending { it.timestamp }
                        } else {
                            newMessages.sortedByDescending { it.timestamp }
                        }
                    } else {
                        error = "Failed to load messages"
                    }
                    isLoading = false
                    isLoadingMore = false
                }

                override fun onFailure(call: Call<ChatMessagesResponse>, t: Throwable) {
                    error = "Network error: ${t.message}"
                    isLoading = false
                    isLoadingMore = false
                }
            })
    }

    // Load initial messages
    LaunchedEffect(selectedUser.id) {
        loadMessages()
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Header
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        userProfile = selectedUser.profile,
                        size = 32.dp,
                        email = selectedUser.email
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(selectedUser.profile.name)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to user list"
                    )
                }
            },
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 4.dp
        )

        // Messages
        Box(modifier = Modifier.weight(1f)) {
            when {
                isLoading && !isLoadingMore -> {
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
                        if (isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }

                        items(messages) { message ->
                            MessageItem(message = message)
                        }

                        // Load more when reaching the top
                        if (hasMoreMessages) {
                            item {
                                LaunchedEffect(Unit) {
                                    currentPage++
                                    loadMessages(currentPage, true)
                                }
                            }
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
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Type a message") },
                    modifier = Modifier.weight(1f),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            RetrofitClient.chatService.sendMessage(
                                NewMessageRequest(
                                    selectedUser.id,
                                    messageText
                                )
                            ).enqueue(object : Callback<ChatMessage> {
                                override fun onResponse(
                                    call: Call<ChatMessage>,
                                    response: Response<ChatMessage>
                                ) {
                                    if (response.isSuccessful && response.body() != null) {
                                        val newMessage = response.body()
                                        messages = listOf(
                                            Message(
                                                newMessage!!.content,
                                                newMessage.senderId == currentUserId,
                                                newMessage.timestamp
                                            )
                                        ) + messages
                                        messageText = ""
                                    }
                                }

                                override fun onFailure(call: Call<ChatMessage>, t: Throwable) {
                                    // Handle error
                                }
                            })
                        }
                    }
                ) {
                    Text("Send")
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: Message) {
    val alignment = if (message.isSentByUser) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (message.isSentByUser) {
        MaterialTheme.colors.primary.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colors.secondary.copy(alpha = 0.12f)
    }
    val shape = if (message.isSentByUser) {
        RoundedCornerShape(8.dp, 0.dp, 8.dp, 8.dp)
    } else {
        RoundedCornerShape(0.dp, 8.dp, 8.dp, 8.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (message.isSentByUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(backgroundColor, shape)
                    .padding(8.dp)
            ) {
                Text(
                    text = message.text,
                    color = MaterialTheme.colors.onSurface
                )
            }
            Text(
                text = formatTimestamp(message.timestamp),
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
}

private fun formatTimestamp(timestamp: String): String {
    return try {
        val instant = java.time.Instant.parse(timestamp)
        val localDateTime = java.time.LocalDateTime.ofInstant(
            instant,
            java.time.ZoneId.systemDefault()
        )
        val now = java.time.LocalDateTime.now()
        
        when {
            localDateTime.toLocalDate() == now.toLocalDate() -> {
                // Today - show time only
                localDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
            }
            localDateTime.toLocalDate() == now.toLocalDate().minusDays(1) -> {
                // Yesterday
                "Yesterday ${localDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}"
            }
            localDateTime.year == now.year -> {
                // This year
                localDateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, HH:mm"))
            }
            else -> {
                // Different year
                localDateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm"))
            }
        }
    } catch (e: Exception) {
        // Fallback if parsing fails
        timestamp
    }
} 