package edu.ucne.recrearte.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "Likes")
data class LikesEntity(
    @PrimaryKey
    val likeId: Int? = null,
    val dateLiked: Date = Date(),
    val customerId: Int = 0,
    val workId: Int = 0
)