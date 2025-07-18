package edu.ucne.recrearte.presentation.work

import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import edu.ucne.recrearte.data.remote.dto.ImagesDto
import edu.ucne.recrearte.presentation.techniques.TechniqueEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkScreenCreate(
    workId: Int? = null,
    viewModel: WorkViewModel = hiltViewModel(),
    goBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiSate.collectAsState()
    val scope = rememberCoroutineScope()

    var expandedTechnique by remember { mutableStateOf(false) }
    var expandedArtist by remember { mutableStateOf(false) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(workId) {
        if (workId != null && workId != 0) {
            viewModel.loadWork(workId)
        } else {
            viewModel.onEvent(WorkEvent.New)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                selectedImageUri = it
                val imageBytes = context.contentResolver.openInputStream(uri)?.readBytes()
                imageBytes?.let { bytes ->
                    val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                    // 👇 Usa el nombre correcto según tu clase ImagesDto
                    viewModel.onEvent(WorkEvent.ImageCreate(ImagesDto(imageId = 0, base64 = base64)))
                }
            }
        }
    )

    // Mostrar Toast si se guarda correctamente
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Toast.makeText(context, uiState.successMessage ?: "Guardado con éxito", Toast.LENGTH_SHORT).show()
            viewModel.onEvent(WorkEvent.ResetSuccessMessage)
            goBack()
        }
    }

    // Mostrar error general
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.onEvent(WorkEvent.ClearErrorMessage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Work") },
                navigationIcon = {
                    IconButton(onClick = goBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)) {

            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onEvent(WorkEvent.TitleChange(it)) },
                label = { Text("Título") },
                isError = uiState.errorTitle!!.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.dimension,
                onValueChange = { viewModel.onEvent(WorkEvent.DimensionChange(it)) },
                label = { Text("Dimensión") },
                isError = uiState.errorDimension!!.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onEvent(WorkEvent.DescriptionChange(it)) },
                label = { Text("Descripción") },
                isError = uiState.errorDescription!!.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = if (uiState.price == 0.0) "" else uiState.price.toString(),
                onValueChange = {
                    viewModel.onEvent(WorkEvent.PriceChange(it.toDoubleOrNull() ?: 0.0))
                },
                label = { Text("Precio") },
                isError = uiState.errorPrice!!.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Technique Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { expandedTechnique = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        uiState.techniquesL.find { it.techniqueId == uiState.techniqueId }?.techniqueName
                            ?: "Seleccione técnico",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = expandedTechnique,
                    onDismissRequest = { expandedTechnique = false }
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

            // Artist Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { expandedArtist = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        uiState.artists.find { it.artistId == uiState.artistId }?.userName
                            ?: if (uiState.artists.isEmpty()) "No artists available" else "Seleccione artista",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = expandedArtist,
                    onDismissRequest = { expandedArtist = false }
                ) {
                    if (uiState.artists.isEmpty()) {
                        DropdownMenuItem(text = { Text("No artists available") }, onClick = { expandedArtist = false })
                    } else {
                        uiState.artists.forEach { artist ->
                            DropdownMenuItem(
                                text = { Text(artist.userName ?: "Unknown") },
                                onClick = {
                                    viewModel.onEvent(WorkEvent.ArtistChange(artist.artistId ?: 0))
                                    expandedArtist = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Imagen
            val requestPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    launcher.launch("image/*")
                } else {
                    Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
                }
            }

            Button(onClick = {
                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    android.Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                }

                when {
                    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                        launcher.launch("image/*")
                    }
                    else -> {
                        requestPermissionLauncher.launch(permission)
                    }
                }
            }) {
                Text("Seleccionar Imagen")
            }




            // Preview de imagen seleccionada
            selectedImageUri?.let {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (workId != null && workId != 0) {
                        viewModel.onEvent(WorkEvent.UpdateWork(workId))
                        goBack()
                    } else {
                        viewModel.onEvent(WorkEvent.CreateWork)
                        goBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
        }
    }
}
