package com.msgtrik.msgtrik.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.msgtrik.msgtrik.models.auth.*
import com.msgtrik.msgtrik.network.RetrofitClient
import com.msgtrik.msgtrik.ui.screens.ProfileScreen
import com.msgtrik.msgtrik.utils.PreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            var user by remember { mutableStateOf<User?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var error by remember { mutableStateOf<String?>(null) }

            // Fetch user profile on launch
            LaunchedEffect(Unit) {
                RetrofitClient.authService.getCurrentUser().enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.isSuccessful) {
                            user = response.body()
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

            if (isLoading) {
                androidx.compose.material.Text("Loading profile...")
            } else if (error != null) {
                androidx.compose.material.Text(error!!)
            } else if (user != null) {
                ProfileScreen(
                    user = user!!,
                    onLogout = {
                        val preferenceManager = PreferenceManager(this@ProfileActivity)
                        preferenceManager.clearTokens()
                        val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    },
                    onSave = { updatedProfile ->
                        val updateRequest = UserProfileUpdateRequest(profile = updatedProfile)
                        RetrofitClient.authService.updateProfile(updateRequest).enqueue(object : Callback<User> {
                            override fun onResponse(call: Call<User>, response: Response<User>) {
                                if (response.isSuccessful) {
                                    user = response.body()
                                    Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to update profile.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onFailure(call: Call<User>, t: Throwable) {
                                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                )
            }
        }
    }
}
