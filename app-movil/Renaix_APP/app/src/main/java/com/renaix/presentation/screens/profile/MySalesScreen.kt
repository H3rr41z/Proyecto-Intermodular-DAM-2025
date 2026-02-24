package com.renaix.presentation.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Sell
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
fun MySalesScreen(
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

    var saleToRate by remember { mutableStateOf<Purchase?>(null) }

    fun load() {
        scope.launch {
            state = UiState.Loading
            userRepository.getMySales()
                .onSuccess { state = UiState.Success(it) }
                .onFailure { state = UiState.Error(it.message ?: "Error al cargar tus ventas") }
        }
    }

    LaunchedEffect(Unit) { load() }

    saleToRate?.let { sale ->
        RatingDialog(
            onDismiss = { saleToRate = null },
            onConfirm = { puntuacion, comentario ->
                saleToRate = null
                scope.launch {
                    ratingRepository.ratePurchase(sale.id, puntuacion, comentario)
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
                title = { Text("Mis ventas") },
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
                message = "Cargando tus ventas..."
            )
            is UiState.Error -> ErrorView(
                message = currentState.message,
                onRetry = { load() },
                modifier = Modifier.padding(padding)
            )
            is UiState.Success -> {
                val sales = currentState.data
                if (sales.isEmpty()) {
                    EmptyStateView(
                        title = "Sin ventas",
                        message = "Aún no has realizado ninguna venta",
                        icon = Icons.Filled.Sell,
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
                        items(items = sales, key = { it.id }) { sale ->
                            SaleItem(
                                sale = sale,
                                onProductClick = { onProductClick(sale.producto.id) },
                                onConfirmar = {
                                    scope.launch {
                                        purchaseRepository.confirmPurchase(sale.id)
                                            .onSuccess { load() }
                                            .onFailure { snackbarHostState.showSnackbar(it.message ?: "Error al confirmar") }
                                    }
                                },
                                onCancelar = {
                                    scope.launch {
                                        purchaseRepository.cancelPurchase(sale.id)
                                            .onSuccess { load() }
                                            .onFailure { snackbarHostState.showSnackbar(it.message ?: "Error al cancelar") }
                                    }
                                },
                                onValorar = { saleToRate = sale }
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
private fun SaleItem(
    sale: Purchase,
    onProductClick: () -> Unit,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    onValorar: () -> Unit
) {
    val statusColor = when (sale.estado) {
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
                    text = sale.producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = sale.estado.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Comprador: ${sale.comprador.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format("%.2f €", sale.precioFinal),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                sale.fechaCompra?.let { fecha ->
                    Text(
                        text = fecha,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val showConfirmar = sale.estado == EstadoCompra.PENDIENTE
            val showCancelar = sale.estado != EstadoCompra.COMPLETADA && sale.estado != EstadoCompra.CANCELADA
            val showValorar = sale.estado == EstadoCompra.COMPLETADA && !sale.vendedorValoro

            if (showConfirmar || showCancelar || showValorar) {
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
                    if (showConfirmar) {
                        Button(onClick = onConfirmar, modifier = Modifier.weight(1f)) {
                            Text("Confirmar", style = MaterialTheme.typography.labelMedium)
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
