package edu.ucne.recrearte.presentation.work

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import edu.ucne.recrearte.R
import edu.ucne.recrearte.data.remote.dto.WorksDto
import edu.ucne.recrearte.presentation.shoppingCarts.ShoppingCartEvent
import edu.ucne.recrearte.presentation.shoppingCarts.ShoppingCartViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkDetailScreen(
    navController: NavController,
    workId: Int,
    viewModel: WorkViewModel = hiltViewModel(),
    shoppingCartViewModel: ShoppingCartViewModel = hiltViewModel(),

) {
    val isOnline by viewModel.networkMonitor.isOnline.collectAsState()
    val isLiked by viewModel.isLiked.collectAsState()
    val isInWishlist by viewModel.isInWishlist.collectAsState()
    val likeCount by viewModel.likeCount.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()

    LaunchedEffect(workId) {
        viewModel.loadWork(workId)
    }

    LaunchedEffect(isOnline) {
        if (!isOnline) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Connect to see updated information",
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    LaunchedEffect(workId) {
        viewModel.loadWork(workId)
    }

    val uiState by viewModel.uiSate.collectAsState()
    val work = remember(uiState.works, workId) {
        uiState.works.find { it.workId == workId } ?: WorksDto(
            workId = 0,
            title = "Work not found",
            description = "The information could not be loaded.",
            dimension = "N/A",
            price = 0.0,
            artistId = 0,
            techniqueId = 0,
            statusId = 1,
            imageUrl = "",
        )
    }

    val currentArtistId = remember { work.artistId }

    LaunchedEffect(currentArtistId) {
        if (currentArtistId > 0 && uiState.nameArtist.isNullOrBlank()) {
            viewModel.findArtist(currentArtistId)
        }
    }

    LaunchedEffect(work.artistId) {
        if (work.artistId > 0 && uiState.nameArtist.isNullOrBlank()) {
            viewModel.findArtist(work.artistId)
        }
    }

    val cartState by shoppingCartViewModel.uiSate.collectAsState()

    LaunchedEffect(cartState.isSuccess, cartState.errorMessage) {
        if (cartState.isSuccess) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = cartState.successMessage ?: "Added to cart",
                    duration = SnackbarDuration.Short
                )
                shoppingCartViewModel.onEvent(ShoppingCartEvent.ResetSuccessMessage)
            }
        }
        if (cartState.errorMessage != null) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = cartState.errorMessage ?: "Error adding to cart",
                    duration = SnackbarDuration.Short
                )
                shoppingCartViewModel.onEvent(ShoppingCartEvent.ClearErrorMessage)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        topBar = {
            TopAppBar(
                title = { Text(
                    "Work Detail",
                        style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                ) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Imagen de la obra - Versión adaptada de WorkCard
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AsyncImage(
                    model = work.imageUrl ?: R.drawable.placeholder_image,
                    contentDescription = work.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.placeholder_image),
                    error = painterResource(R.drawable.placeholder_image)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Título
            Text(
                text = work.title ?: "Untitled",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = work.description ?: "No description",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$${work.price ?: 0.0}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Artist",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.nameArtist?.firstOrNull()?.toString() ?: "A",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = uiState.nameArtist ?: "Unknown artist",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Botón de Like
                IconButton(
                    onClick = { viewModel.onEvent(WorkEvent.ToggleLike) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "$likeCount",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = { viewModel.onEvent(WorkEvent.ToggleWishlist) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isInWishlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Favoritos",
                        tint = if (isInWishlist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    shoppingCartViewModel.onEvent(
                        ShoppingCartEvent.AddToCart( workId)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add to cart", fontSize = 16.sp)
            }
        }
    }
}
@Composable
fun OfflineMessage(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_wifi_off_24), // Añade un icono apropiado
                contentDescription = "Offline",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No internet connection",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Please connect to the internet to view work details",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.popBackStack() },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Go back")
            }
        }
    }
}