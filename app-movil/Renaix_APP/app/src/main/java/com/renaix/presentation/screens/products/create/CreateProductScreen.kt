package com.renaix.presentation.screens.products.create

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.renaix.di.AppContainer
import com.renaix.domain.model.EstadoProducto
import com.renaix.presentation.common.components.RenaixButton
import com.renaix.presentation.common.components.RenaixTextField
import com.renaix.presentation.common.state.UiState

/**
 * Pantalla para crear un producto con selección de imágenes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProductScreen(
    appContainer: AppContainer,
    onNavigateBack: () -> Unit,
    onProductCreated: (Int) -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember {
        CreateProductViewModel(appContainer.productRepository, appContainer.categoryRepository)
    }

    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val selectedImages by viewModel.selectedImages.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()

    var createdProductId by remember { mutableStateOf<Int?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val isLoading = uiState is UiState.Loading || uploadState is UiState.Loading

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        viewModel.addImages(uris)
    }

    // Product created → start uploading images
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UiState.Success -> {
                createdProductId = state.data
                viewModel.uploadImages(state.data, context.contentResolver)
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetUiState()
            }
            else -> {}
        }
    }

    // Image upload finished → navigate to product
    LaunchedEffect(uploadState) {
        when (uploadState) {
            is UiState.Success -> createdProductId?.let { onProductCreated(it) }
            is UiState.Error -> {
                // Product was created; navigate anyway even if upload failed
                createdProductId?.let { onProductCreated(it) }
            }
            else -> {}
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
            // ── Imágenes ──────────────────────────────────────────────────
            Text(
                text = "Fotos del producto *",
                style = MaterialTheme.typography.labelLarge,
                color = if (formState.imagenesError != null) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedImages.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(selectedImages) { uri ->
                        Box(modifier = Modifier.size(88.dp)) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Imagen del producto",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(MaterialTheme.shapes.medium)
                            )
                            IconButton(
                                onClick = { viewModel.removeImage(uri) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(28.dp)
                                    .background(
                                        MaterialTheme.colorScheme.errorContainer,
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Eliminar imagen",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (formState.imagenesError != null) {
                Text(
                    text = formState.imagenesError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            OutlinedButton(
                onClick = { imagePicker.launch("image/*") },
                enabled = selectedImages.size < 10 && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (selectedImages.isEmpty()) "Añadir fotos (mínimo 1) *"
                    else "Añadir más fotos (${selectedImages.size}/10)"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Nombre ────────────────────────────────────────────────────
            RenaixTextField(
                value = formState.nombre,
                onValueChange = { viewModel.updateName(it) },
                label = "Nombre del producto *",
                leadingIcon = Icons.Filled.ShoppingBag,
                imeAction = ImeAction.Next,
                isError = formState.nombreError != null,
                errorMessage = formState.nombreError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Descripción ───────────────────────────────────────────────
            RenaixTextField(
                value = formState.descripcion,
                onValueChange = { viewModel.updateDescription(it) },
                label = "Descripción *",
                leadingIcon = Icons.Filled.Description,
                singleLine = false,
                maxLines = 4,
                minLines = 3,
                isError = formState.descripcionError != null,
                errorMessage = formState.descripcionError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Precio ────────────────────────────────────────────────────
            RenaixTextField(
                value = formState.precio,
                onValueChange = { viewModel.updatePrice(it.filter { c -> c.isDigit() || c == '.' }) },
                label = "Precio (€) *",
                leadingIcon = Icons.Filled.Euro,
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next,
                isError = formState.precioError != null,
                errorMessage = formState.precioError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Categoría ─────────────────────────────────────────────────
            val selectedCategory = formState.availableCategories.find { it.id == formState.categoriaId }
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
                    isError = formState.categoriaError != null,
                    supportingText = formState.categoriaError?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    formState.availableCategories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                viewModel.selectCategory(category.id)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Estado del producto ───────────────────────────────────────
            val currentEstado = EstadoProducto.fromString(formState.estadoProducto)
            var estadoExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = estadoExpanded,
                onExpandedChange = { estadoExpanded = it }
            ) {
                OutlinedTextField(
                    value = currentEstado.displayName,
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
                    EstadoProducto.values().forEach { estado ->
                        DropdownMenuItem(
                            text = { Text(estado.displayName) },
                            onClick = {
                                viewModel.updateEstado(estado.value)
                                estadoExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Ubicación ─────────────────────────────────────────────────
            RenaixTextField(
                value = formState.ubicacion ?: "",
                onValueChange = { viewModel.updateUbicacion(it) },
                label = "Ubicación (opcional)",
                leadingIcon = Icons.Filled.LocationOn,
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Botón crear ───────────────────────────────────────────────
            if (uploadState is UiState.Loading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Subiendo imágenes... (${selectedImages.size})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                RenaixButton(
                    text = if (uiState is UiState.Loading) "Creando..." else "Crear producto",
                    onClick = { viewModel.createProduct() },
                    isLoading = uiState is UiState.Loading
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "* Campos requeridos. Mínimo 1 imagen, máximo 10.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
