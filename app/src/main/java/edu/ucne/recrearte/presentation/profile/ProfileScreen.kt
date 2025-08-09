package edu.ucne.recrearte.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import edu.ucne.recrearte.data.remote.dto.ArtistsDto
import edu.ucne.recrearte.data.remote.dto.CustomersDto
import edu.ucne.recrearte.presentation.login.LoginViewModel
import edu.ucne.recrearte.presentation.navigation.Screen
import edu.ucne.recrearte.util.GetInitials
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val showChangePasswordDialog by viewModel.showChangePasswordDialog.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun handleBackClick() {
        if (isEditing) {
            viewModel.cancelEdit()
        } else {
            onBackClick()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { handleBackClick()} ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isEditing && uiState is ProfileUiState.Success) {
                        Row {
                            IconButton(
                                onClick = { viewModel.showChangePasswordDialog(true) },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = "Change Password")
                            }
                            IconButton(onClick = { viewModel.startEditing() }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                        }
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(bottom = 130.dp)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (uiState) {
                is ProfileUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Loading Profile...")
                    }
                }
                is ProfileUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text((uiState as ProfileUiState.Error).message)
                    }
                }
                is ProfileUiState.Success -> {
                    val data = (uiState as ProfileUiState.Success).userData
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        when (data) {
                            is ArtistsDto -> ArtistProfileContent(
                                artist = data,
                                isEditing = isEditing,
                                onEvent = viewModel::updateField,
                                onSave = viewModel::saveChanges,
                                onCancel = viewModel::cancelEdit,
                                onLogout = {
                                    loginViewModel.logout(navController = navController)
                                    navController.navigate(Screen.LoginScreen) { popUpTo(0) }
                                }
                            )
                            is CustomersDto -> CustomerProfileContent(
                                customer = data,
                                isEditing = isEditing,
                                onEvent = viewModel::updateField,
                                onSave = viewModel::saveChanges,
                                onCancel = viewModel::cancelEdit,
                                onLogout = {
                                    loginViewModel.logout(navController = navController)
                                    navController.navigate(Screen.LoginScreen) { popUpTo(0) }
                                }
                            )
                        }
                    }
                }
            }

            if (showChangePasswordDialog) {
                val errorMessage by viewModel.passwordChangeError.collectAsState()

                ChangesPasswordDialog(
                    onDismiss = {
                        viewModel.showChangePasswordDialog(false)
                        viewModel.clearPasswordError() // Añade esta función en el ViewModel
                    },
                    onChangePassword = { currentPass, newPass, confirmPass ->
                        viewModel.changePassword(
                            currentPass,
                            newPass,
                            confirmPass,
                            onSuccess = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Password succesfully changed")
                                }
                            }
                        )
                    },
                    errorMessage = errorMessage
                )
            }
        }
    }
}

