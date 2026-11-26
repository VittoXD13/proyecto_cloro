package com.example.proyecto_cloro.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import com.example.proyecto_cloro.domain.model.SesionDemo
import com.example.proyecto_cloro.ui.screens.PantallaAccionesCorrectivas
import com.example.proyecto_cloro.ui.screens.PantallaAlertas
import com.example.proyecto_cloro.ui.screens.PantallaHistorial
import com.example.proyecto_cloro.ui.screens.PantallaInicio
import com.example.proyecto_cloro.ui.screens.PantallaInicioSesion
import com.example.proyecto_cloro.ui.screens.PantallaMediciones
import com.example.proyecto_cloro.ui.screens.PantallaPerfil
import com.example.proyecto_cloro.ui.screens.PantallaPuntos
import com.example.proyecto_cloro.ui.viewmodel.MedicionViewModel

private data class Seccion(
    val ruta: String,
    val titulo: String,
    val simbolo: String
)

@Composable
fun AplicacionCloro() {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val viewModel: MedicionViewModel = viewModel()
    val mediciones by viewModel.mediciones.collectAsStateWithLifecycle()
    val puntos by viewModel.puntos.collectAsStateWithLifecycle()
    val acciones by viewModel.acciones.collectAsStateWithLifecycle()
    var sesion by remember { mutableStateOf<SesionDemo?>(null) }
    val rolSesion = sesion?.rol ?: RolUsuario.OPERARIO
    val granjasPermitidas = sesion?.granjasAsignadas.orEmpty()
    val rutaInicialSesion = rolSesion.rutaInicial
    val secciones = remember(rolSesion) {
        when (rolSesion) {
            RolUsuario.OPERARIO -> listOf(
                Seccion("mediciones", "Mediciones", "≡"),
                Seccion("alertas", "Alertas", "!"),
                Seccion("puntos", "Puntos", "⌖"),
                Seccion("historial", "Historial", "◷"),
                Seccion("perfil", "Perfil", "◉")
            )
            RolUsuario.SUPERVISOR -> listOf(
                Seccion("alertas", "Alertas", "!"),
                Seccion("acciones", "Acciones", "✓"),
                Seccion("puntos", "Puntos", "⌖"),
                Seccion("historial", "Historial", "◷"),
                Seccion("perfil", "Perfil", "◉")
            )
            RolUsuario.JEFATURA -> listOf(
                Seccion("inicio", "Inicio", "⌂"),
                Seccion("historial", "Historial", "◷"),
                Seccion("puntos", "Puntos", "⌖"),
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
                                        popUpTo(rutaInicialSesion) { saveState = true }
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
                    PantallaInicioSesion { nuevaSesion ->
                        sesion = nuevaSesion
                        viewModel.iniciarSesion(nuevaSesion)
                        navController.navigate(nuevaSesion.rol.rutaInicial) {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
                composable("inicio") {
                    if (rolSesion.puedeConsultarIndicadores) {
                        PantallaInicio(mediciones, granjasPermitidas)
                    } else {
                        PantallaHistorial(mediciones, granjasPermitidas)
                    }
                }
                composable("mediciones") {
                    if (rolSesion.puedeRegistrarMediciones) {
                        PantallaMediciones(
                            mediciones.filter { it.granja in granjasPermitidas },
                            puntos.filter { it.granja in granjasPermitidas }
                        ) { medicion, completar ->
                            viewModel.guardar(medicion, completar)
                        }
                    } else {
                        PantallaHistorial(mediciones, granjasPermitidas)
                    }
                }
                composable("historial") {
                    PantallaHistorial(mediciones, granjasPermitidas)
                }
                composable("alertas") {
                    if (rolSesion != RolUsuario.JEFATURA) {
                        PantallaAlertas(
                            mediciones = mediciones.filter { it.granja in granjasPermitidas },
                            acciones = acciones,
                            granjasPermitidas = granjasPermitidas,
                            rol = rolSesion,
                            responsable = sesion?.correo.orEmpty(),
                            alGuardarAccion = { viewModel.guardarAccion(it) },
                            alCerrarAlerta = viewModel::cerrarAccion
                        )
                    } else {
                        PantallaHistorial(mediciones, granjasPermitidas)
                    }
                }
                composable("acciones") {
                    if (rolSesion == RolUsuario.SUPERVISOR) {
                        PantallaAccionesCorrectivas(
                            mediciones = mediciones,
                            acciones = acciones,
                            granjasPermitidas = granjasPermitidas
                        )
                    } else {
                        PantallaHistorial(mediciones, granjasPermitidas)
                    }
                }
                composable("puntos") {
                    PantallaPuntos(
                        puntos = puntos,
                        granjasPermitidas = granjasPermitidas,
                        puedeEditar = rolSesion.puedeAdministrarCatalogo,
                        alAgregar = viewModel::agregarPunto
                    )
                }
                composable("perfil") {
                    PantallaPerfil(sesion?.correo.orEmpty(), rolSesion) {
                        sesion = null
                        viewModel.cerrarSesion()
                        navController.navigate("login") {
                            popUpTo(rutaInicialSesion) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }
}
