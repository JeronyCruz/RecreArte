package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.PaymentMethodDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

class PaymentMethodRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
){
    fun getPaymentMethods(): Flow<Resource<List<PaymentMethodDto>>> = flow {
        try {
            emit(Resource.Loading())
            val paymentMethod = remoteDataSource.getPaymentMethod()
            emit(Resource.Success(paymentMethod))
        }catch (e: HttpException){
            emit(Resource.Error("Error de internet: ${e.message()}"))
        } catch (e: Exception) {
            emit(Resource.Error("Error desconocido: ${e.message}"))
        }
    }

    suspend fun createPaymentMethod(paymentMethodDto: PaymentMethodDto) = remoteDataSource.createPaymentMethod(paymentMethodDto)

    suspend fun updatePaymentMethod(id: Int, paymentMethodDto: PaymentMethodDto) = remoteDataSource.updatePaymentMethod(id, paymentMethodDto)

    suspend fun deletePaymentMethod(id: Int) = remoteDataSource.deletePaymentMethod(id)
}