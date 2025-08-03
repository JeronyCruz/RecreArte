package edu.ucne.recrearte.presentation.work

import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import edu.ucne.recrearte.R
import edu.ucne.recrearte.data.remote.dto.WorksDto
import edu.ucne.recrearte.presentation.Home.HomeEvent
import edu.ucne.recrearte.presentation.Home.HomeUiState
import edu.ucne.recrearte.presentation.Home.HomeViewModel
import edu.ucne.recrearte.util.TokenManager
import edu.ucne.recrearte.util.getUserId
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkListScreen(
    drawerState: DrawerState,
    scope: CoroutineScope,
    goToWork: (Int) -> Unit,
    createWork: () -> Unit,
    navController: NavHostController,
    homeViewModel: HomeViewModel = hiltViewModel(),
    workViewModel: WorkViewModel = hiltViewModel(), // Añadido WorkViewModel para eliminar
    tokenManager: TokenManager
) {
    val uiState by homeViewModel.uiSate.collectAsState()
    val searchQuery by homeViewModel.searchQuery.collectAsState()
    val searchResults by homeViewModel.searchResults.collectAsState()

    // Estados para el diálogo de confirmación
    var workToDelete by remember { mutableStateOf<WorksDto?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Obtener el ID del artista logueado
    val userId by remember { derivedStateOf { tokenManager.getUserId() } }
    val artistId = userId

    // Manejar la eliminación
    val handleDelete = { work: WorksDto ->
        workToDelete = work
        showDeleteConfirmation = true
    }

    val onDeleteConfirmed = {
        workToDelete?.let { work ->
            work.workId?.let { workId ->
                workViewModel.onEvent(WorkEvent.DeleteWork(workId))
                // Recargar las obras después de eliminar
                if (artistId != null) {
                    homeViewModel.onEvent(HomeEvent.GetWorksByArtist(artistId))
                }
            }
        }
        showDeleteConfirmation = false
        workToDelete = null
    }

    // Cargar obras del artista logueado
    LaunchedEffect(artistId) {
        if (artistId != null) {
            homeViewModel.onEvent(HomeEvent.GetWorksByArtist(artistId))
        } else {
            Log.e("WorkListScreen", "No user ID available")
        }
    }

    // Mostrar diálogo de confirmación
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirm removing ") },
            text = { Text("¿Are you sure that you want to remove the work ${workToDelete?.title}?") },
            confirmButton = {
                TextButton(
                    onClick = onDeleteConfirmed,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    WorkListBodyScreen(
        drawerState = drawerState,
        scope = scope,
        uiState = uiState,
        reloadWorks = {
            if (artistId != null) {
                homeViewModel.onEvent(HomeEvent.GetWorksByArtist(artistId))
            }
        },
        goToWork = goToWork,
        createWork = createWork,
        deleteWork = handleDelete,
        query = searchQuery,
        searchResults = searchResults,
        onSearchQueryChanged = homeViewModel::onSearchQueryChanged,
        navController = navController
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun WorkListBodyScreen(
    drawerState: DrawerState,
    scope: CoroutineScope,
    uiState: HomeUiState, // Cambiado a HomeUiState
    reloadWorks: () -> Unit,
    goToWork: (Int) -> Unit,
    createWork: () -> Unit,
    deleteWork: (WorksDto) -> Unit,
    query: String,
    searchResults: List<WorksDto>,
    onSearchQueryChanged: (String) -> Unit,
    navController: NavHostController,
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoading,
        onRefresh = reloadWorks
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My main Works",
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
                        androidx.compose.material3.Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    actionIconContentColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = createWork,
            ) {
                Icon(Icons.Filled.Add, "Crear obra")
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
                uiState.isLoading && uiState.worksByArtistsDto.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                uiState.worksByArtistsDto.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Art works not found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Búsqueda
                        item {
                            SearchBar(
                                query = query,
                                onQueryChanged = onSearchQueryChanged,
                                placeholder = "Search works..."
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        val worksToShow = if (query.isNotBlank()) searchResults else uiState.worksByArtistsDto

                        items(worksToShow) { work ->
                            ArtistWorkCard(
                                work = work,
                                onClick = { goToWork(work.workId ?: 0) },
                                showDelete = true,
                                onDelete = { deleteWork(work) }
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
                contentColor = MaterialTheme.colorScheme.primary
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

            if (uiState.isSuccess && !uiState.successMessage.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(
                        text = uiState.successMessage,
                        color = MaterialTheme.colorScheme.primary,
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
    onQueryChanged: (String) -> Unit,
    placeholder: String = "Buscar..."
) {
    androidx.compose.material3.OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        label = { Text(placeholder) },
        singleLine = true
    )
}

@Composable
fun ArtistWorkCard(
    work: WorksDto,
    onClick: () -> Unit,
    showDelete: Boolean = false,
    onDelete: () -> Unit = {}
) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mostrar imagen si está disponible
            // Mostrar imagen desde URL (Cloudinary)
            work.imageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = work.title,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(end = 12.dp),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.placeholder_image),
                    error = painterResource(R.drawable.placeholder_image)
                )
            } ?: Image(
                painter = painterResource(R.drawable.placeholder_image),
                contentDescription = "Placeholder",
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 12.dp),
                contentScale = ContentScale.Crop
                )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick)
            ) {
                Text(
                    text = work.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Techniques: ")
                        }
                        append(work.techniqueId.toString())

                        append("\n")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Dimensions: ")
                        }
                        append(work.dimension)

                        append("\n")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Price: ")
                        }
                        append("$${work.price}")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (showDelete) {
                IconButton(
                    onClick = onDelete,
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
}