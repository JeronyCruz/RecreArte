package edu.ucne.recrearte.presentation.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import edu.ucne.recrearte.presentation.Home.HomeScreen
import edu.ucne.recrearte.presentation.login.LoginScreen
import edu.ucne.recrearte.presentation.paymentMethods.PaymentMethodListScreen
import edu.ucne.recrearte.presentation.paymentMethods.PaymentMethodScreen
import edu.ucne.recrearte.presentation.profile.ProfileScreen
import edu.ucne.recrearte.presentation.signUp.SignUpScreen
import edu.ucne.recrearte.presentation.techniques.TechniqueListScreen
import edu.ucne.recrearte.presentation.techniques.TechniqueScreen
import edu.ucne.recrearte.presentation.work.WorkListScreen
import edu.ucne.recrearte.presentation.work.WorkScreenCreate
import kotlinx.coroutines.launch

@Composable
fun HomeNavHost(
    navHostController: NavHostController,
    modifier: Modifier = Modifier
){
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    NavHost(
        navController = navHostController,
        startDestination = Screen.LoginScreen
    ){

        composable<Screen.LoginScreen> {
            LoginScreen(navController = navHostController)
        }

        composable<Screen.SignUpScreen> {
            SignUpScreen(navController = navHostController)
        }


        composable<Screen.Home> {
           HomeScreen(
               navController = navHostController
           )
       }

        composable<Screen.PaymentMethodList> {
            PaymentMethodListScreen(
                goToPaymentMethod = {id ->
                    navHostController.navigate(Screen.PaymentMethodScreen(id))
                },
                createPaymentMethod = {
                    navHostController.navigate(Screen.PaymentMethodScreen(0))
                },
                drawerState = drawerState,
                scope = scope
            )
        }

        composable<Screen.PaymentMethodScreen> { backStack ->
            val id = backStack.toRoute<Screen.PaymentMethodScreen>().id
            PaymentMethodScreen(
                paymentMethodId = id,
                goBack = {navHostController.popBackStack()}
            )

        }

        composable<Screen.TechniqueList> {
            TechniqueListScreen(
                goToTechnique = {id ->
                    navHostController.navigate(Screen.TechniqueScreen(id))
                },
                createTechnique = {
                    navHostController.navigate(Screen.TechniqueScreen(0))
                },
                drawerState = drawerState,
                scope = scope
            )
        }

        composable<Screen.TechniqueScreen> { backStack ->
            val id =backStack.toRoute<Screen.TechniqueScreen>().id
            TechniqueScreen(
                techniqueId = id,
                goBack = {navHostController.popBackStack()}
            )
        }

        composable<Screen.WorkScreen> { backStack ->
            val id = backStack.toRoute<Screen.WorkScreen>().id
            WorkScreenCreate(
                workId = id,
                goBack = {navHostController.popBackStack()}
            )

        }

        composable<Screen.WorkListScreen> {
            WorkListScreen(
                drawerState = drawerState,
                scope = scope,
                goToWork = { id ->
                    navHostController.navigate(Screen.WorkScreen(id))
                },
                createWork = { navHostController.navigate(Screen.WorkScreen(0)) }
            )
        }

        composable<Screen.FavoritesScreen> {
           PruebaComposable(navController = navHostController)
        }

        composable<Screen.CartScreen> {
            // Tu pantalla de carrito aquí
        }

        composable<Screen.ProfileScreen> {
            ProfileScreen(
                onBackClick = { navHostController.popBackStack() },
                navController = navHostController,
                viewModel = hiltViewModel(),
                loginViewModel = hiltViewModel(),
                onPasswordChangeSuccess = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Contraseña cambiada con éxito")
                    }
                },
                onPasswordChangeError = { error ->
                    scope.launch {
                        snackbarHostState.showSnackbar("Error: $error")
                    }
                }
            )
        }

    }
}