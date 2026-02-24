package com.renaix.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renaix.di.AppContainer
import com.renaix.domain.model.Product
import com.renaix.presentation.common.components.EmptyStateView
import com.renaix.presentation.common.components.ErrorView
import com.renaix.presentation.common.components.LoadingIndicator
import com.renaix.presentation.common.components.ProductCard
import com.renaix.presentation.common.state.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProductsScreen(
    appContainer: AppContainer,
    onNavigateBack: () -> Unit,
    onProductClick: (Int) -> Unit
) {
    var state by remember { mutableStateOf<UiState<List<Product>>>(UiState.Loading) }
    val scope = rememberCoroutineScope()
    val userRepository = appContainer.userRepository

    fun load() {
        scope.launch {
            state = UiState.Loading
            userRepository.getMyProducts()
                .onSuccess { state = UiState.Success(it) }
                .onFailure { state = UiState.Error(it.message ?: "Error al cargar tus productos") }
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis productos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        when (val currentState = state) {
            is UiState.Loading -> LoadingIndicator(
                modifier = Modifier.padding(padding),
                message = "Cargando tus productos..."
            )
            is UiState.Error -> ErrorView(
                message = currentState.message,
                onRetry = { load() },
                modifier = Modifier.padding(padding)
            )
            is UiState.Success -> {
                val products = currentState.data
                if (products.isEmpty()) {
                    EmptyStateView(
                        title = "Sin productos",
                        message = "Aún no has publicado ningún producto",
                        icon = Icons.Filled.ShoppingBag,
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        items(
                            items = products,
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
            else -> {}
        }
    }
}
