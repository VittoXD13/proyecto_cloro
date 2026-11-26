package com.example.proyecto_cloro.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
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
import com.example.proyecto_cloro.domain.model.PuntoControl
import com.example.proyecto_cloro.domain.model.Medicion
import com.example.proyecto_cloro.domain.validation.CampoMedicion
import com.example.proyecto_cloro.domain.validation.validarMedicion
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
            if (medicion.sincronizacionPendiente) {
                Text(
                    "Pendiente de sincronización",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
fun FormularioMedicion(
    puntos: List<PuntoControl>,
    alGuardar: (Medicion, () -> Unit) -> Unit
) {
    var codigoSeleccionado by remember(puntos) {
        mutableStateOf(puntos.firstOrNull()?.codigo.orEmpty())
    }
    var menuPuntosAbierto by remember { mutableStateOf(false) }
    var nivelCloro by remember { mutableStateOf("") }
    var phText by remember { mutableStateOf("") }
    var tempText by remember { mutableStateOf("") }
    var turbidezText by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var mostrarErrores by remember { mutableStateOf(false) }
    val validacion = validarMedicion(nivelCloro, phText, tempText, turbidezText)
    val puntoSeleccionado = puntos.firstOrNull { it.codigo == codigoSeleccionado }

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
                .animateContentSize()
                .padding(16.dp)
        ) {
            Text("Registrar Medición del Punto", style = MaterialTheme.typography.titleLarge)
            Box {
                OutlinedButton(
                    onClick = { menuPuntosAbierto = true },
                    enabled = puntos.isNotEmpty(),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (mostrarErrores && puntoSeleccionado == null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            puntoSeleccionado?.let {
                                "${it.codigo} · ${it.granja} / ${it.galpon} · ${it.nombre}"
                            } ?: "Selecciona un punto de medición"
                        )
                        Text(
                            if (mostrarErrores && puntoSeleccionado == null) "!" else "⌄",
                            color = if (mostrarErrores && puntoSeleccionado == null) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                DropdownMenu(
                    expanded = menuPuntosAbierto,
                    onDismissRequest = { menuPuntosAbierto = false }
                ) {
                    puntos.forEach { punto ->
                        DropdownMenuItem(
                            text = {
                                Text("${punto.codigo} · ${punto.granja}, ${punto.galpon} · ${punto.nombre}")
                            },
                            onClick = {
                                codigoSeleccionado = punto.codigo
                                menuPuntosAbierto = false
                            }
                        )
                    }
                }
            }
            if (mostrarErrores && puntoSeleccionado == null) {
                Text(
                    if (puntos.isEmpty()) {
                        "No hay puntos registrados para esta granja."
                    } else {
                        "Selecciona un punto de medición."
                    },
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (puntoSeleccionado != null) {
                Text(
                    "${puntoSeleccionado.ubicacion} · ${puntoSeleccionado.tipo}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            OutlinedTextField(
                value = nivelCloro,
                onValueChange = {
                    nivelCloro = it
                    mensaje = ""
                },
                label = { Text("Cloro libre (ppm) *") },
                modifier = Modifier.fillMaxWidth(),
                isError = mostrarErrores && CampoMedicion.CLORO in validacion.errores,
                supportingText = {
                    validacion.errores[CampoMedicion.CLORO]?.takeIf { mostrarErrores }?.let {
                        Text(it)
                    }
                },
                trailingIcon = {
                    if (mostrarErrores && CampoMedicion.CLORO in validacion.errores) {
                        Text("!", color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Text("Parámetros adicionales (Opcional)", style = MaterialTheme.typography.titleSmall)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = phText,
                    onValueChange = { phText = it; mensaje = "" },
                    label = { Text("pH") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = mostrarErrores && CampoMedicion.PH in validacion.errores,
                    supportingText = {
                        validacion.errores[CampoMedicion.PH]?.takeIf { mostrarErrores }?.let {
                            Text(it)
                        }
                    },
                    trailingIcon = {
                        if (mostrarErrores && CampoMedicion.PH in validacion.errores) {
                            Text("!", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = tempText,
                    onValueChange = { tempText = it; mensaje = "" },
                    label = { Text("Temp (°C)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = mostrarErrores && CampoMedicion.TEMPERATURA in validacion.errores,
                    supportingText = {
                        validacion.errores[CampoMedicion.TEMPERATURA]?.takeIf { mostrarErrores }?.let {
                            Text(it)
                        }
                    },
                    trailingIcon = {
                        if (mostrarErrores && CampoMedicion.TEMPERATURA in validacion.errores) {
                            Text("!", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = turbidezText,
                    onValueChange = { turbidezText = it; mensaje = "" },
                    label = { Text("Turbidez") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = mostrarErrores && CampoMedicion.TURBIDEZ in validacion.errores,
                    supportingText = {
                        validacion.errores[CampoMedicion.TURBIDEZ]?.takeIf { mostrarErrores }?.let {
                            Text(it)
                        }
                    },
                    trailingIcon = {
                        if (mostrarErrores && CampoMedicion.TURBIDEZ in validacion.errores) {
                            Text("!", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
            Button(
                onClick = {
                    mostrarErrores = true
                    val cloroValido = validacion.cloro
                    if (validacion.esValido && puntoSeleccionado != null && cloroValido != null) {
                        val fecha = SimpleDateFormat(
                            "dd/MM/yyyy HH:mm",
                            Locale.getDefault()
                        ).format(Date())
                        alGuardar(
                            Medicion(
                                granja = puntoSeleccionado.granja,
                                galpon = puntoSeleccionado.galpon,
                                codigoPunto = puntoSeleccionado.codigo,
                                ubicacion = puntoSeleccionado.ubicacion,
                                tipoPunto = puntoSeleccionado.tipo,
                                punto = puntoSeleccionado.nombre,
                                cloro = cloroValido,
                                ph = validacion.ph,
                                temperatura = validacion.temperatura,
                                turbidez = validacion.turbidez,
                                fecha = fecha
                            )
                        ) {
                            nivelCloro = ""
                            phText = ""
                            tempText = ""
                            turbidezText = ""
                            mostrarErrores = false
                            mensaje = "Medición registrada exitosamente."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Medición")
            }
            AnimatedVisibility(visible = mensaje.isNotBlank()) {
                Text(mensaje, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
