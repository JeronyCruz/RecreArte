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
        WHERE workId=:id  AND statusId = 1 
        LIMIT 1
        """
    )
    suspend fun find(id: Int): WorksEntity?
    @Delete
    suspend fun delete(work: WorksEntity)
    @Query("SELECT * FROM Works WHERE statusId = 1")
    suspend fun getAll(): List<WorksEntity>
    @Query("SELECT * FROM Works WHERE artistId=:artistId AND statusId = 1")
    suspend fun getByArtist(artistId: Int): List<WorksEntity>
    @Query("SELECT * FROM Works WHERE techniqueId=:techniqueId AND statusId = 1")
    suspend fun getByTechnique(techniqueId: Int): List<WorksEntity>
    @Query("""
        SELECT w.*
FROM Works w
INNER JOIN (
    SELECT workId, COUNT(*) as likeCount 
    FROM Likes 
    GROUP BY workId
    HAVING COUNT(*) > 0 
) l ON w.workId = l.workId
WHERE w.statusId = 1
ORDER BY l.likeCount DESC
LIMIT 5
    """)
    suspend fun getTop5(): List<WorksEntity>
}