package com.renaix.presentation.screens.products.create

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renaix.domain.usecase.category.GetCategoriesUseCase
import com.renaix.domain.usecase.product.CreateProductUseCase
import com.renaix.presentation.common.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de creación de productos
 *
 * Responsabilidades:
 * - Gestionar el estado del formulario
 * - Validar datos del producto
 * - Crear el producto usando el use case
 * - Manejar imágenes seleccionadas
 */
class CreateProductViewModel(
    private val createProductUseCase: CreateProductUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Int>>(UiState.Idle)
    val uiState: StateFlow<UiState<Int>> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(CreateProductFormState())
    val formState: StateFlow<CreateProductFormState> = _formState.asStateFlow()

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages.asStateFlow()

    init {
        loadCategories()
    }

    /**
     * Carga las categorías disponibles
     */
    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase()
                .onSuccess { categories ->
                    _formState.value = _formState.value.copy(
                        availableCategories = categories
                    )
                }
        }
    }

    /**
     * Actualiza el nombre del producto
     */
    fun updateName(name: String) {
        _formState.value = _formState.value.copy(
            nombre = name,
            nombreError = null
        )
    }

    /**
     * Actualiza la descripción
     */
    fun updateDescription(description: String) {
        _formState.value = _formState.value.copy(
            descripcion = description,
            descripcionError = null
        )
    }

    /**
     * Actualiza el precio
     */
    fun updatePrice(price: String) {
        _formState.value = _formState.value.copy(
            precio = price,
            precioError = null
        )
    }

    /**
     * Selecciona una categoría
     */
    fun selectCategory(categoryId: Int) {
        _formState.value = _formState.value.copy(
            categoriaId = categoryId,
            categoriaError = null
        )
    }

    /**
     * Añade imágenes seleccionadas
     */
    fun addImages(uris: List<Uri>) {
        val currentImages = _selectedImages.value.toMutableList()
        currentImages.addAll(uris)

        // Limitar a 10 imágenes máximo
        _selectedImages.value = currentImages.take(10)
    }

    /**
     * Elimina una imagen
     */
    fun removeImage(uri: Uri) {
        _selectedImages.value = _selectedImages.value.filter { it != uri }
    }

    /**
     * Valida y crea el producto
     */
    fun createProduct() {
        val state = _formState.value

        // Validaciones
        var hasErrors = false

        if (state.nombre.isBlank()) {
            _formState.value = state.copy(nombreError = "El nombre es obligatorio")
            hasErrors = true
        } else if (state.nombre.length < 3) {
            _formState.value = state.copy(nombreError = "El nombre debe tener al menos 3 caracteres")
            hasErrors = true
        }

        if (state.descripcion.isBlank()) {
            _formState.value = state.copy(descripcionError = "La descripción es obligatoria")
            hasErrors = true
        } else if (state.descripcion.length < 10) {
            _formState.value = state.copy(descripcionError = "La descripción debe tener al menos 10 caracteres")
            hasErrors = true
        }

        val priceValue = state.precio.toDoubleOrNull()
        if (priceValue == null || priceValue <= 0) {
            _formState.value = state.copy(precioError = "Precio inválido")
            hasErrors = true
        }

        if (state.categoriaId == null) {
            _formState.value = state.copy(categoriaError = "Selecciona una categoría")
            hasErrors = true
        }

        if (_selectedImages.value.isEmpty()) {
            // Aunque no es obligatorio, podríamos mostrar una advertencia
        }

        if (hasErrors) return

        // Crear producto
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            createProductUseCase(
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

    /**
     * Resetea el estado UI
     */
    fun resetUiState() {
        _uiState.value = UiState.Idle
    }
}

/**
 * Estado del formulario de creación
 */
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
    val availableCategories: List<com.renaix.domain.model.Category> = emptyList()
)
