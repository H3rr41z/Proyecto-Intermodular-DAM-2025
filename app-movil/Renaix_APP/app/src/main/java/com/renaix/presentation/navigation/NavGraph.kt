package com.renaix.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.renaix.di.AppContainer
import com.renaix.presentation.screens.auth.login.LoginScreen
import com.renaix.presentation.screens.auth.register.RegisterScreen
import com.renaix.presentation.screens.chat.ConversationsScreen
import com.renaix.presentation.screens.chat.ChatScreen
import com.renaix.presentation.screens.main.MainScreen
import com.renaix.presentation.screens.map.MapScreen
import com.renaix.presentation.screens.products.create.CreateProductScreen
import com.renaix.presentation.screens.products.detail.ProductDetailScreen
import com.renaix.presentation.screens.products.list.ProductListScreen
import com.renaix.presentation.screens.products.search.SearchScreen
import com.renaix.presentation.screens.profile.ProfileScreen
import com.renaix.presentation.screens.profile.PublicProfileScreen
import com.renaix.presentation.screens.splash.SplashScreen

/**
 * Grafo de navegación principal de la aplicación
 */
@Composable
fun RenaixNavGraph(
    navController: NavHostController,
    appContainer: AppContainer,
    startDestination: String = Screen.Splash.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // ==================== AUTH ====================

        composable(route = Screen.Splash.route) {
            SplashScreen(
                appContainer = appContainer,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Login.route) {
            LoginScreen(
                appContainer = appContainer,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                appContainer = appContainer,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ==================== MAIN ====================

        composable(route = Screen.Main.route) {
            MainScreen(
                appContainer = appContainer,
                onNavigateToProductDetail = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onNavigateToCreateProduct = {
                    navController.navigate(Screen.CreateProduct.route)
                },
                onNavigateToChat = { userId, productId ->
                    navController.navigate(Screen.Chat.createRoute(userId, productId))
                },
                onNavigateToPublicProfile = { userId ->
                    navController.navigate(Screen.PublicProfile.createRoute(userId))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }

        // ==================== PRODUCTS ====================

        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(
                navArgument("productId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            ProductDetailScreen(
                productId = productId,
                appContainer = appContainer,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { userId, prodId ->
                    navController.navigate(Screen.Chat.createRoute(userId, prodId))
                },
                onNavigateToPublicProfile = { userId ->
                    navController.navigate(Screen.PublicProfile.createRoute(userId))
                }
            )
        }

        composable(route = Screen.CreateProduct.route) {
            CreateProductScreen(
                appContainer = appContainer,
                onNavigateBack = { navController.popBackStack() },
                onProductCreated = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId)) {
                        popUpTo(Screen.CreateProduct.route) { inclusive = true }
                    }
                }
            )
        }

        // ==================== CHAT ====================

        composable(
            route = "chat/{userId}?productId={productId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("productId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val productId = backStackEntry.arguments?.getInt("productId")?.takeIf { it > 0 } ?: 0
            ChatScreen(
                otherUserId = userId,
                productId = productId,
                appContainer = appContainer,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProduct = { prodId ->
                    navController.navigate(Screen.ProductDetail.createRoute(prodId))
                }
            )
        }

        // ==================== PUBLIC PROFILE ====================

        composable(
            route = Screen.PublicProfile.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            PublicProfileScreen(
                userId = userId,
                appContainer = appContainer,
                onNavigateBack = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onNavigateToChat = { chatUserId ->
                    navController.navigate(Screen.Chat.createRoute(chatUserId, 0))
                }
            )
        }
    }
}
