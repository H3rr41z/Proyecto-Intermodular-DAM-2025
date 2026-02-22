package com.renaix.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renaix.di.AppContainer
import com.renaix.domain.model.Product
import com.renaix.domain.model.PublicUser
import com.renaix.domain.model.Rating
import com.renaix.presentation.common.components.*
import com.renaix.presentation.common.state.UiState
import kotlinx.coroutines.launch

/**
 * Pantalla de perfil público de un usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    userId: Int,
    appContainer: AppContainer,
    onNavigateBack: () -> Unit,
    onProductClick: (Int) -> Unit,
    onNavigateToChat: (Int) -> Unit
) {
    var userState by remember { mutableStateOf<UiState<PublicUser>>(UiState.Loading) }
    var productsState by remember { mutableStateOf<UiState<List<Product>>>(UiState.Loading) }
    var ratingsState by remember { mutableStateOf<UiState<List<Rating>>>(UiState.Loading) }
    val scope = rememberCoroutineScope()

    val userRepository = appContainer.userRepository
    val ratingRepository = appContainer.ratingRepository

    fun loadData() {
        scope.launch {
            userState = UiState.Loading
            userRepository.getPublicProfile(userId)
                .onSuccess { user -> userState = UiState.Success(user) }
                .onFailure { e -> userState = UiState.Error(e.message ?: "Error") }
        }
        scope.launch {
            productsState = UiState.Loading
            userRepository.getUserProducts(userId)
                .onSuccess { products -> productsState = UiState.Success(products) }
                .onFailure { e -> productsState = UiState.Error(e.message ?: "Error") }
        }
        scope.launch {
            ratingsState = UiState.Loading
            ratingRepository.getUserRatings(userId)
                .onSuccess { ratings -> ratingsState = UiState.Success(ratings) }
                .onFailure { ratingsState = UiState.Idle }
        }
    }

    LaunchedEffect(userId) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        when (val currentUserState = userState) {
            is UiState.Loading -> {
                LoadingIndicator(
                    modifier = Modifier.padding(padding),
                    message = "Cargando perfil..."
                )
            }

            is UiState.Error -> {
                ErrorView(
                    message = currentUserState.message,
                    onRetry = { loadData() },
                    modifier = Modifier.padding(padding)
                )
            }

            is UiState.Success -> {
                val user = currentUserState.data

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Header del usuario
                    item(span = { GridItemSpan(2) }) {
                        Card(
                            modifier = Modifier.fillMaxWidth()
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

                                Text(
                                    text = user.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                // Valoración
                                if (user.valoracionPromedio > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = String.format("%.1f", user.valoracionPromedio),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Botón de contactar
                                OutlinedButton(
                                    onClick = { onNavigateToChat(userId) }
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Contactar")
                                }
                            }
                        }
                    }

                    // Título de productos
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = "Productos en venta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Productos del usuario
                    when (val currentProductsState = productsState) {
                        is UiState.Loading -> {
                            item(span = { GridItemSpan(2) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        is UiState.Error -> {
                            item(span = { GridItemSpan(2) }) {
                                Text(
                                    text = currentProductsState.message,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        is UiState.Success -> {
                            val products = currentProductsState.data
                            if (products.isEmpty()) {
                                item(span = { GridItemSpan(2) }) {
                                    EmptyStateView(
                                        title = "Sin productos",
                                        message = "Este usuario no tiene productos en venta",
                                        icon = Icons.Filled.ShoppingBag
                                    )
                                }
                            } else {
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

                        else -> {}
                    }

                    // ── Valoraciones recibidas ────────────────────────
                    item(span = { GridItemSpan(2) }) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Valoraciones",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    when (val currentRatingsState = ratingsState) {
                        is UiState.Loading -> {
                            item(span = { GridItemSpan(2) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        is UiState.Success -> {
                            val ratings = currentRatingsState.data
                            if (ratings.isEmpty()) {
                                item(span = { GridItemSpan(2) }) {
                                    Text(
                                        text = "Sin valoraciones todavía",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            } else {
                                items(
                                    items = ratings,
                                    key = { it.id },
                                    span = { GridItemSpan(2) }
                                ) { rating ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                repeat(rating.puntuacion) {
                                                    Icon(
                                                        Icons.Filled.Star,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                repeat(5 - rating.puntuacion) {
                                                    Icon(
                                                        Icons.Filled.Star,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.outlineVariant
                                                    )
                                                }
                                                rating.valorador?.let { valorador ->
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = valorador.name,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            rating.comentario?.takeIf { it.isNotBlank() }?.let { texto ->
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = texto,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                            rating.fecha?.let { fecha ->
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = fecha,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        else -> {}
                    }
                }
            }

            else -> {}
        }
    }
}
