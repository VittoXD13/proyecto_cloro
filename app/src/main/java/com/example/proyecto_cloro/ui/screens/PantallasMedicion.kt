package com.example.proyecto_cloro.ui.screens

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyecto_cloro.domain.estado.obtenerEstado
import com.example.proyecto_cloro.domain.model.Medicion
import com.example.proyecto_cloro.domain.model.PuntoControl
import com.example.proyecto_cloro.ui.components.Indicador
import com.example.proyecto_cloro.ui.components.FormularioMedicion
import com.example.proyecto_cloro.ui.components.TarjetaMedicion
import com.example.proyecto_cloro.ui.theme.Advertencia
import com.example.proyecto_cloro.ui.theme.Critico
import com.example.proyecto_cloro.ui.theme.Normal
import java.text.SimpleDateFormat
import java.util.Locale

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
fun PantallaInicio(mediciones: List<Medicion>, granjasPermitidas: Set<String>) {
    val medicionesVisibles = remember(mediciones, granjasPermitidas) {
        mediciones.filter { it.granja in granjasPermitidas }
    }
    val medicionesActuales = remember(medicionesVisibles) {
        medicionesVisibles.distinctBy { it.codigoPunto }
    }
    val conteos = remember(medicionesActuales) {
        medicionesActuales.groupingBy { obtenerEstado(it.cloro) }.eachCount()
    }
    val normales = conteos.getOrDefault("NORMAL", 0)
    val advertencias = conteos.getOrDefault("ADVERTENCIA", 0)
    val criticas = conteos.getOrDefault("CRÍTICO", 0)

    val puntosActuales = medicionesActuales.take(6)
    ContenedorPantalla("Cloro en Línea") {
      Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text("Fuente: simulación local de API; actualización automática cada 5 minutos.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall)
        Text("Indicadores generales", style = MaterialTheme.typography.titleLarge)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Indicador("Normal", normales, Normal, Modifier.weight(1f))
            Indicador("Aviso", advertencias, Advertencia, Modifier.weight(1f))
            Indicador("Crítica", criticas, Critico, Modifier.weight(1f))
        }
        val valores = medicionesActuales.map { it.cloro }
        val promedio = valores.average().takeIf { valores.isNotEmpty() } ?: 0.0
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Resumen de cloro (ppm)", style = MaterialTheme.typography.titleMedium)
                Text("Promedio: ${"%.2f".format(Locale.getDefault(), promedio)}")
                Text("Mínimo: ${valores.minOrNull()?.let { "%.2f".format(Locale.getDefault(), it) } ?: "—"}")
                Text("Máximo: ${valores.maxOrNull()?.let { "%.2f".format(Locale.getDefault(), it) } ?: "—"}")
                Text("Alertas activas: ${advertencias + criticas}")
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Comparación reciente por punto", style = MaterialTheme.typography.titleMedium)
                if (puntosActuales.isEmpty()) {
                    Text("Sin datos para comparar.")
                }
                puntosActuales.forEach { medicion ->
                    Text("${medicion.codigoPunto} · ${"%.2f".format(Locale.getDefault(), medicion.cloro)} ppm")
                    LinearProgressIndicator(
                        progress = { (medicion.cloro / 10.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        Text("Últimas mediciones por punto", style = MaterialTheme.typography.titleLarge)
        if (medicionesVisibles.isEmpty()) {
            Text("Aún no hay mediciones registradas.")
        } else {
            medicionesVisibles.take(3).forEach { TarjetaMedicion(it) }
        }
      }
    }
}

@Composable
fun PantallaMediciones(
    mediciones: List<Medicion>,
    puntos: List<PuntoControl>,
    alGuardar: (Medicion, () -> Unit) -> Unit
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
                Text(
                    "Las lecturas de demostración se actualizan automáticamente cada 5 minutos.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            item {
                FormularioMedicion(puntos, alGuardar)
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
fun PantallaHistorial(
    mediciones: List<Medicion>,
    granjasPermitidas: Set<String>
) {
    var periodoDias by remember { mutableStateOf(7) }
    val granjasDisponibles = remember(granjasPermitidas) {
        listOf("Todas") + granjasPermitidas.sorted()
    }
    var granjaSeleccionada by remember(granjasPermitidas) { mutableStateOf("Todas") }
    val codigosDisponibles = remember(mediciones, granjaSeleccionada, granjasPermitidas) {
        listOf("Todos") + mediciones
            .filter {
                it.granja in granjasPermitidas &&
                    (granjaSeleccionada == "Todas" || it.granja == granjaSeleccionada)
            }
            .map { it.codigoPunto }
            .distinct()
            .sorted()
    }
    var codigoSeleccionado by remember(granjaSeleccionada, granjasPermitidas) {
        mutableStateOf("Todos")
    }
    val fechaMinima = remember(periodoDias) {
        System.currentTimeMillis() - periodoDias * 24L * 60L * 60L * 1000L
    }
    val formato = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val medicionesVisibles = remember(
        mediciones,
        granjasPermitidas,
        fechaMinima,
        granjaSeleccionada,
        codigoSeleccionado
    ) {
        mediciones.filter { medicion ->
            medicion.granja in granjasPermitidas &&
                (granjaSeleccionada == "Todas" || medicion.granja == granjaSeleccionada) &&
                (codigoSeleccionado == "Todos" || medicion.codigoPunto == codigoSeleccionado) &&
                (formato.parse(medicion.fecha)?.time ?: 0L) >= fechaMinima
        }
    }

    ContenedorPantalla("Historial") {
        Text(
            "Consulta registros recientes por período y granja.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1 to "24 h", 7 to "7 días", 30 to "30 días").forEach { (dias, etiqueta) ->
                FilterChip(
                    selected = periodoDias == dias,
                    onClick = { periodoDias = dias },
                    label = { Text(etiqueta) }
                )
            }
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            granjasDisponibles.forEach { granja ->
                FilterChip(
                    selected = granjaSeleccionada == granja,
                    onClick = {
                        granjaSeleccionada = granja
                        codigoSeleccionado = "Todos"
                    },
                    label = { Text(granja) }
                )
            }
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            codigosDisponibles.forEach { codigo ->
                FilterChip(
                    selected = codigoSeleccionado == codigo,
                    onClick = { codigoSeleccionado = codigo },
                    label = { Text(codigo) }
                )
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (medicionesVisibles.isEmpty()) {
                item {
                    Text(
                        "No hay mediciones en este período.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(medicionesVisibles, key = { it.id }) {
                    TarjetaMedicion(it)
                }
            }
        }
    }
}
