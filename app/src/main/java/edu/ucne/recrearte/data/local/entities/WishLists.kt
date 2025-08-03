package edu.ucne.recrearte.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "WishLists")
data class WishListsEntity(
    @PrimaryKey
    val wishListId: Int,
    val customerId: Int,
    val userName: String?
)