package edu.ucne.recrearte.data.remote.dto

data class LoginResponseDto(
    val userId: Int,
    val userName: String,
    val email: String,
    val token: String
)