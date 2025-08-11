package edu.ucne.recrearte.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import edu.ucne.recrearte.data.local.entities.ArtistsEntity


@Dao
interface ArtistDao {
    @Upsert()
    suspend fun save(artists: List<ArtistsEntity>)
    @Upsert()
    suspend fun saveOne(artists: ArtistsEntity)
    @Query(
        """
        SELECT * 
        FROM Artists 
        WHERE artistId=:id  
        LIMIT 1
        """
    )
    suspend fun find(id: Int): ArtistsEntity?
    @Delete
    suspend fun delete(artist: ArtistsEntity)
    @Query("SELECT * FROM Artists")
    suspend fun getAll(): List<ArtistsEntity>
}