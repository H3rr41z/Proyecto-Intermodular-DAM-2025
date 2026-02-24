package com.renaix.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Rutas de navegación de la aplicación
 */
sealed class Screen(val route: String) {
    // Auth
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")

    // Main
    object Main : Screen("main")

    // Products
    object ProductList : Screen("products")
    object ProductDetail : Screen("products/{productId}") {
        fun createRoute(productId: Int) = "products/$productId"
    }
    object CreateProduct : Screen("products/create")
    object EditProduct : Screen("products/{productId}/edit") {
        fun createRoute(productId: Int) = "products/$productId/edit"
    }
    object Search : Screen("search")

    // Profile
    object Profile : Screen("profile")
    object EditProfile : Screen("profile/edit")
    object Favorites : Screen("profile/favorites")
    object MyProducts : Screen("profile/products")
    object MyPurchases : Screen("profile/purchases")
    object MySales : Screen("profile/sales")

    // Map
    object Map : Screen("map")

    // Chat
    object Conversations : Screen("conversations")
    object Chat : Screen("chat/{userId}") {
        fun createRoute(userId: Int, productId: Int? = null): String {
            return if (productId != null) "chat/$userId?productId=$productId"
            else "chat/$userId"
        }
    }

    // Public Profile
    object PublicProfile : Screen("user/{userId}") {
        fun createRoute(userId: Int) = "user/$userId"
    }
}

/**
 * Items del BottomNavigation
 */
enum class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Products(
        route = Screen.ProductList.route,
        title = "Productos",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    Search(
        route = Screen.Search.route,
        title = "Buscar",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    ),
    Map(
        route = Screen.Map.route,
        title = "Mapa",
        selectedIcon = Icons.Filled.Map,
        unselectedIcon = Icons.Outlined.Map
    ),
    Chat(
        route = Screen.Conversations.route,
        title = "Chat",
        selectedIcon = Icons.AutoMirrored.Filled.Chat,
        unselectedIcon = Icons.AutoMirrored.Outlined.Chat
    ),
    Profile(
        route = Screen.Profile.route,
        title = "Perfil",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}
