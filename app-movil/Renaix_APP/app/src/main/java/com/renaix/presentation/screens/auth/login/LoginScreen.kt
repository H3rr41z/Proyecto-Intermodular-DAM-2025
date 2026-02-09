package com.renaix.presentation.screens.auth.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.renaix.di.AppContainer
import com.renaix.presentation.common.components.RenaixButton
import com.renaix.presentation.common.components.RenaixPasswordField
import com.renaix.presentation.common.components.RenaixTextField
import com.renaix.presentation.common.components.RenaixTextButton
import com.renaix.presentation.common.state.UiState

/**
 * Pantalla de Login
 */
@Composable
fun LoginScreen(
    appContainer: AppContainer,
    onNavigateToRegister: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val viewModel = remember { LoginViewModel(appContainer.loginUseCase) }

    val uiState by viewModel.uiState.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Manejar estado de éxito
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo/Título
            Text(
                text = "RENAIX",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Inicia sesión para continuar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Formulario
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

            RenaixPasswordField(
                value = password,
                onValueChange = viewModel::onPasswordChange,
                label = "Contraseña",
                leadingIcon = Icons.Filled.Lock,
                imeAction = ImeAction.Done,
                onImeAction = { viewModel.login() },
                isError = passwordError != null,
                errorMessage = passwordError
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de login
            RenaixButton(
                text = "Iniciar sesión",
                onClick = { viewModel.login() },
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Link a registro
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿No tienes cuenta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                RenaixTextButton(
                    text = "Regístrate",
                    onClick = onNavigateToRegister
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
