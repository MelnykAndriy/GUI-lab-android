package com.msgtrik.msgtrik.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.msgtrik.msgtrik.R
import com.msgtrik.msgtrik.models.auth.AuthResponse
import com.msgtrik.msgtrik.models.auth.ErrorResponse
import com.msgtrik.msgtrik.models.auth.LoginRequest
import com.msgtrik.msgtrik.network.RetrofitClient
import com.msgtrik.msgtrik.ui.theme.Dimensions
import com.msgtrik.msgtrik.ui.theme.MsgtrikTheme
import com.msgtrik.msgtrik.utils.PreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

class LoginActivity : ComponentActivity() {
    private lateinit var preferenceManager: PreferenceManager

    // Email validation pattern
    private val emailPattern = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
                "@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(applicationContext)

        // Check if user is already logged in
        if (preferenceManager.getAccessToken() != null) {
            RetrofitClient.init(this@LoginActivity.applicationContext)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContent {
            MsgtrikTheme {
                LoginScreen()
            }
        }
    }

    @Composable
    fun LoginScreen() {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var emailError by remember { mutableStateOf<String?>(null) }
        var passwordError by remember { mutableStateOf<String?>(null) }
        var showPassword by remember { mutableStateOf(false) }
        var hasAttemptedLogin by remember { mutableStateOf(false) }
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo and Slogan
            Box(
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_round),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(96.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Welcome to Msgtrik.", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(24.dp))

            // Email field with validation
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    // Clear errors when user types
                    if (hasAttemptedLogin) {
                        emailError = if (!emailPattern.matcher(it).matches()) {
                            "Please enter a valid email address"
                        } else null
                        // Clear credential error when user starts typing
                        if (errorMessage?.contains("Incorrect email or password") == true) {
                            errorMessage = null
                            passwordError = null
                        }
                    }
                },
                label = { Text("Email") },
                isError = emailError != null || errorMessage?.contains("Incorrect email or password") == true,
                modifier = Modifier.width(Dimensions.InputFieldWidth),
                singleLine = true
            )
            if (emailError != null) {
                Text(
                    text = emailError!!,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Password field with show/hide toggle
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    // Clear credential error when user starts typing
                    if (errorMessage?.contains("Incorrect email or password") == true) {
                        errorMessage = null
                        passwordError = null
                    }
                },
                label = { Text("Password") },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (showPassword) "Hide password" else "Show password"
                        )
                    }
                },
                isError = passwordError != null || errorMessage?.contains("Incorrect email or password") == true,
                modifier = Modifier.width(Dimensions.InputFieldWidth),
                singleLine = true
            )
            if (passwordError != null) {
                Text(
                    text = passwordError!!,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Error message display
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Button(
                onClick = { 
                    hasAttemptedLogin = true
                    // Validate before sending request
                    when {
                        email.isEmpty() -> {
                            emailError = "Email is required"
                            return@Button
                        }
                        !emailPattern.matcher(email).matches() -> {
                            emailError = "Please enter a valid email address"
                            return@Button
                        }
                        password.isEmpty() -> {
                            passwordError = "Password is required"
                            return@Button
                        }
                        else -> {
                            handleLogin(
                                email,
                                password,
                                { isLoading = it },
                                { message -> 
                                    errorMessage = message
                                    // Set both fields to error state for incorrect credentials
                                    if (message?.contains("Incorrect email or password") == true) {
                                        emailError = null
                                        passwordError = null
                                    }
                                }
                            )
                        }
                    }
                },
                modifier = Modifier.width(280.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Login")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    context.startActivity(Intent(context, RegisterActivity::class.java))
                },
                modifier = Modifier.width(280.dp)
            ) {
                Text("Register")
            }
        }
    }

    private fun handleLogin(
        email: String,
        password: String,
        setLoading: (Boolean) -> Unit,
        setError: (String?) -> Unit
    ) {
        setLoading(true)
        setError(null)
        val loginRequest = LoginRequest(email, password)

        RetrofitClient.authService.login(loginRequest).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                setLoading(false)
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    // Store tokens
                    preferenceManager.saveAccessToken(authResponse.access)
                    preferenceManager.saveRefreshToken(authResponse.refresh)
                    // Initialize RetrofitClient with application context
                    RetrofitClient.init(this@LoginActivity.applicationContext)
                    // Navigate to MainActivity
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    try {
                        // Try to parse error response
                        val errorBody = response.errorBody()?.string()
                        if (errorBody != null) {
                            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                            when (response.code()) {
                                401 -> setError(errorResponse.message ?: "Incorrect email or password")
                                403 -> setError("Account is locked. Please contact support.")
                                404 -> setError("Account not found")
                                else -> setError(errorResponse.message ?: "Login failed: ${response.message()}")
                            }
                        } else {
                            setError("Login failed: ${response.message()}")
                        }
                    } catch (e: Exception) {
                        setError("Login failed: ${response.message()}")
                    }
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                setLoading(false)
                setError("Network error: Please check your internet connection")
            }
        })
    }
} 