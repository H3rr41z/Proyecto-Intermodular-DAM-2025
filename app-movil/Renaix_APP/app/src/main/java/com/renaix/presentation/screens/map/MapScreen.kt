package com.renaix.presentation.screens.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.renaix.di.AppContainer
import com.renaix.domain.model.Product
import com.renaix.presentation.common.state.UiState
import kotlinx.coroutines.launch

/**
 * Pantalla de mapa con productos geolocalizados
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    appContainer: AppContainer,
    onProductClick: (Int) -> Unit
) {
    var productsState by remember { mutableStateOf<UiState<List<Product>>>(UiState.Loading) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    val scope = rememberCoroutineScope()

    val getProductsUseCase = appContainer.getProductsUseCase

    // Posición inicial del mapa (Barcelona como ejemplo)
    val defaultPosition = LatLng(41.3851, 2.1734)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPosition, 12f)
    }

    LaunchedEffect(Unit) {
        scope.launch {
            productsState = UiState.Loading
            getProductsUseCase(page = 1)
                .onSuccess { products ->
                    productsState = UiState.Success(products)
                }
                .onFailure { exception ->
                    productsState = UiState.Error(exception.message ?: "Error al cargar productos")
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = false,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false
                )
            ) {
                when (val state = productsState) {
                    is UiState.Success -> {
                        state.data.forEach { product ->
                            // Si el producto tiene ubicación, mostrar marcador
                            // Nota: En producción, el producto tendría lat/lng
                            // Por ahora usamos una ubicación aleatoria para demo
                            val productLocation = getProductLocation(product)
                            if (productLocation != null) {
                                Marker(
                                    state = MarkerState(position = productLocation),
                                    title = product.nombre,
                                    snippet = "${product.precio}€",
                                    onClick = {
                                        selectedProduct = product
                                        true
                                    }
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }

            // Loading overlay
            if (productsState is UiState.Loading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Error snackbar
            if (productsState is UiState.Error) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text((productsState as UiState.Error).message)
                }
            }

            // Card del producto seleccionado
            selectedProduct?.let { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    onClick = { onProductClick(product.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icono/imagen del producto
                        Surface(
                            modifier = Modifier.size(60.dp),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.ShoppingBag,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.nombre,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${product.precio}€",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            product.ubicacion?.let { ubicacion ->
                                Text(
                                    text = ubicacion,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { selectedProduct = null }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Obtiene la ubicación del producto.
 * En una implementación real, el producto tendría coordenadas lat/lng.
 * Por ahora, si tiene ubicación textual, genera coordenadas de demo.
 */
private fun getProductLocation(product: Product): LatLng? {
    if (product.ubicacion.isNullOrBlank()) return null

    // En producción, usar las coordenadas reales del producto
    // Por ahora, generamos una posición aleatoria cerca de Barcelona para demo
    val baseLat = 41.3851
    val baseLng = 2.1734
    val offset = (product.id % 100) * 0.001

    return LatLng(baseLat + offset, baseLng + offset)
}
