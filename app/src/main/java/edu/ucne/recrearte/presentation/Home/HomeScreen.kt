package edu.ucne.recrearte.presentation.Home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import edu.ucne.recrearte.presentation.navigation.Screen

@Composable
fun HomeScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top)
    ) {
        Text(
            text = "Home Pruebas",
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(bottom = 16.dp),
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp)),
            color = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                //Payment
                OutlinedButton(
                    onClick = { navController.navigate(Screen.PaymentMethodList) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .border(
                            width = 2.dp,
                            color = Color.Blue,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = Color.Transparent,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "PaymentMethods",
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                }
                //Technique
                OutlinedButton(
                    onClick = { navController.navigate(Screen.TechniqueList) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .border(
                            width = 2.dp,
                            color = Color.Blue,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = Color.Transparent,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Techniques",
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}