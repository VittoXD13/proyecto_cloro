package com.example.proyecto_cloro.ui.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyecto_cloro.ui.screens.PantallaAlertas
import com.example.proyecto_cloro.ui.screens.PantallaHistorial
import com.example.proyecto_cloro.ui.screens.PantallaInicio
import com.example.proyecto_cloro.ui.screens.PantallaMediciones
import com.example.proyecto_cloro.ui.viewmodel.MedicionViewModel

private data class Seccion(
    val ruta: String,
    val titulo: String,
    val simbolo: String
)

private val secciones = listOf(
    Seccion("inicio", "Inicio", "⌂"),
    Seccion("mediciones", "Mediciones", "≡"),
    Seccion("historial", "Historial", "◷"),
    Seccion("alertas", "Alertas", "!")
)

@Composable
fun AplicacionCloro() {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val viewModel: MedicionViewModel = viewModel()
    val mediciones by viewModel.mediciones.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 6.dp
            ) {
                secciones.forEach { seccion ->
                    NavigationBarItem(
                        selected = rutaActual == seccion.ruta,
                        onClick = {
                            navController.navigate(seccion.ruta) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(seccion.simbolo, fontSize = 22.sp) },
                        label = { Text(seccion.titulo) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "inicio",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            composable("inicio") {
                PantallaInicio(mediciones)
            }
            composable("mediciones") {
                PantallaMediciones(mediciones, viewModel::guardar)
            }
            composable("historial") {
                PantallaHistorial(mediciones)
            }
            composable("alertas") {
                PantallaAlertas(mediciones)
            }
        }
    }
}
