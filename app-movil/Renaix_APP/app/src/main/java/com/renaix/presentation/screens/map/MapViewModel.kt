package com.renaix.presentation.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renaix.domain.model.Product
import com.renaix.domain.usecase.product.GetProductsUseCase
import com.renaix.presentation.common.state.UiState
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla del mapa
 *
 * Responsabilidades:
 * - Cargar productos con ubicación
 * - Gestionar la ubicación del usuario
 * - Filtrar productos por distancia
 * - Manejar el estado de la cámara del mapa
 */
class MapViewModel(
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {

    private val _products = MutableStateFlow<UiState<List<Product>>>(UiState.Idle)
    val products: StateFlow<UiState<List<Product>>> = _products.asStateFlow()

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    private val _maxDistanceKm = MutableStateFlow<Int?>(null)
    val maxDistanceKm: StateFlow<Int?> = _maxDistanceKm.asStateFlow()

    private val _mapReady = MutableStateFlow(false)
    val mapReady: StateFlow<Boolean> = _mapReady.asStateFlow()

    init {
        loadProducts()
    }

    /**
     * Marca el mapa como listo
     */
    fun onMapReady() {
        _mapReady.value = true
    }

    /**
     * Actualiza la ubicación del usuario
     */
    fun updateUserLocation(latLng: LatLng) {
        _userLocation.value = latLng
    }

    /**
     * Carga los productos disponibles
     */
    fun loadProducts() {
        viewModelScope.launch {
            _products.value = UiState.Loading

            getProductsUseCase(
                limit = 100, // Cargar más productos para el mapa
                offset = 0
            ).onSuccess { products ->
                _products.value = UiState.Success(products)
            }.onFailure { error ->
                _products.value = UiState.Error(
                    error.message ?: "Error al cargar productos"
                )
            }
        }
    }

    /**
     * Selecciona un producto en el mapa
     */
    fun selectProduct(product: Product?) {
        _selectedProduct.value = product
    }

    /**
     * Establece el filtro de distancia máxima
     */
    fun setMaxDistance(distanceKm: Int?) {
        _maxDistanceKm.value = distanceKm
        // TODO: Recargar productos con filtro de distancia
    }

    /**
     * Calcula la distancia entre dos puntos geográficos
     * Usa la fórmula de Haversine
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Radio de la Tierra en km

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c
    }

    /**
     * Filtra productos por distancia máxima desde la ubicación del usuario
     */
    fun getProductsWithinDistance(): List<Product> {
        val location = _userLocation.value
        val maxDist = _maxDistanceKm.value
        val productsState = _products.value

        if (location == null || maxDist == null || productsState !is UiState.Success) {
            return emptyList()
        }

        // TODO: Filtrar productos por distancia cuando se añadan campos de geolocalización al modelo Product
        // Por ahora, retorna todos los productos
        return productsState.data
    }
}