@Composable
private fun ArtistProfileContent(
    artist: ArtistsDto,
    isEditing: Boolean,
    onEvent: (ProfileEvent) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(2.dp, MaterialTheme.colorScheme.onPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = GetInitials(artist.firstName ?: "", artist.lastName ?: ""),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${artist.firstName ?: ""} ${artist.lastName ?: ""}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (artist.roleId == 1) "Administrator" else "Artist",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        if (isEditing) {
            EditableProfileFields(
                userData = artist,
                onEvent = onEvent,
                isArtist = true,
                onSave = onSave,
                onCancel = onCancel
            )
        } else {
            NonEditableProfileFields(
                userData = artist,
                isArtist = true,
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun CustomerProfileContent(
    customer: CustomersDto,
    isEditing: Boolean,
    onEvent: (ProfileEvent) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(2.dp, MaterialTheme.colorScheme.onPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = GetInitials(customer.firstName ?: "", customer.lastName ?: ""),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${customer.firstName ?: ""} ${customer.lastName ?: ""}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (customer.roleId == 1) "Administrator" else "Customer",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        if (isEditing) {
            EditableProfileFields(
                userData = customer,
                onEvent = onEvent,
                isArtist = false,
                onSave = onSave,
                onCancel = onCancel
            )
        } else {
            NonEditableProfileFields(
                userData = customer,
                isArtist = false,
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun NonEditableProfileFields(
    userData: Any,
    isArtist: Boolean,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = "Profile details",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                when (userData) {
                    is ArtistsDto -> {
                        ProfileField(label = "UserName", value = "@${userData.userName ?: "usuario"}")
                        ProfileField(label = "Email", value = userData.email)
                        ProfileField(label = "Phone Number", value = userData.phoneNumber)
                        ProfileField(label = "Document Number", value = userData.documentNumber)
                        ProfileField(label = "Art Style", value = userData.artStyle)
                        ProfileField(label = "Social Media", value = userData.socialMediaLinks)
                    }
                    is CustomersDto -> {
                        ProfileField(label = "UserName", value = "${userData.userName ?: "usuario"}")
                        ProfileField(label = "Email", value = userData.email)
                        ProfileField(label = "Phone Number", value = userData.phoneNumber)
                        ProfileField(label = "Address", value = userData.address ?: "455 Oak Ave, Airytown, USA")
                        ProfileField(label = "Document Number", value = userData.documentNumber)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Logout")
                }
            }
        }
    }
}

@Composable
private fun EditableProfileFields(
    userData: Any,
    onEvent: (ProfileEvent) -> Unit,
    isArtist: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    // Obtenemos los estados editables del ViewModel
    val editableArtist by viewModel.editableArtist.collectAsState()
    val editableCustomer by viewModel.editableCustomer.collectAsState()

    // Obtenemos los estados de error
    val validationErrors = when (uiState) {
        is ProfileUiState.Success -> (uiState as ProfileUiState.Success).validationErrors
        else -> ValidationErrors()
    }

    // Determinamos qué datos mostrar
    val currentData = remember(isArtist, editableArtist, editableCustomer) {
        when {
            isArtist -> editableArtist ?: (userData as ArtistsDto)
            else -> editableCustomer ?: (userData as CustomersDto)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = "Editar perfil",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                when (currentData) {
                    is ArtistsDto -> {
                        OutlinedTextField(
                            value = currentData.userName ?: "",
                            onValueChange = { onEvent(ProfileEvent.UserNameChange(it)) },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = validationErrors.userName != null,
                            supportingText = {
                                validationErrors.userName?.let { error ->
                                    Text(
                                        text = error,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = currentData.firstName ?: "",
                            onValueChange = { onEvent(ProfileEvent.FirstNameChange(it)) },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = currentData.lastName ?: "",
                            onValueChange = { onEvent(ProfileEvent.LastNameChange(it)) },
                            label = { Text("Lastname") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = currentData.email ?: "",
                            onValueChange = { onEvent(ProfileEvent.EmailChange(it)) },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = validationErrors.email != null,
                            supportingText = {
                                validationErrors.email?.let { error ->
                                    Text(
                                        text = error,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = currentData.phoneNumber ?: "",
                            onValueChange = { onEvent(ProfileEvent.PhoneNumberChange(it)) },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = validationErrors.phoneNumber != null,
                            supportingText = {
                                validationErrors.phoneNumber?.let { error ->
                                    Text(
                                        text = error,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = currentData.documentNumber ?: "",
                            onValueChange = { onEvent(ProfileEvent.DocumentNumberChange(it)) },
                            label = { Text("Document Number") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = validationErrors.documentNumber != null,
                            supportingText = {
                                validationErrors.documentNumber?.let { error ->
                                    Text(
                                        text = error,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = currentData.artStyle ?: "",
                            onValueChange = { onEvent(ProfileEvent.ArtStyleChange(it)) },
                            label = { Text("Art style") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = currentData.socialMediaLinks ?: "",
                            onValueChange = { onEvent(ProfileEvent.SocialMediaLinksChange(it)) },
                            label = { Text("Social Media") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    is CustomersDto -> {
                        OutlinedTextField(
                            value = currentData.userName ?: "",
                            onValueChange = { onEvent(ProfileEvent.UserNameChange(it)) },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = validationErrors.userName != null,
                            supportingText = {
                                validationErrors.userName?.let { error ->
                                    Text(
                                        text = error,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = currentData.firstName ?: "",
                            onValueChange = { onEvent(ProfileEvent.FirstNameChange(it)) },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = currentData.lastName ?: "",
                            onValueChange = { onEvent(ProfileEvent.LastNameChange(it)) },
                            label = { Text("Last name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = currentData.email ?: "",
                            onValueChange = { onEvent(ProfileEvent.EmailChange(it)) },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = validationErrors.email != null,
                            supportingText = {
                                validationErrors.email?.let { error ->
                                    Text(
                                        text = error,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = currentData.phoneNumber ?: "",
                            onValueChange = { onEvent(ProfileEvent.PhoneNumberChange(it)) },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = validationErrors.phoneNumber != null,
                            supportingText = {
                                validationErrors.phoneNumber?.let { error ->
                                    Text(
                                        text = error,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = currentData.documentNumber ?: "",
                            onValueChange = { onEvent(ProfileEvent.DocumentNumberChange(it)) },
                            label = { Text("Document Number") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = validationErrors.documentNumber != null,
                            supportingText = {
                                validationErrors.documentNumber?.let { error ->
                                    Text(
                                        text = error,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = currentData.address ?: "455 Oak Ave, Airytown, USA",
                            onValueChange = { onEvent(ProfileEvent.AddressChange(it)) },
                            label = { Text("Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onCancel
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = onSave
                    ) {
                        Text("Save changes")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value ?: "",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

