package com.msgtrik.msgtrik.network.services

import com.msgtrik.msgtrik.models.auth.AuthResponse
import com.msgtrik.msgtrik.models.auth.AvatarUploadResponse
import com.msgtrik.msgtrik.models.auth.LoginRequest
import com.msgtrik.msgtrik.models.auth.TokenRefreshRequest
import com.msgtrik.msgtrik.models.auth.User
import com.msgtrik.msgtrik.models.auth.UserProfileUpdateRequest
import com.msgtrik.msgtrik.models.auth.UserRegisterRequest
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface AuthService {
    @POST("/api/users/login/")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @POST("/api/users/register/")
    fun register(@Body request: UserRegisterRequest): Call<AuthResponse>

    @POST("/api/users/token/refresh/")
    fun refreshToken(@Body request: TokenRefreshRequest): Call<AuthResponse>

    @GET("/api/users/me/")
    fun getCurrentUser(): Call<User>

    @PUT("/api/users/me")
    fun updateProfile(@Body body: UserProfileUpdateRequest): Call<User>

    @Multipart
    @POST("/api/users/me/avatar")
    fun uploadAvatar(@Part avatar: MultipartBody.Part): Call<AvatarUploadResponse>
} 