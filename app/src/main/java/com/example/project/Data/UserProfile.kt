package com.example.project.Data

data class UserProfile(
    val username: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val gender: String = "",
    val profilePicUrl: String? = null
)
