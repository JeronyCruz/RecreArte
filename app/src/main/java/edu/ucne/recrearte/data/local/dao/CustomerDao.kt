package edu.ucne.recrearte.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import edu.ucne.recrearte.data.local.entities.CustomersEntity


@Dao
interface CustomerDao {
    @Upsert()
    suspend fun save(customer: List<CustomersEntity>)
    @Query(
        """
        SELECT * 
        FROM Customers 
        WHERE customerId=:id  
        LIMIT 1
        """
    )
    suspend fun find(id: Int): CustomersEntity?
    @Delete
    suspend fun delete(customer: CustomersEntity)
    @Query("SELECT * FROM Customers")
    suspend fun getAll(): List<CustomersEntity>
}