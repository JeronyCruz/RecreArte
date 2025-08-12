package edu.ucne.recrearte.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import edu.ucne.recrearte.data.local.entities.PaymentMethodsEntity

@Dao
interface PaymentMethodDao {
    @Upsert()
    suspend fun save(paymentMethods: List<PaymentMethodsEntity>)
    @Upsert()
    suspend fun saveOne(paymentMethods: PaymentMethodsEntity)
    @Query(
        """
        SELECT * 
        FROM PaymentMethods 
        WHERE paymentMethodId=:id  
        LIMIT 1
        """
    )
    suspend fun find(id: Int): PaymentMethodsEntity?
    @Delete
    suspend fun delete(paymentMethod: PaymentMethodsEntity)
    @Query("DELETE FROM PaymentMethods WHERE paymentMethodId = :id")
    suspend fun deleteById(id: Int)
    @Query("SELECT * FROM PaymentMethods")
    suspend fun getAll(): List<PaymentMethodsEntity>
}