package edu.ucne.recrearte.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import edu.ucne.recrearte.data.local.entities.ArtistsListEntity


@Dao
interface ArtistListDao {
    @Upsert()
    suspend fun save(artists: List<ArtistsListEntity>)
    @Upsert()
    suspend fun saveOne(artists: ArtistsListEntity)
    @Query(
        """
        SELECT * 
        FROM ArtistsList 
        WHERE artistId=:id  
        LIMIT 1
        """
    )
    suspend fun find(id: Int): ArtistsListEntity?
    @Delete
    suspend fun delete(artist: ArtistsListEntity)
    @Query("SELECT * FROM ArtistsList")
    suspend fun getAll(): List<ArtistsListEntity>
}