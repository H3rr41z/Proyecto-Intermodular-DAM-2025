package com.renaix.presentation.screens.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.renaix.di.AppContainer
import com.renaix.domain.model.Conversation
import com.renaix.presentation.common.components.EmptyStateView
import com.renaix.presentation.common.components.ErrorView
import com.renaix.presentation.common.components.LoadingIndicator
import com.renaix.presentation.common.state.UiState
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

/**
 * Pantalla de lista de conversaciones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    appContainer: AppContainer,
    onConversationClick: (Int, Int) -> Unit // (otherUserId, productId)
) {
    var conversationsState by remember { mutableStateOf<UiState<List<Conversation>>>(UiState.Loading) }
    val scope = rememberCoroutineScope()

    val getConversationsUseCase = appContainer.getConversationsUseCase

    fun loadConversations() {
        scope.launch {
            conversationsState = UiState.Loading
            getConversationsUseCase()
                .onSuccess { conversations ->
                    conversationsState = UiState.Success(conversations)
                }
                .onFailure { exception ->
                    conversationsState = UiState.Error(exception.message ?: "Error al cargar conversaciones")
                }
        }
    }

    LaunchedEffect(Unit) {
        loadConversations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mensajes") }
            )
        }
    ) { padding ->
        when (val state = conversationsState) {
            is UiState.Loading -> {
                LoadingIndicator(
                    modifier = Modifier.padding(padding),
                    message = "Cargando conversaciones..."
                )
            }

            is UiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { loadConversations() },
                    modifier = Modifier.padding(padding)
                )
            }

            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    EmptyStateView(
                        modifier = Modifier.padding(padding),
                        title = "Sin mensajes",
                        message = "Contacta con vendedores para iniciar una conversación",
                        icon = Icons.Filled.Chat
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        items(
                            items = state.data,
                            key = { it.id }
                        ) { conversation ->
                            ConversationItem(
                                conversation = conversation,
                                onClick = {
                                    onConversationClick(
                                        conversation.otherUser.id,
                                        conversation.product.id
                                    )
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }

            else -> {}
        }
    }
}

@Composable
private fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        headlineContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.otherUser.name,
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                conversation.lastMessage?.fechaEnvio?.let { fecha ->
                    Text(
                        text = formatConversationDate(fecha),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        supportingContent = {
            Column {
                // Producto
                Text(
                    text = conversation.product.nombre,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Último mensaje
                conversation.lastMessage?.let { message ->
                    Text(
                        text = message.contenido,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        leadingContent = {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        trailingContent = {
            if (conversation.unreadCount > 0) {
                Badge {
                    Text(conversation.unreadCount.toString())
                }
            }
        }
    )
}

private fun formatConversationDate(dateString: String): String {
    return try {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val date = java.time.LocalDateTime.parse(dateString, formatter)
        val now = java.time.LocalDateTime.now()

        when {
            date.toLocalDate() == now.toLocalDate() -> {
                date.format(DateTimeFormatter.ofPattern("HH:mm"))
            }
            date.toLocalDate() == now.toLocalDate().minusDays(1) -> {
                "Ayer"
            }
            else -> {
                date.format(DateTimeFormatter.ofPattern("dd/MM"))
            }
        }
    } catch (e: Exception) {
        dateString
    }
}
