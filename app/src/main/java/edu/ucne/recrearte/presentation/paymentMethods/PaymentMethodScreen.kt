package edu.ucne.recrearte.presentation.paymentMethods

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodScreen(
    paymentMethodId: Int? = null,
    viewModel: PaymentMethodViewModel = hiltViewModel(),
    goBack: () -> Unit
) {
    LaunchedEffect(paymentMethodId) {
        paymentMethodId?.let { id ->
            viewModel.onEvent(PaymentMethodEvent.PaymentMethodIdChange(id))
            viewModel.onEvent(PaymentMethodEvent.GetPaymentMethods)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (paymentMethodId != null) "Editar Método de Pago" else "Crear Método de Pago",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = goBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Blue
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        PaymentMethodBodyScreen(
            modifier = Modifier.padding(padding),
            paymentMethodId = paymentMethodId,
            viewModel = viewModel,
            goBack = goBack
        )
    }
}

@Composable
fun PaymentMethodBodyScreen(
    modifier: Modifier = Modifier,
    paymentMethodId: Int?,
    viewModel: PaymentMethodViewModel,
    goBack: () -> Unit
) {
    val uiState by viewModel.uiSate.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = uiState.paymentMethodName,
            onValueChange = { viewModel.onEvent(PaymentMethodEvent.NameChange(it)) },
            label = { Text("Nombre del Método de Pago") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    viewModel.onEvent(PaymentMethodEvent.New)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Limpiar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Limpiar")
            }

            Button(
                onClick = {
                    if (paymentMethodId == null) {
                        viewModel.onEvent(PaymentMethodEvent.CreatePaymentMethod)
                    } else {
                        viewModel.onEvent(PaymentMethodEvent.UpdatePaymentMethod(paymentMethodId))
                    }
                    goBack()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Guardar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar")
            }
        }
    }
}