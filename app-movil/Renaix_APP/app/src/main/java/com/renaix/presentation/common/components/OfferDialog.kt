package com.renaix.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.renaix.ui.theme.Purple500
import com.renaix.ui.theme.Success
import com.renaix.ui.theme.Warning

/**
 * Dialog para realizar una oferta de precio sobre un producto.
 *
 * @param currentPrice Precio actual del producto
 * @param productName Nombre del producto
 * @param onDismiss Callback al cerrar el dialog
 * @param onConfirm Callback al confirmar la oferta con el precio propuesto
 */
@Composable
fun OfferDialog(
    currentPrice: Double,
    productName: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var offerPrice by remember { mutableStateOf(currentPrice.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.LocalOffer,
                contentDescription = null,
                tint = Purple500,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Proponer Precio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nombre del producto
                Text(
                    text = productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                HorizontalDivider()

                // Precio actual
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Precio publicado:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${currentPrice}€",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Purple500
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Campo de oferta
                OutlinedTextField(
                    value = offerPrice,
                    onValueChange = {
                        offerPrice = it
                        error = null
                    },
                    label = { Text("Tu oferta (€)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    isError = error != null,
                    supportingText = error?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    prefix = { Text("€ ") }
                )

                // Indicador de ahorro/descuento
                val offeredAmount = offerPrice.toDoubleOrNull()
                if (offeredAmount != null && offeredAmount < currentPrice) {
                    val savings = currentPrice - offeredAmount
                    val percentage = (savings / currentPrice * 100).toInt()

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Success.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = Success,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Ahorras:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                text = "%.2f€ (-%d%%)".format(savings, percentage),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Success
                            )
                        }
                    }
                }

                // Aviso informativo
                Text(
                    text = "El vendedor recibirá tu oferta y podrá aceptarla o negociar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = offerPrice.toDoubleOrNull()
                    when {
                        amount == null -> {
                            error = "Introduce un precio válido"
                        }
                        amount <= 0 -> {
                            error = "El precio debe ser mayor que 0€"
                        }
                        amount > currentPrice * 2 -> {
                            error = "La oferta no puede ser más del doble del precio original"
                        }
                        else -> {
                            onConfirm(amount)
                        }
                    }
                }
            ) {
                Text("Enviar Oferta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
