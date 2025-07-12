package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.PaymentMethodDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class PaymentMethodRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
){
    fun getPaymentMethods(): Flow<Resource<List<PaymentMethodDto>>> = flow {
        try {
            println("📡 [DEBUG] Intentando obtener payment methods...")
            emit(Resource.Loading())

            val methods = remoteDataSource.getPaymentMethod()
            println("✅ [DEBUG] API devolvió ${methods.size} payment methods")

            emit(Resource.Success(methods))
        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                401 -> "Tu sesión ha expirado. Por favor inicia sesión nuevamente"
                403 -> "No tienes permiso para acceder a este recurso"
                else -> "Error del servidor (${e.code()})"
            }
            println("🔴 [DEBUG] Error HTTP: $errorMsg")
            emit(Resource.Error(errorMsg))
        } catch (e: IOException) {
            val errorMsg = "Error de conexión: ${e.message}"
            println("🔴 [DEBUG] Network Error: $errorMsg")
            emit(Resource.Error(errorMsg))
        } catch (e: Exception) {
            val errorMsg = "Error inesperado: ${e.message}"
            println("🔴 [DEBUG] Unexpected Error: $errorMsg")
            emit(Resource.Error(errorMsg))
        }
    }

    suspend fun createPaymentMethod(paymentMethodDto: PaymentMethodDto) = remoteDataSource.createPaymentMethod(paymentMethodDto)

    suspend fun updatePaymentMethod(id: Int, paymentMethodDto: PaymentMethodDto) = remoteDataSource.updatePaymentMethod(id, paymentMethodDto)

    suspend fun deletePaymentMethod(id: Int) = remoteDataSource.deletePaymentMethod(id)
}