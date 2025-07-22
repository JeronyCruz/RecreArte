package edu.ucne.recrearte.data.remote.dto

data class ShoppingCartDetailsDto(
    val itemId: Int?,
    val workId: Int,
    val workTitle: String,
    val price: Double
)
