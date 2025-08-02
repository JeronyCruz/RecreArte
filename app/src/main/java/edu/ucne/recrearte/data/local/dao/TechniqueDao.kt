package edu.ucne.recrearte.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import edu.ucne.recrearte.data.local.entities.TechniquesEntity

@Dao
interface TechniqueDao {
    @Upsert()
    suspend fun save(techniques: List<TechniquesEntity>)
    @Query(
        """
        SELECT * 
        FROM Techniques 
        WHERE techniqueId=:id  
        LIMIT 1
        """
    )
    suspend fun find(id: Int): TechniquesEntity?
    @Delete
    suspend fun delete(technique: TechniquesEntity)
    @Query("SELECT * FROM Techniques")
    suspend fun getAll(): List<TechniquesEntity>
}