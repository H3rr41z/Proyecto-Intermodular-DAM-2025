package com.renaix.presentation.screens.products.create

import android.content.ContentResolver
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renaix.domain.repository.CategoryRepository
import com.renaix.domain.repository.ProductRepository
import com.renaix.presentation.common.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateProductViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Int>>(UiState.Idle)
    val uiState: StateFlow<UiState<Int>> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(CreateProductFormState())
    val formState: StateFlow<CreateProductFormState> = _formState.asStateFlow()

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages.asStateFlow()

    private val _uploadState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uploadState: StateFlow<UiState<Unit>> = _uploadState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories()
                .onSuccess { categories ->
                    _formState.value = _formState.value.copy(
                        availableCategories = categories
                    )
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

    fun addImages(uris: List<Uri>) {
        val currentImages = _selectedImages.value.toMutableList()
        currentImages.addAll(uris)
        _selectedImages.value = currentImages.take(10)
        if (_selectedImages.value.isNotEmpty()) {
            _formState.value = _formState.value.copy(imagenesError = null)
        }
    }

    fun removeImage(uri: Uri) {
        _selectedImages.value = _selectedImages.value.filter { it != uri }
    }

    fun createProduct() {
        val state = _formState.value

        var nombreError: String? = null
        var descripcionError: String? = null
        var precioError: String? = null
        var categoriaError: String? = null
        var imagenesError: String? = null

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

        if (_selectedImages.value.isEmpty()) {
            imagenesError = "Añade al menos una imagen"
        }

        val hasErrors = nombreError != null || descripcionError != null ||
                precioError != null || categoriaError != null || imagenesError != null

        if (hasErrors) {
            _formState.value = state.copy(
                nombreError = nombreError,
                descripcionError = descripcionError,
                precioError = precioError,
                categoriaError = categoriaError,
                imagenesError = imagenesError
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            productRepository.createProduct(
                nombre = state.nombre,
                descripcion = state.descripcion,
                precio = priceValue!!,
                categoriaId = state.categoriaId!!,
                estadoProducto = state.estadoProducto,
                antiguedad = state.antiguedad,
                ubicacion = state.ubicacion,
                etiquetaIds = state.etiquetaIds
            ).onSuccess { productId ->
                _uiState.value = UiState.Success(productId)
            }.onFailure { error ->
                _uiState.value = UiState.Error(
                    error.message ?: "Error al crear el producto"
                )
            }
        }
    }

    fun uploadImages(productId: Int, contentResolver: ContentResolver) {
        val images = _selectedImages.value
        viewModelScope.launch {
            _uploadState.value = UiState.Loading
            var firstImage = true
            for (uri in images) {
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
                    // skip failed image, continue with next
                }
            }
            _uploadState.value = UiState.Success(Unit)
        }
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    fun resetUploadState() {
        _uploadState.value = UiState.Idle
    }
}

data class CreateProductFormState(
    val nombre: String = "",
    val nombreError: String? = null,
    val descripcion: String = "",
    val descripcionError: String? = null,
    val precio: String = "",
    val precioError: String? = null,
    val categoriaId: Int? = null,
    val categoriaError: String? = null,
    val estadoProducto: String = "nuevo",
    val antiguedad: String? = null,
    val ubicacion: String? = null,
    val etiquetaIds: List<Int> = emptyList(),
    val availableCategories: List<com.renaix.domain.model.Category> = emptyList(),
    val imagenesError: String? = null
)
