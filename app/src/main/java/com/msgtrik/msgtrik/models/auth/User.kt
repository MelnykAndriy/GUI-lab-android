package com.msgtrik.msgtrik.models.auth

data class User(
    val id: Int,
    val email: String,
    val profile: UserProfile
)

data class UserProfile(
    val name: String,
    val gender: String?,
    val dob: String?,
    val avatarUrl: String?,
    val avatarColor: String?
)

data class UserProfileUpdateRequest(
    val profile: ProfileUpdateFields
)

data class ProfileUpdateFields(
    val name: String? = null,
    val gender: String? = null,
    val dob: String? = null,
    val avatarUrl: String? = null,
    val avatarColor: String? = null
)

data class AvatarUploadResponse(
    val avatarUrl: String
) 