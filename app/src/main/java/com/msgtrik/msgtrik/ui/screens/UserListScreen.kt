package com.msgtrik.msgtrik.ui.screens

import android.content.Intent
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
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msgtrik.msgtrik.models.chat.ChatUser
import com.msgtrik.msgtrik.models.chat.RecentChat
import com.msgtrik.msgtrik.models.chat.RecentChatsResponse
import com.msgtrik.msgtrik.network.RetrofitClient
import com.msgtrik.msgtrik.ui.components.UserAvatar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun UserListScreen(onUserSelected: (ChatUser) -> Unit) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<ChatUser?>(null) }
    var recentChats by remember { mutableStateOf<List<RecentChat>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)) {

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
                    Text("Chats", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { /* TODO: New chat */ }) {
                        Text("+ New")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search users ...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = {
                                isLoading = true
                                RetrofitClient.chatService.getUserByEmail(searchQuery)
                                    .enqueue(object : Callback<ChatUser> {
                                        override fun onResponse(
                                            call: Call<ChatUser>,
                                            response: Response<ChatUser>
                                        ) {
                                            isLoading = false
                                            searchResult =
                                                if (response.isSuccessful) response.body() else null
                                        }

                                        override fun onFailure(call: Call<ChatUser>, t: Throwable) {
                                            isLoading = false
                                            searchResult = null
                                        }
                                    })
                            }) {
                                Icon(
                                    painterResource(android.R.drawable.ic_menu_search),
                                    contentDescription = "Search"
                                )
                            }
                        }
                    }
                )
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                if (searchResult != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onUserSelected(searchResult!!) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            userProfile = searchResult!!.profile,
                            size = 36.dp,
                            email = searchResult!!.email
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(searchResult!!.profile.name, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(searchResult!!.email, color = Color.Gray, fontSize = 12.sp)
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Recent Chats", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(recentChats) { chat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable { onUserSelected(chat.user) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(
                                userProfile = chat.user.profile,
                                size = 36.dp,
                                email = chat.user.email
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(chat.user.profile.name, fontWeight = FontWeight.Medium)
                                Text(
                                    chat.lastMessage?.content ?: "",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                chat.lastMessage?.timestamp?.take(10)?.replace("-", "/") ?: "",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        Divider()
                    }
                }
            }
        }
    }

    // Load recent chats on screen launch
    LaunchedEffect(Unit) {
        RetrofitClient.chatService.getRecentChats().enqueue(object : Callback<RecentChatsResponse> {
            override fun onResponse(
                call: Call<RecentChatsResponse>,
                response: Response<RecentChatsResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    recentChats = response.body()!!.chats
                }
            }

            override fun onFailure(call: Call<RecentChatsResponse>, t: Throwable) {
                // Handle error
            }
        })
    }
} 