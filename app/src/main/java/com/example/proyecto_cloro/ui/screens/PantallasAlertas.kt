package com.example.proyecto_cloro.ui.screens

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.proyecto_cloro.domain.estado.obtenerEstado
import com.example.proyecto_cloro.domain.model.AccionCorrectiva
import com.example.proyecto_cloro.domain.model.Medicion
import com.example.proyecto_cloro.domain.model.RolUsuario
import com.example.proyecto_cloro.ui.theme.Advertencia
import com.example.proyecto_cloro.ui.theme.Critico
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PantallaAlertas(
    mediciones: List<Medicion>,
    acciones: List<AccionCorrectiva>,
    granjasPermitidas: Set<String>,
    rol: RolUsuario,
    responsable: String,
    alGuardarAccion: (AccionCorrectiva) -> Unit,
    alCerrarAlerta: (Int, String, String) -> Unit
) {
    var medicionSeleccionada by remember { mutableStateOf<Medicion?>(null) }
    val alertas = mediciones.filter {
        it.granja in granjasPermitidas && obtenerEstado(it.cloro) != "NORMAL"
    }

    ContenedorPantalla("Alertas") {
        Text(
            "Desviaciones detectadas automáticamente. Los valores críticos se destacan.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Activas: ${alertas.count { alerta -> acciones.none { it.medicionId == alerta.id && it.cerrada } }}")
            Text("Críticas: ${alertas.count { obtenerEstado(it.cloro) == "CRÍTICO" }}")
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (alertas.isEmpty()) {
                item {
                    Text(
                        "No hay desviaciones registradas.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(alertas, key = { it.id }) { alerta ->
                val accion = acciones.firstOrNull { it.medicionId == alerta.id }
                val ultimaVerificacion = accion?.let { actual ->
                    mediciones
                        .filter {
                            it.id > actual.medicionBaseId &&
                                it.codigoPunto == alerta.codigoPunto &&
                                it.granja == alerta.granja
                        }
                        .maxByOrNull { it.id }
                }
                val verificada = ultimaVerificacion?.let {
                    obtenerEstado(it.cloro) == "NORMAL"
                } == true
                val colorAlerta = if (obtenerEstado(alerta.cloro) == "CRÍTICO") {
                    Critico
                } else {
                    Advertencia
                }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "${obtenerEstado(alerta.cloro)} · ${alerta.granja} · ${alerta.codigoPunto}",
                            color = colorAlerta,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("${alerta.punto}: ${"%.2f".format(Locale.getDefault(), alerta.cloro)} ppm")
                        Text("Desviación: ${if (alerta.cloro < 2.0) "cloro insuficiente" else "cloro excesivo"}.")
                        Text("Fecha: ${alerta.fecha}")
                        if (accion == null) {
                            Button(
                                onClick = { medicionSeleccionada = alerta },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Registrar acción correctiva")
                            }
                        } else {
                            Text("Acción: ${accion.descripcion}")
                            Text("Responsable: ${accion.responsable} · ${accion.fecha}")
                            Text(if (accion.cerrada) "Alerta cerrada" else "Acción pendiente de validación")
                            if (
                                rol == RolUsuario.SUPERVISOR &&
                                !accion.cerrada &&
                                verificada
                            ) {
                                Button(
                                    onClick = {
                                        val fecha = SimpleDateFormat(
                                            "dd/MM/yyyy HH:mm",
                                            Locale.getDefault()
                                        ).format(Date())
                                        alCerrarAlerta(alerta.id, fecha, responsable)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Validar nueva medición y cerrar")
                                }
                            } else if (rol == RolUsuario.SUPERVISOR && !accion.cerrada) {
                                Text(
                                    "Para cerrar, registra una nueva medición normal en este punto.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    medicionSeleccionada?.let { alerta ->
        DialogoAccionCorrectiva(
            medicion = alerta,
            responsable = responsable,
            onDismiss = { medicionSeleccionada = null },
            onGuardar = {
                alGuardarAccion(it.copy(medicionBaseId = mediciones.maxOfOrNull { medicion -> medicion.id } ?: 0))
                medicionSeleccionada = null
            }
        )
    }
}

@Composable
private fun DialogoAccionCorrectiva(
    medicion: Medicion,
    responsable: String,
    onDismiss: () -> Unit,
    onGuardar: (AccionCorrectiva) -> Unit
) {
    val context = LocalContext.current
    var descripcion by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var uriPendiente by remember { mutableStateOf<Uri?>(null) }
    var archivoPendiente by remember { mutableStateOf<File?>(null) }
    var error by remember { mutableStateOf("") }
    val camara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { capturada ->
        if (capturada) {
            fotoUri = uriPendiente
        } else {
            val archivo = archivoPendiente
            if (archivo?.exists() == true && !archivo.delete()) {
                error = "No se pudo eliminar la foto cancelada."
            }
        }
        uriPendiente = null
        archivoPendiente = null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Acción correctiva") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${medicion.granja} · ${medicion.codigoPunto} · ${medicion.punto}")
                Text("Desviación: ${if (medicion.cloro < 2.0) "cloro insuficiente" else "cloro excesivo"}")
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = {
                        descripcion = it
                        error = ""
                    },
                    label = { Text("Acción realizada") },
                    isError = error.isNotBlank(),
                    supportingText = if (error.isNotBlank()) {
                        { Text(error) }
                    } else {
                        null
                    },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = {
                        try {
                            val directorio = File(context.filesDir, "fotos_accion")
                            if (!directorio.exists() && !directorio.mkdirs()) {
                                error = "No se pudo preparar el almacenamiento de la foto."
                            } else {
                                val archivo = File.createTempFile(
                                    "accion_${medicion.id}_",
                                    ".jpg",
                                    directorio
                                )
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    archivo
                                )
                                uriPendiente = uri
                                archivoPendiente = archivo
                                camara.launch(uri)
                            }
                        } catch (exception: IOException) {
                            error = "No se pudo crear el archivo para la foto."
                        } catch (exception: IllegalArgumentException) {
                            error = "No se pudo abrir la cámara."
                        } catch (exception: ActivityNotFoundException) {
                            error = "No hay una aplicación de cámara disponible."
                        } catch (exception: SecurityException) {
                            error = "No se concedió acceso a la cámara."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (fotoUri == null) "Tomar foto de la acción" else "Foto adjunta · volver a tomar")
                }
                fotoUri?.let {
                    Text("Evidencia fotográfica adjunta.", color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (descripcion.isBlank()) {
                        error = "Describe la acción realizada."
                    } else {
                        val fecha = SimpleDateFormat(
                            "dd/MM/yyyy HH:mm",
                            Locale.getDefault()
                        ).format(Date())
                        onGuardar(
                            AccionCorrectiva(
                                medicionId = medicion.id,
                                descripcion = descripcion.trim(),
                                responsable = responsable,
                                fecha = fecha,
                                fotoUri = fotoUri?.toString()
                            )
                        )
                    }
                }
            ) {
                Text("Guardar acción")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun PantallaAccionesCorrectivas(
    mediciones: List<Medicion>,
    acciones: List<AccionCorrectiva>,
    granjasPermitidas: Set<String>
) {
    val accionesVisibles = acciones.filter { accion ->
        mediciones.firstOrNull { it.id == accion.medicionId }?.granja in granjasPermitidas
    }
    ContenedorPantalla("Acciones correctivas") {
        Text(
            "Seguimiento de responsables, evidencias, acciones y cierre validado.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (accionesVisibles.isEmpty()) {
                item { Text("Aún no hay acciones correctivas.") }
            }
            items(accionesVisibles, key = { it.medicionId }) { accion ->
                val medicion = mediciones.firstOrNull { it.id == accion.medicionId }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            "${medicion?.granja ?: ""} · ${medicion?.codigoPunto ?: ""}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(accion.descripcion)
                        Text("Responsable: ${accion.responsable}")
                        Text("Registrada: ${accion.fecha}")
                        Text("Lectura asociada: ${medicion?.let { "%.2f ppm".format(Locale.getDefault(), it.cloro) } ?: "—"}")
                        accion.fechaCierre?.let { Text("Cerrada: $it") }
                        accion.validador?.let { Text("Validada por: $it") }
                        Text(if (accion.cerrada) "Estado: cerrada" else "Estado: pendiente")
                        if (accion.fotoUri != null) {
                            Text("Incluye evidencia fotográfica")
                        }
                    }
                }
            }
        }
    }
}
