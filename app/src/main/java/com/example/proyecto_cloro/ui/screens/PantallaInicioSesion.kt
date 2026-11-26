package com.example.proyecto_cloro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.proyecto_cloro.data.auth.AutenticacionDemo
import com.example.proyecto_cloro.data.auth.ResultadoInicioSesion
import com.example.proyecto_cloro.domain.model.SesionDemo

@Composable
fun PantallaInicioSesion(onIniciarSesion: (SesionDemo) -> Unit) {
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var errorCorreo by remember { mutableStateOf<String?>(null) }
    var errorClave by remember { mutableStateOf<String?>(null) }
    var credencialesInvalidas by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Acceso demostrativo con cuentas ficticias y permisos por rol.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )
        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
                errorCorreo = null
                credencialesInvalidas = false
            },
            label = { Text("Correo electrónico") },
            singleLine = true,
            isError = errorCorreo != null || credencialesInvalidas,
            supportingText = {
                errorCorreo?.let { Text(it) }
            },
            trailingIcon = {
                if (errorCorreo != null || credencialesInvalidas) {
                    Text("!", color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = clave,
            onValueChange = {
                clave = it
                errorClave = null
                credencialesInvalidas = false
            },
            label = { Text("Contraseña") },
            singleLine = true,
            isError = errorClave != null || credencialesInvalidas,
            supportingText = {
                errorClave?.let { Text(it) }
            },
            trailingIcon = {
                if (errorClave != null || credencialesInvalidas) {
                    Text("!", color = MaterialTheme.colorScheme.error)
                }
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )
        Text(
            "Cuentas de prueba: operario1@demo.cloro, supervisor1@demo.cloro o " +
                "jefatura1@demo.cloro. Contraseña: ${AutenticacionDemo.CLAVE_DEMO}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 16.dp)
        )
        if (credencialesInvalidas) {
            Text(
                "Correo o contraseña incorrectos.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Button(
            onClick = {
                errorCorreo = when {
                    correo.isBlank() -> "Ingresa tu correo."
                    !correo.trim().matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) ->
                        "Ingresa un correo válido."
                    else -> null
                }
                errorClave = if (clave.isBlank()) "Ingresa tu contraseña." else null
                credencialesInvalidas = false
                if (errorCorreo == null && errorClave == null) {
                    when (val resultado = AutenticacionDemo.iniciarSesion(correo, clave)) {
                        is ResultadoInicioSesion.Exitoso -> onIniciarSesion(resultado.sesion)
                        ResultadoInicioSesion.CredencialesInvalidas ->
                            credencialesInvalidas = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            Text("Ingresar")
        }
    }
}
