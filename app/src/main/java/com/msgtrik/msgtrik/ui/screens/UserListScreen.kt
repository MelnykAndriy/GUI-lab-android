package com.msgtrik.msgtrik.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.msgtrik.msgtrik.models.chat.ChatUser
import com.msgtrik.msgtrik.models.chat.RecentChat
import com.msgtrik.msgtrik.models.chat.RecentChatsResponse
import com.msgtrik.msgtrik.network.RetrofitClient
import com.msgtrik.msgtrik.ui.components.UserAvatar
import com.msgtrik.msgtrik.ui.components.UserListItem
import com.msgtrik.msgtrik.utils.formatTimestamp
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun UserListScreen(
    modifier: Modifier = Modifier,
    onUserSelected: (ChatUser) -> Unit
) {
    var recentChats by remember { mutableStateOf<List<RecentChat>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<ChatUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Polling interval in milliseconds (10 seconds for recent chats)
    val pollingInterval = 10000L

    // Function to load recent chats
    fun loadRecentChats() {
        RetrofitClient.chatService.getRecentChats()
            .enqueue(object : Callback<RecentChatsResponse> {
                override fun onResponse(
                    call: Call<RecentChatsResponse>,
                    response: Response<RecentChatsResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        recentChats = response.body()!!.chats
                    } else {
                        error = "Failed to load recent chats"
                    }
                    isLoading = false
                }

                override fun onFailure(call: Call<RecentChatsResponse>, t: Throwable) {
                    error = "Network error: ${t.message}"
                    isLoading = false
                }
            })
    }

    // Load initial recent chats
    LaunchedEffect(Unit) {
        loadRecentChats()
    }

    // Set up polling for recent chats
    LaunchedEffect(Unit) {
        while (true) {
            try {
                delay(pollingInterval)
                loadRecentChats()
            } catch (e: Exception) {
                // If there's an error, wait a bit longer before retrying
                delay(pollingInterval * 2)
            }
        }
    }

    // Function to search users
    fun searchUsers(query: String) {
        if (query.isBlank()) {
            searchResults = emptyList()
            return
        }

        RetrofitClient.chatService.getUserByEmail(query)
            .enqueue(object : Callback<ChatUser> {
                override fun onResponse(
                    call: Call<ChatUser>,
                    response: Response<ChatUser>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        searchResults = listOf(response.body()!!)
                    } else {
                        searchResults = emptyList()
                    }
                }

                override fun onFailure(call: Call<ChatUser>, t: Throwable) {
                    searchResults = emptyList()
                    error = "Search failed: ${t.message}"
                }
            })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Chats Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Chats",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { /* TODO: New chat */ }) {
                        Text("+ New")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        searchUsers(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search users by email") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                )

                if (isLoading && recentChats.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                if (error != null && recentChats.isEmpty()) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (searchQuery.isNotBlank()) {
                    // Show search results
                    Column {
                        Text(
                            "Search Results",
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        searchResults.forEach { user ->
                            UserListItem(
                                user = user,
                                onClick = { onUserSelected(user) }
                            )
                        }
                        if (searchResults.isEmpty()) {
                            Text(
                                "No users found",
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                } else {
                    // Show recent chats
                    Column {
                        Text(
                            "Recent Chats",
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(recentChats) { chat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onUserSelected(chat.user) }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UserAvatar(
                                        userProfile = chat.user.profile,
                                        size = 40.dp,
                                        email = chat.user.email
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            chat.user.profile.name,
                                            style = MaterialTheme.typography.subtitle1,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (chat.lastMessage != null) {
                                            Text(
                                                chat.lastMessage.content,
                                                style = MaterialTheme.typography.body2,
                                                color = if (chat.unreadCount > 0) {
                                                    MaterialTheme.colors.primary
                                                } else {
                                                    MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                                },
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        if (chat.lastMessage != null) {
                                            Text(
                                                formatTimestamp(chat.lastMessage.timestamp),
                                                style = MaterialTheme.typography.caption,
                                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                        if (chat.unreadCount > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 4.dp)
                                                    .background(
                                                        MaterialTheme.colors.primary,
                                                        CircleShape
                                                    )
                                                    .size(20.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = chat.unreadCount.toString(),
                                                    color = MaterialTheme.colors.onPrimary,
                                                    style = MaterialTheme.typography.caption
                                                )
                                            }
                                        }
                                    }
                                }
                                Divider()
                            }
                        }
                    }
                }
            }
        }
    }
} 