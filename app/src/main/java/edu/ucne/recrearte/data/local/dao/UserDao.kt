package edu.ucne.recrearte.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import edu.ucne.recrearte.data.local.entities.UsersEntity

@Dao
interface UserDao {
    @Upsert()
    suspend fun save(users: List<UsersEntity>)
    @Upsert()
    suspend fun saveOne(users: UsersEntity)
    @Query(
        """
        SELECT * 
        FROM Users 
        WHERE userId=:id  
        LIMIT 1
        """
    )
    suspend fun find(id: Int): UsersEntity?
    @Delete
    suspend fun delete(user: UsersEntity)
    @Query("SELECT * FROM Users")
    suspend fun getAll(): List<UsersEntity>
}