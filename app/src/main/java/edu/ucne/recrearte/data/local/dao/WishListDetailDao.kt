package edu.ucne.recrearte.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import edu.ucne.recrearte.data.local.entities.WishListDetailsEntity

@Dao
interface WishListDetailsDao {
    @Upsert
    suspend fun save(details: List<WishListDetailsEntity>)

    @Upsert
    suspend fun saveOne(detail: WishListDetailsEntity)

    @Query("DELETE FROM WishListDetails WHERE wishListId = :wishListId AND workId = :workId")
    suspend fun delete(wishListId: Int, workId: Int)

    @Query("SELECT COUNT(*) FROM WishListDetails WHERE wishListId = :wishListId AND workId = :workId")
    suspend fun exists(wishListId: Int, workId: Int): Boolean

    @Query("DELETE FROM WishListDetails WHERE wishListId = :wishListId")
    suspend fun deleteByWishListId(wishListId: Int)
}