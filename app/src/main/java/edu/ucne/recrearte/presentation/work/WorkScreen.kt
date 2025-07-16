package edu.ucne.recrearte.presentation.work

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.ucne.recrearte.data.remote.dto.ImagesDto
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkScreen(
    workId: Int? = null,
    viewModel: WorkViewModel = hiltViewModel(),
    goBack: () -> Unit
) {
    LaunchedEffect(workId) {
        if (workId != null && workId != 0) {
            viewModel.onEvent(WorkEvent.WorkdIdChange(workId))
        } else {
            viewModel.onEvent(WorkEvent.New)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (workId != 0) "Update Work" else "Create Work",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = goBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        WorkFormBody(
            Modifier.padding(padding),
            viewModel,
            workId,
            goBack
        )
    }
}

@Composable
fun WorkFormBody(
    modifier: Modifier = Modifier,
    viewModel: WorkViewModel,
    workId: Int?,
    goBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState = viewModel.uiSate.collectAsState().value
    var expandedArtist by remember { mutableStateOf(false) }
    var expandedTechnique by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = uiState.title,
            onValueChange = { viewModel.onEvent(WorkEvent.TitleChange(it)) },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (uiState.errorTitle!!.isNotEmpty()) {
            Text(
                text = uiState.errorTitle,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.dimension,
            onValueChange = { viewModel.onEvent(WorkEvent.DimensionChange(it)) },
            label = { Text("Dimension") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (uiState.errorDimension!!.isNotEmpty()) {
            Text(
                text = uiState.errorDimension,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.description,
            onValueChange = { viewModel.onEvent(WorkEvent.DescriptionChange(it)) },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (uiState.errorDescription!!.isNotEmpty()) {
            Text(
                text = uiState.errorDescription,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.price.toString(),
            onValueChange = {
                val price = it.toDoubleOrNull() ?: 0.0
                viewModel.onEvent(WorkEvent.PriceChange(price))
            },
            label = { Text("Price") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (uiState.errorPrice!!.isNotEmpty()) {
            Text(
                text = uiState.errorPrice,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandedTechnique = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    uiState.techniquesL.find { it.techniqueId == uiState.techniqueId }?.techniqueName
                        ?: "Seleccione técnico",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Técnico")
            }
            DropdownMenu(
                expanded = expandedTechnique,
                onDismissRequest = { expandedTechnique = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                uiState.techniquesL.forEach { technique ->
                    DropdownMenuItem(
                        text = { Text(technique.techniqueName) },
                        onClick = {
                            viewModel.onEvent(WorkEvent.TechniqueChange(technique.techniqueId ?: 0))
                            expandedTechnique = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandedArtist = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    uiState.artists.find { it.artistId == uiState.artistId }?.userName
                        ?: if (uiState.artists.isEmpty()) "No artists available" else "Seleccione artista",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = "artist")
            }
            DropdownMenu(
                expanded = expandedArtist,
                onDismissRequest = { expandedArtist = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.artists.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No artists available") },
                        onClick = { expandedArtist = false }
                    )
                } else {
                    uiState.artists.forEach { artist ->
                        DropdownMenuItem(
                            text = { Text(artist.userName ?: "Unknown Artist") },
                            onClick = {
                                viewModel.onEvent(WorkEvent.ArtistChange(artist.artistId ?: 0))
                                expandedArtist = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ImagePicker(viewModel,uiState.title)
        SelectedImages(viewModel)

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { viewModel.onEvent(WorkEvent.New) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.inversePrimary,
                    contentColor = MaterialTheme.colorScheme.inverseSurface
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Clear")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear")
            }
            Button(
                onClick = {
                    if (workId == null || workId == 0) {
                        viewModel.onEvent(WorkEvent.CreateWork)
                    } else {
                        viewModel.onEvent(WorkEvent.UpdateWork(workId))
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Save")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save")
            }
        }

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        LaunchedEffect(uiState.isSuccess) {
            if (uiState.isSuccess) {
                Toast.makeText(context, uiState.successMessage ?: "Success", Toast.LENGTH_SHORT).show()
                delay(1000)
                viewModel.onEvent(WorkEvent.ResetSuccessMessage)
                goBack()
            }
        }
    }
}

//@Composable
//fun ImagePicker(
//    viewModel: WorkViewModel,
//    title: String
//) {
//    val context = LocalContext.current
//    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
//        uri?.let {
//            val bitmap = if (Build.VERSION.SDK_INT < 28) {
//                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
//            } else {
//                val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
//                android.graphics.ImageDecoder.decodeBitmap(source)
//            }
//            val base64 = ImageUtils.encodeImageToBase64(bitmap)
//            val image = ImagesDto(
//                imageId = 0,
//                title = title,
//                base64 = base64
//            )
//            viewModel.addImage(image)
//        }
//    }
//    Button(
//        onClick = { launcher.launch("image/*") },
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Text("Add Image")
//    }
//}
@Composable
fun ImagePicker(viewModel: WorkViewModel, title: String) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    try {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                            decoder.isMutableRequired = true
                        }
                    } catch (e: Exception) {
                        Log.e("ImagePicker", "ImageDecoder failed, trying MediaStore", e)
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                            ?: throw IllegalStateException("Both ImageDecoder and MediaStore failed")
                    }
                }

                // Redimensionar la imagen si es muy grande
                val maxSize = 1024
                val width = bitmap.width
                val height = bitmap.height

                val scaledBitmap = if (width > maxSize || height > maxSize) {
                    val scale = maxSize.toFloat() / maxOf(width, height)
                    Bitmap.createScaledBitmap(
                        bitmap,
                        (width * scale).toInt(),
                        (height * scale).toInt(),
                        true
                    ).also {
                        bitmap.recycle() // Liberar memoria del bitmap original
                    }
                } else {
                    bitmap
                }

                // Convertir a Base64
                val base64 = ImageUtils.encodeImageToBase64(scaledBitmap)
                if (base64.isEmpty()) {
                    throw IllegalStateException("Failed to encode image to Base64")
                }

                // Crear DTO y agregar al ViewModel
                val image = ImagesDto(
                    imageId = 0,
                    title = title,
                    base64 = base64
                )
                viewModel.addImage(image)

            } catch (e: Exception) {
                // Mostrar error al usuario
                Toast.makeText(
                    context,
                    "Error loading image: ${e.message ?: "Unknown error"}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("ImagePicker", "Error loading image", e)
            }
        }
    }

    Button(
        onClick = { launcher.launch("image/*") },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Add Image")
    }
}

//@Composable
//fun SelectedImages(viewModel: WorkViewModel) {
//    val images = viewModel.uiSate.collectAsState().value.Images
//    LazyRow {
//        itemsIndexed(images) { index, image ->
//            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                image.base64?.let { base64 ->
//                    val byteArray = Base64.decode(base64, Base64.DEFAULT)
//                    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
//                    Image(
//                        bitmap = bitmap.asImageBitmap(),
//                        contentDescription = null,
//                        modifier = Modifier.size(100.dp).padding(4.dp)
//                    )
//                }
//                Button(
//                    onClick = { viewModel.removeImage(index) },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
//                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
//                    )
//                ) {
//                    Text("Remove")
//                }
//            }
//        }
//    }
//}
@Composable
fun SelectedImages(viewModel: WorkViewModel) {
    val images = viewModel.uiSate.collectAsState().value.Images

    LazyRow {
        itemsIndexed(images) { index, image ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                image.base64?.let { base64 ->
                    val bitmap = ImageUtils.decodeBase64ToBitmap(base64)
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .padding(4.dp)
                        )
                    } ?: run {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Error loading image")
                        }
                    }
                }

                Button(
                    onClick = { viewModel.removeImage(index) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Remove")
                }
            }
        }
    }
}
