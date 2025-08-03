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
    @Upsert()
    suspend fun saveOne(works: WorksEntity)
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
    @Query("SELECT * FROM Works WHERE artistId=:artistId")
    suspend fun getByArtist(artistId: Int): List<WorksEntity>
    @Query("SELECT * FROM Works WHERE techniqueId=:techniqueId")
    suspend fun getByTechnique(techniqueId: Int): List<WorksEntity>
//    @Query("""
//        SELECT w.*, COUNT(l.workId) as likeCount
//        FROM Works w
//        LEFT JOIN Likes l ON w.workId = l.workId
//        WHERE w.statusId = 1
//        GROUP BY w.workId
//        ORDER BY likeCount DESC
//        LIMIT 5
//    """)
//    suspend fun getTop5(): List<WorksEntity>
}