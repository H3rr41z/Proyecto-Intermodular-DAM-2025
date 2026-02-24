package com.renaix.presentation.screens.products.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renaix.domain.model.CategoriaDenuncia
import com.renaix.domain.model.ProductDetail
import com.renaix.domain.model.TipoDenuncia
import com.renaix.domain.repository.CommentRepository
import com.renaix.domain.repository.ProductRepository
import com.renaix.domain.repository.PurchaseRepository
import com.renaix.domain.repository.ReportRepository
import com.renaix.domain.repository.UserRepository
import com.renaix.presentation.common.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val productRepository: ProductRepository,
    private val purchaseRepository: PurchaseRepository,
    private val commentRepository: CommentRepository,
    private val reportRepository: ReportRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<ProductDetail>>(UiState.Loading)
    val state: StateFlow<UiState<ProductDetail>> = _state.asStateFlow()

    private val _buyState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val buyState: StateFlow<UiState<Unit>> = _buyState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId.asStateFlow()

    private val _commentActionState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val commentActionState: StateFlow<UiState<Unit>> = _commentActionState.asStateFlow()

    private val _reportState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val reportState: StateFlow<UiState<Unit>> = _reportState.asStateFlow()

    private val _publishState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val publishState: StateFlow<UiState<Unit>> = _publishState.asStateFlow()

    private var currentProductId: Int = -1

    init {
        viewModelScope.launch {
            userRepository.getProfile()
                .onSuccess { user -> _currentUserId.value = user.id }
        }
    }

    fun loadProduct(productId: Int) {
        currentProductId = productId
        viewModelScope.launch {
            _state.value = UiState.Loading
            _isFavorite.value = productRepository.isFavorite(productId)
            productRepository.getProductDetail(productId)
                .onSuccess { product -> _state.value = UiState.Success(product) }
                .onFailure { exception ->
                    _state.value = UiState.Error(exception.message ?: "Error al cargar producto")
                }
        }
    }

    fun toggleFavorite() {
        if (currentProductId < 0) return
        viewModelScope.launch {
            if (_isFavorite.value) {
                productRepository.removeFromFavorites(currentProductId)
                _isFavorite.value = false
            } else {
                productRepository.addToFavorites(currentProductId)
                _isFavorite.value = true
            }
        }
    }

    fun buyProduct(productId: Int, notas: String? = null) {
        viewModelScope.launch {
            _buyState.value = UiState.Loading
            purchaseRepository.createPurchase(productId, notas)
                .onSuccess { _buyState.value = UiState.Success(Unit) }
                .onFailure { exception ->
                    _buyState.value = UiState.Error(exception.message ?: "Error al comprar")
                }
        }
    }

    fun createComment(texto: String) {
        if (currentProductId < 0 || texto.isBlank()) return
        viewModelScope.launch {
            _commentActionState.value = UiState.Loading
            commentRepository.createComment(currentProductId, texto)
                .onSuccess {
                    _commentActionState.value = UiState.Success(Unit)
                    loadProduct(currentProductId)
                }
                .onFailure { e ->
                    _commentActionState.value = UiState.Error(e.message ?: "Error al comentar")
                }
        }
    }

    fun deleteComment(commentId: Int) {
        viewModelScope.launch {
            _commentActionState.value = UiState.Loading
            commentRepository.deleteComment(commentId)
                .onSuccess {
                    _commentActionState.value = UiState.Success(Unit)
                    loadProduct(currentProductId)
                }
                .onFailure { e ->
                    _commentActionState.value = UiState.Error(e.message ?: "Error al eliminar comentario")
                }
        }
    }

    fun reportProduct(motivo: String, categoria: CategoriaDenuncia) {
        if (currentProductId < 0) return
        viewModelScope.launch {
            _reportState.value = UiState.Loading
            reportRepository.createReport(
                tipo = TipoDenuncia.PRODUCTO,
                motivo = motivo,
                categoria = categoria,
                productoId = currentProductId
            )
                .onSuccess { _reportState.value = UiState.Success(Unit) }
                .onFailure { e ->
                    _reportState.value = UiState.Error(e.message ?: "Error al denunciar")
                }
        }
    }

    fun resetBuyState() {
        _buyState.value = UiState.Idle
    }

    fun resetCommentActionState() {
        _commentActionState.value = UiState.Idle
    }

    fun resetReportState() {
        _reportState.value = UiState.Idle
    }

    fun publishProduct() {
        if (currentProductId < 0) return
        viewModelScope.launch {
            _publishState.value = UiState.Loading
            productRepository.publishProduct(currentProductId)
                .onSuccess {
                    _publishState.value = UiState.Success(Unit)
                    loadProduct(currentProductId)
                }
                .onFailure { e ->
                    _publishState.value = UiState.Error(e.message ?: "Error al publicar el producto")
                }
        }
    }

    fun resetPublishState() {
        _publishState.value = UiState.Idle
    }
}
