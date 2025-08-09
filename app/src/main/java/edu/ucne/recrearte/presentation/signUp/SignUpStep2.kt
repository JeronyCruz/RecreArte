package edu.ucne.recrearte.presentation.signUp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpStep2(
    uiState: SignUpUiState,
    onEvent: (SignUpEvent) -> Unit
) {
    val scrollState = rememberScrollState()
    val roundedShape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "Complete Your Profile",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // First Name
        OutlinedTextField(
            value = uiState.firstName,
            onValueChange = { onEvent(SignUpEvent.FirstNameChange(it)) },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = roundedShape,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            isError = uiState.errorFirstName != null
        )

        uiState.errorFirstName?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Last Name
        OutlinedTextField(
            value = uiState.lastName,
            onValueChange = { onEvent(SignUpEvent.LastNameChange(it)) },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = roundedShape,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            isError = uiState.errorLastName != null
        )

        uiState.errorLastName?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Username
        OutlinedTextField(
            value = uiState.userName,
            onValueChange = { onEvent(SignUpEvent.UserNameChange(it)) },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = roundedShape,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            isError = uiState.errorUserName != null
        )

        uiState.errorUserName?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Phone Number
        OutlinedTextField(
            value = uiState.phoneNumber,
            onValueChange = { onEvent(SignUpEvent.PhoneNumberChange(it)) },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = roundedShape,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            isError = uiState.errorPhoneNumber != null
        )

        uiState.errorPhoneNumber?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Document Number
        OutlinedTextField(
            value = uiState.documentNumber,
            onValueChange = { onEvent(SignUpEvent.DocumentNumberChange(it)) },
            label = { Text("Document Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = roundedShape,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            isError = uiState.errorDocumentNumber != null
        )

        uiState.errorDocumentNumber?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Role Selection
        Text(
            text = "Select your role:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onEvent(SignUpEvent.RoleChange(false)) }
            ) {
                Checkbox(
                    checked = !uiState.isArtist,
                    onCheckedChange = { onEvent(SignUpEvent.RoleChange(false)) }
                )
                Text("Customer")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onEvent(SignUpEvent.RoleChange(true)) }
            ) {
                Checkbox(
                    checked = uiState.isArtist,
                    onCheckedChange = { onEvent(SignUpEvent.RoleChange(true)) }
                )
                Text("Artist")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Conditional fields based on role
        if (uiState.isArtist) {
            // Art Style
            OutlinedTextField(
                value = uiState.artStyle,
                onValueChange = { onEvent(SignUpEvent.ArtStyleChange(it)) },
                label = { Text("Art Style") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = roundedShape,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                isError = uiState.errorArtStyle != null
            )

            uiState.errorArtStyle?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Social Media Links
            OutlinedTextField(
                value = uiState.socialMediaLinks,
                onValueChange = { onEvent(SignUpEvent.SocialMediaLinksChange(it)) },
                label = { Text("Social Media Links") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = roundedShape,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                isError = uiState.errorSocialMediaLinks != null
            )

            uiState.errorSocialMediaLinks?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        } else {
            // Address
            OutlinedTextField(
                value = uiState.address,
                onValueChange = { onEvent(SignUpEvent.AddressChange(it)) },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = roundedShape,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                isError = uiState.errorAddress != null
            )

            uiState.errorAddress?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sign Up Button
        Button(
            onClick = { onEvent(SignUpEvent.SignUp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(24.dp),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Sign Up")
            }
        }

        // Error message
        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}