package com.msgtrik.msgtrik.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.msgtrik.msgtrik.network.services.AuthService
import com.msgtrik.msgtrik.network.services.ChatService
import com.msgtrik.msgtrik.utils.Constants

object RetrofitClient {
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }

    val chatService: ChatService by lazy {
        retrofit.create(ChatService::class.java)
    }
} 