package edu.ucne.recrearte.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Roles")
data class RolesEntity(
    @PrimaryKey
    val roleId: Int? = null,
    val description: String? = ""
)