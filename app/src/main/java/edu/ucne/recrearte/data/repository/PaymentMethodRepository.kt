package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.local.dao.PaymentMethodDao
import edu.ucne.recrearte.data.local.entities.PaymentMethodsEntity
import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.PaymentMethodsDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class PaymentMethodRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val paymentMethodDao: PaymentMethodDao
){
    fun getPaymentMethods(): Flow<Resource<List<PaymentMethodsDto>>> = flow {
        try {
            println("[DEBUG] Intentando obtener payment methods...")
            emit(Resource.Loading())


            val remoteMethods = remoteDataSource.getPaymentMethod()
            println("[DEBUG] API devolvió ${remoteMethods.size} payment methods")

            // 2. Convertir y guardar en base de datos local
            val methodsEntities = remoteMethods.map { it.toEntity() }
            paymentMethodDao.save(methodsEntities)


            emit(Resource.Success(remoteMethods))

        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                401 -> "Tu sesión ha expirado. Por favor inicia sesión nuevamente"
                403 -> "No tienes permiso para acceder a este recurso"
                else -> "Error del servidor (${e.code()})"
            }
            println("[DEBUG] Error HTTP: $errorMsg")


            val localMethods = paymentMethodDao.getAll().map { it.toDto() }
            if (localMethods.isNotEmpty()) {
                println("[DEBUG] Usando datos locales (${localMethods.size} métodos)")
                emit(Resource.Success(localMethods))
            } else {
                emit(Resource.Error(errorMsg))
            }

        } catch (e: IOException) {
            val errorMsg = "Error de conexión: ${e.message}"
            println("[DEBUG] Network Error: $errorMsg")


            val localMethods = paymentMethodDao.getAll().map { it.toDto() }
            if (localMethods.isNotEmpty()) {
                println("[DEBUG] Usando datos locales (${localMethods.size} métodos)")
                emit(Resource.Success(localMethods))
            } else {
                emit(Resource.Error(errorMsg))
            }

        } catch (e: Exception) {
            val errorMsg = "Unexpected error: ${e.message}"
            println("[DEBUG] Unexpected Error: $errorMsg")


            val localMethods = paymentMethodDao.getAll().map { it.toDto() }
            if (localMethods.isNotEmpty()) {
                println("[DEBUG] Usando datos locales (${localMethods.size} métodos)")
                emit(Resource.Success(localMethods))
            } else {
                emit(Resource.Error(errorMsg))
            }
        }
    }

    fun getPaymentMethodById(id: Int): Flow<Resource<PaymentMethodsDto>> = flow {
        try {
            println("[DEBUG] Intentando obtener payment method con ID $id...")
            emit(Resource.Loading())


            val remoteMethod = remoteDataSource.getPaymentMethodById(id)
            println("[DEBUG] API devolvió: ${remoteMethod.paymentMethodName}")


            val methodEntity = remoteMethod.toEntity()
            paymentMethodDao.saveOne(methodEntity)


            emit(Resource.Success(remoteMethod))

        } catch (e: HttpException) {
            val errorMsg = "Error HTTP: ${e.message()}"
            println("[DEBUG] $errorMsg")


            val localMethod = paymentMethodDao.find(id)?.toDto()
            if (localMethod != null) {
                println("[DEBUG] Usando datos locales: ${localMethod.paymentMethodName}")
                emit(Resource.Success(localMethod))
            } else {
                emit(Resource.Error(errorMsg))
            }

        } catch (e: IOException) {
            val errorMsg = "Connection error: ${e.message}"
            println("[DEBUG] $errorMsg")


            val localMethod = paymentMethodDao.find(id)?.toDto()
            if (localMethod != null) {
                println("[DEBUG] Usando datos locales: ${localMethod.paymentMethodName}")
                emit(Resource.Success(localMethod))
            } else {
                emit(Resource.Error(errorMsg))
            }

        } catch (e: Exception) {
            val errorMsg = "Unexpected error: ${e.message}"
            println("[DEBUG] $errorMsg")


            val localMethod = paymentMethodDao.find(id)?.toDto()
            if (localMethod != null) {
                println("[DEBUG] Usando datos locales: ${localMethod.paymentMethodName}")
                emit(Resource.Success(localMethod))
            } else {
                emit(Resource.Error(errorMsg))
            }
        }
    }

    private fun PaymentMethodsDto.toEntity() = PaymentMethodsEntity(
        paymentMethodId = this.paymentMethodId,
        paymentMethodName = this.paymentMethodName
    )

    private fun PaymentMethodsEntity.toDto() = PaymentMethodsDto(
        paymentMethodId = this.paymentMethodId,
        paymentMethodName = this.paymentMethodName
    )

    suspend fun createPaymentMethod(paymentMethodsDto: PaymentMethodsDto) = remoteDataSource.createPaymentMethod(paymentMethodsDto)

    suspend fun updatePaymentMethod(id: Int, paymentMethodsDto: PaymentMethodsDto) = remoteDataSource.updatePaymentMethod(id, paymentMethodsDto)

    suspend fun deletePaymentMethod(id: Int): Boolean{
        return try {
            remoteDataSource.deletePaymentMethod(id)
            paymentMethodDao.deleteById(id)
            true
        }catch (e: Exception){
            false
        }
    }
}