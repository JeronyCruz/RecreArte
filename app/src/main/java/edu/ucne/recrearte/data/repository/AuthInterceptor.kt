package edu.ucne.recrearte.data.repository

import edu.ucne.recrearte.util.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Debug: Verificar la URL
        println("🌐 [DEBUG] Llamando a: ${request.url}")

        // Obtener el token actual
        val token = tokenManager.getToken()
        println("🔑 [DEBUG] Token disponible: ${token?.take(10)}...")

        // Si no hay token o es la ruta de login, continuar sin modificar
        if (token == null || request.url.encodedPath.contains("/api/Login")) {
            println("⚠️ [DEBUG] No se añade header Authorization")
            return chain.proceed(request)
        }

        // Añadir header de autorización
        val authenticatedRequest = request.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        println("✅ [DEBUG] Header Authorization añadido")

        val response = chain.proceed(authenticatedRequest)

        // Manejar respuesta no autorizada
        if (response.code == 401) {
            println("🔴 [DEBUG] Error 401 - Token inválido/expirado")
            tokenManager.clearToken()
        }

        return response
    }
}