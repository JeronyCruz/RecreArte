package edu.ucne.recrearte.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import edu.ucne.recrearte.data.local.entities.RolesEntity

@Dao
interface RoleDao {
    @Upsert()
    suspend fun save(roles: List<RolesEntity>)
    @Upsert()
    suspend fun saveOne(roles: RolesEntity)
    @Query(
        """
        SELECT * 
        FROM Roles 
        WHERE roleId=:id  
        LIMIT 1
        """
    )
    suspend fun find(id: Int): RolesEntity?
    @Delete
    suspend fun delete(role: RolesEntity)
    @Query("SELECT * FROM Roles")
    suspend fun getAll(): List<RolesEntity>
}