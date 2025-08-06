package edu.ucne.recrearte.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import edu.ucne.recrearte.data.local.entities.WishListsEntity
import edu.ucne.recrearte.data.local.entities.WorksEntity


@Dao
interface WishListDao {
    @Upsert()
    suspend fun save(wishLists: List<WishListsEntity>)
    @Upsert()
    suspend fun saveOne(wishList: WishListsEntity)
    @Query(
        """
        SELECT * 
        FROM WishLists 
        WHERE wishListId=:id  
        LIMIT 1
        """
    )
    suspend fun find(id: Int): WishListsEntity?
    @Delete
    suspend fun delete(wishList: WishListsEntity)
    @Query("SELECT * FROM WishLists")
    suspend fun getAll(): List<WishListsEntity>
    @Query("""
        SELECT w.* 
    FROM Works w
    INNER JOIN WishListDetails wld ON w.WorkId = wld.WorkId
    INNER JOIN WishLists wl ON wl.WishListId = wld.WishListId
    WHERE wl.CustomerId = :customerId
    AND w.StatusId = 1
    """)
    suspend fun getByCustomer(customerId: Int): List<WorksEntity>
    @Query("SELECT * FROM WishLists WHERE customerId = :customerId LIMIT 1")
    suspend fun findByCustomerId(customerId: Int): WishListsEntity?
}