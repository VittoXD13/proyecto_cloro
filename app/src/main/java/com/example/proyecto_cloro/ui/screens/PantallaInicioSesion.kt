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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.proyecto_cloro.domain.model.RolUsuario

private val rolesDisponibles = listOf(
    RolUsuario.OPERARIO,
    RolUsuario.SUPERVISOR,
    RolUsuario.JEFATURA
)

@Composable
fun PantallaInicioSesion(onIniciarSesion: (String, RolUsuario) -> Unit) {
    var correo by remember { mutableStateOf("") }
    var contraseña by remember { mutableStateOf("") }
    var mostrarError by remember { mutableStateOf(false) }
    var rolSeleccionado by remember { mutableStateOf(RolUsuario.OPERARIO) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Iniciar sesión",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            "Ingresa tus datos y elige un rol de prueba para continuar.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
                mostrarError = false
            },
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = contraseña,
            onValueChange = {
                contraseña = it
                mostrarError = false
            },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )
        Text(
            "Rol principal (modo de prueba)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 20.dp, bottom = 4.dp)
        )
        rolesDisponibles.forEach { rol ->
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = rolSeleccionado == rol,
                        role = Role.RadioButton,
                        onClick = { rolSeleccionado = rol }
                    )
                    .padding(vertical = 2.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = rolSeleccionado == rol,
                    onClick = { rolSeleccionado = rol }
                )
                Text(rol.etiqueta)
            }
        }
        if (mostrarError) {
            Text(
                "Ingresa tu correo y contraseña.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Button(
            onClick = {
                if (correo.isNotBlank() && contraseña.isNotBlank()) {
                    onIniciarSesion(correo.trim(), rolSeleccionado)
                } else {
                    mostrarError = true
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
