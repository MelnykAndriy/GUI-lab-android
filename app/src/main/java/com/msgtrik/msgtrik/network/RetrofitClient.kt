package com.msgtrik.msgtrik.network

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.msgtrik.msgtrik.models.auth.AuthResponse
import com.msgtrik.msgtrik.models.auth.TokenRefreshRequest
import com.msgtrik.msgtrik.network.services.AuthService
import com.msgtrik.msgtrik.network.services.ChatService
import com.msgtrik.msgtrik.utils.Constants
import com.msgtrik.msgtrik.utils.PreferenceManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val authInterceptor = Interceptor { chain ->
        println("Interceptor: Received request ${chain.request().url}")
        val originalRequest: Request = chain.request()
        val builder = originalRequest.newBuilder()
        val token = appContext?.let { PreferenceManager(it).getAccessToken() }
        if (!token.isNullOrBlank()) {
            println("Interceptor: Adding auth token header")
            builder.addHeader("Authorization", "Bearer $token")
        } else {
            println("Interceptor: No auth token available") 
        }
        val response = chain.proceed(builder.build())
        println("Interceptor: Response code ${response.code}")

        // If unauthorized, try to refresh token and retry once
        if (response.code == 401 && appContext != null) {
            response.close()
            val prefManager = PreferenceManager(appContext!!)
            val refreshToken = prefManager.getRefreshToken()
            if (!refreshToken.isNullOrBlank()) {
                val refreshCall = retrofit.create(AuthService::class.java)
                    .refreshToken(TokenRefreshRequest(refreshToken))
                val refreshResponse: Response<AuthResponse> = try {
                    refreshCall.execute()
                } catch (e: Exception) {
                    return@Interceptor response
                }
                if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                    val newAccess = refreshResponse.body()!!.access
                    prefManager.saveAccessToken(newAccess)
                    // Retry original request with new token
                    val newRequest = originalRequest.newBuilder()
                        .removeHeader("Authorization")
                        .addHeader("Authorization", "Bearer $newAccess")
                        .build()
                    return@Interceptor chain.proceed(newRequest)
                }
            }
        }
        response
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }

    val chatService: ChatService by lazy {
        retrofit.create(ChatService::class.java)
    }
} 