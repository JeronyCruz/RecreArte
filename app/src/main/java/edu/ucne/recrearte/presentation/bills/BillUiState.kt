package edu.ucne.recrearte.presentation.bills

import edu.ucne.recrearte.data.remote.dto.BillsDto

data class BillUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val createdBill: BillsDto? = null,
    val errorMessage: String? = null,
    val isCardValid: Boolean = false,
    val cardValidationMessage: String? = null
)