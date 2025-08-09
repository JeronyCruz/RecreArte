package edu.ucne.recrearte.presentation.paymentMethods

sealed interface PaymentMethodEvent {
    data class PaymentMethodIdChange(val paymentMethodId: Int): PaymentMethodEvent
    data class NameChange(val name: String): PaymentMethodEvent

    data object GetPaymentMethods: PaymentMethodEvent
    data object CreatePaymentMethod: PaymentMethodEvent
    data object New: PaymentMethodEvent
    data class UpdatePaymentMethod(val id: Int): PaymentMethodEvent
    data class DeletePaymentMethod(val id: Int): PaymentMethodEvent
    data object ResetSuccessMessage: PaymentMethodEvent
    data object ClearErrorMessage: PaymentMethodEvent
}