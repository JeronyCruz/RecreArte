package edu.ucne.recrearte.presentation.signUp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import edu.ucne.recrearte.presentation.navigation.Screen

@Composable
fun SignUpScreen(
    navController: NavHostController,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            // Navegar a Home y limpiar el back stack
            navController.navigate(Screen.RecreArteScren) {
                // Esto llevará al usuario directamente a Home sin poder volver atrás
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    SignUpContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = {
            if (uiState.currentStep == 2) {
                viewModel.onEvent(SignUpEvent.PreviousStep)
            } else {
                navController.popBackStack()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpContent(
    uiState: SignUpUiState,
    onEvent: (SignUpEvent) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            if (uiState.currentStep == 2) {
                TopAppBar(
                    title = { Text("Sign Up") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.currentStep) {
                1 -> SignUpStep1(uiState, onEvent)
                2 -> SignUpStep2(uiState, onEvent)
            }
        }
    }
}