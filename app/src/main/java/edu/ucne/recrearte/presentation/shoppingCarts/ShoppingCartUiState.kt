package edu.ucne.recrearte.presentation.shoppingCarts

import edu.ucne.recrearte.data.remote.dto.ShoppingCartDetailsDto

data class ShoppingCartUiState(
    val items: List<ShoppingCartDetailsDto> = emptyList(),
    val subTotal: Double = 0.0,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val base64: String? = null
)