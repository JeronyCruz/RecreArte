package edu.ucne.recrearte.data.remote.dto

data class ChangePasswordDto(
    val userId: Int,
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)