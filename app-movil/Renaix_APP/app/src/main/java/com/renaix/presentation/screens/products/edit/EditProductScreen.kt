package com.renaix.presentation.screens.products.edit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.renaix.presentation.common.components.ErrorView
import com.renaix.presentation.common.components.LoadingIndicator
import com.renaix.presentation.common.components.RenaixButton
import com.renaix.presentation.common.components.RenaixTextField
import com.renaix.presentation.common.state.UiState
import com.renaix.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    productId: Int,
    appContainer: AppContainer,
    onNavigateBack: () -> Unit,
    onProductSaved: (Int) -> Unit,
    onProductDeleted: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = remember {
        EditProductViewModel(
            productId = productId,
            productRepository = appContainer.productRepository,
            categoryRepository = appContainer.categoryRepository
        )
    }

    val loadState by viewModel.loadState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val existingImages by viewModel.existingImages.collectAsState()
    val imagesToDelete by viewModel.imagesToDelete.collectAsState()
    val newImages by viewModel.newImages.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> viewModel.addNewImages(uris) }

    LaunchedEffect(saveState) {
        when (val s = saveState) {
            is UiState.Success -> onProductSaved(productId)
            is UiState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    LaunchedEffect(deleteState) {
        when (val s = deleteState) {
            is UiState.Success -> onProductDeleted()
            is UiState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetDeleteState()
            }
            else -> {}
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Filled.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Eliminar producto") },
            text = { Text("¿Estás seguro de que quieres eliminar este producto? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteProduct()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar producto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                    }
                },
                actions = {
                    if (loadState is UiState.Success) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            enabled = deleteState !is UiState.Loading && saveState !is UiState.Loading
                        ) {
                            Icon(
                                Icons.Filled.DeleteForever,
                                contentDescription = "Eliminar producto",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val s = loadState) {
            is UiState.Loading -> LoadingIndicator(
                modifier = Modifier.padding(padding),
                message = "Cargando producto..."
            )
            is UiState.Error -> ErrorView(
                message = s.message,
                onRetry = {},
                modifier = Modifier.padding(padding)
            )
            is UiState.Success -> {
                val isSaving = saveState is UiState.Loading || deleteState is UiState.Loading
                val visibleExisting = existingImages.filter { it.id !in imagesToDelete }
                val totalImages = visibleExisting.size + newImages.size

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // ── Imágenes ──────────────────────────────────────────────
                    Text(
                        text = "Fotos del producto",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (visibleExisting.isNotEmpty() || newImages.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Existing images
                            items(items = visibleExisting, key = { "existing-${it.id}" }) { image ->
                                Box(modifier = Modifier.size(88.dp)) {
                                    AsyncImage(
                                        model = Constants.imageUrl(image.urlImagen),
                                        contentDescription = "Imagen del producto",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(MaterialTheme.shapes.medium)
                                    )
                                    if (image.esPrincipal) {
                                        Surface(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(2.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = MaterialTheme.shapes.extraSmall
                                        ) {
                                            Text(
                                                text = "Principal",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { viewModel.markImageForDeletion(image.id) },
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

                            // New local images
                            items(items = newImages, key = { "new-${it}" }) { uri ->
                                Box(modifier = Modifier.size(88.dp)) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Nueva imagen",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(MaterialTheme.shapes.medium)
                                            .border(
                                                2.dp,
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.shapes.medium
                                            )
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeNewImage(uri) },
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

                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") },
                        enabled = totalImages < 10 && !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (totalImages == 0) "Añadir fotos"
                            else "Añadir más fotos ($totalImages/10)"
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Nombre ────────────────────────────────────────────────
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

                    // ── Descripción ───────────────────────────────────────────
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

                    // ── Precio ────────────────────────────────────────────────
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

                    // ── Categoría ─────────────────────────────────────────────
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

                    // ── Estado del producto ───────────────────────────────────
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
                            EstadoProducto.entries.forEach { estado ->
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

                    // ── Ubicación ─────────────────────────────────────────────
                    RenaixTextField(
                        value = formState.ubicacion ?: "",
                        onValueChange = { viewModel.updateUbicacion(it) },
                        label = "Ubicación (opcional)",
                        leadingIcon = Icons.Filled.LocationOn,
                        imeAction = ImeAction.Done
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // ── Botón guardar ─────────────────────────────────────────
                    RenaixButton(
                        text = if (isSaving) "Guardando..." else "Guardar cambios",
                        onClick = { viewModel.saveProduct(context.contentResolver) },
                        isLoading = isSaving
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "* Campos requeridos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {}
        }
    }
}
