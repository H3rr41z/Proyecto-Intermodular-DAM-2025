package com.renaix.presentation.screens.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.renaix.R
import com.renaix.di.AppContainer
import com.renaix.presentation.navigation.BottomNavItem
import com.renaix.presentation.navigation.Screen
import com.renaix.presentation.screens.chat.ConversationsScreen
import com.renaix.presentation.screens.map.MapScreen
import com.renaix.presentation.screens.products.list.ProductListScreen
import com.renaix.presentation.screens.products.search.SearchScreen
import com.renaix.presentation.screens.profile.ProfileScreen
import com.renaix.ui.theme.Purple500
import com.renaix.ui.theme.Purple700
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Pantalla principal con Bottom Navigation, Drawer y FAB animado
 * Diseño moderno siguiendo Material Design 3
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    appContainer: AppContainer,
    onNavigateToProductDetail: (Int) -> Unit,
    onNavigateToCreateProduct: () -> Unit,
    onNavigateToChat: (Int, Int?) -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Determinar si mostrar FAB (solo en pantalla de productos)
    val showFab = currentDestination?.route == Screen.ProductList.route

    // Estado del usuario para el drawer
    val userRepository = appContainer.userRepository
    val chatRepository = appContainer.chatRepository
    var userName by remember { mutableStateOf("Usuario") }
    var userEmail by remember { mutableStateOf("") }

    // Badge de mensajes no leídos
    var unreadMessagesCount by remember { mutableIntStateOf(0) }

    // Modo oscuro
    val preferencesManager = appContainer.preferencesManager
    val isDarkMode by preferencesManager.isDarkMode.collectAsState()

    // Dialog de confirmación de logout
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Cargar perfil del usuario
    LaunchedEffect(Unit) {
        userRepository.getProfile().onSuccess { profile ->
            userName = profile.name
            userEmail = profile.email
        }
    }

    // Polling para mensajes no leídos (cada 30 segundos)
    LaunchedEffect(Unit) {
        while (isActive) {
            chatRepository.getUnreadMessages().onSuccess { unread ->
                unreadMessagesCount = unread.total
            }
            delay(30_000) // 30 segundos
        }
    }

    // Resetear contador cuando se entra a conversaciones
    LaunchedEffect(currentDestination?.route) {
        if (currentDestination?.route == Screen.Conversations.route) {
            // Recargar al entrar para actualizar el badge
            chatRepository.getUnreadMessages().onSuccess { unread ->
                unreadMessagesCount = unread.total
            }
        }
    }

    // Dialog de confirmación de cerrar sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text("Cerrar sesión")
            },
            text = {
                Text("¿Estás seguro de que quieres cerrar sesión?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp)
            ) {
                DrawerContent(
                    userName = userName,
                    userEmail = userEmail,
                    isDarkMode = isDarkMode,
                    onDarkModeToggle = { preferencesManager.setDarkMode(it) },
                    onNavigateToMyProducts = {
                        scope.launch { drawerState.close() }
                    },
                    onNavigateToMyPurchases = {
                        scope.launch { drawerState.close() }
                    },
                    onNavigateToMySales = {
                        scope.launch { drawerState.close() }
                    },
                    onNavigateToSettings = {
                        scope.launch { drawerState.close() }
                    },
                    onLogout = {
                        scope.launch { drawerState.close() }
                        showLogoutDialog = true
                    }
                )
            }
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = {
                val currentTitle = when (currentDestination?.route) {
                    Screen.ProductList.route -> "Renaix"
                    Screen.Search.route -> "Buscar"
                    Screen.Map.route -> "Mapa"
                    Screen.Conversations.route -> "Mensajes"
                    Screen.Profile.route -> "Mi Perfil"
                    else -> "Renaix"
                }

                CenterAlignedTopAppBar(
                    title = {
                        if (currentDestination?.route == Screen.ProductList.route) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_renaix_logo),
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentTitle,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                text = currentTitle,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.semantics {
                                contentDescription = "Abrir menú lateral"
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = null
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                ModernBottomNavigation(
                    currentDestination = currentDestination,
                    unreadMessagesCount = unreadMessagesCount,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            },
            floatingActionButton = {
                AnimatedFab(
                    visible = showFab,
                    onClick = onNavigateToCreateProduct
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.ProductList.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { fadeIn(animationSpec = tween(200)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) }
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
                        onNavigateToMyProducts = { },
                        onNavigateToMyPurchases = { },
                        onNavigateToMySales = { },
                        onNavigateToEditProfile = { },
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

/**
 * Contenido del Drawer con diseño moderno y toggle de modo oscuro
 */
@Composable
private fun DrawerContent(
    userName: String,
    userEmail: String,
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    onNavigateToMyProducts: () -> Unit,
    onNavigateToMyPurchases: () -> Unit,
    onNavigateToMySales: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight()
    ) {
        // Header con gradiente
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Purple700, Purple500)
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        DrawerMenuItem(
            icon = Icons.Outlined.Inventory2,
            label = "Mis productos",
            onClick = onNavigateToMyProducts
        )

        DrawerMenuItem(
            icon = Icons.Outlined.ShoppingBag,
            label = "Mis compras",
            onClick = onNavigateToMyPurchases
        )

        DrawerMenuItem(
            icon = Icons.Outlined.Sell,
            label = "Mis ventas",
            onClick = onNavigateToMySales
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Toggle de modo oscuro
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Modo oscuro",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Switch(
                checked = isDarkMode,
                onCheckedChange = onDarkModeToggle
            )
        }

        DrawerMenuItem(
            icon = Icons.Outlined.Settings,
            label = "Ajustes",
            onClick = onNavigateToSettings
        )

        Spacer(modifier = Modifier.weight(1f))

        DrawerMenuItem(
            icon = Icons.AutoMirrored.Outlined.Logout,
            label = "Cerrar sesión",
            onClick = onLogout,
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Item del menú del Drawer
 */
@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint
            )
        },
        label = {
            Text(
                text = label,
                color = tint
            )
        },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}

/**
 * Bottom Navigation moderno con indicador animado y badge de mensajes
 */
@Composable
private fun ModernBottomNavigation(
    currentDestination: androidx.navigation.NavDestination?,
    unreadMessagesCount: Int,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        BottomNavItem.values().forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            val showBadge = item == BottomNavItem.Chat && unreadMessagesCount > 0

            val scale by animateFloatAsState(
                targetValue = if (selected) 1.1f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "navItemScale"
            )

            NavigationBarItem(
                icon = {
                    if (showBadge) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ) {
                                    Text(
                                        text = if (unreadMessagesCount > 99) "99+" else unreadMessagesCount.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = null,
                                modifier = Modifier.scale(scale)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = null,
                            modifier = Modifier.scale(scale)
                        )
                    }
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = selected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.semantics {
                    contentDescription = if (showBadge)
                        "Navegar a ${item.title}, $unreadMessagesCount mensajes sin leer"
                    else
                        "Navegar a ${item.title}"
                }
            )
        }
    }
}

/**
 * FAB animado con efecto de aparición y pulso suave
 */
@Composable
private fun AnimatedFab(
    visible: Boolean,
    onClick: () -> Unit
) {
    val fabScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fabScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "fabPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    if (fabScale > 0f) {
        LargeFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .scale(fabScale * pulseScale)
                .semantics { contentDescription = "Crear nuevo producto" }
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}