package com.msgtrik.msgtrik.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.msgtrik.msgtrik.R
import com.msgtrik.msgtrik.models.auth.AuthResponse
import com.msgtrik.msgtrik.models.auth.ErrorResponse
import com.msgtrik.msgtrik.models.auth.UserRegisterRequest
import com.msgtrik.msgtrik.network.RetrofitClient
import com.msgtrik.msgtrik.ui.activities.LoginActivity
import com.msgtrik.msgtrik.ui.components.DatePickerDropdown
import com.msgtrik.msgtrik.ui.components.GenderDropdown
import com.msgtrik.msgtrik.ui.theme.Dimensions
import com.msgtrik.msgtrik.utils.DateUtils
import com.msgtrik.msgtrik.utils.PreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val emailInteractionSource = remember { MutableInteractionSource() }
    val isEmailFocused by emailInteractionSource.collectIsFocusedAsState()
    var wasEmailFocused by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Email validation pattern
    val emailPattern = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
                "@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

    // Password validation function
    fun validatePassword(pass: String): String? {
        return when {
            pass.length < 8 -> "Password must be at least 8 characters long"
            !pass.any { it.isUpperCase() } -> "Password must contain at least one uppercase letter"
            !pass.any { it.isLowerCase() } -> "Password must contain at least one lowercase letter"
            !pass.any { it.isDigit() } -> "Password must contain at least one number"
            !pass.any { !it.isLetterOrDigit() } -> "Password must contain at least one special character"
            else -> null
        }
    }

    // Validate email when focus is lost
    if (!isEmailFocused && wasEmailFocused && email.isNotEmpty()) {
        emailError = if (!emailPattern.matcher(email).matches()) {
            "Please enter a valid email address"
        } else null
    }
    wasEmailFocused = isEmailFocused

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Back button and title row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    context.startActivity(Intent(context, LoginActivity::class.java))
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Login"
                )
            }
        }

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
        Text("Create an Account", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Join our community and start chatting", style = MaterialTheme.typography.body2)
        Spacer(modifier = Modifier.height(24.dp))

        // Name field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.width(Dimensions.InputFieldWidth)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Email field with validation
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                // Clear errors when user starts typing again
                emailError = null
                errorMessage = null
            },
            label = { Text("Email") },
            isError = emailError != null || errorMessage?.contains("Email already exists") == true,
            modifier = Modifier.width(Dimensions.InputFieldWidth),
            interactionSource = emailInteractionSource
        )
        if (emailError != null || errorMessage?.contains("Email already exists") == true) {
            Text(
                text = emailError ?: errorMessage!!,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Password field with validation
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = validatePassword(it)
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError != null,
            modifier = Modifier.width(Dimensions.InputFieldWidth)
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

        // Gender picker dropdown
        GenderDropdown(
            selectedGender = gender,
            onGenderSelected = { gender = it },
            modifier = Modifier.width(Dimensions.InputFieldWidth)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Date of Birth picker
        DatePickerDropdown(
            selectedDate = dob,
            onDateSelected = { dob = it },
            modifier = Modifier.width(Dimensions.InputFieldWidth)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                errorMessage = null
                val request = UserRegisterRequest(
                    name,
                    email,
                    password,
                    gender,
                    DateUtils.formatDateForApi(dob)
                )
                RetrofitClient.authService.register(request)
                    .enqueue(object : Callback<AuthResponse> {
                        override fun onResponse(
                            call: Call<AuthResponse>,
                            response: Response<AuthResponse>
                        ) {
                            isLoading = false
                            if (response.isSuccessful) {
                                // Store tokens
                                val authResponse = response.body()!!
                                val preferenceManager = PreferenceManager(context)
                                preferenceManager.saveAccessToken(authResponse.access)
                                preferenceManager.saveRefreshToken(authResponse.refresh)
                                // Initialize RetrofitClient with application context
                                RetrofitClient.init(context.applicationContext)
                                onRegisterSuccess()
                            } else {
                                try {
                                    val errorBody = response.errorBody()?.string()
                                    val errorJson = Gson().fromJson(errorBody, ErrorResponse::class.java)
                                    errorMessage = errorJson.message
                                } catch (e: Exception) {
                                    errorMessage = "Registration failed: ${response.message()}"
                                }
                            }
                        }

                        override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                            isLoading = false
                            errorMessage = "Error: ${t.message}"
                        }
                    })
            },
            modifier = Modifier
                .width(Dimensions.InputFieldWidth),
            enabled = !isLoading &&
                    name.isNotBlank() &&
                    email.isNotBlank() && emailError == null &&
                    password.isNotBlank() && passwordError == null &&
                    gender.isNotBlank() &&
                    dob.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colors.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Register")
            }
        }
    }
}