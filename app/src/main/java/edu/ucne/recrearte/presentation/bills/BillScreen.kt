package edu.ucne.recrearte.presentation.bills

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import edu.ucne.recrearte.data.remote.dto.BillsDto
import edu.ucne.recrearte.data.remote.dto.WorksDto
import edu.ucne.recrearte.presentation.work.WorkEvent
import edu.ucne.recrearte.presentation.work.WorkViewModel
import edu.ucne.recrearte.util.isValidCreditCardNumber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillScreen(
    navController: NavController,
    viewModel: BillViewModel = hiltViewModel(),
    workViewModel: WorkViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val workState by workViewModel.uiSate.collectAsState()
    var showCardDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var cardNumber by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.onEvent(BillEvent.LoadCheckout)
        workViewModel.onEvent(WorkEvent.GetWorks)
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            workViewModel.onEvent(WorkEvent.GetWorks)
            showSuccessDialog = true
        }
    }

    if (showCardDialog) {
        CardValidationDialog(
            cardNumber = cardNumber,
            onCardNumberChange = { cardNumber = it },
            onValidate = {
                if (cardNumber.isValidCreditCardNumber()) {
                    viewModel.onEvent(BillEvent.CreateBill)
                    val workIds = state.createdBill?.billDetails?.map { it.workId } ?: emptyList()
                    if (workIds.isNotEmpty()) {
                        workViewModel.onEvent(WorkEvent.UpdateWorksStatus(workIds, 2))
                    }
                    showCardDialog = false
                }
            },
            onDismiss = { showCardDialog = false },
            errorMessage = if (cardNumber.isNotEmpty() && !cardNumber.isValidCreditCardNumber())
                "Invalid card number" else null
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                navController.popBackStack()
            },
            title = {
                Text(
                    text = "¡Successful Purchase!",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Your purchase has been successful. Thank you for your preference..",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Finalize Purchase",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.errorMessage != null -> {
                    ErrorState(
                        message = state.errorMessage!!,
                        onRetry = { viewModel.onEvent(BillEvent.LoadCheckout) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.createdBill != null -> {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ){
                        CheckoutContent(
                            bill = state.createdBill!!,
                            works = workState.works,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 80.dp)
                                .weight(1f)
                        )
                        Divider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )

                        Button(
                            onClick = { showCardDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = !state.isLoading && state.createdBill != null,
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Confirm Purchase")
                            }
                        }
                        Spacer(modifier = Modifier.height(55.dp))
                    }

                }
            }
        }
    }
}

@Composable
fun CardValidationDialog(
    cardNumber: String,
    onCardNumberChange: (String) -> Unit,
    onValidate: () -> Unit,
    onDismiss: () -> Unit,
    errorMessage: String?
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter your card details",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = {
                        if (it.length <= 16 && it.all { c -> c.isDigit() }) {
                            onCardNumberChange(it)
                        }
                    },
                    label = { Text("Card number") },
                    placeholder = { Text("1234567890123456") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    isError = errorMessage != null,
                    singleLine = true
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = onValidate,
                        enabled = cardNumber.length == 16 && cardNumber.isValidCreditCardNumber()
                    ) {
                        Text("Pay")
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckoutContent(
    bill: BillsDto,
    works: List<WorksDto>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .padding(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Customer Information",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = bill.customerName,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Payment Method",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Divider()
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Credit card",
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Purchase Summary",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Divider()

            bill.billDetails.forEach { item ->
                val work = works.find { it.workId == item.workId }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = work?.title ?: "Work not available",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "$${"%.2f".format(item.subtotal)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal:")
                Text("$${"%.2f".format(bill.billDetails.sumOf { it.subtotal })}")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Taxes:")
                Text("$${"%.2f".format(bill.taxes)}")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total:",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "$${"%.2f".format(bill.total)}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}