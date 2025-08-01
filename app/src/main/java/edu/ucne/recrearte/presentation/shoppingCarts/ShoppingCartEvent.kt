package edu.ucne.recrearte.presentation.shoppingCarts

sealed interface ShoppingCartEvent {
    object GetCart : ShoppingCartEvent
    data class AddToCart(val workId: Int) : ShoppingCartEvent
    data class RemoveFromCart(val itemId: Int) : ShoppingCartEvent
    object ClearCart : ShoppingCartEvent
    object ResetSuccessMessage : ShoppingCartEvent
    object ClearErrorMessage : ShoppingCartEvent
}