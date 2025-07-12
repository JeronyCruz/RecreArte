package edu.ucne.recrearte.util

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManagement @Inject constructor(
    private val context: Context
) : TokenManager {
    private val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    override fun saveToken(token: String) {
        sharedPref.edit().apply {
            putString("jwt_token", token)
            apply() // Usar apply() para operación asíncrona
        }
        println("🔐 [DEBUG] Token guardado: ${token.take(10)}...")
    }

    override fun getToken(): String? {
        return sharedPref.getString("jwt_token", null).also {
            println("🔍 [DEBUG] Token recuperado: ${it?.take(10)}...")
        }
    }

    override fun clearToken() {
        sharedPref.edit().remove("jwt_token").apply()
        println("🧹 [DEBUG] Token eliminado")
    }
}