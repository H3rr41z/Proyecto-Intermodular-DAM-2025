package com.renaix.presentation.screens.products.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renaix.domain.model.Product
import com.renaix.domain.usecase.product.GetProductsUseCase
import com.renaix.presentation.common.state.PaginatedState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la lista de productos
 */
class ProductListViewModel(
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PaginatedState<Product>())
    val state: StateFlow<PaginatedState<Product>> = _state.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            getProductsUseCase(page = 1)
                .onSuccess { products ->
                    _state.value = _state.value.copy(
                        items = products,
                        isLoading = false,
                        page = 1,
                        hasMore = products.size >= 20,
                        endReached = products.size < 20
                    )
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar productos"
                    )
                }
        }
    }

    fun loadMore() {
        if (_state.value.isLoading || _state.value.endReached) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val nextPage = _state.value.page + 1
            getProductsUseCase(page = nextPage)
                .onSuccess { products ->
                    _state.value = _state.value.copy(
                        items = _state.value.items + products,
                        isLoading = false,
                        page = nextPage,
                        hasMore = products.size >= 20,
                        endReached = products.size < 20
                    )
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true, error = null)

            getProductsUseCase(page = 1)
                .onSuccess { products ->
                    _state.value = PaginatedState(
                        items = products,
                        isRefreshing = false,
                        page = 1,
                        hasMore = products.size >= 20,
                        endReached = products.size < 20
                    )
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isRefreshing = false,
                        error = exception.message
                    )
                }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
