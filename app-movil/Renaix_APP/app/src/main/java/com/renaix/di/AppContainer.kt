package com.renaix.di

import android.content.Context
import com.renaix.data.local.database.DatabaseDriverFactory
import com.renaix.data.local.preferences.PreferencesManager
import com.renaix.data.local.preferences.SecurePreferences
import com.renaix.data.remote.api.KtorClient
import com.renaix.data.remote.api.RenaixApi
import com.renaix.data.remote.datasource.AuthRemoteDataSource
import com.renaix.data.remote.datasource.CategoryRemoteDataSource
import com.renaix.data.remote.datasource.ChatRemoteDataSource
import com.renaix.data.remote.datasource.ProductRemoteDataSource
import com.renaix.data.remote.datasource.PurchaseRemoteDataSource
import com.renaix.data.remote.datasource.UserRemoteDataSource
import com.renaix.data.repository.AuthRepositoryImpl
import com.renaix.data.repository.CategoryRepositoryImpl
import com.renaix.data.repository.ChatRepositoryImpl
import com.renaix.data.repository.ProductRepositoryImpl
import com.renaix.data.repository.PurchaseRepositoryImpl
import com.renaix.data.repository.UserRepositoryImpl
import com.renaix.domain.repository.AuthRepository
import com.renaix.domain.repository.CategoryRepository
import com.renaix.domain.repository.ChatRepository
import com.renaix.domain.repository.ProductRepository
import com.renaix.domain.repository.PurchaseRepository
import com.renaix.domain.repository.UserRepository
import com.renaix.domain.usecase.auth.CheckSessionUseCase
import com.renaix.domain.usecase.auth.LoginUseCase
import com.renaix.domain.usecase.auth.LogoutUseCase
import com.renaix.domain.usecase.auth.RegisterUseCase
import com.renaix.domain.usecase.category.GetCategoriesUseCase
import com.renaix.domain.usecase.chat.GetConversationsUseCase
import com.renaix.domain.usecase.chat.GetMessagesUseCase
import com.renaix.domain.usecase.chat.SendMessageUseCase
import com.renaix.domain.usecase.product.BuyProductUseCase
import com.renaix.domain.usecase.product.CreateProductUseCase
import com.renaix.domain.usecase.product.GetProductDetailUseCase
import com.renaix.domain.usecase.product.GetProductsUseCase
import com.renaix.domain.usecase.product.SearchProductsUseCase
import com.renaix.domain.usecase.user.GetProfileUseCase
import com.renaix.domain.usecase.user.GetPublicProfileUseCase
import com.renaix.domain.usecase.user.GetUserProductsUseCase

/**
 * Contenedor de dependencias manual (DI)
 * Implementa el patrón AppContainer para gestionar la creación
 * e inyección de dependencias sin usar frameworks como Hilt
 */
interface AppContainer {
    // Core dependencies
    val preferencesManager: PreferencesManager

    // Repositories
    val authRepository: AuthRepository
    val productRepository: ProductRepository
    val userRepository: UserRepository
    val categoryRepository: CategoryRepository
    val chatRepository: ChatRepository
    val purchaseRepository: PurchaseRepository

    // Use Cases - Auth
    val loginUseCase: LoginUseCase
    val registerUseCase: RegisterUseCase
    val logoutUseCase: LogoutUseCase
    val checkSessionUseCase: CheckSessionUseCase

    // Use Cases - Products
    val getProductsUseCase: GetProductsUseCase
    val getProductDetailUseCase: GetProductDetailUseCase
    val createProductUseCase: CreateProductUseCase
    val searchProductsUseCase: SearchProductsUseCase
    val buyProductUseCase: BuyProductUseCase

    // Use Cases - User
    val getProfileUseCase: GetProfileUseCase
    val getUserPublicProfileUseCase: GetPublicProfileUseCase
    val getUserProductsUseCase: GetUserProductsUseCase

    // Use Cases - Categories
    val getCategoriesUseCase: GetCategoriesUseCase

    // Use Cases - Chat
    val getConversationsUseCase: GetConversationsUseCase
    val getMessagesUseCase: GetMessagesUseCase
    val sendMessageUseCase: SendMessageUseCase
}

/**
 * Implementación del contenedor de dependencias
 */
