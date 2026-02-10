package com.renaix.presentation.screens.products.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renaix.domain.model.Category
import com.renaix.domain.model.Product
import com.renaix.domain.usecase.category.GetCategoriesUseCase
import com.renaix.domain.usecase.product.SearchProductsUseCase
import com.renaix.presentation.common.state.UiState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de búsqueda
 *
 * Responsabilidades:
 * - Gestionar el texto de búsqueda con debounce
 * - Aplicar filtros (categoría, precio, etc.)
 * - Realizar búsquedas y mostrar resultados
 */
class SearchViewModel(
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<UiState<List<Product>>>(UiState.Idle)
    val searchResults: StateFlow<UiState<List<Product>>> = _searchResults.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()

    private val _minPrice = MutableStateFlow<Double?>(null)
    val minPrice: StateFlow<Double?> = _minPrice.asStateFlow()

    private val _maxPrice = MutableStateFlow<Double?>(null)
    val maxPrice: StateFlow<Double?> = _maxPrice.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    init {
        loadCategories()
        setupSearchDebounce()
    }

    /**
     * Carga las categorías disponibles
     */
    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase()
                .onSuccess { categories ->
                    _categories.value = categories
                }
        }
    }

    /**
     * Configura el debounce para la búsqueda automática
     */
    @OptIn(FlowPreview::class)
    private fun setupSearchDebounce() {
        viewModelScope.launch {
            _searchQuery
                .debounce(500) // Esperar 500ms después del último cambio
                .collect { query ->
                    if (query.isNotBlank()) {
                        performSearch()
                    } else {
                        _searchResults.value = UiState.Idle
                    }
                }
        }
    }

    /**
     * Actualiza el texto de búsqueda
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Selecciona/deselecciona una categoría para filtrar
     */
    fun selectCategory(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
        if (_searchQuery.value.isNotBlank()) {
            performSearch()
        }
    }

    /**
     * Establece el rango de precios
     */
    fun setPriceRange(min: Double?, max: Double?) {
        _minPrice.value = min
        _maxPrice.value = max
        if (_searchQuery.value.isNotBlank()) {
            performSearch()
        }
    }

    /**
     * Cambia el orden de los resultados
     */
    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        if (_searchResults.value is UiState.Success) {
            performSearch()
        }
    }

    /**
     * Limpia todos los filtros
     */
    fun clearFilters() {
        _selectedCategoryId.value = null
        _minPrice.value = null
        _maxPrice.value = null
        _sortOrder.value = SortOrder.NEWEST
        if (_searchQuery.value.isNotBlank()) {
            performSearch()
        }
    }

    /**
     * Realiza la búsqueda con los filtros aplicados
     */
    fun performSearch() {
        val query = _searchQuery.value
        if (query.isBlank()) {
            _searchResults.value = UiState.Idle
            return
        }

        viewModelScope.launch {
            _searchResults.value = UiState.Loading

            searchProductsUseCase(
                query = query,
                categoriaId = _selectedCategoryId.value,
                precioMin = _minPrice.value,
                precioMax = _maxPrice.value,
                orden = _sortOrder.value.apiValue
            ).onSuccess { products ->
                _searchResults.value = UiState.Success(products)
            }.onFailure { error ->
                _searchResults.value = UiState.Error(
                    error.message ?: "Error al buscar productos"
                )
            }
        }
    }
}

/**
 * Opciones de ordenamiento
 */
enum class SortOrder(val apiValue: String, val displayName: String) {
    NEWEST("newest", "Más recientes"),
    PRICE_LOW_HIGH("price_asc", "Precio: menor a mayor"),
    PRICE_HIGH_LOW("price_desc", "Precio: mayor a menor"),
    NAME("name", "Nombre A-Z")
}
