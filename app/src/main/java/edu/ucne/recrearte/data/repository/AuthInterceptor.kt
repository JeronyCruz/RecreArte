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

        // Rutas que no requieren token o manejo especial
        when {
            // Rutas públicas que no necesitan token
            request.url.encodedPath.contains("/api/Login") -> {
                println("⚠️ [DEBUG] Ruta pública, no se añade header Authorization")
                return chain.proceed(request)
            }
            // Ruta de cambio de contraseña - necesita token pero no debe borrarlo si falla
            request.url.encodedPath.contains("/api/Users/change-password") -> {
                println("🔄 [DEBUG] Ruta de cambio de contraseña - manejo especial")
                if (token != null) {
                    val authRequest = request.newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                    return chain.proceed(authRequest)
                }
                return chain.proceed(request)
            }
        }

        // Para todas las demás rutas que requieren autenticación
        if (token == null) {
            println("🔴 [DEBUG] Token nulo para ruta protegida")
            return chain.proceed(request) // Esto fallará con 401 en el backend
        }

        // Añadir header de autorización
        val authenticatedRequest = request.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        println("✅ [DEBUG] Header Authorization añadido")

        val response = chain.proceed(authenticatedRequest)

        // Manejar respuesta no autorizada (excepto para cambio de contraseña)
        if (response.code == 401 && !request.url.encodedPath.contains("/api/Users/change-password")) {
            println("🔴 [DEBUG] Error 401 - Token inválido/expirado (limpiando token)")
            tokenManager.clearToken()
        }

        return response
    }
}