package com.renaix.presentation.screens.products.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.renaix.di.AppContainer
import com.renaix.domain.model.Category
import com.renaix.domain.model.EstadoProducto
import com.renaix.presentation.common.components.RenaixButton
import com.renaix.presentation.common.components.RenaixTextField
import com.renaix.presentation.common.state.UiState
import kotlinx.coroutines.launch

/**
 * Pantalla para crear un producto
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProductScreen(
    appContainer: AppContainer,
    onNavigateBack: () -> Unit,
    onProductCreated: (Int) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedEstado by remember { mutableStateOf(EstadoProducto.BUEN_ESTADO) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val getCategoriesUseCase = appContainer.getCategoriesUseCase
    val createProductUseCase = appContainer.createProductUseCase

    // Cargar categorías
    LaunchedEffect(Unit) {
        getCategoriesUseCase()
            .onSuccess { categories = it }
    }

    // Mostrar errores
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            error = null
        }
    }

    fun createProduct() {
        if (nombre.isBlank() || descripcion.isBlank() || precio.isBlank() || selectedCategory == null) {
            error = "Completa todos los campos requeridos"
            return
        }

        val precioDouble = precio.toDoubleOrNull()
        if (precioDouble == null || precioDouble <= 0) {
            error = "Precio inválido"
            return
        }

        scope.launch {
            isLoading = true
            createProductUseCase(
                nombre = nombre,
                descripcion = descripcion,
                precio = precioDouble,
                categoriaId = selectedCategory!!.id,
                estadoProducto = selectedEstado.value,
                ubicacion = ubicacion.takeIf { it.isNotBlank() }
            )
                .onSuccess { productId ->
                    onProductCreated(productId)
                }
                .onFailure { exception ->
                    error = exception.message ?: "Error al crear producto"
                    isLoading = false
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear producto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Nombre
            RenaixTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = "Nombre del producto *",
                leadingIcon = Icons.Filled.ShoppingBag,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Descripción
            RenaixTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = "Descripción *",
                leadingIcon = Icons.Filled.Description,
                singleLine = false,
                maxLines = 4,
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Precio
            RenaixTextField(
                value = precio,
                onValueChange = { precio = it.filter { c -> c.isDigit() || c == '.' } },
                label = "Precio (€) *",
                leadingIcon = Icons.Filled.Euro,
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Categoría
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                selectedCategory = category
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Estado del producto
            var estadoExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = estadoExpanded,
                onExpandedChange = { estadoExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedEstado.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estado del producto") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = estadoExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = estadoExpanded,
                    onDismissRequest = { estadoExpanded = false }
                ) {
                    EstadoProducto.entries.forEach { estado ->
                        DropdownMenuItem(
                            text = { Text(estado.displayName) },
                            onClick = {
                                selectedEstado = estado
                                estadoExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ubicación
            RenaixTextField(
                value = ubicacion,
                onValueChange = { ubicacion = it },
                label = "Ubicación (opcional)",
                leadingIcon = Icons.Filled.LocationOn,
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón crear
            RenaixButton(
                text = "Crear producto",
                onClick = { createProduct() },
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "* Campos requeridos. Después de crear el producto podrás añadir imágenes y publicarlo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
