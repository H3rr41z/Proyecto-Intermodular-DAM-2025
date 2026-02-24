package com.renaix.presentation.screens.products.list

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.renaix.di.AppContainer
import com.renaix.domain.model.Category
import com.renaix.presentation.common.components.*
import com.valentinilk.shimmer.shimmer

/**
 * Pantalla de lista de productos con pull-to-refresh
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    appContainer: AppContainer,
    onProductClick: (Int) -> Unit
) {
    val viewModel = remember { ProductListViewModel(appContainer.productRepository) }
    val state by viewModel.state.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Categorías y filtro
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }

    // Cargar categorías
    LaunchedEffect(Unit) {
        appContainer.categoryRepository.getCategories().onSuccess {
            categories = it
        }
    }

    // Filtrar productos por categoría seleccionada
    val filteredItems = remember(state.items, selectedCategoryId) {
        if (selectedCategoryId == null) {
            state.items
        } else {
            state.items.filter { it.categoria.id == selectedCategoryId }
        }
    }

    // Pull-to-refresh state
    val pullToRefreshState = rememberPullToRefreshState()

    // Manejar el gesto de pull-to-refresh
    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            viewModel.refresh()
        }
    }

    // Actualizar estado del indicador cuando termina el refresh
    LaunchedEffect(state.isRefreshing) {
        if (!state.isRefreshing) {
            pullToRefreshState.endRefresh()
        }
    }

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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            when {
                state.isLoading && state.items.isEmpty() -> {
                    // Skeleton loaders con shimmer
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .shimmer()
                    ) {
                        items(6) {
                            ProductCardPlaceholder()
                        }
                    }
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
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Filtros por categoría
                        if (categories.isNotEmpty()) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Chip "Todos"
                                item {
                                    FilterChip(
                                        selected = selectedCategoryId == null,
                                        onClick = { selectedCategoryId = null },
                                        label = { Text("Todos") },
                                        leadingIcon = if (selectedCategoryId == null) {
                                            { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(18.dp)) }
                                        } else null
                                    )
                                }
                                // Chips de categorías
                                items(categories) { category ->
                                    FilterChip(
                                        selected = selectedCategoryId == category.id,
                                        onClick = {
                                            selectedCategoryId = if (selectedCategoryId == category.id) null else category.id
                                        },
                                        label = { Text(category.name) },
                                        leadingIcon = if (selectedCategoryId == category.id) {
                                            { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(18.dp)) }
                                        } else null
                                    )
                                }
                            }
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(
                                items = filteredItems,
                                key = { _, product -> product.id }
                            ) { index, product ->
                            // Animación de entrada escalonada
                            val animatedProgress = remember { Animatable(0f) }
                            LaunchedEffect(product.id) {
                                animatedProgress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        delayMillis = index * 50,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                            }

                            ProductCard(
                                product = product,
                                onClick = { onProductClick(product.id) },
                                isFavorite = favorites.contains(product.id),
                                onFavoriteClick = { viewModel.toggleFavorite(product.id) },
                                modifier = Modifier.graphicsLayer {
                                    alpha = animatedProgress.value
                                    translationY = (1f - animatedProgress.value) * 50f
                                }
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
            }

            // Pull-to-refresh indicator — solo visible cuando el usuario arrastra o se está recargando
            if (pullToRefreshState.progress > 0f || pullToRefreshState.isRefreshing) {
                PullToRefreshContainer(
                    state = pullToRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
