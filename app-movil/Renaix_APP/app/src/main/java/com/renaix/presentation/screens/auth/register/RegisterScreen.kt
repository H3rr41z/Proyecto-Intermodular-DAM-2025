package com.renaix.presentation.screens.auth.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.renaix.di.AppContainer
import com.renaix.presentation.common.components.RenaixButton
import com.renaix.presentation.common.components.RenaixPasswordField
import com.renaix.presentation.common.components.RenaixTextField
import com.renaix.presentation.common.components.RenaixTextButton
import com.renaix.presentation.common.state.UiState

/**
 * Pantalla de Registro
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    appContainer: AppContainer,
    onNavigateBack: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val viewModel = remember { RegisterViewModel(appContainer.registerUseCase) }

    val uiState by viewModel.uiState.collectAsState()
    val name by viewModel.name.collectAsState()
    val email by viewModel.email.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val nameError by viewModel.nameError.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            onNavigateToMain()
        } else if (uiState is UiState.Error) {
            snackbarHostState.showSnackbar(
                message = (uiState as UiState.Error).message,
                duration = SnackbarDuration.Short
            )
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Únete a Renaix",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Crea tu cuenta para empezar a comprar y vender",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Nombre
            RenaixTextField(
                value = name,
                onValueChange = viewModel::onNameChange,
                label = "Nombre completo",
                leadingIcon = Icons.Filled.Person,
                imeAction = ImeAction.Next,
                isError = nameError != null,
                errorMessage = nameError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email
            RenaixTextField(
                value = email,
                onValueChange = viewModel::onEmailChange,
                label = "Email",
                leadingIcon = Icons.Filled.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                isError = emailError != null,
                errorMessage = emailError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Teléfono (opcional)
            RenaixTextField(
                value = phone,
                onValueChange = viewModel::onPhoneChange,
                label = "Teléfono (opcional)",
                leadingIcon = Icons.Filled.Phone,
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contraseña
            RenaixPasswordField(
                value = password,
                onValueChange = viewModel::onPasswordChange,
                label = "Contraseña",
                leadingIcon = Icons.Filled.Lock,
                imeAction = ImeAction.Next,
                isError = passwordError != null,
                errorMessage = passwordError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirmar contraseña
            RenaixPasswordField(
                value = confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = "Confirmar contraseña",
                leadingIcon = Icons.Filled.Lock,
                imeAction = ImeAction.Done,
                onImeAction = { viewModel.register() },
                isError = confirmPasswordError != null,
                errorMessage = confirmPasswordError
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de registro
            RenaixButton(
                text = "Crear cuenta",
                onClick = { viewModel.register() },
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Link a login
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Ya tienes cuenta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                RenaixTextButton(
                    text = "Inicia sesión",
                    onClick = onNavigateBack
                )
            }
        }
    }
}
