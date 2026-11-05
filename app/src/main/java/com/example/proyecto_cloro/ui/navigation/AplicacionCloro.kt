package com.example.proyecto_cloro.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyecto_cloro.domain.model.RolUsuario
import com.example.proyecto_cloro.ui.screens.PantallaHistorial
import com.example.proyecto_cloro.ui.screens.PantallaInicio
import com.example.proyecto_cloro.ui.screens.PantallaInicioSesion
import com.example.proyecto_cloro.ui.screens.PantallaMediciones
import com.example.proyecto_cloro.ui.screens.PantallaPerfil
import com.example.proyecto_cloro.ui.viewmodel.MedicionViewModel

private data class Seccion(
    val ruta: String,
    val titulo: String,
    val simbolo: String
)

private data class SesionActual(
    val correo: String = "",
    val rol: RolUsuario = RolUsuario.OPERARIO
)

@Composable
fun AplicacionCloro() {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val viewModel: MedicionViewModel = viewModel()
    val mediciones by viewModel.mediciones.collectAsStateWithLifecycle()
    var sesion by remember { mutableStateOf(SesionActual()) }
    val rolSesion = sesion.rol
    val rutaInicialSesion = rolSesion.rutaInicial
    val secciones = remember(rolSesion) {
        when (rolSesion) {
            RolUsuario.OPERARIO -> listOf(
                Seccion("mediciones", "Mediciones", "≡"),
                Seccion("historial", "Historial", "◷"),
                Seccion("perfil", "Perfil", "◉")
            )
            RolUsuario.SUPERVISOR -> listOf(
                Seccion("historial", "Alertas", "!"),
                Seccion("perfil", "Perfil", "◉")
            )
            RolUsuario.JEFATURA -> listOf(
                Seccion("inicio", "Inicio", "⌂"),
                Seccion("historial", "Historial", "◷"),
                Seccion("perfil", "Perfil", "◉")
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (rutaActual != null && rutaActual != "login") {
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
                                        popUpTo(rutaInicialSesion) {
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
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "login",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                composable("login") {
                    PantallaInicioSesion { correo, rol ->
                        sesion = SesionActual(correo, rol)
                        navController.navigate(rol.rutaInicial) {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
                composable("inicio") {
                    if (rolSesion.puedeConsultarIndicadores) {
                        PantallaInicio(mediciones)
                    } else {
                        PantallaHistorial(mediciones, rolSesion)
                    }
                }
                composable("mediciones") {
                    if (rolSesion.puedeRegistrarMediciones) {
                        PantallaMediciones(mediciones, viewModel::guardar)
                    } else {
                        PantallaHistorial(mediciones, rolSesion)
                    }
                }
                composable("historial") {
                    PantallaHistorial(mediciones, rolSesion)
                }
                composable("perfil") {
                    PantallaPerfil(sesion.correo, rolSesion) {
                        val rutaDeSesion = rutaInicialSesion
                        sesion = SesionActual()
                        navController.navigate("login") {
                            popUpTo(rutaDeSesion) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }
}
