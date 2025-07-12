package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.PaymentMethodsDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

class PaymentMethodRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
){
    fun getPaymentMethods(): Flow<Resource<List<PaymentMethodsDto>>> = flow {
        try {
            emit(Resource.Loading())
            val paymentMethod = remoteDataSource.getPaymentMethod()
            emit(Resource.Success(paymentMethod))
        }catch (e: HttpException){
            emit(Resource.Error("Internet error: ${e.message()}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
    }

    suspend fun createPaymentMethod(paymentMethodsDto: PaymentMethodsDto) = remoteDataSource.createPaymentMethod(paymentMethodsDto)

    suspend fun updatePaymentMethod(id: Int, paymentMethodsDto: PaymentMethodsDto) = remoteDataSource.updatePaymentMethod(id, paymentMethodsDto)

    suspend fun deletePaymentMethod(id: Int) = remoteDataSource.deletePaymentMethod(id)
}