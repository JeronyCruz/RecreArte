package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.CustomersDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CustomerRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
){
    fun getCustomers(): Flow<Resource<List<CustomersDto>>> = flow {
        try {
            println("[DEBUG] Intentando obtener Customer...")
            emit(Resource.Loading())

            val methods = remoteDataSource.getCustomers()
            println("[DEBUG] API devolvio ${methods.size} Customer")

            emit(Resource.Success(methods))
        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                401 -> "Tu sesión ha expirado. Por favor inicia sesión nuevamente"
                403 -> "No tienes permiso para acceder a este recurso"
                else -> "Error del servidor (${e.code()})"
            }
            println("[DEBUG] Error HTTP: $errorMsg")
            emit(Resource.Error(errorMsg))
        } catch (e: IOException) {
            val errorMsg = "Error de conexión: ${e.message}"
            println("[DEBUG] Network Error: $errorMsg")
            emit(Resource.Error(errorMsg))
        } catch (e: Exception) {
            val errorMsg = "Error inesperado: ${e.message}"
            println("[DEBUG] Unexpected Error: $errorMsg")
            emit(Resource.Error(errorMsg))
        }catch (e: HttpException){
            emit(Resource.Error("Internet error: ${e.message()}"))
        }
    }

    suspend fun getCustomerById(id: Int): Resource<CustomersDto> {
        return try {
            val customer = remoteDataSource.getCustomerById(id)
            Resource.Success(customer)
        } catch (e: HttpException) {
            Resource.Error("Internet error: ${e.message()}")
        } catch (e: Exception) {
            Resource.Error("Unknown error: ${e.message}")
        }
    }

    suspend fun createCustomer(customersDto: CustomersDto): Resource<CustomersDto> { // Cambia el tipo de retorno
        return try {
            val response = remoteDataSource.createCustomer(customersDto)
            Resource.Success(response) // Devuelve el DTO completo con el token
        } catch (e: HttpException) {
            Resource.Error("Internet error: ${e.message()}")
        } catch (e: Exception) {
            Resource.Error("Unknown error: ${e.message}")
        }
    }

    suspend fun updateCustomer(id: Int, customersDto: CustomersDto) = remoteDataSource.updateCustomer(id, customersDto)

    suspend fun deleteCustomer(id: Int) = remoteDataSource.deleteCustomer(id)
}