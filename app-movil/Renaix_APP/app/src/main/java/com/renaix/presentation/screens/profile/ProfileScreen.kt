package com.renaix.presentation.screens.profile

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
import androidx.compose.ui.unit.dp
import com.renaix.di.AppContainer
import com.renaix.presentation.common.components.ErrorView
import com.renaix.presentation.common.components.LoadingIndicator
import com.renaix.presentation.common.state.UiState

/**
 * Pantalla de perfil del usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    appContainer: AppContainer,
    onNavigateToMyProducts: () -> Unit,
    onNavigateToMyPurchases: () -> Unit,
    onNavigateToMySales: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel = remember {
        ProfileViewModel(
            appContainer.getProfileUseCase,
            appContainer.logoutUseCase
        )
    }

    val state by viewModel.state.collectAsState()
    val logoutState by viewModel.logoutState.collectAsState()

    LaunchedEffect(logoutState) {
        if (logoutState is UiState.Success) {
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Ajustes")
                    }
                }
            )
        }
    ) { padding ->
        when (val currentState = state) {
            is UiState.Loading -> {
                LoadingIndicator(
                    modifier = Modifier.padding(padding),
                    message = "Cargando perfil..."
                )
            }

            is UiState.Error -> {
                ErrorView(
                    message = currentState.message,
                    onRetry = { viewModel.loadProfile() },
                    modifier = Modifier.padding(padding)
                )
            }

            is UiState.Success -> {
                val user = currentState.data

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header con avatar y datos
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar
                            Surface(
                                modifier = Modifier.size(80.dp),
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Nombre
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            // Email
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Valoración
                            user.valoracionPromedio?.let { rating ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = String.format("%.1f", rating),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    user.totalValoraciones?.let { total ->
                                        Text(
                                            text = " ($total valoraciones)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Opciones del menú
                    ProfileMenuItem(
                        icon = Icons.Filled.ShoppingBag,
                        title = "Mis productos",
                        subtitle = "Gestiona tus productos publicados",
                        onClick = onNavigateToMyProducts
                    )

                    ProfileMenuItem(
                        icon = Icons.Filled.ShoppingCart,
                        title = "Mis compras",
                        subtitle = "Historial de compras realizadas",
                        onClick = onNavigateToMyPurchases
                    )

                    ProfileMenuItem(
                        icon = Icons.Filled.Sell,
                        title = "Mis ventas",
                        subtitle = "Historial de ventas realizadas",
                        onClick = onNavigateToMySales
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Cerrar sesión
                    ProfileMenuItem(
                        icon = Icons.Filled.Logout,
                        title = "Cerrar sesión",
                        subtitle = "Salir de tu cuenta",
                        onClick = { viewModel.logout() },
                        isDestructive = true,
                        isLoading = logoutState.isLoading
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            else -> {}
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    isLoading: Boolean = false
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!isLoading) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
    )
}

private fun Modifier.clickable(onClick: () -> Unit): Modifier {
    return this.then(
        androidx.compose.foundation.clickable(onClick = onClick)
    )
}
