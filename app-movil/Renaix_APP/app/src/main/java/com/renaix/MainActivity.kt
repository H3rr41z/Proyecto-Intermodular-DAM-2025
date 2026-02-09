package com.renaix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.renaix.di.AppContainerImpl
import com.renaix.presentation.navigation.RenaixNavGraph
import com.renaix.ui.theme.RenaixTheme

/**
 * MainActivity - Punto de entrada de la aplicación
 *
 * Configura:
 * - Theme (Material 3 con tema morado personalizado)
 * - NavController para navegación
 * - AppContainer para inyección de dependencias
 * - NavHost con todas las rutas de la app
 */
class MainActivity : ComponentActivity() {

    // AppContainer - Contenedor de dependencias manual
    private lateinit var appContainer: AppContainerImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilitar edge-to-edge para aprovechar toda la pantalla
        enableEdgeToEdge()

        // Inicializar AppContainer
        appContainer = AppContainerImpl(applicationContext)

        setContent {
            // NavController para gestionar la navegación
            val navController = rememberNavController()

            // Tema personalizado de Renaix
            RenaixTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Grafo de navegación principal
                    RenaixNavGraph(
                        navController = navController,
                        appContainer = appContainer
                    )
                }
            }
        }
    }
}
