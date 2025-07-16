package edu.ucne.recrearte

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import edu.ucne.recrearte.presentation.navigation.HomeNavHost
import edu.ucne.recrearte.presentation.navigation.MainAppScreen
import edu.ucne.recrearte.presentation.navigation.Screen
import edu.ucne.recrearte.ui.theme.RecreArteTheme
import edu.ucne.recrearte.util.TokenManager
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Verificar token antes de mostrar la UI
        val token = tokenManager.getToken()
        println("🔑 Token al iniciar: ${token?.take(5)}...") // Debug

        setContent {
            RecreArteTheme {
                val navController = rememberNavController()

                // Efecto para manejar la navegación inicial
                LaunchedEffect(token) {
                    val startDestination = if (token != null) {
                        Screen.Home // Si hay token, ir al home
                    } else {
                        Screen.LoginScreen // Si no hay token, ir a login
                    }

                    navController.navigate(startDestination) {
                        popUpTo(0) // Limpiar back stack
                    }
                }

                MainAppScreen(navController = navController)

//                Scaffold(
//                    modifier = Modifier.fillMaxSize()
//                ) { padding ->
//                    HomeNavHost(
//                        navHostController = navController,
//                        modifier = Modifier.padding(padding)
//                    )
//                }
            }
        }
    }
}

