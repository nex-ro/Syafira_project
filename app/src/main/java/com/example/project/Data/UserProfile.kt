package com.example.project.Data

data class UserProfile(
    val username: String = "",
    val fullName: String = "",
    val pass:String ="",
    val email: String = "",
    val phone: String = "",
    val gender: String = "",
    val role :String ="User",
    val profilePicUrl: String? = null
)
