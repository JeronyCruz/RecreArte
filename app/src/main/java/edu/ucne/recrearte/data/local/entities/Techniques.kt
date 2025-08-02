package edu.ucne.recrearte.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Techniques")
data class TechniquesEntity(
    @PrimaryKey
    val techniqueId: Int? = null,
    val techniqueName: String = ""
)