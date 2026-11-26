package com.example.proyecto_cloro.domain.validation

data class ResultadoValidacionInicioSesion(
    val errorCorreo: String? = null,
    val errorClave: String? = null
) {
    val esValido: Boolean
        get() = errorCorreo == null && errorClave == null
}

fun validarInicioSesion(correo: String, clave: String): ResultadoValidacionInicioSesion {
    val errorCorreo = when {
        correo.isBlank() -> "Ingresa tu correo."
        !correo.trim().matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) ->
            "Ingresa un correo válido."
        else -> null
    }
    val errorClave = if (clave.isBlank()) "Ingresa tu contraseña." else null
    return ResultadoValidacionInicioSesion(errorCorreo, errorClave)
}
