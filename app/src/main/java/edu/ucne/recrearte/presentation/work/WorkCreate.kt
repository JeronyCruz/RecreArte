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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
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
    var showErrors by remember { mutableStateOf(false) }

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
                    viewModel.onEvent(
                        WorkEvent.ImageUpdate(
                            ImagesDto(
                                imageId = uiState.imageId,
                                base64 = base64
                            )
                        )
                    )
                }
            }
        }
    )

    // Mostrar mensaje de éxito
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Toast.makeText(context, uiState.successMessage ?: "Saved successfully", Toast.LENGTH_SHORT).show()
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
                title = {
                    Text(
                        if (workId != 0) "Edit Work" else "Create Work",
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Campo Título
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onEvent(WorkEvent.TitleChange(it)) },
                label = { Text("Title") },
                isError = showErrors && uiState.errorTitle!!.isNotEmpty(),
                supportingText = {
                    if (showErrors && uiState.errorTitle!!.isNotEmpty()) {
                        Text(
                            text = uiState.errorTitle!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo Dimensión
            OutlinedTextField(
                value = uiState.dimension,
                onValueChange = { viewModel.onEvent(WorkEvent.DimensionChange(it)) },
                label = { Text("Dimension") },
                isError = showErrors && uiState.errorDimension!!.isNotEmpty(),
                supportingText = {
                    if (showErrors && uiState.errorDimension!!.isNotEmpty()) {
                        Text(
                            text = uiState.errorDimension!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo Descripción
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onEvent(WorkEvent.DescriptionChange(it)) },
                label = { Text("Description") },
                isError = showErrors && uiState.errorDescription!!.isNotEmpty(),
                supportingText = {
                    if (showErrors && uiState.errorDescription!!.isNotEmpty()) {
                        Text(
                            text = uiState.errorDescription!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo Precio
            OutlinedTextField(
                value = if (uiState.price == 0.0) "" else uiState.price.toString(),
                onValueChange = {
                    viewModel.onEvent(WorkEvent.PriceChange(it.toDoubleOrNull() ?: 0.0))
                },
                label = { Text("Price") },
                isError = showErrors && uiState.errorPrice!!.isNotEmpty(),
                supportingText = {
                    if (showErrors && uiState.errorPrice!!.isNotEmpty()) {
                        Text(
                            text = uiState.errorPrice!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Técnica
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { expandedTechnique = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        uiState.techniquesL.find { it.techniqueId == uiState.techniqueId }?.techniqueName
                            ?: "Select technique",
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

            if (showErrors && uiState.errorMessage?.isNotEmpty() == true) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Imagen
            val requestPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    launcher.launch("image/*")
                } else {
                    Toast.makeText(context, "permission denied", Toast.LENGTH_SHORT).show()
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
                Text(if (uiState.imageId > 0) "Change Image" else "Select Image")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Vista previa de imagen seleccionada
            selectedImageUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            // Vista previa de imagen existente (si no hay nueva selección)
            if (selectedImageUri == null && !uiState.base64.isNullOrBlank()) {
                AsyncImage(
                    model = "data:image/*;base64,${uiState.base64}",
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Guardar
            Button(
                onClick = {
                    showErrors = true
                    if (workId != null && workId != 0) {
                        viewModel.onEvent(WorkEvent.UpdateWork(workId))
                    } else {
                        viewModel.onEvent(WorkEvent.CreateWork)
                    }

                    // Verificar si hay errores después de la validación
                    val hasErrors = listOf(
                        uiState.errorTitle,
                        uiState.errorDimension,
                        uiState.errorDescription,
                        uiState.errorPrice,
                        uiState.errorMessage
                    ).any { !it.isNullOrEmpty() }

                    // Navegar solo si no hay errores (el éxito se maneja en LaunchedEffect)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}