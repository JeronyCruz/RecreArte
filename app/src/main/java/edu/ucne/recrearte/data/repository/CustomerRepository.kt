package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.local.dao.CustomerDao
import edu.ucne.recrearte.data.local.entities.CustomersEntity
import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.CustomersDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CustomerRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val customerDao: CustomerDao
){
    fun getCustomers(): Flow<Resource<List<CustomersDto>>> = flow {
        try {
            println("[DEBUG] Intentando obtener Customers...")
            emit(Resource.Loading())

            val remoteCustomers = remoteDataSource.getCustomers()
            println("[DEBUG] API devolvió ${remoteCustomers.size} customers")

            val customersEntities = remoteCustomers.map { it.toEntity() }
            customerDao.save(customersEntities)

            emit(Resource.Success(remoteCustomers))

        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                401 -> "Tu sesión ha expirado. Por favor inicia sesión nuevamente"
                403 -> "No tienes permiso para acceder a este recurso"
                else -> "Error del servidor (${e.code()})"
            }
            println("[DEBUG] Error HTTP: $errorMsg")

            val localCustomers = customerDao.getAll().map { it.toDto() }
            if (localCustomers.isNotEmpty()) {
                println("[DEBUG] Usando datos locales (${localCustomers.size} customers)")
                emit(Resource.Success(localCustomers))
            } else {
                emit(Resource.Error(errorMsg))
            }

        } catch (e: IOException) {
            val errorMsg = "Error de conexión: ${e.message}"
            println("[DEBUG] Network Error: $errorMsg")

            val localCustomers = customerDao.getAll().map { it.toDto() }
            if (localCustomers.isNotEmpty()) {
                println("[DEBUG] Usando datos locales (${localCustomers.size} customers)")
                emit(Resource.Success(localCustomers))
            } else {
                emit(Resource.Error(errorMsg))
            }

        } catch (e: Exception) {
            val errorMsg = "Error inesperado: ${e.message}"
            println("[DEBUG] Unexpected Error: $errorMsg")

            val localCustomers = customerDao.getAll().map { it.toDto() }
            if (localCustomers.isNotEmpty()) {
                println("[DEBUG] Usando datos locales (${localCustomers.size} customers)")
                emit(Resource.Success(localCustomers))
            } else {
                emit(Resource.Error(errorMsg))
            }
        }
    }

    fun getCustomerById(id: Int): Flow<Resource<CustomersDto>> = flow {
        try {
            println("[DEBUG] Intentando obtener customer con ID $id...")
            emit(Resource.Loading())

            val remoteCustomer = remoteDataSource.getCustomerById(id)
            println("[DEBUG] API devolvió: ${remoteCustomer.firstName} ${remoteCustomer.lastName}")

            val customerEntity = remoteCustomer.toEntity()
            customerDao.saveOne(customerEntity)


            emit(Resource.Success(remoteCustomer))

        } catch (e: HttpException) {
            val errorMsg = "Error HTTP: ${e.message()}"
            println("[DEBUG] $errorMsg")


            val localCustomer = customerDao.find(id)?.toDto()
            if (localCustomer != null) {
                println("[DEBUG] Usando datos locales: ${localCustomer.firstName} ${localCustomer.lastName}")
                emit(Resource.Success(localCustomer))
            } else {
                emit(Resource.Error(errorMsg))
            }

        } catch (e: IOException) {
            val errorMsg = "Error de conexión: ${e.message}"
            println("[DEBUG] $errorMsg")


            val localCustomer = customerDao.find(id)?.toDto()
            if (localCustomer != null) {
                println("[DEBUG] Usando datos locales: ${localCustomer.firstName} ${localCustomer.lastName}")
                emit(Resource.Success(localCustomer))
            } else {
                emit(Resource.Error(errorMsg))
            }

        } catch (e: Exception) {
            val errorMsg = "Error inesperado: ${e.message}"
            println("[DEBUG] $errorMsg")


            val localCustomer = customerDao.find(id)?.toDto()
            if (localCustomer != null) {
                println("[DEBUG] Usando datos locales: ${localCustomer.firstName} ${localCustomer.lastName}")
                emit(Resource.Success(localCustomer))
            } else {
                emit(Resource.Error(errorMsg))
            }
        }
    }

    // Conversión de DTO a Entity
    private fun CustomersDto.toEntity() = CustomersEntity(
        customerId = this.customerId,
        address = this.address,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        password = this.password,
        userName = this.userName,
        phoneNumber = this.phoneNumber,
        documentNumber = this.documentNumber,
        updateAt = this.updateAt,
        roleId = this.roleId,
        description = this.description,
        token = this.token
    )

    // Conversión de Entity a DTO
    private fun CustomersEntity.toDto() = CustomersDto(
        customerId = this.customerId,
        address = this.address,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        password = this.password,
        userName = this.userName,
        phoneNumber = this.phoneNumber,
        documentNumber = this.documentNumber,
        updateAt = this.updateAt,
        roleId = this.roleId,
        description = this.description,
        token = this.token
    )

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

    suspend fun changePassword(userId: Int, currentPassword: String, newPassword: String, confirmPassword: String): Boolean {
        return remoteDataSource.changePassword(userId, currentPassword, newPassword, confirmPassword)
    }

    suspend fun updateCustomer(id: Int, customersDto: CustomersDto): Resource<Unit> {
        return try {
            remoteDataSource.updateCustomer(id, customersDto)
            Resource.Success(Unit) // Indicamos éxito sin datos
        } catch (e: HttpException) {
            Resource.Error("HTTP error: ${e.message()}")
        } catch (e: Exception) {
            Resource.Error("Update failed: ${e.message}")
        }
    }
    suspend fun deleteCustomer(id: Int) = remoteDataSource.deleteCustomer(id)
}