package com.example.proyecto_cloro.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyecto_cloro.domain.model.RolUsuario
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PantallaPerfil(correo: String, rol: RolUsuario, onCerrarSesion: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var fotoActualizada by remember(correo) { mutableIntStateOf(0) }
    var guardandoFoto by remember { mutableStateOf(false) }
    var errorFoto by remember { mutableStateOf<String?>(null) }
    val fotoPerfil by produceState<Bitmap?>(
        initialValue = null,
        key1 = correo,
        key2 = fotoActualizada
    ) {
        value = withContext(Dispatchers.IO) {
            cargarFotoPerfil(context, correo, (120 * context.resources.displayMetrics.density).toInt())
        }
    }
    val selectorDeFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                guardandoFoto = true
                errorFoto = null
                try {
                    withContext(Dispatchers.IO) {
                        guardarFotoPerfil(context, correo, uri)
                    }
                    fotoActualizada++
                } catch (_: IOException) {
                    errorFoto = "No se pudo guardar la foto. Inténtalo de nuevo."
                } catch (_: SecurityException) {
                    errorFoto = "No se pudo acceder a la imagen seleccionada."
                } finally {
                    guardandoFoto = false
                }
            }
        }
    }

    ContenedorPantalla("Perfil") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (fotoPerfil != null) {
                        Image(
                            bitmap = fotoPerfil!!.asImageBitmap(),
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(120.dp)
                        )
                    } else {
                        Text(
                            "Sin foto",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(
                    onClick = { selectorDeFoto.launch("image/*") },
                    enabled = !guardandoFoto
                ) {
                    if (guardandoFoto) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (fotoPerfil == null) "Elegir foto" else "Cambiar foto")
                    }
                }
                if (errorFoto != null) {
                    Text(
                        errorFoto!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    "Cuenta de prueba",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Correo electrónico",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(correo, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Rol principal",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(rol.etiqueta, style = MaterialTheme.typography.bodyLarge)
            }
        }
        Button(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}

private fun guardarFotoPerfil(context: Context, correo: String, uri: Uri) {
    val archivo = archivoFotoPerfil(context, correo)
    val temporal = File(archivo.parentFile, "${archivo.name}.tmp")
    try {
        val entrada = context.contentResolver.openInputStream(uri)
            ?: throw IOException("No se pudo abrir la imagen seleccionada")
        entrada.use { input ->
            temporal.outputStream().use { output -> input.copyTo(output) }
        }

        val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(temporal.absolutePath, opciones)
        if (opciones.outWidth <= 0 || opciones.outHeight <= 0) {
            throw IOException("El archivo seleccionado no es una imagen válida")
        }
        if (!temporal.renameTo(archivo)) {
            throw IOException("No se pudo guardar la imagen en el dispositivo")
        }
    } finally {
        if (temporal.exists()) temporal.delete()
    }
}

private fun cargarFotoPerfil(context: Context, correo: String, tamañoObjetivo: Int): Bitmap? {
    val archivo = archivoFotoPerfil(context, correo)
    if (!archivo.exists()) return null

    val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(archivo.absolutePath, opciones)
    if (opciones.outWidth <= 0 || opciones.outHeight <= 0) return null

    opciones.inSampleSize = 1
    while (
        opciones.outWidth / opciones.inSampleSize > tamañoObjetivo * 2 ||
        opciones.outHeight / opciones.inSampleSize > tamañoObjetivo * 2
    ) {
        opciones.inSampleSize *= 2
    }
    opciones.inJustDecodeBounds = false
    return BitmapFactory.decodeFile(archivo.absolutePath, opciones)
}

private fun archivoFotoPerfil(context: Context, correo: String): File {
    val correoNormalizado = correo.trim().lowercase()
    val hash = MessageDigest.getInstance("SHA-256")
        .digest(correoNormalizado.toByteArray())
        .joinToString("") { "%02x".format(it.toInt() and 0xff) }
    return File(context.filesDir, "foto_perfil_$hash")
}
