package com.renaix.presentation.screens.products.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renaix.di.AppContainer
import com.renaix.presentation.common.components.*

/**
 * Pantalla de lista de productos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    appContainer: AppContainer,
    onProductClick: (Int) -> Unit
) {
    val viewModel = remember { ProductListViewModel(appContainer.getProductsUseCase) }
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar errores
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productos") },
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !state.isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Actualizar"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading && state.items.isEmpty() -> {
                    LoadingIndicator(message = "Cargando productos...")
                }

                state.showEmptyState -> {
                    EmptyStateView(
                        title = "No hay productos",
                        message = "Sé el primero en publicar un producto",
                        icon = Icons.Filled.ShoppingBag,
                        actionText = "Actualizar",
                        onAction = { viewModel.loadProducts() }
                    )
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = state.items,
                            key = { it.id }
                        ) { product ->
                            ProductCard(
                                product = product,
                                onClick = { onProductClick(product.id) }
                            )
                        }

                        // Loading more indicator
                        if (state.isLoading && state.items.isNotEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                ListLoadingIndicator()
                            }
                        }

                        // Load more trigger
                        if (!state.endReached && !state.isLoading) {
                            item(span = { GridItemSpan(2) }) {
                                LaunchedEffect(Unit) {
                                    viewModel.loadMore()
                                }
                            }
                        }
                    }
                }
            }

            // Show refresh indicator
            if (state.isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
