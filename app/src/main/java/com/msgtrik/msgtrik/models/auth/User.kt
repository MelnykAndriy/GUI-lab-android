package com.msgtrik.msgtrik.models.auth

data class User(
    val id: Int,
    val email: String,
    val profile: UserProfile
)

data class UserProfile(
    val name: String,
    val avatarUrl: String?,
    val avatarColor: String?
) 