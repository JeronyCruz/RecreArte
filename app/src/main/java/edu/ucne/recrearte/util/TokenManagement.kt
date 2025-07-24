package edu.ucne.recrearte.util

import android.content.Context
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Base64
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.io.encoding.ExperimentalEncodingApi

@Singleton
class TokenManagement @Inject constructor(
    private val context: Context
) : TokenManager {
    private val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val roleIdFlow = MutableStateFlow<Int?>(sharedPref.getInt("role_id", -1).takeIf { it != -1 })

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

    override fun saveRoleId(roleId: Int) {
        try {
            sharedPref.edit().putInt("role_id", roleId).apply()
            println("💾 [DEBUG] RoleId $roleId guardado exitosamente")
        } catch (e: Exception) {
            println("❌ [ERROR] Error guardando roleId: ${e.message}")
        }
    }


    override fun getRoleId(): Int? {
        // Primero intenta obtener de SharedPreferences
        val savedRoleId = sharedPref.getInt("role_id", -1).takeIf { it != -1 }

        // Si no está en SharedPreferences, intenta extraer del token
        return savedRoleId ?: getRoleIdFromToken()?.also { roleId ->
            // Guarda en SharedPreferences para futuras consultas
            saveRoleId(roleId)
        }
    }

    override fun getRoleIdFromToken(): Int? {
        val token = getToken() ?: return null
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val payload = String(
                Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING),
                Charsets.UTF_8
            )

            val jsonObject = JSONObject(payload)

            // Busca el roleId en diferentes claims posibles
            when {
                jsonObject.has("role") -> jsonObject.getInt("role") // Versión corta
                jsonObject.has("RoleId") -> jsonObject.getInt("RoleId") // CamelCase
                jsonObject.has("http://schemas.microsoft.com/ws/2008/06/identity/claims/role") ->
                    jsonObject.getInt("http://schemas.microsoft.com/ws/2008/06/identity/claims/role")
                else -> null
            }.also { roleId ->
                if (roleId == null) {
                    println("⚠️ No se encontró roleId en claims. Claims disponibles: ${jsonObject.keys().asSequence().toList()}")
                } else {
                    println("✅ RoleId encontrado en token: $roleId")
                }
            }
        } catch (e: Exception) {
            println("❌ Error extrayendo roleId del token: ${e.message}")
            null
        }
    }




}