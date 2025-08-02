package edu.ucne.recrearte.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import edu.ucne.recrearte.data.local.entities.WorksEntity

@Dao
interface WorkDao{
    @Upsert()
    suspend fun save(works: List<WorksEntity>)
    @Query(
        """
        SELECT * 
        FROM Works 
        WHERE workId=:id  
        LIMIT 1
        """
    )
    suspend fun find(id: Int): WorksEntity?
    @Delete
    suspend fun delete(work: WorksEntity)
    @Query("SELECT * FROM Works")
    suspend fun getAll(): List<WorksEntity>
}