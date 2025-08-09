package edu.ucne.recrearte.presentation.shoppingCarts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.repository.ShoppingCartRepository
import edu.ucne.recrearte.util.TokenManager
import edu.ucne.recrearte.util.getUserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingCartViewModel @Inject constructor(
    private val repository: ShoppingCartRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingCartUiState())
    val uiSate = _uiState.asStateFlow()

    fun onEvent(event: ShoppingCartEvent) {
        when (event) {
            is ShoppingCartEvent.GetCart -> getCart()
            is ShoppingCartEvent.AddToCart -> addToCart(event.workId)
            is ShoppingCartEvent.RemoveFromCart -> removeFromCart(event.itemId)
            is ShoppingCartEvent.ClearCart -> clearCart()
            ShoppingCartEvent.ResetSuccessMessage -> resetSuccessMessage()
            ShoppingCartEvent.ClearErrorMessage -> clearErrorMessage()
        }
    }

    private fun getCurrentCustomerId(): Int? {
        return tokenManager.getUserId()
    }

    private fun getCart() {
        val customerId = getCurrentCustomerId() ?: run {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Unauthenticated user"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            when (val result = repository.getCart(customerId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        items = result.data?.items ?: emptyList(),
                        subTotal = result.data?.subTotal ?: 0.0,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "Unknown error while getting the cart"
                    )
                }
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun addToCart(workId: Int) {
        val customerId = getCurrentCustomerId() ?: run {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Unauthenticated user"
            )
            return
        }

        viewModelScope.launch {
            try {
                repository.addToCart(customerId, workId)
                getCart()
                _uiState.value = _uiState.value.copy(
                    isSuccess = true,
                    successMessage = "Added to cart"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Could not add to cart: ${e.message}"
                )
            }
        }
    }

    private fun removeFromCart(itemId: Int) {
        val customerId = getCurrentCustomerId() ?: run {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Unauthenticated user"
            )
            return
        }

        viewModelScope.launch {
            try {
                repository.removeFromCart(itemId)
                getCart()
                _uiState.value = _uiState.value.copy(
                    isSuccess = true,
                    successMessage = "Item removed from cart"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error deleting item: ${e.message}"
                )
            }
        }
    }

    private fun clearCart() {
        val customerId = getCurrentCustomerId() ?: run {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Unauthenticated user"
            )
            return
        }

        viewModelScope.launch {
            try {
                repository.clearCart(customerId)
                getCart()
                _uiState.value = _uiState.value.copy(
                    isSuccess = true,
                    successMessage = "Cart emptied"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "The cart could not be emptied: ${e.message}"
                )
            }
        }
    }

    private fun resetSuccessMessage() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            isSuccess = false
        )
    }

    private fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }
}