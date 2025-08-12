package edu.ucne.recrearte.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import edu.ucne.recrearte.util.TokenManager

@Composable
fun MainAppScreen(
    navController: NavHostController,
    tokenManager: TokenManager,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Usar rememberUpdatedState para el roleId para garantizar actualizaciones
    val currentRoleId by rememberUpdatedState(tokenManager.getRoleId())

    val bottomBarRoutes = listOf(
        Screen.RecreArteScreen::class.qualifiedName,
        Screen.FavoritesScreen::class.qualifiedName,
        Screen.CartScreen::class.qualifiedName,
        Screen.ProfileScreen::class.qualifiedName
    )

    val showBottomBar = bottomBarRoutes.contains(currentRoute)

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                val roleId by rememberUpdatedState(currentRoleId)
                NavigationBar {
                    // Filtramos los items basados en el roleId
                    val visibleItems = when (roleId) {
                        1, 2 -> {
                            BottomNavDestination.entries.filter { it != BottomNavDestination.Cart }
                        }
                        3 -> {
                            BottomNavDestination.entries.filter { it != BottomNavDestination.AdminArtistMenu }
                        }
                        else -> {
                            BottomNavDestination.entries.filter { it != BottomNavDestination.AdminArtistMenu }
                        }
                    }

                    visibleItems.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.screen::class.qualifiedName,
                            onClick = {
                                navController.navigate(destination.screen) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (currentRoute == destination.screen::class.qualifiedName)
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
            modifier = Modifier.padding(contentPadding),
            tokenManager = tokenManager
        )
    }
}


enum class BottomNavDestination(
    val title: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val screen: Screen
) {
    RecreArteScreen(
        title = "Start",
        filledIcon = Icons.Filled.Home,
        outlinedIcon = Icons.Outlined.Home,
        screen = Screen.RecreArteScreen
    ),
    Favorites(
        title = "Favorites",
        filledIcon = Icons.Filled.Favorite,
        outlinedIcon = Icons.Outlined.FavoriteBorder,
        screen = Screen.FavoritesScreen
    ),
    Cart(
        title = "Cart",
        filledIcon = Icons.Filled.ShoppingCart,
        outlinedIcon = Icons.Outlined.ShoppingCart,
        screen = Screen.CartScreen
    ),
    Profile(
        title = "Profile",
        filledIcon = Icons.Filled.Person,
        outlinedIcon = Icons.Outlined.Person,
        screen = Screen.ProfileScreen
    ),
    AdminArtistMenu(
    title = "Menu",
    filledIcon = Icons.Default.Menu,
    outlinedIcon = Icons.Outlined.Menu,
    screen = Screen.AdminArtistMenuScreen
    );

    companion object {
        fun fromScreen(screen: Screen): BottomNavDestination? {
            return entries.firstOrNull { it.screen::class == screen::class }
        }
    }
}