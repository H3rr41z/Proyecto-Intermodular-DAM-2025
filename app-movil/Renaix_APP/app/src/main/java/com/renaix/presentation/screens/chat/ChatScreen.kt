package com.renaix.presentation.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.renaix.di.AppContainer
import com.renaix.domain.model.Message
import com.renaix.domain.model.MessageType
import com.renaix.presentation.common.components.AcceptedOfferCard
import com.renaix.presentation.common.components.ErrorView
import com.renaix.presentation.common.components.LoadingIndicator
import com.renaix.presentation.common.components.OfferMessageCard
import com.renaix.presentation.common.components.RejectedOfferCard
import com.renaix.presentation.common.components.RenaixTextField
import com.renaix.presentation.common.state.UiState
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

/**
 * Pantalla de chat individual
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    otherUserId: Int,
    productId: Int,
    appContainer: AppContainer,
    onNavigateBack: () -> Unit,
    onNavigateToProduct: (Int) -> Unit
) {
    var messagesState by remember { mutableStateOf<UiState<List<Message>>>(UiState.Loading) }
    var messageText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    val chatRepository = appContainer.chatRepository
    val currentUserId = appContainer.preferencesManager.getUserId()

    // Estado para diálogo de contraoferta
    var showCounterOfferDialog by remember { mutableStateOf(false) }
    var selectedOfferForCounter by remember { mutableStateOf<Message?>(null) }
    var isProcessingOffer by remember { mutableStateOf(false) }

    fun loadMessages() {
        scope.launch {
            messagesState = UiState.Loading
            chatRepository.getConversation(otherUserId, productId)
                .onSuccess { messages ->
                    messagesState = UiState.Success(messages)
                    // Scroll al final
                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
                .onFailure { exception ->
                    messagesState = UiState.Error(exception.message ?: "Error al cargar mensajes")
                }
        }
    }

    fun sendMessage() {
        if (messageText.isBlank() || isSending) return

        val text = messageText
        messageText = ""
        isSending = true

        scope.launch {
            chatRepository.sendMessage(
                    receptorId = otherUserId,
                    texto = text,
                    productoId = productId
                )
                .onSuccess {
                    loadMessages()
                }
                .onFailure { exception ->
                    messageText = text
                    snackbarHostState.showSnackbar(exception.message ?: "Error al enviar mensaje")
                }
            isSending = false
        }
    }

    LaunchedEffect(Unit) {
        loadMessages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToProduct(productId) }) {
                        Icon(Icons.Filled.ShoppingBag, contentDescription = "Ver producto")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            ChatInputBar(
                messageText = messageText,
                onMessageChange = { messageText = it },
                onSend = { sendMessage() },
                isSending = isSending
            )
        }
    ) { padding ->
        when (val state = messagesState) {
            is UiState.Loading -> {
                LoadingIndicator(
                    modifier = Modifier.padding(padding),
                    message = "Cargando mensajes..."
                )
            }

            is UiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { loadMessages() },
                    modifier = Modifier.padding(padding)
                )
            }

            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Inicia la conversación",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(
                            items = state.data,
                            key = { it.id }
                        ) { message ->
                            MessageItem(
                                message = message,
                                isOwnMessage = message.emisor.id == currentUserId,
                                onAcceptOffer = {
                                    if (isProcessingOffer) return@MessageItem
                                    isProcessingOffer = true
                                    scope.launch {
                                        chatRepository.acceptOffer(message.id)
                                            .onSuccess { (_, purchase) ->
                                                snackbarHostState.showSnackbar(
                                                    "Oferta aceptada. Compra creada por ${String.format("%.2f", purchase.precioFinal)}€"
                                                )
                                                loadMessages()
                                            }
                                            .onFailure { exception ->
                                                snackbarHostState.showSnackbar(
                                                    exception.message ?: "Error al aceptar oferta"
                                                )
                                            }
                                        isProcessingOffer = false
                                    }
                                },
                                onRejectOffer = {
                                    if (isProcessingOffer) return@MessageItem
                                    isProcessingOffer = true
                                    scope.launch {
                                        chatRepository.rejectOffer(message.id)
                                            .onSuccess {
                                                snackbarHostState.showSnackbar("Oferta rechazada")
                                                loadMessages()
                                            }
                                            .onFailure { exception ->
                                                snackbarHostState.showSnackbar(
                                                    exception.message ?: "Error al rechazar oferta"
                                                )
                                            }
                                        isProcessingOffer = false
                                    }
                                },
                                onCounterOffer = {
                                    selectedOfferForCounter = message
                                    showCounterOfferDialog = true
                                }
                            )
                        }
                    }
                }
            }

            else -> {}
        }
    }

    // Diálogo de contraoferta
    if (showCounterOfferDialog && selectedOfferForCounter != null) {
        CounterOfferDialog(
            originalOffer = selectedOfferForCounter!!,
            onDismiss = {
                showCounterOfferDialog = false
                selectedOfferForCounter = null
            },
            onConfirm = { counterPrice ->
                showCounterOfferDialog = false
                isProcessingOffer = true
                scope.launch {
                    chatRepository.sendCounterOffer(
                        ofertaId = selectedOfferForCounter!!.id,
                        precioContraoferta = counterPrice
                    )
                        .onSuccess {
                            snackbarHostState.showSnackbar("Contraoferta enviada")
                            loadMessages()
                        }
                        .onFailure { exception ->
                            snackbarHostState.showSnackbar(
                                exception.message ?: "Error al enviar contraoferta"
                            )
                        }
                    isProcessingOffer = false
                    selectedOfferForCounter = null
                }
            }
        )
    }
}

/**
 * Diálogo para enviar una contraoferta
 */
