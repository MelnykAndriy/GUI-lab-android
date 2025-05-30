package com.msgtrik.msgtrik.network.services

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import com.msgtrik.msgtrik.models.auth.LoginRequest
import com.msgtrik.msgtrik.models.auth.AuthResponse

interface AuthService {
    @POST("/api/users/login/")
    fun login(@Body request: LoginRequest): Call<AuthResponse>
} 