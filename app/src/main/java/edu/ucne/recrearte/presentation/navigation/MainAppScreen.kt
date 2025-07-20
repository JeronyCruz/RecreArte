package edu.ucne.recrearte.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.ucne.recrearte.presentation.navigation.Screen.Home
import edu.ucne.recrearte.presentation.navigation.Screen.FavoritesScreen
import edu.ucne.recrearte.presentation.navigation.Screen.CartScreen
import edu.ucne.recrearte.presentation.navigation.Screen.ProfileScreen

@Composable
fun MainAppScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Lista de pantallas que SÍ deben mostrar la barra
    val bottomBarRoutes = listOf(
        Screen.Home::class.qualifiedName,
        Screen.FavoritesScreen::class.qualifiedName,
        Screen.CartScreen::class.qualifiedName,
        Screen.ProfileScreen::class.qualifiedName
    )

    // ¿La ruta actual está en la lista?
    val showBottomBar = bottomBarRoutes.contains(currentRoute)

    // Estado seleccionado
    var selectedDestination by rememberSaveable {
        mutableIntStateOf(
            BottomNavDestination.entries.indexOfFirst { dest ->
                currentRoute == dest.screen::class.qualifiedName
            }.takeIf { it != -1 } ?: 0
        )
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {   // 👈 SOLO si está en las rutas principales
                NavigationBar {
                    BottomNavDestination.entries.forEachIndexed { index, destination ->
                        NavigationBarItem(
                            selected = selectedDestination == index,
                            onClick = {
                                navController.navigate(destination.screen) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                                selectedDestination = index
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selectedDestination == index)
                                        destination.filledIcon
                                    else destination.outlinedIcon,
                                    contentDescription = destination.title
                                )
                            },
                            label = { Text(destination.title) }
                        )
                    }
                }
            }
        }
    ) { contentPadding ->
        HomeNavHost(
            navHostController = navController,
            modifier = Modifier.padding(contentPadding)
        )
    }
}


enum class BottomNavDestination(
    val title: String,
    val filledIcon: ImageVector,  // Icono relleno
    val outlinedIcon: ImageVector, // Icono de contorno
    val screen: Screen
) {
    Home(
        title = "Inicio",
        filledIcon = Icons.Filled.Home,
        outlinedIcon = Icons.Outlined.Home,
        screen = Screen.Home
    ),
    Favorites(
        title = "Favoritos",
        filledIcon = Icons.Filled.Favorite,
        outlinedIcon = Icons.Outlined.FavoriteBorder,
        screen = Screen.FavoritesScreen
    ),
    Cart(
        title = "Carrito",
        filledIcon = Icons.Filled.ShoppingCart,
        outlinedIcon = Icons.Outlined.ShoppingCart,
        screen = Screen.CartScreen
    ),
    Profile(
        title = "Perfil",
        filledIcon = Icons.Filled.Person,
        outlinedIcon = Icons.Outlined.Person,
        screen = Screen.ProfileScreen
    );

    companion object {
        fun fromScreen(screen: Screen): BottomNavDestination? {
            return entries.firstOrNull { it.screen::class == screen::class }
        }
    }
}