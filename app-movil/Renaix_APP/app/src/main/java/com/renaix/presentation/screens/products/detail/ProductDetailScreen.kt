package com.renaix.presentation.screens.products.detail

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.renaix.di.AppContainer
import com.renaix.domain.model.CategoriaDenuncia
import com.renaix.domain.model.EstadoVenta
import com.renaix.presentation.common.components.ErrorView
import com.renaix.presentation.common.components.LoadingIndicator
import com.renaix.presentation.common.components.OfferDialog
import com.renaix.presentation.common.components.RenaixButton
import com.renaix.presentation.common.components.ZoomableImageDialog
import com.renaix.presentation.common.state.UiState
import com.renaix.ui.theme.Purple500
import com.renaix.ui.theme.CustomShapes
import com.renaix.util.Constants
import com.renaix.util.toEuroPrice
import kotlinx.coroutines.launch

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
    onNavigateToPublicProfile: (Int) -> Unit,
    onNavigateToEditProduct: (Int) -> Unit = {}
) {
    val viewModel = remember {
        ProductDetailViewModel(
            appContainer.productRepository,
            appContainer.purchaseRepository,
            appContainer.commentRepository,
            appContainer.reportRepository,
            appContainer.userRepository
        )
    }

    val state by viewModel.state.collectAsState()
    val buyState by viewModel.buyState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val commentActionState by viewModel.commentActionState.collectAsState()
    val reportState by viewModel.reportState.collectAsState()
    val publishState by viewModel.publishState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val chatRepository = appContainer.chatRepository
    val context = LocalContext.current
    var isSendingOffer by remember { mutableStateOf(false) }

    // Función para compartir producto
    fun shareProduct(productName: String, productPrice: String, productDescription: String) {
        val shareText = """
            |🦋 ¡Mira este producto en Renaix!
            |
            |📦 $productName
            |💰 $productPrice
            |
            |$productDescription
            |
            |Descarga Renaix para ver más productos de segunda mano.
        """.trimMargin()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir producto"))
    }

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

    LaunchedEffect(commentActionState) {
        when (commentActionState) {
            is UiState.Error -> {
                snackbarHostState.showSnackbar((commentActionState as UiState.Error).message)
                viewModel.resetCommentActionState()
            }
            is UiState.Success -> viewModel.resetCommentActionState()
            else -> {}
        }
    }

    LaunchedEffect(reportState) {
        when (reportState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Denuncia enviada correctamente")
                viewModel.resetReportState()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((reportState as UiState.Error).message)
                viewModel.resetReportState()
            }
            else -> {}
        }
    }

    LaunchedEffect(publishState) {
        when (publishState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Producto publicado correctamente")
                viewModel.resetPublishState()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((publishState as UiState.Error).message)
                viewModel.resetPublishState()
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (state is UiState.Success) {
                        val product = (state as UiState.Success).data
                        // Botón editar (solo propietario)
                        if (currentUserId == product.propietario.id) {
                            IconButton(onClick = { onNavigateToEditProduct(productId) }) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Editar producto",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        // Botón de favorito
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Botón de compartir
                        IconButton(
                            onClick = {
                                shareProduct(
                                    product.nombre,
                                    product.precio.toEuroPrice(),
                                    product.descripcion?.let { if (it.length > 100) it.take(100) + "..." else it } ?: ""
                                )
                            }
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "Compartir producto")
                        }
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
                    // Estado para el zoom de imagen
                    var zoomImageUrl by remember { mutableStateOf<String?>(null) }

                    // Dialog de zoom
                    zoomImageUrl?.let { url ->
                        ZoomableImageDialog(
                            imageUrl = url,
                            onDismiss = { zoomImageUrl = null }
                        )
                    }

                    // Imágenes con indicador
                    if (product.imagenes.isNotEmpty()) {
                        val lazyRowState = rememberLazyListState()
                        val currentImageIndex by remember {
                            derivedStateOf {
                                val layoutInfo = lazyRowState.layoutInfo
                                val visibleItems = layoutInfo.visibleItemsInfo
                                if (visibleItems.isEmpty()) 0
                                else {
                                    val firstVisibleItem = visibleItems.first()
                                    val itemWidth = firstVisibleItem.size
                                    val offset = lazyRowState.firstVisibleItemScrollOffset
                                    if (offset > itemWidth / 2) {
                                        (lazyRowState.firstVisibleItemIndex + 1).coerceAtMost(product.imagenes.size - 1)
                                    } else {
                                        lazyRowState.firstVisibleItemIndex
                                    }
                                }
                            }
                        }

                        LazyRow(
                            state = lazyRowState,
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(product.imagenes) { imagen ->
                                val imageUrl = Constants.imageUrl(imagen.urlImagen)

                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Toca para ampliar",
                                    modifier = Modifier
                                        .size(280.dp)
                                        .clip(CustomShapes.ProductImage)
                                        .clickable { zoomImageUrl = imageUrl },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        // Indicador de posición (puntos)
                        if (product.imagenes.size > 1) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                product.imagenes.forEachIndexed { index, _ ->
                                    val isSelected = index == currentImageIndex
                                    Surface(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .size(if (isSelected) 10.dp else 8.dp),
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.outlineVariant
                                    ) {}
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contenido
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        // Precio
                        Text(
                            text = product.precio.toEuroPrice(),
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

                        // Ubicación con mini-mapa
                        if (!product.ubicacion.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))

                            // Calcular coordenadas para el mapa (demo basado en ID)
                            val productLocation = remember(product.id) {
                                val baseLat = 41.3851 // Barcelona
                                val baseLng = 2.1734
                                val offset = (product.id % 100) * 0.001
                                LatLng(baseLat + offset, baseLng + offset)
                            }

                            val cameraPositionState = rememberCameraPositionState {
                                position = CameraPosition.fromLatLngZoom(productLocation, 15f)
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column {
                                    // Texto de ubicación
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = product.ubicacion,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    // Mini-mapa
                                    GoogleMap(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)),
                                        cameraPositionState = cameraPositionState,
                                        properties = MapProperties(
                                            isMyLocationEnabled = false,
                                            mapType = MapType.NORMAL
                                        ),
                                        uiSettings = MapUiSettings(
                                            zoomControlsEnabled = false,
                                            zoomGesturesEnabled = false,
                                            scrollGesturesEnabled = false,
                                            tiltGesturesEnabled = false,
                                            rotationGesturesEnabled = false,
                                            mapToolbarEnabled = false
                                        )
                                    ) {
                                        Marker(
                                            state = MarkerState(position = productLocation),
                                            title = product.nombre
                                        )
                                    }
                                }
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

                        // Sistema de negociación
                        var showOfferDialog by remember { mutableStateOf(false) }
                        var showBuyDialog by remember { mutableStateOf(false) }

                        // Botón publicar (solo propietario + borrador)
                        if (product.estadoVenta == EstadoVenta.BORRADOR &&
                            currentUserId == product.propietario.id
                        ) {
                            val isPublishing = publishState is UiState.Loading
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.publishProduct() },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isPublishing,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                if (isPublishing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onTertiary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Publicando...")
                                } else {
                                    Icon(Icons.Filled.Publish, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Publicar producto")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (product.estadoVenta.value == "disponible" && currentUserId != product.propietario.id) {
                            val isBuying = buyState is UiState.Loading
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Botón principal: Comprar ahora
                                Button(
                                    onClick = { showBuyDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isBuying
                                ) {
                                    if (isBuying) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Comprando...")
                                    } else {
                                        Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Comprar ahora")
                                    }
                                }

                                // Botones secundarios: Hacer oferta + Contactar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showOfferDialog = true },
                                        modifier = Modifier.weight(1f),
                                        enabled = !isBuying
                                    ) {
                                        Icon(Icons.Filled.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Hacer Oferta")
                                    }
                                    OutlinedButton(
                                        onClick = { onNavigateToChat(product.propietario.id, productId) },
                                        modifier = Modifier.weight(1f),
                                        enabled = !isBuying
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Contactar")
                                    }
                                }

                                // Info de precio
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Purple500.copy(alpha = 0.1f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Precio publicado:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = product.precio.toEuroPrice(),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        // Dialog de compra
                        if (showBuyDialog) {
                            var buyNotas by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showBuyDialog = false },
                                title = { Text("Confirmar compra") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text(
                                            text = "¿Comprar ${product.nombre} por ${product.precio.toEuroPrice()}?",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        OutlinedTextField(
                                            value = buyNotas,
                                            onValueChange = { buyNotas = it },
                                            label = { Text("Notas para el vendedor (opcional)") },
                                            placeholder = { Text("Ej: Recojo en mano, envío por mensajería...") },
                                            modifier = Modifier.fillMaxWidth(),
                                            maxLines = 3
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(onClick = {
                                        showBuyDialog = false
                                        viewModel.buyProduct(productId, buyNotas.trim().ifBlank { null })
                                    }) {
                                        Text("Comprar")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showBuyDialog = false }) {
                                        Text("Cancelar")
                                    }
                                }
                            )
                        }

                        // Dialog de oferta
                        if (showOfferDialog) {
                            OfferDialog(
                                currentPrice = product.precio,
                                productName = product.nombre,
                                onDismiss = { showOfferDialog = false },
                                onConfirm = { offeredPrice ->
                                    showOfferDialog = false
                                    if (isSendingOffer) return@OfferDialog
                                    isSendingOffer = true
                                    scope.launch {
                                        chatRepository.sendOffer(
                                            productoId = productId,
                                            precioOfertado = offeredPrice
                                        )
                                            .onSuccess {
                                                snackbarHostState.showSnackbar(
                                                    "Oferta enviada por ${String.format("%.2f", offeredPrice)}€"
                                                )
                                                // Navegar al chat para ver la oferta enviada
                                                onNavigateToChat(product.propietario.id, productId)
                                            }
                                            .onFailure { exception ->
                                                snackbarHostState.showSnackbar(
                                                    exception.message ?: "Error al enviar oferta"
                                                )
                                            }
                                        isSendingOffer = false
                                    }
                                }
                            )
                        }

                        // ── Comentarios ──────────────────────────────────
                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Comentarios (${product.comentarios.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (product.comentarios.isEmpty()) {
                            Text(
                                text = "Sé el primero en comentar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            product.comentarios.forEach { comment ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = comment.usuario.name,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (comment.usuario.id == currentUserId) {
                                                IconButton(
                                                    onClick = { viewModel.deleteComment(comment.id) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Filled.Delete,
                                                        contentDescription = "Eliminar comentario",
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = comment.texto,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        comment.fecha?.let { fecha ->
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

                        // Campo para nuevo comentario
                        Spacer(modifier = Modifier.height(12.dp))
                        var commentText by remember { mutableStateOf("") }
                        val isPostingComment = commentActionState is UiState.Loading
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                placeholder = { Text("Añadir un comentario...") },
                                modifier = Modifier.weight(1f),
                                maxLines = 3,
                                enabled = !isPostingComment
                            )
                            IconButton(
                                onClick = {
                                    if (commentText.isNotBlank()) {
                                        viewModel.createComment(commentText)
                                        commentText = ""
                                    }
                                },
                                enabled = commentText.isNotBlank() && !isPostingComment
                            ) {
                                if (isPostingComment) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                } else {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Enviar comentario"
                                    )
                                }
                            }
                        }

                        // ── Denunciar producto ────────────────────────────
                        Spacer(modifier = Modifier.height(16.dp))
                        var showReportDialog by remember { mutableStateOf(false) }

                        TextButton(
                            onClick = { showReportDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Filled.Flag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Denunciar producto",
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        if (showReportDialog) {
                            var reportMotivo by remember { mutableStateOf("") }
                            var reportCategoria by remember { mutableStateOf(CategoriaDenuncia.OTRO) }
                            var expandedCategoria by remember { mutableStateOf(false) }

                            AlertDialog(
                                onDismissRequest = { showReportDialog = false },
                                title = { Text("Denunciar producto") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedTextField(
                                            value = reportMotivo,
                                            onValueChange = { reportMotivo = it },
                                            label = { Text("Motivo") },
                                            placeholder = { Text("Describe el motivo de la denuncia") },
                                            modifier = Modifier.fillMaxWidth(),
                                            maxLines = 4
                                        )
                                        ExposedDropdownMenuBox(
                                            expanded = expandedCategoria,
                                            onExpandedChange = { expandedCategoria = it }
                                        ) {
                                            OutlinedTextField(
                                                value = reportCategoria.displayName,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Categoría") },
                                                trailingIcon = {
                                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoria)
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .menuAnchor()
                                            )
                                            ExposedDropdownMenu(
                                                expanded = expandedCategoria,
                                                onDismissRequest = { expandedCategoria = false }
                                            ) {
                                                CategoriaDenuncia.values().forEach { cat ->
                                                    DropdownMenuItem(
                                                        text = { Text(cat.displayName) },
                                                        onClick = {
                                                            reportCategoria = cat
                                                            expandedCategoria = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            viewModel.reportProduct(reportMotivo, reportCategoria)
                                            showReportDialog = false
                                        },
                                        enabled = reportMotivo.isNotBlank()
                                    ) {
                                        Text("Denunciar")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showReportDialog = false }) {
                                        Text("Cancelar")
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }

            else -> {}
        }
    }
}
