package edu.ucne.recrearte.presentation.paymentMethods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.PaymentMethodsDto
import edu.ucne.recrearte.data.repository.PaymentMethodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentMethodViewModel @Inject constructor(
    private val repository: PaymentMethodRepository
): ViewModel(){
    private val _uiState = MutableStateFlow(PaymentMethodUiState())
    val uiSate = _uiState.asStateFlow()
    private val _loading = MutableStateFlow(false)
    val loading : StateFlow<Boolean> = _loading
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _searchResults = MutableStateFlow<List<PaymentMethodsDto>>(emptyList())
    val searchResults: StateFlow<List<PaymentMethodsDto>> = _searchResults.asStateFlow()

    fun onEvent(event: PaymentMethodEvent){
        when(event){
            PaymentMethodEvent.ClearErrorMessage -> clearErrorMessage()
            PaymentMethodEvent.CreatePaymentMethod -> createPaymentMethod()
            is PaymentMethodEvent.DeletePaymentMethod -> deletePaymentMethods(event.id)
            PaymentMethodEvent.GetPaymentMethods -> getPaymentMethods()
            is PaymentMethodEvent.NameChange -> nameOnChange(event.name)
            PaymentMethodEvent.New -> new()
            is PaymentMethodEvent.PaymentMethodIdChange -> paymentMethodIdOnChange(event.paymentMethodId)
            PaymentMethodEvent.ResetSuccessMessage -> resetSuccessMessage()
            is PaymentMethodEvent.UpdatePaymentMethod -> updatePaymentMethod(event.id)
        }
    }

    init {
        getPaymentMethods()
        //Para la busqueda
        viewModelScope.launch {
            _searchQuery
                .debounce(600)
                .distinctUntilChanged()
                .mapLatest { query ->
                    filterPaymentMethod(query)
                }
                .collectLatest { filtered ->
                    _searchResults.value = filtered
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun filterPaymentMethod(query: String): List<PaymentMethodsDto> {
        return if (query.isBlank()) {
            _uiState.value.PaymentMethods
        } else {
            _uiState.value.PaymentMethods.filter {
                it.paymentMethodName.contains(query, ignoreCase = true)
            }
        }
    }

    private fun nameOnChange(name: String){
        _uiState.value = _uiState.value
            .copy(paymentMethodName = name)
    }

    private fun paymentMethodIdOnChange(id: Int){
        _uiState.value = _uiState.value
            .copy(paymentMethodId = id)
    }

    private fun createPaymentMethod(){
        val name = _uiState.value.paymentMethodName.trim()
        val validationError = isValidPaymentMethodName(name)

        if (validationError != null) {
            _uiState.value = _uiState.value.copy(errorMessage = validationError)
            return
        }
        viewModelScope.launch {
            try {
                val method = PaymentMethodsDto(
                    paymentMethodId = 0,
                    paymentMethodName = _uiState.value.paymentMethodName
                )
                repository.createPaymentMethod(method)
                _uiState.value = _uiState.value.copy(
                    isSuccess = true,
                    successMessage = "Payment method created successfully",
                    paymentMethodName = "",
                    paymentMethodId = null
                )
                onEvent(PaymentMethodEvent.GetPaymentMethods)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error creating: ${e.message}")
            }
        }
    }
//revisar
    private fun updatePaymentMethod(id: Int){
        val name = _uiState.value.paymentMethodName.trim()
        val validationError = isValidPaymentMethodName(name)

        if (validationError != null) {
            _uiState.value = _uiState.value.copy(errorMessage = validationError)
            return
        }
        viewModelScope.launch {
            try {
                val method = PaymentMethodsDto(
                    paymentMethodId = id,
                    paymentMethodName = _uiState.value.paymentMethodName
                )
                repository.updatePaymentMethod(id, method)
                _uiState.value = _uiState.value.copy(
                    isSuccess = true,
                    successMessage = "Payment method updated successfully"
                )
                onEvent(PaymentMethodEvent.GetPaymentMethods)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error updating: ${e.message}")
            }
        }
    }

    private fun new(){
        _uiState.value = _uiState.value.copy(
            paymentMethodId = null,
            paymentMethodName = "",
            errorMessage = null,
            isSuccess = false,
            successMessage = null
        )
    }

    private fun resetSuccessMessage(){
        _uiState.value = _uiState.value
            .copy(
                isSuccess = false, successMessage = null
            )
    }

    private fun clearErrorMessage(){
        _uiState.value = _uiState.value
            .copy(errorMessage = null)
    }

    private fun deletePaymentMethods(id: Int){
        viewModelScope.launch {
            try {
                repository.deletePaymentMethod(id)
                _uiState.value = _uiState.value.copy(
                    isSuccess = true,
                    successMessage = "Payment method successfully removed"
                )
                onEvent(PaymentMethodEvent.GetPaymentMethods)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error deleting: ${e.message}")
            }
        }
    }

    fun getPaymentMethods(){
        viewModelScope.launch {
            repository.getPaymentMethods().collectLatest { getting ->
                when (getting){
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }

                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                PaymentMethods = getting.data ?: emptyList(),
                                isLoading = false
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun isValidPaymentMethodName(name: String): String? {
        if (name.isBlank()) {
            return "The name cannot be empty."
        }

        val regex = Regex("^[a-zA-Z0-9\\sáéíóúÁÉÍÓÚñÑüÜ.-]*$")
        if (!regex.matches(name)) {
            return "The name contains illegal characters."
        }

        return null
    }
}