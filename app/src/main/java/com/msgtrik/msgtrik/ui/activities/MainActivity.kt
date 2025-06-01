package com.msgtrik.msgtrik.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.msgtrik.msgtrik.models.auth.User
import com.msgtrik.msgtrik.models.auth.UserProfileUpdateRequest
import com.msgtrik.msgtrik.models.chat.ChatUser
import com.msgtrik.msgtrik.network.RetrofitClient
import com.msgtrik.msgtrik.ui.screens.ChatScreen
import com.msgtrik.msgtrik.ui.screens.ProfileScreen
import com.msgtrik.msgtrik.ui.screens.UserListScreen
import com.msgtrik.msgtrik.utils.PreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var selectedTab by remember { mutableStateOf(0) } // 0 = Chats, 1 = Profile
                var selectedUser by remember { mutableStateOf<ChatUser?>(null) }
                var currentUser by remember { mutableStateOf<User?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                var error by remember { mutableStateOf<String?>(null) }
                val context = LocalContext.current

                // Fetch current user on launch
                LaunchedEffect(Unit) {
                    RetrofitClient.authService.getCurrentUser().enqueue(object : Callback<User> {
                        override fun onResponse(call: Call<User>, response: Response<User>) {
                            if (response.isSuccessful) {
                                currentUser = response.body()
                                isLoading = false
                            } else {
                                val errorBody = response.errorBody()?.string()
                                val code = response.code()
                                error = "Failed to load profile. Status: $code, Error: $errorBody"
                                isLoading = false
                            }
                        }

                        override fun onFailure(call: Call<User>, t: Throwable) {
                            error = "Network error: ${t.message}"
                            isLoading = false
                        }
                    })
                }

                Scaffold(
                    bottomBar = {
                        BottomNavigation {
                            BottomNavigationItem(
                                icon = {
                                    Icon(
                                        Icons.Filled.Bolt,
                                        contentDescription = "Chats"
                                    )
                                },
                                label = { Text("Chats") },
                                selected = selectedTab == 0,
                                onClick = {
                                    selectedTab = 0
                                    selectedUser = null // Go back to user list
                                }
                            )
                            BottomNavigationItem(
                                icon = {
                                    Icon(
                                        Icons.Filled.AccountCircle,
                                        contentDescription = "Profile"
                                    )
                                },
                                label = { Text("Profile") },
                                selected = selectedTab == 1,
                                onClick = {
                                    selectedTab = 1
                                    selectedUser =
                                        null // Clear selected user when switching to profile
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when {
                            isLoading -> {
                                Text("Loading...")
                            }

                            error != null -> {
                                Text(error!!)
                            }

                            selectedTab == 0 && selectedUser == null -> {
                                UserListScreen(onUserSelected = { user ->
                                    selectedUser = user
                                })
                            }

                            selectedTab == 0 && selectedUser != null -> {
                                ChatScreen(
                                    selectedUser = selectedUser!!,
                                    currentUserId = currentUser!!.id,
                                    onBackClick = {
                                        selectedUser = null
                                    }
                                )
                            }

                            selectedTab == 1 && selectedUser == null && currentUser != null -> {
                                // Show current user's profile
                                ProfileScreen(
                                    user = currentUser!!,
                                    onLogout = {
                                        val preferenceManager = PreferenceManager(context)
                                        preferenceManager.clearTokens()
                                        val intent = Intent(context, LoginActivity::class.java)
                                        intent.flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        context.startActivity(intent)
                                        finish()
                                    },
                                    onSave = { updatedProfile ->
                                        val request =
                                            UserProfileUpdateRequest(profile = updatedProfile)
                                        RetrofitClient.authService.updateProfile(request)
                                            .enqueue(object : Callback<User> {
                                                override fun onResponse(
                                                    call: Call<User>,
                                                    response: Response<User>
                                                ) {
                                                    if (response.isSuccessful) {
                                                        currentUser = response.body()
                                                        Toast.makeText(
                                                            context,
                                                            "Profile updated!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Failed to update profile.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }

                                                override fun onFailure(
                                                    call: Call<User>,
                                                    t: Throwable
                                                ) {
                                                    Toast.makeText(
                                                        context,
                                                        "Network error: ${t.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            })
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 