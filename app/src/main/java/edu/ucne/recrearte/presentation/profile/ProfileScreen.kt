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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
    loginViewModel: LoginViewModel = hiltViewModel(),
    onPasswordChangeSuccess: () -> Unit = {},
    onPasswordChangeError: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var tempArtist by remember { mutableStateOf<ArtistsDto?>(null) }
    var tempCustomer by remember { mutableStateOf<CustomersDto?>(null) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState) {
        when (uiState) {
            is ProfileUiState.Success -> {
                val data = (uiState as ProfileUiState.Success).userData
                when (data) {
                    is ArtistsDto -> tempArtist = data
                    is CustomersDto -> tempCustomer = data
                }
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isEditing && uiState is ProfileUiState.Success) {
                        Row {
                            IconButton(
                                onClick = { showChangePasswordDialog = true },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = "Change Password")
                            }
                            IconButton(onClick = { isEditing = true }) {
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
                        Text("Cargando perfil...")
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
                                artist = if (isEditing) tempArtist ?: data else data,
                                isEditing = isEditing,
                                onEvent = { event ->
                                    when (event) {
                                        is ProfileEvent.UserNameChange -> {
                                            tempArtist = (tempArtist ?: data).copy(userName = event.userName)
                                        }
                                        is ProfileEvent.FirstNameChange -> {
                                            tempArtist = (tempArtist ?: data).copy(firstName = event.firstName)
                                        }
                                        is ProfileEvent.LastNameChange -> {
                                            tempArtist = (tempArtist ?: data).copy(lastName = event.lastName)
                                        }
                                        is ProfileEvent.EmailChange -> {
                                            tempArtist = (tempArtist ?: data).copy(email = event.email)
                                        }
                                        is ProfileEvent.PhoneNumberChange -> {
                                            tempArtist = (tempArtist ?: data).copy(phoneNumber = event.phoneNumber)
                                        }
                                        is ProfileEvent.DocumentNumberChange -> {
                                            tempArtist = (tempArtist ?: data).copy(documentNumber = event.documentNumber)
                                        }
                                        is ProfileEvent.ArtStyleChange -> {
                                            tempArtist = (tempArtist ?: data).copy(artStyle = event.artStyle)
                                        }
                                        is ProfileEvent.SocialMediaLinksChange -> {
                                            tempArtist = (tempArtist ?: data).copy(socialMediaLinks = event.socialMediaLinks)
                                        }
                                        is ProfileEvent.PasswordChange -> {
                                            tempArtist = (tempArtist ?: data).copy(password = event.password)
                                        }
                                        ProfileEvent.SaveChanges -> {
                                            tempArtist?.let { viewModel.updateProfile(it) }
                                            isEditing = false
                                        }
                                        ProfileEvent.CancelEdit -> {
                                            isEditing = false
                                            tempArtist = null
                                        }
                                        else -> {}
                                    }
                                },
                                onLogout = {
                                    loginViewModel.logout()
                                    navController.navigate(Screen.LoginScreen) { popUpTo(0) }
                                }
                            )
                            is CustomersDto -> CustomerProfileContent(
                                customer = if (isEditing) tempCustomer ?: data else data,
                                isEditing = isEditing,
                                onEvent = { event ->
                                    when (event) {
                                        is ProfileEvent.UserNameChange -> {
                                            tempCustomer = (tempCustomer ?: data).copy(userName = event.userName)
                                        }
                                        is ProfileEvent.FirstNameChange -> {
                                            tempCustomer = (tempCustomer ?: data).copy(firstName = event.firstName)
                                        }
                                        is ProfileEvent.LastNameChange -> {
                                            tempCustomer = (tempCustomer ?: data).copy(lastName = event.lastName)
                                        }
                                        is ProfileEvent.EmailChange -> {
                                            tempCustomer = (tempCustomer ?: data).copy(email = event.email)
                                        }
                                        is ProfileEvent.PhoneNumberChange -> {
                                            tempCustomer = (tempCustomer ?: data).copy(phoneNumber = event.phoneNumber)
                                        }
                                        is ProfileEvent.DocumentNumberChange -> {
                                            tempCustomer = (tempCustomer ?: data).copy(documentNumber = event.documentNumber)
                                        }
                                        is ProfileEvent.AddressChange -> {
                                            tempCustomer = (tempCustomer ?: data).copy(address = event.address)
                                        }
                                        is ProfileEvent.PasswordChange -> {
                                            tempCustomer = (tempCustomer ?: data).copy(password = event.password)
                                        }
                                        ProfileEvent.SaveChanges -> {
                                            tempCustomer?.let { viewModel.updateProfile(it) }
                                            isEditing = false
                                        }
                                        ProfileEvent.CancelEdit -> {
                                            isEditing = false
                                            tempCustomer = null
                                        }
                                        else -> {}
                                    }
                                },
                                onLogout = {
                                    loginViewModel.logout()
                                    navController.navigate(Screen.LoginScreen) { popUpTo(0) }
                                }
                            )
                        }
                    }
                }
            }

            if (showChangePasswordDialog) {
                ChangesPasswordDialog(
                    onDismiss = { showChangePasswordDialog = false },
                    onChangePassword = { currentPass, newPass, confirmPass ->
                        viewModel.changePassword(
                            currentPass,
                            newPass,
                            confirmPass,
                            onSuccess = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Contraseña cambiada con éxito")
                                }
                            },
                            onError = { error ->
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error: $error")
                                }
                            }
                        )
                    }
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
    onLogout: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }

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
                text = "Artista",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        if (isEditing) {
            EditableProfileFields(
                artist,
                onEvent,
                isArtist = true,
                newPassword = newPassword,
                onPasswordChange = { newPassword = it }
            )
        } else {
            NonEditableProfileFields(
                artist,
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
    onLogout: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }

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
                text = "Cliente",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        if (isEditing) {
            EditableProfileFields(
                customer,
                onEvent,
                isArtist = false,
                newPassword = newPassword,
                onPasswordChange = { newPassword = it }
            )
        } else {
            NonEditableProfileFields(
                customer,
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
            text = "Detalles del perfil",
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
                        ProfileField(label = "Nombre de usuario", value = "@${userData.userName ?: "usuario"}")
                        ProfileField(label = "Correo electrónico", value = userData.email)
                        ProfileField(label = "Teléfono", value = userData.phoneNumber)
                        ProfileField(label = "Edad", value = "28")
                        ProfileField(label = "Número de documento", value = userData.documentNumber)
                        ProfileField(label = "Estilo artístico", value = userData.artStyle)
                        ProfileField(label = "Redes sociales", value = userData.socialMediaLinks)
                    }
                    is CustomersDto -> {
                        ProfileField(label = "Nombre de usuario", value = "${userData.userName ?: "usuario"}")
                        ProfileField(label = "Correo electrónico", value = userData.email)
                        ProfileField(label = "Teléfono", value = userData.phoneNumber)
                        ProfileField(label = "Edad", value = "28")
                        ProfileField(label = "Dirección", value = userData.address ?: "455 Oak Ave, Airytown, USA")
                        ProfileField(label = "Número de documento", value = userData.documentNumber)
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
                    Text("Cerrar sesión")
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
    newPassword: String,
    onPasswordChange: (String) -> Unit
) {
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
                when (userData) {
                    is ArtistsDto -> {
                        OutlinedTextField(
                            value = userData.firstName ?: "",
                            onValueChange = { onEvent(ProfileEvent.UserNameChange(it)) },
                            label = { Text("Nombre de Usuario") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = userData.firstName ?: "",
                            onValueChange = { onEvent(ProfileEvent.FirstNameChange(it)) },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = userData.lastName ?: "",
                            onValueChange = { onEvent(ProfileEvent.LastNameChange(it)) },
                            label = { Text("Apellido") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = userData.email ?: "",
                            onValueChange = { onEvent(ProfileEvent.EmailChange(it)) },
                            label = { Text("Correo electrónico") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = userData.phoneNumber ?: "",
                            onValueChange = { onEvent(ProfileEvent.PhoneNumberChange(it)) },
                            label = { Text("Teléfono") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = userData.documentNumber ?: "",
                            onValueChange = { onEvent(ProfileEvent.DocumentNumberChange(it)) },
                            label = { Text("Número de documento") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = userData.artStyle ?: "",
                            onValueChange = { onEvent(ProfileEvent.ArtStyleChange(it)) },
                            label = { Text("Estilo artístico") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = userData.socialMediaLinks ?: "",
                            onValueChange = { onEvent(ProfileEvent.SocialMediaLinksChange(it)) },
                            label = { Text("Redes sociales") },
                            modifier = Modifier.fillMaxWidth()
                        )

                    }

                    is CustomersDto -> {
                        OutlinedTextField(
                            value = userData.userName ?: "",
                            onValueChange = { onEvent(ProfileEvent.UserNameChange(it)) },
                            label = { Text("Nombre de Usuario") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = userData.firstName ?: "",
                            onValueChange = { onEvent(ProfileEvent.FirstNameChange(it)) },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = userData.lastName ?: "",
                            onValueChange = { onEvent(ProfileEvent.LastNameChange(it)) },
                            label = { Text("Apellido") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = userData.email ?: "",
                            onValueChange = { onEvent(ProfileEvent.EmailChange(it)) },
                            label = { Text("Correo electrónico") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = userData.phoneNumber ?: "",
                            onValueChange = { onEvent(ProfileEvent.PhoneNumberChange(it)) },
                            label = { Text("Teléfono") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = userData.documentNumber ?: "",
                            onValueChange = { onEvent(ProfileEvent.DocumentNumberChange(it)) },
                            label = { Text("Número de documento") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = userData.address ?: "455 Oak Ave, Airytown, USA",
                            onValueChange = { onEvent(ProfileEvent.AddressChange(it)) },
                            label = { Text("Dirección") },
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
                        onClick = { onEvent(ProfileEvent.CancelEdit) }
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = { onEvent(ProfileEvent.SaveChanges) }
                    ) {
                        Text("Guardar cambios")
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
