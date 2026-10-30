package com.example.proyecto_cloro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_cloro.domain.estado.obtenerEstado
import com.example.proyecto_cloro.domain.model.Medicion
import com.example.proyecto_cloro.ui.components.Indicador
import com.example.proyecto_cloro.ui.components.FormularioMedicion
import com.example.proyecto_cloro.ui.components.TarjetaMedicion

@Composable
fun ContenedorPantalla(
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(titulo, style = MaterialTheme.typography.headlineMedium)
        contenido()
    }
}

@Composable
fun PantallaInicio(mediciones: List<Medicion>) {
    val conteos = remember(mediciones) {
        mediciones.groupingBy { obtenerEstado(it.cloro) }.eachCount()
    }
    val normales = conteos.getOrDefault("NORMAL", 0)
    val advertencias = conteos.getOrDefault("ADVERTENCIA", 0)
    val criticas = conteos.getOrDefault("CRÍTICO", 0)

    ContenedorPantalla("Cloro en Línea") {
        Text("Monitoreo de calidad del agua")
        Text("Resumen de mediciones", style = MaterialTheme.typography.titleLarge)

        androidx.compose.foundation.layout.Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Indicador("Normales", normales, Color(0xFF238636), Modifier.weight(1f))
            Indicador("Advertencia", advertencias, Color(0xFFE6A000), Modifier.weight(1f))
            Indicador("Críticas", criticas, Color(0xFFD32F2F), Modifier.weight(1f))
        }

        Text("Últimas mediciones", style = MaterialTheme.typography.titleLarge)
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                count = minOf(mediciones.size, 3),
                key = { index -> mediciones[index].id }
            ) { index ->
                TarjetaMedicion(mediciones[index])
            }
        }
    }
}

@Composable
fun PantallaMediciones(
    mediciones: List<Medicion>,
    alGuardar: (Medicion) -> Unit
) {
    ContenedorPantalla("Mediciones") {
        FormularioMedicion(alGuardar)
        HorizontalDivider()
        Text("Puntos de control registrados", style = MaterialTheme.typography.titleMedium)
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(mediciones, key = { it.id }) {
                TarjetaMedicion(it)
            }
        }
    }
}

@Composable
fun PantallaHistorial(mediciones: List<Medicion>) {
    ContenedorPantalla("Historial") {
        Text("Registro de mediciones disponibles")
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(mediciones, key = { it.id }) {
                TarjetaMedicion(it)
            }
        }
    }
}

@Composable
fun PantallaAlertas(mediciones: List<Medicion>) {
    val alertas = remember(mediciones) {
        mediciones.filter { obtenerEstado(it.cloro) != "NORMAL" }
    }

    ContenedorPantalla("Alertas") {
        Text("Puntos que requieren revisión")
        if (alertas.isEmpty()) {
            Text("No hay alertas activas.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(alertas, key = { it.id }) {
                    TarjetaMedicion(it)
                }
            }
        }
        Text("Las alertas son ficticias y requieren validación.", fontSize = 12.sp)
    }
}
