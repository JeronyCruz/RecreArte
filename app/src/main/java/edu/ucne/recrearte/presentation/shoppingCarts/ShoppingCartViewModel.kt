package edu.ucne.recrearte.presentation.shoppingCarts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
        return tokenManager.getUserId() // Asume que TokenManager tiene este método
    }

    private fun getCart() {
        val customerId = getCurrentCustomerId() ?: run {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Usuario no autenticado"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val cart = repository.getCart(customerId)
                _uiState.value = _uiState.value.copy(
                    items = cart.items,
                    subTotal = cart.subTotal,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al obtener el carrito: ${e.message}"
                )
            }
        }
    }

    private fun addToCart(workId: Int) {
        val customerId = getCurrentCustomerId() ?: run {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Usuario no autenticado"
            )
            return
        }

        viewModelScope.launch {
            try {
                repository.addToCart(customerId, workId)
                getCart() // Refrescar el carrito
                _uiState.value = _uiState.value.copy(
                    isSuccess = true,
                    successMessage = "Agregado al carrito"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "No se pudo agregar al carrito: ${e.message}"
                )
            }
        }
    }

    private fun removeFromCart(itemId: Int) {
        val customerId = getCurrentCustomerId() ?: run {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Usuario no autenticado"
            )
            return
        }

        viewModelScope.launch {
            try {
                repository.removeFromCart(itemId)
                getCart() // Refrescar el carrito
                _uiState.value = _uiState.value.copy(
                    isSuccess = true,
                    successMessage = "Item eliminado del carrito"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al eliminar el item: ${e.message}"
                )
            }
        }
    }

    private fun clearCart() {
        val customerId = getCurrentCustomerId() ?: run {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Usuario no autenticado"
            )
            return
        }

        viewModelScope.launch {
            try {
                repository.clearCart(customerId)
                getCart() // Refrescar el carrito (debería estar vacío)
                _uiState.value = _uiState.value.copy(
                    isSuccess = true,
                    successMessage = "Carrito vaciado"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "No se pudo vaciar el carrito: ${e.message}"
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