package edu.ucne.recrearte.presentation.bills

import edu.ucne.recrearte.data.remote.dto.BillsDetailsDto

sealed interface BillEvent {
    data object LoadCheckout : BillEvent
    data class SetPaymentMethod(val id: Int, val name: String) : BillEvent
    data object CreateBill : BillEvent
    data object ResetState : BillEvent
    data class ValidateCreditCard(val cardNumber: String) : BillEvent
}