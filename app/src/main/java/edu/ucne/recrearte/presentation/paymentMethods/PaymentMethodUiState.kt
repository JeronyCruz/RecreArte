package edu.ucne.recrearte.presentation.paymentMethods

import edu.ucne.recrearte.data.remote.dto.PaymentMethodsDto

data class PaymentMethodUiState(
    val paymentMethodId: Int? = null,
    val paymentMethodName: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val successMessage: String? = null,
    val errorName: String? = null,
    val PaymentMethods: List<PaymentMethodsDto> = emptyList()
)