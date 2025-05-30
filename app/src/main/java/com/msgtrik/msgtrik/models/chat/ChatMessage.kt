package com.msgtrik.msgtrik.models.chat

data class ChatMessage(
    val id: Int,
    val senderId: Int,
    val receiverId: Int,
    val content: String,
    val timestamp: String,
    val read: Boolean? = null
) 