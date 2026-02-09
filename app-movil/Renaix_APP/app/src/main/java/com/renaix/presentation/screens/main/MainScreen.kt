package com.renaix.presentation.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.renaix.di.AppContainer
import com.renaix.presentation.navigation.BottomNavItem
import com.renaix.presentation.navigation.Screen
import com.renaix.presentation.screens.chat.ConversationsScreen
import com.renaix.presentation.screens.map.MapScreen
import com.renaix.presentation.screens.products.list.ProductListScreen
import com.renaix.presentation.screens.products.search.SearchScreen
import com.renaix.presentation.screens.profile.ProfileScreen

/**
 * Pantalla principal con BottomNavigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    appContainer: AppContainer,
    onNavigateToProductDetail: (Int) -> Unit,
    onNavigateToCreateProduct: () -> Unit,
    onNavigateToChat: (Int, Int?) -> Unit,
    onNavigateToPublicProfile: (Int) -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Determinar si mostrar FAB
    val showFab = currentDestination?.route == Screen.ProductList.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomNavItem.entries.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = onNavigateToCreateProduct,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Crear producto"
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.ProductList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.ProductList.route) {
                ProductListScreen(
                    appContainer = appContainer,
                    onProductClick = onNavigateToProductDetail
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    appContainer = appContainer,
                    onProductClick = onNavigateToProductDetail
                )
            }

            composable(Screen.Map.route) {
                MapScreen(
                    appContainer = appContainer,
                    onProductClick = onNavigateToProductDetail
                )
            }

            composable(Screen.Conversations.route) {
                ConversationsScreen(
                    appContainer = appContainer,
                    onConversationClick = { userId, productId ->
                        onNavigateToChat(userId, productId)
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    appContainer = appContainer,
                    onNavigateToMyProducts = { /* TODO */ },
                    onNavigateToMyPurchases = { /* TODO */ },
                    onNavigateToMySales = { /* TODO */ },
                    onNavigateToEditProfile = { /* TODO */ },
                    onLogout = onLogout
                )
            }
        }
    }
}
