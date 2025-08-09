package edu.ucne.recrearte.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import edu.ucne.recrearte.data.local.entities.LikesEntity
import edu.ucne.recrearte.data.local.entities.WorksEntity

@Dao
interface LikeDao {
    @Upsert()
    suspend fun save(like: List<LikesEntity>)
    @Query(
        """
        SELECT * 
        FROM Likes 
        WHERE likeId=:id  
        LIMIT 1
        """
    )
    suspend fun find(id: Int): LikesEntity?
    @Delete
    suspend fun delete(like: LikesEntity)
    @Query("SELECT * FROM Likes")
    suspend fun getAll(): List<LikesEntity>
    @Query("""
        SELECT DISTINCT w.* 
        FROM Works w
        INNER JOIN Likes l ON w.workId = l.workId
        WHERE l.customerId = :customerId AND w.statusId = 1
    """)
    suspend fun getWorksLikedByCustomer(customerId: Int): List<WorksEntity>

    @Query("SELECT * FROM Likes WHERE customerId = :customerId AND workId = :workId LIMIT 1")
    suspend fun getLikeByCustomerAndWork(customerId: Int, workId: Int):LikesEntity?

    @Query("DELETE FROM Likes WHERE customerId = :customerId AND workId = :workId")
    suspend fun deleteByCustomerAndWork(customerId: Int, workId: Int)

}