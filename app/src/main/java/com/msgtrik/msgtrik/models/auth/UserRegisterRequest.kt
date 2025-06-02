package com.msgtrik.msgtrik.models.auth

data class UserRegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val gender: String,
    val dob: String
) 