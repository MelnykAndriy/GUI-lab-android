package com.msgtrik.msgtrik.models.chat

data class RecentChat(
    val user: ChatUser,
    val lastMessage: ChatMessage?,
    val unreadCount: Int
) 