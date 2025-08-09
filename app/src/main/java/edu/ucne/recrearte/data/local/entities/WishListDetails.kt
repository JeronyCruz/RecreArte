package edu.ucne.recrearte.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "WishListDetails",
    primaryKeys = ["wishListId", "workId"],
    foreignKeys = [
        ForeignKey(
            entity = WishListsEntity::class,
            parentColumns = ["wishListId"],
            childColumns = ["wishListId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WorksEntity::class,
            parentColumns = ["workId"],
            childColumns = ["workId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WishListDetailsEntity(
    val wishListId: Int,
    val workId: Int
)
