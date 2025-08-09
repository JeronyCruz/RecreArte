package edu.ucne.recrearte.data.remote.dto

data class ShoppingCartsDto(
    val shoppingCartId: Int?,
    val subTotal: Double,
    val items: List<ShoppingCartDetailsDto>
)