package edu.ucne.recrearte.presentation.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import edu.ucne.recrearte.presentation.Home.HomeScreen
import edu.ucne.recrearte.presentation.paymentMethods.PaymentMethodListScreen

@Composable
fun HomeNavHost(
    navHostController: NavHostController
){
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navHostController,
        startDestination = Screen.Home
    ){
        composable<Screen.Home> {
            HomeScreen(
                navController = navHostController
            )
        }

        composable<Screen.PaymentMethodList> {
            PaymentMethodListScreen(
                goToPaymentMethod = {id ->
                    navHostController.navigate(Screen.PaymentMethodScreen(id ?: 0))
                },
                createPaymentMethod = {
                    navHostController.navigate(Screen.PaymentMethodScreen(0))
                },
                editPaymentMethod = { pay ->
                    val id = pay.paymentMethodId ?: 0 // O maneja como prefieras el null
                    navHostController.navigate(Screen.PaymentMethodScreen(id))
                },
                drawerState = drawerState,
                scope = scope
            )
        }
    }
}