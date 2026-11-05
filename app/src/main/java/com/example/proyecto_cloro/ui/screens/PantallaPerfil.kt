package com.example.proyecto_cloro.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val PREFERENCIAS_PERFIL = "perfil"
private const val CLAVE_FOTO_PERFIL = "foto_uri"

@Composable
fun PantallaPerfil(correo: String, rol: RolUsuario, onCerrarSesion: () -> Unit) {
    val context = LocalContext.current
    val preferencias = remember {
        context.getSharedPreferences(PREFERENCIAS_PERFIL, Context.MODE_PRIVATE)
    }
    var fotoUri by remember(correo) {
        mutableStateOf(preferencias.getString(CLAVE_FOTO_PERFIL, null)?.let(Uri::parse))
    }
    var foto by remember(correo) { mutableStateOf<Bitmap?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    val selectorDeFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                preferencias.edit {
                    putString(CLAVE_FOTO_PERFIL, uri.toString())
                }
                fotoUri = uri
                mensajeError = null
            } catch (_: SecurityException) {
                mensajeError = "No se pudo guardar el acceso a la foto. Intenta elegir otra."
            }
        }
    }

    LaunchedEffect(fotoUri) {
        val uri = fotoUri
        foto = null
        mensajeError = null
        if (uri != null) {
            try {
                foto = cargarFoto(context, uri)
                if (foto == null) {
                    mensajeError = "No se pudo cargar la foto seleccionada."
                }
            } catch (_: IOException) {
                mensajeError = "No se pudo abrir la foto seleccionada."
            } catch (_: SecurityException) {
                mensajeError = "La app ya no tiene acceso a esta foto. Selecciona otra."
            }
        }
    }

    ContenedorPantalla("Perfil") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val imagen = foto
                    if (imagen != null) {
                        Image(
                            bitmap = imagen.asImageBitmap(),
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
                Button(onClick = { selectorDeFoto.launch(arrayOf("image/*")) }) {
                    Text(if (fotoUri == null) "Elegir foto" else "Cambiar foto")
                }
                if (fotoUri != null) {
                    OutlinedButton(
                        onClick = {
                            preferencias.edit {
                                remove(CLAVE_FOTO_PERFIL)
                            }
                            fotoUri = null
                            foto = null
                            mensajeError = null
                        }
                    ) {
                        Text("Quitar foto")
                    }
                }
                mensajeError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
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

private suspend fun cargarFoto(context: Context, uri: Uri): Bitmap? =
    withContext(Dispatchers.IO) {
        val opcionesDeTamanio = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val flujoDeLimites = context.contentResolver.openInputStream(uri)
            ?: return@withContext null
        flujoDeLimites.use {
            BitmapFactory.decodeStream(it, null, opcionesDeTamanio)
        }
        if (opcionesDeTamanio.outWidth <= 0 || opcionesDeTamanio.outHeight <= 0) {
            return@withContext null
        }

        val tamanioObjetivo = (144 * context.resources.displayMetrics.density).toInt()
        var factorMuestreo = 1
        while (
            opcionesDeTamanio.outWidth / factorMuestreo > tamanioObjetivo ||
            opcionesDeTamanio.outHeight / factorMuestreo > tamanioObjetivo
        ) {
            factorMuestreo *= 2
        }

        val opcionesDeDecodificacion = BitmapFactory.Options().apply {
            inSampleSize = factorMuestreo
        }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opcionesDeDecodificacion)
        }
    }
