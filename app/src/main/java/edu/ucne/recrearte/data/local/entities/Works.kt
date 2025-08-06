package edu.ucne.recrearte.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Works")
data class WorksEntity(
    @PrimaryKey
    val workId: Int? = null,
    val title: String = "",
    val dimension: String = "",
    val techniqueId: Int = 0,
    val artistId: Int = 0,
    val statusId: Int = 0,
    val price: Double = 0.0,
    val description: String = "",
    val imageUrl: String = ""
)