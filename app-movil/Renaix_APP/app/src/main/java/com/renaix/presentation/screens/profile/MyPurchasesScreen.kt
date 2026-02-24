package com.renaix.presentation.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renaix.di.AppContainer
import com.renaix.domain.model.EstadoCompra
import com.renaix.domain.model.Purchase
import com.renaix.presentation.common.components.EmptyStateView
import com.renaix.presentation.common.components.ErrorView
import com.renaix.presentation.common.components.LoadingIndicator
import com.renaix.presentation.common.components.RatingDialog
import com.renaix.presentation.common.state.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPurchasesScreen(
    appContainer: AppContainer,
    onNavigateBack: () -> Unit,
    onProductClick: (Int) -> Unit = {}
) {
    var state by remember { mutableStateOf<UiState<List<Purchase>>>(UiState.Loading) }
    val scope = rememberCoroutineScope()
    val userRepository = appContainer.userRepository
    val purchaseRepository = appContainer.purchaseRepository
    val ratingRepository = appContainer.ratingRepository
    val snackbarHostState = remember { SnackbarHostState() }

    var purchaseToRate by remember { mutableStateOf<Purchase?>(null) }

    fun load() {
        scope.launch {
            state = UiState.Loading
            userRepository.getMyPurchases()
                .onSuccess { state = UiState.Success(it) }
                .onFailure { state = UiState.Error(it.message ?: "Error al cargar tus compras") }
        }
    }

    LaunchedEffect(Unit) { load() }

    purchaseToRate?.let { purchase ->
        RatingDialog(
            onDismiss = { purchaseToRate = null },
            onConfirm = { puntuacion, comentario ->
                purchaseToRate = null
                scope.launch {
                    ratingRepository.ratePurchase(purchase.id, puntuacion, comentario)
                        .onSuccess {
                            snackbarHostState.showSnackbar("Valoración enviada correctamente")
                            load()
                        }
                        .onFailure {
                            snackbarHostState.showSnackbar(it.message ?: "Error al enviar valoración")
                        }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis compras") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val currentState = state) {
            is UiState.Loading -> LoadingIndicator(
                modifier = Modifier.padding(padding),
                message = "Cargando tus compras..."
            )
            is UiState.Error -> ErrorView(
                message = currentState.message,
                onRetry = { load() },
                modifier = Modifier.padding(padding)
            )
            is UiState.Success -> {
                val purchases = currentState.data
                if (purchases.isEmpty()) {
                    EmptyStateView(
                        title = "Sin compras",
                        message = "Aún no has realizado ninguna compra",
                        icon = Icons.Filled.ShoppingCart,
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        items(items = purchases, key = { it.id }) { purchase ->
                            PurchaseItem(
                                purchase = purchase,
                                onProductClick = { onProductClick(purchase.producto.id) },
                                onCompletar = {
                                    scope.launch {
                                        purchaseRepository.completePurchase(purchase.id)
                                            .onSuccess { load() }
                                            .onFailure { snackbarHostState.showSnackbar(it.message ?: "Error al completar") }
                                    }
                                },
                                onCancelar = {
                                    scope.launch {
                                        purchaseRepository.cancelPurchase(purchase.id)
                                            .onSuccess { load() }
                                            .onFailure { snackbarHostState.showSnackbar(it.message ?: "Error al cancelar") }
                                    }
                                },
                                onValorar = { purchaseToRate = purchase }
                            )
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun PurchaseItem(
    purchase: Purchase,
    onProductClick: () -> Unit,
    onCompletar: () -> Unit,
    onCancelar: () -> Unit,
    onValorar: () -> Unit
) {
    val statusColor = when (purchase.estado) {
        EstadoCompra.COMPLETADA -> MaterialTheme.colorScheme.primary
        EstadoCompra.CONFIRMADA -> MaterialTheme.colorScheme.tertiary
        EstadoCompra.CANCELADA -> MaterialTheme.colorScheme.error
        EstadoCompra.PENDIENTE -> MaterialTheme.colorScheme.secondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProductClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = purchase.producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = purchase.estado.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Vendedor: ${purchase.vendedor.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format("%.2f €", purchase.precioFinal),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                purchase.fechaCompra?.let { fecha ->
                    Text(
                        text = fecha,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val showCompletar = purchase.estado == EstadoCompra.CONFIRMADA
            val showCancelar = purchase.estado != EstadoCompra.COMPLETADA && purchase.estado != EstadoCompra.CANCELADA
            val showValorar = purchase.estado == EstadoCompra.COMPLETADA && !purchase.compradorValoro

            if (showCompletar || showCancelar || showValorar) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showValorar) {
                        OutlinedButton(onClick = onValorar, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Valorar", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    if (showCompletar) {
                        Button(onClick = onCompletar, modifier = Modifier.weight(1f)) {
                            Text("Completar", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    if (showCancelar) {
                        OutlinedButton(
                            onClick = onCancelar,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Cancelar", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
