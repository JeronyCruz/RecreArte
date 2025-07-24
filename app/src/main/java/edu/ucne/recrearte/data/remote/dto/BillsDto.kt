package edu.ucne.recrearte.data.remote.dto

import java.util.Date

data class BillsDto(
    val billId: Int?,
    val date: String,
    val total: Double,
    val taxes: Double,
    val discount: Double,
    val customerId: Int,
    val customerName: String,
    val paymentMethodId: Int,
    val paymentMethodName: String,
    val stateId: Int,
    val stateName: String,
    val billDetails: List<BillsDetailsDto>
)