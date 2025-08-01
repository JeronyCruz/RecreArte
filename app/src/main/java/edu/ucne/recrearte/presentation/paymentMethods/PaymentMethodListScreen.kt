package edu.ucne.recrearte.presentation.paymentMethods

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.ucne.recrearte.data.remote.dto.PaymentMethodsDto
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodListScreen(
    drawerState: DrawerState,
    scope: CoroutineScope,
    viewModel: PaymentMethodViewModel = hiltViewModel(),
    goToPaymentMethod: (Int) -> Unit,
    createPaymentMethod: () -> Unit
) {
    val uiState by viewModel.uiSate.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var methodToDelete by remember { mutableStateOf<PaymentMethodsDto?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val handleDelete = { method: PaymentMethodsDto ->
        methodToDelete = method
        showDeleteConfirmation = true
    }

    val onDeleteConfirmed = {
        methodToDelete?.let { method ->
            viewModel.onEvent(PaymentMethodEvent.DeletePaymentMethod(method.paymentMethodId!!))
        }
        showDeleteConfirmation = false
        methodToDelete = null
    }

    // Cargar lista
    LaunchedEffect(Unit) {
        viewModel.onEvent(PaymentMethodEvent.GetPaymentMethods)
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirm deletion") },
            text = { Text("Are you sure you want to delete the payment method ${methodToDelete?.paymentMethodName}?") },
            confirmButton = {
                TextButton(onClick = onDeleteConfirmed) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    PaymentMethodListBodyScreen(
        drawerState = drawerState,
        scope = scope,
        uiState = uiState,
        reloadPaymentMethods = { viewModel.onEvent(PaymentMethodEvent.GetPaymentMethods) },
        goToPaymentMethod = goToPaymentMethod,
        createPaymentMethod = createPaymentMethod,
        deletePaymentMethod = handleDelete,
        query = viewModel.searchQuery.collectAsStateWithLifecycle().value,
        searchResults = viewModel.searchResults.collectAsStateWithLifecycle().value,
        onSearchQueryChanged = viewModel::onSearchQueryChanged
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun PaymentMethodListBodyScreen(
    drawerState: DrawerState,
    scope: CoroutineScope,
    uiState: PaymentMethodUiState,
    reloadPaymentMethods: () -> Unit,
    goToPaymentMethod: (Int) -> Unit,
    createPaymentMethod: () -> Unit,
    deletePaymentMethod: (PaymentMethodsDto) -> Unit,
    query: String,
    searchResults: List<PaymentMethodsDto>,
    onSearchQueryChanged: (String) -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoading,
        onRefresh = reloadPaymentMethods
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = " Payment Methods ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    actionIconContentColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = createPaymentMethod
            ) {
                Icon(Icons.Filled.Add, "Create a new payment method")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            when {
                uiState.isLoading && uiState.PaymentMethods.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                uiState.PaymentMethods.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No payment methods found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Busqueda
                        item {
                            SearchBar(
                                query = query,
                                onQueryChanged = onSearchQueryChanged
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        val methodsToShow = if (query.isNotBlank()) searchResults else uiState.PaymentMethods

                        items(methodsToShow) { paymentMethod ->
                            PaymentMethodCard(
                                paymentMethod = paymentMethod,
                                goToPaymentMethod = { goToPaymentMethod(paymentMethod.paymentMethodId ?: 0) },
                                deletePaymentMethod = deletePaymentMethod
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = MaterialTheme.colorScheme.secondary
            )

            if (!uiState.errorMessage.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                ) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit
) {
    androidx.compose.material3.OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        label = { Text("Find payment method...") },
        singleLine = true
    )
}

@Composable
fun PaymentMethodCard(
    paymentMethod: PaymentMethodsDto,
    goToPaymentMethod: () -> Unit,
    deletePaymentMethod: (PaymentMethodsDto) -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable{goToPaymentMethod},
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = goToPaymentMethod)
            ) {
                Text(
                    text = paymentMethod.paymentMethodName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("ID: ")
                        }
                        append(paymentMethod.paymentMethodId.toString())
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.surfaceDim
                )
            }

            // Botón de eliminar
            IconButton(
                onClick = { deletePaymentMethod(paymentMethod) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}