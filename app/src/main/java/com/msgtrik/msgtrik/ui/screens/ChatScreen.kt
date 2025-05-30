package com.msgtrik.msgtrik.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msgtrik.msgtrik.models.Message
import com.msgtrik.msgtrik.models.chat.*
import com.msgtrik.msgtrik.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.msgtrik.msgtrik.models.chat.RecentChatsResponse

@Composable
fun ChatScreen() {
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var recentChats by remember { mutableStateOf<List<RecentChat>>(emptyList()) }
    var selectedUserId by remember { mutableStateOf<Int?>(null) }
    var messageText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Recent chats row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            items(recentChats) { chat ->
                ChatItem(
                    chat = chat,
                    onClick = {
                        selectedUserId = chat.user.id
                        loadMessages(chat.user.id) { newMessages ->
                            messages = newMessages
                        }
                    }
                )
            }
        }

        // Messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            reverseLayout = true
        ) {
            items(messages) { message ->
                MessageItem(message = message)
            }
        }

        // Message input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    selectedUserId?.let { userId ->
                        sendMessage(userId, messageText) {
                            messageText = ""
                            loadMessages(userId) { newMessages ->
                                messages = newMessages
                            }
                        }
                    }
                },
                enabled = messageText.isNotBlank() && selectedUserId != null
            ) {
                Text("Send")
            }
        }
    }

    // Load recent chats when the screen is created
    LaunchedEffect(Unit) {
        loadRecentChats { newChats ->
            recentChats = newChats
        }
    }
}

@Composable
private fun ChatItem(chat: RecentChat, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .clickable(onClick = onClick),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = chat.user.profile.name,
                style = MaterialTheme.typography.h6
            )
            chat.lastMessage?.let { message ->
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun MessageItem(message: Message) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = if (message.isSentByUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            backgroundColor = if (message.isSentByUser)
                MaterialTheme.colors.primary
            else
                MaterialTheme.colors.surface,
            elevation = 2.dp
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(8.dp),
                color = if (message.isSentByUser)
                    MaterialTheme.colors.onPrimary
                else
                    MaterialTheme.colors.onSurface
            )
        }
    }
}

private fun loadRecentChats(onSuccess: (List<RecentChat>) -> Unit) {
    RetrofitClient.chatService.getRecentChats().enqueue(object : Callback<RecentChatsResponse> {
        override fun onResponse(call: Call<RecentChatsResponse>, response: Response<RecentChatsResponse>) {
            if (response.isSuccessful && response.body() != null) {
                onSuccess(response.body()!!.chats)
            }
        }
        override fun onFailure(call: Call<RecentChatsResponse>, t: Throwable) {
            // Handle error
        }
    })
}

private fun loadMessages(userId: Int, onSuccess: (List<Message>) -> Unit) {
    RetrofitClient.chatService.getChatMessages(userId).enqueue(object : Callback<ChatMessagesResponse> {
        override fun onResponse(call: Call<ChatMessagesResponse>, response: Response<ChatMessagesResponse>) {
            if (response.isSuccessful && response.body() != null) {
                val messages = response.body()!!.messages.map {
                    Message(it.content, it.senderId != userId)
                }
                onSuccess(messages)
            }
        }
        override fun onFailure(call: Call<ChatMessagesResponse>, t: Throwable) {
            // Handle error
        }
    })
}

private fun sendMessage(userId: Int, content: String, onSuccess: () -> Unit) {
    val request = NewMessageRequest(receiverId = userId, content = content)
    RetrofitClient.chatService.sendMessage(request).enqueue(object : Callback<ChatMessage> {
        override fun onResponse(call: Call<ChatMessage>, response: Response<ChatMessage>) {
            if (response.isSuccessful && response.body() != null) {
                onSuccess()
            }
        }
        override fun onFailure(call: Call<ChatMessage>, t: Throwable) {
            // Handle error
        }
    })
} 