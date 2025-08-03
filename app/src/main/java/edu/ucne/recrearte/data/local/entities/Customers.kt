package edu.ucne.recrearte.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "Customers")
data class CustomersEntity(
    @PrimaryKey
    val customerId : Int? = null,
    val address : String? = "",
    val firstName : String? = "",
    val lastName : String? = "",
    val email : String? = "",
    val password : String? = "",
    val userName : String? = "",
    val phoneNumber : String? = "",
    val documentNumber : String? = "",
    val updateAt : Date = Date(),
    val roleId : Int = 0,
    val description : String? = "",
    val token: String? = ""
)