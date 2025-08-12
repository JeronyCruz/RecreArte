package edu.ucne.recrearte.presentation.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.recrearte.data.repository.BillRepository
import edu.ucne.recrearte.data.repository.ShoppingCartRepository
import edu.ucne.recrearte.util.TokenManager
import edu.ucne.recrearte.util.getUserId
import edu.ucne.recrearte.util.isValidCreditCardNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillViewModel @Inject constructor(
    private val billRepository: BillRepository,
    private val shoppingCartRepository: ShoppingCartRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: BillEvent) {
        when (event) {
            BillEvent.CreateBill -> createBill()
            BillEvent.LoadCheckout -> loadCheckout()
            BillEvent.ResetState -> resetState()
            is BillEvent.SetPaymentMethod -> setPaymentMethod(event.id, event.name)
            is BillEvent.ValidateCreditCard -> validateCreditCard(event.cardNumber)
        }
    }

    private fun getCurrentCustomerId(): Int? {
        return tokenManager.getUserId()
    }

    fun validateCreditCard(cardNumber: String) {
        val isValid = cardNumber.isValidCreditCardNumber()
        _uiState.update { it.copy(
            isCardValid = isValid,
            cardValidationMessage = if (isValid) null else "Invalid card number"
        ) }
    }

    private fun loadCheckout() {
        val customerId = getCurrentCustomerId() ?: run {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Unauthenticated user"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val checkoutData = shoppingCartRepository.checkout(customerId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    createdBill = checkoutData
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading checkout: ${e.message}"
                )
            }
        }
    }

    private fun setPaymentMethod(id: Int, name: String) {
        _uiState.value.createdBill?.let { currentBill ->
            _uiState.value = _uiState.value.copy(
                createdBill = currentBill.copy(
                    paymentMethodId = id,
                    paymentMethodName = name
                )
            )
        }
    }

    private fun createBill() {
        val currentBill = _uiState.value.createdBill ?: run {
            _uiState.value = _uiState.value.copy(
                errorMessage = "There is no invoice data to create"
            )
            return
        }

        val customerId = getCurrentCustomerId() ?: run {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Unauthenticated user"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {

                val createdBill = billRepository.createBill(currentBill)

                shoppingCartRepository.clearCart(customerId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    createdBill = createdBill
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error creating invoice: ${e.message}"
                )
            }
        }
    }

    private fun resetState() {
        _uiState.value = BillUiState()
    }
}