class AppContainerImpl(private val context: Context) : AppContainer {

    // ==================== DATA LAYER ====================

    // Preferences
    private val securePreferences by lazy {
        SecurePreferences(context)
    }

    override val preferencesManager: PreferencesManager by lazy {
        PreferencesManager(securePreferences)
    }

    // Database
    private val databaseDriverFactory by lazy {
        DatabaseDriverFactory(context)
    }

    // Network - Ktor Client
    private val publicHttpClient by lazy {
        KtorClient.createPublicClient()
    }

    private val authenticatedHttpClient by lazy {
        KtorClient.createAuthenticatedClient(preferencesManager)
    }

    // API
    private val renaixApi by lazy {
        RenaixApi(publicHttpClient, authenticatedHttpClient)
    }

    // Data Sources
    private val authRemoteDataSource by lazy {
        AuthRemoteDataSource(renaixApi)
    }

    private val productRemoteDataSource by lazy {
        ProductRemoteDataSource(renaixApi)
    }

    private val userRemoteDataSource by lazy {
        UserRemoteDataSource(renaixApi)
    }

    private val categoryRemoteDataSource by lazy {
        CategoryRemoteDataSource(renaixApi)
    }

    private val chatRemoteDataSource by lazy {
        ChatRemoteDataSource(renaixApi)
    }

    private val purchaseRemoteDataSource by lazy {
        PurchaseRemoteDataSource(renaixApi)
    }

    // ==================== DOMAIN LAYER - REPOSITORIES ====================

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authRemoteDataSource, preferencesManager)
    }

    override val productRepository: ProductRepository by lazy {
        ProductRepositoryImpl(productRemoteDataSource)
    }

    override val userRepository: UserRepository by lazy {
        UserRepositoryImpl(userRemoteDataSource)
    }

    override val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(categoryRemoteDataSource)
    }

    override val chatRepository: ChatRepository by lazy {
        ChatRepositoryImpl(chatRemoteDataSource)
    }

    override val purchaseRepository: PurchaseRepository by lazy {
        PurchaseRepositoryImpl(purchaseRemoteDataSource)
    }

    // ==================== DOMAIN LAYER - USE CASES ====================

    // Auth Use Cases
    override val loginUseCase: LoginUseCase by lazy {
        LoginUseCase(authRepository)
    }

    override val registerUseCase: RegisterUseCase by lazy {
        RegisterUseCase(authRepository)
    }

    override val logoutUseCase: LogoutUseCase by lazy {
        LogoutUseCase(authRepository)
    }

    override val checkSessionUseCase: CheckSessionUseCase by lazy {
        CheckSessionUseCase(authRepository)
    }

    // Product Use Cases
    override val getProductsUseCase: GetProductsUseCase by lazy {
        GetProductsUseCase(productRepository)
    }

    override val getProductDetailUseCase: GetProductDetailUseCase by lazy {
        GetProductDetailUseCase(productRepository)
    }

    override val createProductUseCase: CreateProductUseCase by lazy {
        CreateProductUseCase(productRepository)
    }

    override val searchProductsUseCase: SearchProductsUseCase by lazy {
        SearchProductsUseCase(productRepository)
    }

    override val buyProductUseCase: BuyProductUseCase by lazy {
        BuyProductUseCase(purchaseRepository)
    }

    // User Use Cases
    override val getProfileUseCase: GetProfileUseCase by lazy {
        GetProfileUseCase(userRepository)
    }

    override val getUserPublicProfileUseCase: GetPublicProfileUseCase by lazy {
        GetPublicProfileUseCase(userRepository)
    }

    override val getUserProductsUseCase: GetUserProductsUseCase by lazy {
        GetUserProductsUseCase(userRepository)
    }

    // Category Use Cases
    override val getCategoriesUseCase: GetCategoriesUseCase by lazy {
        GetCategoriesUseCase(categoryRepository)
    }

    // Chat Use Cases
    override val getConversationsUseCase: GetConversationsUseCase by lazy {
        GetConversationsUseCase(chatRepository)
    }

    override val getMessagesUseCase: GetMessagesUseCase by lazy {
        GetMessagesUseCase(chatRepository)
    }

    override val sendMessageUseCase: SendMessageUseCase by lazy {
        SendMessageUseCase(chatRepository)
    }
}
