package com.msgtrik.msgtrik.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.msgtrik.msgtrik.models.Message
import com.msgtrik.msgtrik.models.chat.ChatMessage
import com.msgtrik.msgtrik.models.chat.ChatMessagesResponse
import com.msgtrik.msgtrik.models.chat.ChatUser
import com.msgtrik.msgtrik.models.chat.MarkReadResponse
import com.msgtrik.msgtrik.models.chat.NewMessageRequest
import com.msgtrik.msgtrik.network.RetrofitClient
import com.msgtrik.msgtrik.ui.components.UserAvatar
import com.msgtrik.msgtrik.ui.theme.ChatColors
import com.msgtrik.msgtrik.utils.DateUtils

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    // Polling interval in milliseconds (3 seconds)
    val pollingInterval = 3000L

    // Remember the list state to maintain scroll position
    val listState = rememberLazyListState()

    // Remember coroutine scope for animations
    val coroutineScope = rememberCoroutineScope()

    // Function to mark messages as read
    fun markMessagesAsRead() {
        RetrofitClient.chatService.markMessagesAsRead(selectedUser.id)
            .enqueue(object : Callback<MarkReadResponse> {
                override fun onResponse(
                    call: Call<MarkReadResponse>,
                    response: Response<MarkReadResponse>
                ) {
                    // No need to do anything on success
                }

                override fun onFailure(call: Call<MarkReadResponse>, t: Throwable) {
                    // Silently fail - we'll try again next time
                }
            })
    }

    // Function to load messages
    fun loadMessages(page: Int = 1, loadingMore: Boolean = false) {
        if (loadingMore) {
            if (!hasMoreMessages || isLoading) return
        }

        if (loadingMore) {
            isLoadingMore = true
        } else if (messages.isEmpty()) {
            // Only show loading indicator for initial load
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
                                timestamp = it.timestamp,
                                read = it.read ?: false
                            )
                        }

                        // Update pagination state
                        hasMoreMessages =
                            newMessages.isNotEmpty() && page < response.body()!!.pagination.pages

                        // For polling (page 1), check if we have new messages
                        if (page == 1 && !loadingMore) {
                            val existingMessageIds =
                                messages.map { "${it.text}${it.timestamp}" }.toSet()
                            val newMessagesList = newMessages.filterNot {
                                "${it.text}${it.timestamp}" in existingMessageIds
                            }

                            if (newMessagesList.isNotEmpty()) {
                                // Only add new messages to the existing list
                                messages = (newMessagesList + messages)
                                    .sortedByDescending { it.timestamp }

                                // Mark messages as read when we receive new ones
                                markMessagesAsRead()
                            }
                        } else {
                            // For pagination, merge with existing messages
                            messages = (messages + newMessages)
                                .distinctBy { "${it.text}${it.timestamp}" }
                                .sortedByDescending { it.timestamp }
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

    // Load initial messages and mark them as read
    LaunchedEffect(selectedUser.id) {
        loadMessages()
        markMessagesAsRead()
    }

    // Set up polling with error handling and backoff
    LaunchedEffect(selectedUser.id) {
        while (true) {
            try {
                delay(pollingInterval)
                loadMessages(page = 1)
            } catch (e: Exception) {
                // If there's an error, wait a bit longer before retrying
                delay(pollingInterval * 2)
            }
        }
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
                isLoading && messages.isEmpty() -> {
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
                        state = listState,
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

                        items(
                            items = messages,
                            key = { message: Message -> "${message.text}${message.timestamp}" }
                        ) { message ->
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
                                                text = newMessage!!.content,
                                                isSentByUser = true,
                                                timestamp = newMessage.timestamp
                                            )
                                        ) + messages

                                        messageText = ""

                                        coroutineScope.launch {
                                            listState.animateScrollToItem(0)
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<ChatMessage>, t: Throwable) {
                                    error = "Failed to send message: ${t.message}"
                                }
                            })
                        }
                    },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .height(56.dp) // Standard height for OutlinedTextField
                        .aspectRatio(1f) // Make it square
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send message",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: Message) {
    val alignment = if (message.isSentByUser) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (message.isSentByUser) {
        ChatColors.messageSentBackground()
    } else {
        ChatColors.messageReceivedBackground()
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
            Row(
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = if (message.isSentByUser) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateUtils.formatDateForDisplay(message.timestamp),
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
                if (message.isSentByUser) {
                    Text(
                        text = " • ${if (message.read) "Read" else "Sent"}",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}