package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.Resource
import edu.ucne.recrearte.data.remote.dto.ChangePasswordDto
import edu.ucne.recrearte.data.remote.dto.UsersDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
){
    fun getUsers(): Flow<Resource<List<UsersDto>>> = flow {
        try {
            println("[DEBUG] Intentando obtener User...")
            emit(Resource.Loading())

            val methods = remoteDataSource.getUsers()
            println("[DEBUG] API devolvio ${methods.size} Users")

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

    suspend fun getUserById(id: Int): Resource<UsersDto> {
        return try {
            val user = remoteDataSource.getUserById(id)
            Resource.Success(user)
        } catch (e: HttpException) {
            Resource.Error("Internet error: ${e.message()}")
        } catch (e: Exception) {
            Resource.Error("Unknown error: ${e.message}")
        }
    }

    suspend fun changePassword(
        userId: Int,
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Resource<Boolean> {
        return try {
            val success = remoteDataSource.changePassword(
                userId = userId,
                currentPassword = currentPassword,
                newPassword = newPassword,
                confirmPassword = confirmPassword
            )

            if (success) {
                Resource.Success(true)
            } else {
                Resource.Error("The password entered is not the current one!")
            }
        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                400 -> "Datos inválidos"
                401 -> "Contraseña actual incorrecta"
                403 -> "No autorizado"
                500 -> "Error del servidor"
                else -> "Error desconocido (${e.code()})"
            }
            Resource.Error(errorMsg)
        } catch (e: IOException) {
            Resource.Error("Connection error: ${e.message ?: "Check your internet connection"}")
        } catch (e: Exception) {
            Resource.Error("Unexpected error: ${e.message ?: "An error occurred"}")
        }
    }
}