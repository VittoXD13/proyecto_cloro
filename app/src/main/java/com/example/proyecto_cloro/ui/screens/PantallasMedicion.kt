package com.example.proyecto_cloro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_cloro.domain.estado.obtenerEstado
import com.example.proyecto_cloro.domain.model.Medicion
import com.example.proyecto_cloro.ui.components.Indicador
import com.example.proyecto_cloro.ui.components.FormularioMedicion
import com.example.proyecto_cloro.ui.components.TarjetaMedicion
import com.example.proyecto_cloro.ui.theme.Advertencia
import com.example.proyecto_cloro.ui.theme.Critico
import com.example.proyecto_cloro.ui.theme.Normal

@Composable
fun ContenedorPantalla(
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EncabezadoPantalla(
            titulo,
            Modifier.padding(top = 20.dp)
        )
        contenido()
    }
}

@Composable
private fun EncabezadoPantalla(titulo: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(5.dp)
                .height(30.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Text(
            titulo,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
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
        Text(
            "Monitoreo de calidad del agua",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Text("Resumen de mediciones", style = MaterialTheme.typography.titleLarge)

        androidx.compose.foundation.layout.Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Indicador("Normales", normales, Normal, Modifier.weight(1f))
            Indicador("Advertencia", advertencias, Advertencia, Modifier.weight(1f))
            Indicador("Críticas", criticas, Critico, Modifier.weight(1f))
        }

        Text("Últimas mediciones", style = MaterialTheme.typography.titleLarge)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (mediciones.isEmpty()) {
                item {
                    Text(
                        "Aún no hay mediciones registradas.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(
                    count = minOf(mediciones.size, 3),
                    key = { index -> mediciones[index].id }
                ) { index ->
                    TarjetaMedicion(mediciones[index])
                }
            }
        }
    }
}

@Composable
fun PantallaMediciones(
    mediciones: List<Medicion>,
    alGuardar: (Medicion) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        EncabezadoPantalla(
            "Mediciones",
            Modifier.padding(start = 20.dp, top = 20.dp, bottom = 4.dp)
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                FormularioMedicion(alGuardar)
            }
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Text(
                    "Puntos de control registrados",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(mediciones, key = { it.id }) {
                TarjetaMedicion(it)
            }
        }
    }
}

@Composable
fun PantallaHistorial(mediciones: List<Medicion>) {
    ContenedorPantalla("Historial") {
        Text(
            "Registro de mediciones disponibles",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
        Text(
            "Puntos que requieren revisión",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (alertas.isEmpty()) {
            Text(
                "No hay alertas activas.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alertas, key = { it.id }) {
                    TarjetaMedicion(it)
                }
            }
        }
        Text(
            "Las alertas son ficticias y requieren validación.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
