package edu.ucne.recrearte.presentation.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    userRoleId: Int? = null
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomNavItem.RecreArteScreen,
        BottomNavItem.Favorites,
        BottomNavItem.Cart,
        BottomNavItem.Profile,
//        BottomNavItem.AdminArtistMenu
    )

    val filteredItems = if (userRoleId == 1 || userRoleId == 2) {
        items + BottomNavItem.AdminArtistMenu
    } else {
        items
    }

    Surface(
        modifier = Modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(25.dp),
                clip = true
            ),
        shape = RoundedCornerShape(25.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        NavigationBar(
            modifier = modifier
                .height(60.dp)  // Altura ajustada
                .padding(horizontal = 4.dp),  // Padding horizontal reducido
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            filteredItems.forEach { item ->
                val selected = currentRoute == item.screen::class.simpleName
                NavigationBarItem(
                    modifier = Modifier.padding(vertical = 6.dp),
                    icon = {
                        Icon(
                            imageVector = if (selected) item.filledIcon else item.outlinedIcon,
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                        )
                    },
                    selected = selected,
                    onClick = {
                        navController.navigate(item.screen) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                    alwaysShowLabel = true
                )
            }
        }
    }
}

sealed class BottomNavItem(
    val title: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val screen: Screen
) {
    object RecreArteScreen : BottomNavItem(
        title = "Start",
        filledIcon = Icons.Filled.Home,
        outlinedIcon = Icons.Outlined.Home,
        screen = Screen.RecreArteScreen
    )

    object Favorites : BottomNavItem(
        title = "Favorites",
        filledIcon = Icons.Filled.Favorite,
        outlinedIcon = Icons.Outlined.Favorite,
        screen = Screen.FavoritesScreen
    )

    object Cart : BottomNavItem(
        title = "Cart",
        filledIcon = Icons.Filled.ShoppingCart,
        outlinedIcon = Icons.Outlined.ShoppingCart,
        screen = Screen.CartScreen
    )

    object Profile : BottomNavItem(
        title = "Profile",
        filledIcon = Icons.Filled.Person,
        outlinedIcon = Icons.Outlined.Person,
        screen = Screen.ProfileScreen
    )

    object AdminArtistMenu : BottomNavItem(
        title = "Menu",
        filledIcon = Icons.Filled.Menu,
        outlinedIcon = Icons.Outlined.Menu,
        screen = Screen.AdminArtistMenuScreen
    )
}