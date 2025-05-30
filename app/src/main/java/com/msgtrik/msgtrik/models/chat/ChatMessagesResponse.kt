package com.msgtrik.msgtrik.models.chat

data class ChatMessagesResponse(
    val messages: List<ChatMessage>,
    val pagination: Pagination
) 