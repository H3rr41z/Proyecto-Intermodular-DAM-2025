package com.renaix.presentation.screens.products.edit

import android.content.ContentResolver
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renaix.domain.model.Category
import com.renaix.domain.model.EstadoProducto
import com.renaix.domain.model.ProductImage
import com.renaix.domain.repository.CategoryRepository
import com.renaix.domain.repository.ProductRepository
import com.renaix.presentation.common.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditProductViewModel(
    private val productId: Int,
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _loadState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val loadState: StateFlow<UiState<Unit>> = _loadState.asStateFlow()

    private val _saveState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val saveState: StateFlow<UiState<Unit>> = _saveState.asStateFlow()

    private val _formState = MutableStateFlow(EditProductFormState())
    val formState: StateFlow<EditProductFormState> = _formState.asStateFlow()

    // Existing images from the server
    private val _existingImages = MutableStateFlow<List<ProductImage>>(emptyList())
    val existingImages: StateFlow<List<ProductImage>> = _existingImages.asStateFlow()

    // IDs of existing images to delete
    private val _imagesToDelete = MutableStateFlow<Set<Int>>(emptySet())
    val imagesToDelete: StateFlow<Set<Int>> = _imagesToDelete.asStateFlow()

    // New local images to upload
    private val _newImages = MutableStateFlow<List<Uri>>(emptyList())
    val newImages: StateFlow<List<Uri>> = _newImages.asStateFlow()

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _loadState.value = UiState.Loading
            val categoriesResult = categoryRepository.getCategories()
            val productResult = productRepository.getProductDetail(productId)

            val categories = categoriesResult.getOrElse { emptyList() }

            productResult
                .onSuccess { product ->
                    _existingImages.value = product.imagenes
                    _formState.value = EditProductFormState(
                        nombre = product.nombre,
                        descripcion = product.descripcion ?: "",
                        precio = String.format("%.2f", product.precio).trimEnd('0').trimEnd('.').ifEmpty { "0" },
                        categoriaId = product.categoria.id,
                        estadoProducto = product.estadoProducto.value,
                        ubicacion = product.ubicacion,
                        availableCategories = categories
                    )
                    _loadState.value = UiState.Success(Unit)
                }
                .onFailure { e ->
                    _loadState.value = UiState.Error(e.message ?: "Error al cargar el producto")
                }
        }
    }

    fun updateName(name: String) {
        _formState.value = _formState.value.copy(nombre = name, nombreError = null)
    }

    fun updateDescription(description: String) {
        _formState.value = _formState.value.copy(descripcion = description, descripcionError = null)
    }

    fun updatePrice(price: String) {
        _formState.value = _formState.value.copy(precio = price, precioError = null)
    }

    fun selectCategory(categoryId: Int) {
        _formState.value = _formState.value.copy(categoriaId = categoryId, categoriaError = null)
    }

    fun updateEstado(estadoProducto: String) {
        _formState.value = _formState.value.copy(estadoProducto = estadoProducto)
    }

    fun updateUbicacion(ubicacion: String) {
        _formState.value = _formState.value.copy(ubicacion = ubicacion)
    }

    fun markImageForDeletion(imageId: Int) {
        _imagesToDelete.value = _imagesToDelete.value + imageId
    }

    fun addNewImages(uris: List<Uri>) {
        val current = _newImages.value.toMutableList()
        current.addAll(uris)
        val remaining = 10 - (_existingImages.value.size - _imagesToDelete.value.size)
        _newImages.value = current.take(maxOf(0, remaining))
    }

    fun removeNewImage(uri: Uri) {
        _newImages.value = _newImages.value.filter { it != uri }
    }

    fun saveProduct(contentResolver: ContentResolver) {
        val state = _formState.value

        var nombreError: String? = null
        var descripcionError: String? = null
        var precioError: String? = null
        var categoriaError: String? = null

        if (state.nombre.isBlank()) {
            nombreError = "El nombre es obligatorio"
        } else if (state.nombre.length < 3) {
            nombreError = "El nombre debe tener al menos 3 caracteres"
        }

        if (state.descripcion.isBlank()) {
            descripcionError = "La descripción es obligatoria"
        } else if (state.descripcion.length < 10) {
            descripcionError = "La descripción debe tener al menos 10 caracteres"
        }

        val priceValue = state.precio.toDoubleOrNull()
        if (priceValue == null || priceValue <= 0) {
            precioError = "Precio inválido"
        }

        if (state.categoriaId == null) {
            categoriaError = "Selecciona una categoría"
        }

        val hasErrors = nombreError != null || descripcionError != null ||
                precioError != null || categoriaError != null

        if (hasErrors) {
            _formState.value = state.copy(
                nombreError = nombreError,
                descripcionError = descripcionError,
                precioError = precioError,
                categoriaError = categoriaError
            )
            return
        }

        viewModelScope.launch {
            _saveState.value = UiState.Loading

            // 1. Update product fields
            val updateResult = productRepository.updateProduct(
                productId = productId,
                nombre = state.nombre,
                descripcion = state.descripcion,
                precio = priceValue!!,
                categoriaId = state.categoriaId!!,
                estadoProducto = state.estadoProducto,
                ubicacion = state.ubicacion?.takeIf { it.isNotBlank() }
            )

            if (updateResult.isFailure) {
                _saveState.value = UiState.Error(
                    updateResult.exceptionOrNull()?.message ?: "Error al guardar el producto"
                )
                return@launch
            }

            // 2. Delete marked images
            for (imageId in _imagesToDelete.value) {
                productRepository.deleteProductImage(productId, imageId)
            }

            // 3. Upload new images
            val existingVisible = _existingImages.value.filter { it.id !in _imagesToDelete.value }
            var firstImage = existingVisible.isEmpty()
            for (uri in _newImages.value) {
                try {
                    val bytes = contentResolver.openInputStream(uri)?.readBytes() ?: continue
                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    productRepository.addProductImage(
                        productId = productId,
                        imageBase64 = base64,
                        esPrincipal = firstImage
                    )
                    firstImage = false
                } catch (e: Exception) {
                    // skip failed image
                }
            }

            _saveState.value = UiState.Success(Unit)
        }
    }

    fun resetSaveState() {
        _saveState.value = UiState.Idle
    }

    private val _deleteState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val deleteState: StateFlow<UiState<Unit>> = _deleteState.asStateFlow()

    fun deleteProduct() {
        viewModelScope.launch {
            _deleteState.value = UiState.Loading
            productRepository.deleteProduct(productId)
                .onSuccess { _deleteState.value = UiState.Success(Unit) }
                .onFailure { _deleteState.value = UiState.Error(it.message ?: "Error al eliminar el producto") }
        }
    }

    fun resetDeleteState() {
        _deleteState.value = UiState.Idle
    }
}

data class EditProductFormState(
    val nombre: String = "",
    val nombreError: String? = null,
    val descripcion: String = "",
    val descripcionError: String? = null,
    val precio: String = "",
    val precioError: String? = null,
    val categoriaId: Int? = null,
    val categoriaError: String? = null,
    val estadoProducto: String = "nuevo",
    val ubicacion: String? = null,
    val availableCategories: List<Category> = emptyList()
)
