package com.example.proyecto_cloro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_cloro.domain.estado.obtenerEstado
import com.example.proyecto_cloro.domain.model.Medicion
import com.example.proyecto_cloro.ui.theme.Advertencia
import com.example.proyecto_cloro.ui.theme.Critico
import com.example.proyecto_cloro.ui.theme.Normal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun Indicador(
    titulo: String,
    cantidad: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                titulo,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                cantidad.toString(),
                color = color,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TarjetaMedicion(medicion: Medicion) {
    val estado = obtenerEstado(medicion.cloro)
    val textoCloro = remember(medicion.cloro) {
        String.format(Locale.US, "Cloro: %.2f ppm", medicion.cloro)
    }
    val colorEstado = when (estado) {
        "NORMAL" -> Normal
        "ADVERTENCIA" -> Advertencia
        else -> Critico
    }
    val complementarios = remember(medicion.ph, medicion.temperatura, medicion.turbidez) {
        buildList {
            medicion.ph?.let { add("pH: $it") }
            medicion.temperatura?.let { add("Temp: $it °C") }
            medicion.turbidez?.let { add("Turb: $it NTU") }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${medicion.granja} - ${medicion.galpon}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    medicion.codigoPunto,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "${medicion.punto} (${medicion.tipoPunto}) - ${medicion.ubicacion}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                textoCloro,
                style = MaterialTheme.typography.headlineSmall
            )
            if (complementarios.isNotEmpty()) {
                Text(
                    complementarios.joinToString(" | "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = colorEstado.copy(alpha = 0.12f)
            ) {
                Text(
                    estado,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = colorEstado,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                "Fecha: ${medicion.fecha}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FormularioMedicion(alGuardar: (Medicion) -> Unit) {
    var granja by remember { mutableStateOf("Granja 1") }
    var galpon by remember { mutableStateOf("Galpón A") }
    var codigoPunto by remember { mutableStateOf("P-001") }
    var ubicacion by remember { mutableStateOf("Bebedero Línea 1") }
    var tipoPunto by remember { mutableStateOf("Bebedero") }
    var punto by remember { mutableStateOf("Línea Final") }
    var nivelCloro by remember { mutableStateOf("") }
    var phText by remember { mutableStateOf("") }
    var tempText by remember { mutableStateOf("") }
    var turbidezText by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Registrar Medición del Punto", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = granja,
                    onValueChange = { granja = it },
                    label = { Text("Granja") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = galpon,
                    onValueChange = { galpon = it },
                    label = { Text("Galpón") },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = codigoPunto,
                    onValueChange = { codigoPunto = it },
                    label = { Text("Cód. Punto") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = tipoPunto,
                    onValueChange = { tipoPunto = it },
                    label = { Text("Tipo Punto") },
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = ubicacion,
                onValueChange = { ubicacion = it },
                label = { Text("Ubicación exacta") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = punto,
                onValueChange = { punto = it },
                label = { Text("Nombre del punto") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = nivelCloro,
                onValueChange = { nivelCloro = it },
                label = { Text("Cloro Libre (ppm / mg/L) * Obligatorio") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Text("Parámetros adicionales (Opcional)", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = phText,
                    onValueChange = { phText = it },
                    label = { Text("pH") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = tempText,
                    onValueChange = { tempText = it },
                    label = { Text("Temp (°C)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = turbidezText,
                    onValueChange = { turbidezText = it },
                    label = { Text("Turbidez") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
            Button(
                onClick = {
                    val cloroVal = nivelCloro.replace(',', '.').toDoubleOrNull()
                    val phVal = phText.replace(',', '.').toDoubleOrNull()
                    val tempVal = tempText.replace(',', '.').toDoubleOrNull()
                    val turbidezVal = turbidezText.replace(',', '.').toDoubleOrNull()

                    if (granja.isNotBlank() && punto.isNotBlank() && cloroVal != null && cloroVal >= 0) {
                        val fecha = SimpleDateFormat(
                            "dd/MM/yyyy HH:mm",
                            Locale.getDefault()
                        ).format(Date())
                        alGuardar(
                            Medicion(
                                granja = granja.trim(),
                                galpon = galpon.trim(),
                                codigoPunto = codigoPunto.trim(),
                                ubicacion = ubicacion.trim(),
                                tipoPunto = tipoPunto.trim(),
                                punto = punto.trim(),
                                cloro = cloroVal,
                                ph = phVal,
                                temperatura = tempVal,
                                turbidez = turbidezVal,
                                fecha = fecha
                            )
                        )
                        nivelCloro = ""
                        phText = ""
                        tempText = ""
                        turbidezText = ""
                        mensaje = "Medición registrada exitosamente."
                    } else {
                        mensaje = "Ingresa un valor válido de cloro libre."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Medición")
            }
            if (mensaje.isNotBlank()) {
                Text(mensaje, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
