package com.renaix.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RatingDialog(
    onDismiss: () -> Unit,
    onConfirm: (puntuacion: Int, comentario: String?) -> Unit
) {
    var puntuacion by remember { mutableIntStateOf(0) }
    var comentario by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Valorar transacción") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Estrellas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    (1..5).forEach { estrella ->
                        IconButton(onClick = { puntuacion = estrella }) {
                            Icon(
                                imageVector = if (estrella <= puntuacion) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "$estrella estrellas",
                                modifier = Modifier.size(36.dp),
                                tint = if (estrella <= puntuacion) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }

                Text(
                    text = when (puntuacion) {
                        1 -> "Muy malo"
                        2 -> "Malo"
                        3 -> "Regular"
                        4 -> "Bueno"
                        5 -> "Excelente"
                        else -> "Selecciona una puntuación"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    label = { Text("Comentario (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(puntuacion, comentario.trim().ifBlank { null })
                },
                enabled = puntuacion > 0
            ) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
