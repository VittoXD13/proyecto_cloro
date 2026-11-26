package com.example.proyecto_cloro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyecto_cloro.domain.model.PuntoControl

@Composable
fun PantallaPuntos(
    puntos: List<PuntoControl>,
    granjasPermitidas: Set<String>,
    puedeEditar: Boolean,
    alAgregar: (PuntoControl, (Boolean) -> Unit) -> Unit
) {
    var granja by remember(granjasPermitidas) {
        mutableStateOf(granjasPermitidas.firstOrNull().orEmpty())
    }
    var galpon by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var mostrarErrores by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    val errores = mapOf(
        "granja" to when {
            granja.isBlank() -> "Ingresa la granja."
            granja.trim() !in granjasPermitidas -> "Selecciona una granja asignada."
            else -> null
        },
        "galpon" to if (galpon.isBlank()) "Ingresa el galpón." else null,
        "codigo" to if (codigo.isBlank()) "Ingresa el código." else null,
        "ubicacion" to if (ubicacion.isBlank()) "Ingresa la ubicación." else null,
        "tipo" to if (tipo.isBlank()) "Ingresa el tipo de punto." else null,
        "nombre" to if (nombre.isBlank()) "Ingresa el nombre." else null
    )
    val puntosVisibles = puntos.filter { it.granja in granjasPermitidas }

    ContenedorPantalla("Puntos de medición") {
        Text(
            "Catálogo local de puntos por granja. Los códigos deben ser únicos.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (puedeEditar) Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (granjasPermitidas.size > 1) {
                    OutlinedTextField(
                        value = granja,
                        onValueChange = { granja = it },
                        label = { Text("Granja") },
                        isError = mostrarErrores && errores["granja"] != null,
                        supportingText = {
                            errores["granja"]?.takeIf { mostrarErrores }?.let { Text(it) }
                        },
                        trailingIcon = {
                            if (mostrarErrores && errores["granja"] != null) {
                                Text("!", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("Granja: $granja", style = MaterialTheme.typography.titleMedium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoCatalogo(galpon, { galpon = it }, "Galpón", errores["galpon"], mostrarErrores, Modifier.weight(1f))
                    CampoCatalogo(codigo, { codigo = it }, "Código", errores["codigo"], mostrarErrores, Modifier.weight(1f))
                }
                CampoCatalogo(ubicacion, { ubicacion = it }, "Ubicación", errores["ubicacion"], mostrarErrores)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoCatalogo(tipo, { tipo = it }, "Tipo de punto", errores["tipo"], mostrarErrores, Modifier.weight(1f))
                    CampoCatalogo(nombre, { nombre = it }, "Nombre", errores["nombre"], mostrarErrores, Modifier.weight(1f))
                }
                if (mensaje.isNotBlank()) {
                    Text(
                        mensaje,
                        color = if (mensaje.startsWith("No se pudo")) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
                Button(
                    onClick = {
                        mostrarErrores = true
                        if (errores.values.all { it == null }) {
                            alAgregar(
                                PuntoControl(
                                    granja = granja.trim(),
                                    galpon = galpon.trim(),
                                    codigo = codigo.trim(),
                                    ubicacion = ubicacion.trim(),
                                    tipo = tipo.trim(),
                                    nombre = nombre.trim()
                                )
                            ) { agregado ->
                                mensaje = if (agregado) {
                                    "Punto agregado al catálogo."
                                } else {
                                    "No se pudo agregar: el código ya existe."
                                }
                                if (agregado) {
                                    galpon = ""
                                    codigo = ""
                                    ubicacion = ""
                                    tipo = ""
                                    nombre = ""
                                    mostrarErrores = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Agregar punto")
                }
            }
        }
        Text("${puntosVisibles.size} puntos registrados", style = MaterialTheme.typography.titleMedium)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(puntosVisibles, key = { it.id }) { punto ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text("${punto.codigo} · ${punto.nombre}", style = MaterialTheme.typography.titleMedium)
                        Text("${punto.granja} · ${punto.galpon} · ${punto.tipo}")
                        Text(
                            punto.ubicacion,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CampoCatalogo(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    mostrarErrores: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = mostrarErrores && error != null,
        supportingText = {
            error?.takeIf { mostrarErrores }?.let { Text(it) }
        },
        trailingIcon = {
            if (mostrarErrores && error != null) {
                Text("!", color = MaterialTheme.colorScheme.error)
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}