@Composable
private fun CounterOfferDialog(
    originalOffer: Message,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var counterPrice by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val offerData = originalOffer.offerData ?: return

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.SwapHoriz,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Contraoferta",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Producto: ${offerData.productName}",
                    style = MaterialTheme.typography.bodyMedium
                )

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Oferta recibida:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "${String.format("%.2f", offerData.offeredPrice)}€",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Precio original:", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "${String.format("%.2f", offerData.originalPrice)}€",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = counterPrice,
                    onValueChange = {
                        counterPrice = it
                        error = null
                    },
                    label = { Text("Tu contraoferta (€)") },
                    isError = error != null,
                    supportingText = error?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    prefix = { Text("€ ") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = counterPrice.toDoubleOrNull()
                    when {
                        amount == null -> error = "Introduce un precio válido"
                        amount <= 0 -> error = "El precio debe ser mayor que 0€"
                        else -> onConfirm(amount)
                    }
                }
            ) {
                Text("Enviar Contraoferta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ChatInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean
) {
    Surface(
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe un mensaje...") },
                maxLines = 4,
                shape = MaterialTheme.shapes.extraLarge,
                colors = OutlinedTextFieldDefaults.colors()
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilledIconButton(
                onClick = onSend,
                enabled = messageText.isNotBlank() && !isSending
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                }
            }
        }
    }
}

/**
 * Componente que decide qué tipo de mensaje mostrar según el MessageType
 */
@Composable
private fun MessageItem(
    message: Message,
    isOwnMessage: Boolean,
    onAcceptOffer: () -> Unit,
    onRejectOffer: () -> Unit,
    onCounterOffer: () -> Unit
) {
    when (message.messageType) {
        MessageType.TEXT -> {
            MessageBubble(
                message = message,
                isOwnMessage = isOwnMessage
            )
        }
        MessageType.OFFER, MessageType.COUNTER_OFFER -> {
            message.offerData?.let { offer ->
                OfferMessageCard(
                    offer = offer,
                    isOwnMessage = isOwnMessage,
                    onAccept = onAcceptOffer,
                    onReject = onRejectOffer,
                    onCounterOffer = if (message.messageType == MessageType.OFFER) onCounterOffer else null
                )
            }
        }
        MessageType.OFFER_ACCEPTED -> {
            message.offerData?.let { offer ->
                AcceptedOfferCard(offer = offer)
            }
        }
        MessageType.OFFER_REJECTED -> {
            message.offerData?.let { offer ->
                RejectedOfferCard(offer = offer)
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    isOwnMessage: Boolean
) {
    val bubbleColor = if (isOwnMessage) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isOwnMessage) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val alignment = if (isOwnMessage) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                        bottomEnd = if (isOwnMessage) 4.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            Text(
                text = message.texto,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = formatMessageTime(message.fecha ?: ""),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatMessageTime(dateString: String): String {
    return try {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val date = java.time.LocalDateTime.parse(dateString, formatter)
        date.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        dateString
    }
}
