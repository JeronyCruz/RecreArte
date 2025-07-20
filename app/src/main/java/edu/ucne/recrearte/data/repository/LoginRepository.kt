package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.data.remote.RemoteDataSource
import edu.ucne.recrearte.data.remote.dto.LoginRequestDto
import edu.ucne.recrearte.data.remote.dto.LoginResponseDto
import edu.ucne.recrearte.util.TokenManager
import javax.inject.Inject

class LoginRepository  @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val tokenManager: TokenManager
){
    suspend fun loginUser(loginRequest: LoginRequestDto): LoginResponseDto {
        return remoteDataSource.loginUser(loginRequest)
    }

    suspend fun logout(): Boolean {
        return try {
            remoteDataSource.logoutUser()
            tokenManager.clearToken()
            true
        } catch (e: Exception) {
            tokenManager.clearToken()
            false
        }
    }
}