package com.msgtrik.msgtrik.models.chat

data class NewMessageRequest(
    val receiverId: Int,
    val content: String
) 