package edu.ucne.recrearte.presentation.AdminArtistMenu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import edu.ucne.recrearte.presentation.navigation.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminArtistMenuSheet(
    roleId: Int,
    onDismiss: () -> Unit,
    navController: NavHostController,
    sheetState: SheetState,
    scope: CoroutineScope
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = if (roleId == 1) "Menú of Aministrator" else "Menú of Artist",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (roleId == 1) {
                MenuItem(
                    text = "Payments Methods",
                    icon = Icons.Default.Payment,
                    onClick = {

                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                navController.navigate(Screen.PaymentMethodList) {
                                    launchSingleTop = true
                                    popUpTo(Screen.AdminArtistMenuScreen) {
                                        inclusive = true
                                    }
                                }
                            }
                        }
                    }
                )

                MenuItem(
                    text = "Techniques",
                    icon = Icons.Default.Brush,
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                navController.navigate(Screen.TechniqueList) {
                                    launchSingleTop = true
                                    popUpTo(Screen.AdminArtistMenuScreen) {
                                        inclusive = true
                                    }
                                }
                            }
                        }
                    }
                )
            }

            if (roleId == 2) {
                MenuItem(
                    text = "My Works",
                    icon = Icons.Default.Palette,
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                navController.navigate(Screen.WorkListScreen) {
                                    launchSingleTop = true
                                    popUpTo(Screen.AdminArtistMenuScreen) {
                                        inclusive = true
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MenuItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}