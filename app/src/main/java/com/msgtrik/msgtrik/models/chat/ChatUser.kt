package com.msgtrik.msgtrik.models.chat

import com.msgtrik.msgtrik.models.auth.UserProfile

data class ChatUser(
    val id: Int,
    val email: String,
    val profile: UserProfile
) 