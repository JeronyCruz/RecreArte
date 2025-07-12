package edu.ucne.recrearte.util

interface TokenManager {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
}