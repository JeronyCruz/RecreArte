package edu.ucne.recrearte.util

import android.util.Base64
import org.json.JSONObject

fun TokenManager.getUserId(): Int? {
    val token = getToken() ?: return null
    return try {
        val parts = token.split(".")
        if (parts.size != 3) return null

        val payload = String(
            Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING),
            Charsets.UTF_8
        )

        val jsonObject = JSONObject(payload)

        // Busca en ambos posibles claims
        when {
            jsonObject.has("nameid") -> jsonObject.getInt("nameid") // Versión corta
            jsonObject.has("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier") ->
                jsonObject.getInt("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier")
            else -> null
        }.also { userId ->
            if (userId == null) {
                println("⚠️ No se encontró userID en claims: ${jsonObject.keys().asSequence().toList()}")
            } else {
                println("✅ UserID encontrado: $userId")
            }
        }
    } catch (e: Exception) {
        println("❌ Error parsing token: ${e.message}")
        null
    }
}