package edu.ucne.recrearte.presentation.techniques

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechniqueScreen(
    techniqueId: Int? = null,
    viewModel: TechniqueViewModel = hiltViewModel(),
    goBack: () -> Unit
) {
    LaunchedEffect(techniqueId) {
        techniqueId?.let { id ->
            viewModel.onEvent(TechniqueEvent.TechniquedIdChange(id))
            viewModel.onEvent(TechniqueEvent.GetTechniques)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (techniqueId != 0) "Update technique" else "Create technique ",
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
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Go back",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        TechniqueBodyScreen(
            modifier = Modifier.padding(padding),
            techniqueId = techniqueId,
            viewModel = viewModel,
            goBack = goBack
        )
    }
}

@Composable
fun TechniqueBodyScreen(
    modifier: Modifier = Modifier,
    techniqueId: Int?,
    viewModel: TechniqueViewModel,
    goBack: () -> Unit
) {
    val uiState by viewModel.uiSate.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .padding(16.dp)
    ) {
        val uiState = viewModel.uiSate.collectAsState().value

        OutlinedTextField(
            value = uiState.techniqueName,
            onValueChange = {
                viewModel.onEvent(TechniqueEvent.NameChange(it))
            },
            label = { Text("Name Technique") },
            isError = uiState.errorMessage != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    viewModel.onEvent(TechniqueEvent.New)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Clear")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear")
            }

            Button(
                onClick = {
                    if (techniqueId == null) {
                        viewModel.onEvent(TechniqueEvent.CreateTechnique)
                    } else {
                        viewModel.onEvent(TechniqueEvent.UpdateTechnique(techniqueId))
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

            LaunchedEffect(uiState.isSuccess) {
                if (uiState.isSuccess) {
                    goBack
                }
            }

        }
    }
}