package edu.ucne.recrearte.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PaymentMethods")
data class PaymentMethodsEntity(
    @PrimaryKey
    val paymentMethodId: Int? = null,
    val paymentMethodName: String = ""
)