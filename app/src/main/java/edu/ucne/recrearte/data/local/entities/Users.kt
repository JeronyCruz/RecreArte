package edu.ucne.recrearte.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "Users")
data class UsersEntity(
    @PrimaryKey
    val userId : Int? = null,
    val firstName : String? = "",
    val lastName : String? = "",
    val email : String? = "",
    val password : String? = "",
    val userName : String? = "",
    val phoneNumber : String? = "",
    val documentNumber : String? = "",
    val updateAt : Date = Date(),
    val roleId : Int = 0,
    val description : String? = ""
)