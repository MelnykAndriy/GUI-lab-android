package com.msgtrik.msgtrik.network.services

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.msgtrik.msgtrik.models.chat.NewMessageRequest
import com.msgtrik.msgtrik.models.chat.ChatMessage
import com.msgtrik.msgtrik.models.chat.RecentChatsResponse
import com.msgtrik.msgtrik.models.chat.ChatMessagesResponse
import com.msgtrik.msgtrik.models.chat.ChatUser
import com.msgtrik.msgtrik.models.chat.MarkReadResponse

interface ChatService {
    @GET("/api/chats/")
    fun getRecentChats(): Call<RecentChatsResponse>

    @GET("/api/chats/messages/{userId}")
    fun getChatMessages(
        @Path("userId") userId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Call<ChatMessagesResponse>

    @POST("/api/chats/messages/")
    fun sendMessage(@Body request: NewMessageRequest): Call<ChatMessage>

    @GET("/api/users/search/{email}/")
    fun getUserByEmail(@Path("email") email: String): Call<ChatUser>

    @POST("/api/chats/messages/{userId}/read/")
    fun markMessagesAsRead(@Path("userId") userId: Int): Call<MarkReadResponse>
} 