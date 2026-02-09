package com.renaix.presentation.screens.products.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renaix.di.AppContainer
import com.renaix.domain.model.Product
import com.renaix.domain.usecase.product.SearchProductsUseCase
import com.renaix.presentation.common.components.*
import com.renaix.presentation.common.state.UiState
import com.renaix.ui.theme.CustomShapes
import kotlinx.coroutines.launch

/**
 * Pantalla de búsqueda de productos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    appContainer: AppContainer,
    onProductClick: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<UiState<List<Product>>>(UiState.Idle) }
    val scope = rememberCoroutineScope()

    val searchProducts = appContainer.searchProductsUseCase

    fun performSearch() {
        if (searchQuery.isBlank()) return

        scope.launch {
            searchResults = UiState.Loading
            searchProducts(query = searchQuery)
                .onSuccess { products ->
                    searchResults = UiState.Success(products)
                }
                .onFailure { exception ->
                    searchResults = UiState.Error(exception.message ?: "Error en la búsqueda")
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar productos...") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = CustomShapes.SearchBar,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Limpiar")
                                }
                            }
                        }
                    )
                },
                actions = {
                    IconButton(onClick = { performSearch() }) {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = searchResults) {
                is UiState.Idle -> {
                    EmptyStateView(
                        title = "Buscar productos",
                        message = "Escribe algo para buscar productos",
                        icon = Icons.Filled.Search
                    )
                }

                is UiState.Loading -> {
                    LoadingIndicator(message = "Buscando...")
                }

                is UiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { performSearch() }
                    )
                }

                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        EmptyStateView(
                            title = "Sin resultados",
                            message = "No se encontraron productos para \"$searchQuery\"",
                            icon = Icons.Filled.SearchOff
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = state.data,
                                key = { it.id }
                            ) { product ->
                                ProductCard(
                                    product = product,
                                    onClick = { onProductClick(product.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
