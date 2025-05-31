package com.msgtrik.msgtrik.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.msgtrik.msgtrik.models.auth.AuthResponse
import com.msgtrik.msgtrik.models.auth.LoginRequest
import com.msgtrik.msgtrik.network.RetrofitClient
import com.msgtrik.msgtrik.utils.PreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.msgtrik.msgtrik.ui.activities.RegisterActivity
import androidx.compose.ui.platform.LocalContext

class LoginActivity : ComponentActivity() {
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(this)

        // Check if user is already logged in
        if (preferenceManager.getAccessToken() != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    LoginScreen()
                }
            }
        }
    }

    @Composable
    fun LoginScreen() {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { handleLogin(email, password) { isLoading = it } },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colors.onPrimary)
                } else {
                    Text("Login")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    context.startActivity(Intent(context, RegisterActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }
        }
    }

    private fun handleLogin(email: String, password: String, setLoading: (Boolean) -> Unit) {
        setLoading(true)
        val loginRequest = LoginRequest(email, password)

        RetrofitClient.authService.login(loginRequest).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                setLoading(false)
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    // Store tokens
                    preferenceManager.saveAccessToken(authResponse.access)
                    preferenceManager.saveRefreshToken(authResponse.refresh)
                    // Navigate to MainActivity
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                setLoading(false)
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
} 