package edu.ucne.recrearte.data.remote.dto

data class BillsDetailsDto(
    val billDetailsId: Int,
    val billId: Int,
    val workId: Int,
    val subtotal: Double
)