package edu.ucne.recrearte.presentation.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.recrearte.data.remote.dto.BillsDetailsDto
import edu.ucne.recrearte.data.remote.dto.BillsDto
import edu.ucne.recrearte.data.remote.dto.WorksDto
import edu.ucne.recrearte.data.repository.BillRepository
import edu.ucne.recrearte.data.repository.CustomerRepository
import edu.ucne.recrearte.data.repository.ShoppingCartRepository
import edu.ucne.recrearte.di.DateAdapter
import edu.ucne.recrearte.presentation.work.WorkViewModel
import edu.ucne.recrearte.util.TokenManager
import edu.ucne.recrearte.util.getUserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.Date
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
        }
    }

    private fun getCurrentCustomerId(): Int? {
        return tokenManager.getUserId()
    }

    private fun loadCheckout() {
        val customerId = getCurrentCustomerId() ?: run {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Usuario no autenticado"
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
                    errorMessage = "Error al cargar checkout: ${e.message}"
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
                errorMessage = "No hay datos de factura para crear"
            )
            return
        }

        val customerId = getCurrentCustomerId() ?: run {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Usuario no autenticado"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                // 1. Crear la factura en el backend
                val createdBill = billRepository.createBill(currentBill)

                // 2. Vaciar el carrito
                shoppingCartRepository.clearCart(customerId)

                // 3. Actualizar estado
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    createdBill = createdBill
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al crear factura: ${e.message}"
                )
            }
        }
    }

    private fun resetState() {
        _uiState.value = BillUiState()
    }
}