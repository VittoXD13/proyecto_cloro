package com.example.proyecto_cloro.ui.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
        bottomBar = {
            NavigationBar {
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
                        label = { Text(seccion.titulo) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "inicio",
            modifier = Modifier.padding(padding)
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
