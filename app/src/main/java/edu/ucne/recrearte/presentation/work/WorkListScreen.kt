package edu.ucne.recrearte.presentation.work

import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import edu.ucne.recrearte.data.remote.dto.WorksDto
import edu.ucne.recrearte.presentation.navigation.Screen
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
    viewModel: WorkViewModel = hiltViewModel(),
    tokenManager: TokenManager
) {
    val uiState by viewModel.uiSate.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    // Obtener el ID del artista logueado como String primero
    val userId by remember { derivedStateOf { tokenManager.getUserId() } }

    // Convertir a Int solo cuando sea necesario
//    val artistId = remember(userId) {
//        userId?.toIntOrNull()
//    }

    // Cargar obras del artista logueado
    LaunchedEffect(userId) {
        if (userId != null) {
            viewModel.getWorksForLoggedArtist()
        } else {
            Log.e("WorkListScreen", "No user ID available")
        }
    }

    WorkListBodyScreen(
        drawerState = drawerState,
        scope = scope,
        uiState = uiState,
        reloadWorks = {
            if (userId != null) {
                viewModel.getWorksForLoggedArtist()
            }
        },
        goToWork = goToWork,
        createWork = createWork,
        deleteWork = { work ->
            work.workId?.let { workId ->
                viewModel.onEvent(WorkEvent.DeleteWork(workId))
                // Recargar solo si userId está disponible
                userId?.let { viewModel.getWorksForLoggedArtist() }
            }
        },
        query = searchQuery,
        searchResults = searchResults,
        onSearchQueryChanged = viewModel::onSearchQueryChanged
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun WorkListBodyScreen(
    drawerState: DrawerState,
    scope: CoroutineScope,
    uiState: WorkUiState,
    reloadWorks: () -> Unit,
    goToWork: (Int) -> Unit,
    createWork: () -> Unit,
    deleteWork: (WorksDto) -> Unit,
    query: String,
    searchResults: List<WorksDto>,
    onSearchQueryChanged: (String) -> Unit
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
                        text = "Obras de Arte",
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
                uiState.isLoading && uiState.works.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                uiState.works.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron obras",
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
                                placeholder = "Buscar obras..."
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        val worksToShow = if (query.isNotBlank()) searchResults else uiState.works

                        items(worksToShow) { work ->
                            WorkCard(
                                work = work,
                                goToWork = { goToWork(work.workId ?: 0) },
                                deleteWork = deleteWork
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
fun WorkItem(
    work: WorksDto,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onItemClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (!work.base64.isNullOrEmpty()) {
                // Mostrar imagen si está disponible
                AsyncImage(
                    model = work.base64,
                    contentDescription = work.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = work.title,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = work.description ?: "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Precio: $${work.price}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun WorkCard(
    work: WorksDto,
    goToWork: () -> Unit,
    deleteWork: (WorksDto) -> Unit // Puedes mapear a WorksDto aquí si lo necesitas
) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(onClick = goToWork),
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
            if (work.base64!!.isNotBlank()) {
                val imageBytes = Base64.decode(work.base64, Base64.DEFAULT)
                val bitmap = remember(work.base64) {
                    try {
                        android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            ?.asImageBitmap()
                    } catch (e: Exception) {
                        null
                    }
                }

                bitmap?.let {
                    Image(
                        bitmap = it,
                        contentDescription = "Imagen de la obra",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(end = 12.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = goToWork)
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
                            append("Técnica: ")
                        }
                        append(work.techniqueId.toString())

                        append("\n")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Dimensiones: ")
                        }
                        append(work.dimension)

                        append("\n")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Precio: ")
                        }
                        append("$${work.price}")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = {
                    // Puedes mapear aquí a WorksDto si lo necesitas
                    deleteWork(
                        WorksDto(
                            workId = work.workId,
                            title = work.title,
                            dimension = work.dimension,
                            techniqueId = work.techniqueId,
                            artistId = work.artistId,
                            price = work.price,
                            description = work.description,
                            imageId = work.imageId
                        )
                    )
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
