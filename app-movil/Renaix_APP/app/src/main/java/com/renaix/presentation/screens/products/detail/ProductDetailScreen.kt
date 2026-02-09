package com.renaix.presentation.screens.products.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.renaix.di.AppContainer
import com.renaix.presentation.common.components.ErrorView
import com.renaix.presentation.common.components.LoadingIndicator
import com.renaix.presentation.common.components.RenaixButton
import com.renaix.presentation.common.state.UiState
import com.renaix.ui.theme.CustomShapes
import com.renaix.util.Constants
import java.text.NumberFormat
import java.util.Locale

/**
 * Pantalla de detalle de producto
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Int,
    appContainer: AppContainer,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (Int, Int) -> Unit,
    onNavigateToPublicProfile: (Int) -> Unit
) {
    val viewModel = remember {
        ProductDetailViewModel(
            appContainer.getProductDetailUseCase,
            appContainer.buyProductUseCase
        )
    }

    val state by viewModel.state.collectAsState()
    val buyState by viewModel.buyState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    LaunchedEffect(buyState) {
        when (buyState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Compra realizada correctamente")
                viewModel.resetBuyState()
                viewModel.loadProduct(productId)
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((buyState as UiState.Error).message)
                viewModel.resetBuyState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val currentState = state) {
            is UiState.Loading -> {
                LoadingIndicator(
                    modifier = Modifier.padding(padding),
                    message = "Cargando producto..."
                )
            }

            is UiState.Error -> {
                ErrorView(
                    message = currentState.message,
                    onRetry = { viewModel.loadProduct(productId) },
                    modifier = Modifier.padding(padding)
                )
            }

            is UiState.Success -> {
                val product = currentState.data

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Imágenes
                    if (product.imagenes.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(product.imagenes) { imagen ->
                                val imageUrl = if (imagen.urlImagen.startsWith("http")) {
                                    imagen.urlImagen
                                } else {
                                    "${Constants.API_BASE_URL.removeSuffix("/api/v1")}${imagen.urlImagen}"
                                }

                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(280.dp)
                                        .clip(CustomShapes.ProductImage),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contenido
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        // Precio
                        Text(
                            text = formatPrice(product.precio),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Nombre
                        Text(
                            text = product.nombre,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Estado y categoría
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(
                                onClick = { },
                                label = { Text(product.estadoProducto.displayName) }
                            )
                            AssistChip(
                                onClick = { },
                                label = { Text(product.categoria.name) }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Descripción
                        if (!product.descripcion.isNullOrBlank()) {
                            Text(
                                text = "Descripción",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = product.descripcion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Ubicación
                        if (!product.ubicacion.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = product.ubicacion,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Vendedor
                        Card(
                            onClick = { onNavigateToPublicProfile(product.propietario.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.propietario.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    product.propietario.valoracionPromedio?.let { rating ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Filled.Star,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = String.format("%.1f", rating),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                                Icon(
                                    Icons.Filled.ChevronRight,
                                    contentDescription = null
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Botones de acción
                        if (product.estadoVenta.value == "disponible") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onNavigateToChat(product.propietario.id, productId) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.Chat, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Contactar")
                                }

                                RenaixButton(
                                    text = "Comprar",
                                    onClick = { viewModel.buyProduct(productId) },
                                    isLoading = buyState.isLoading,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }

            else -> {}
        }
    }
}

private fun formatPrice(price: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    return format.format(price)
}
