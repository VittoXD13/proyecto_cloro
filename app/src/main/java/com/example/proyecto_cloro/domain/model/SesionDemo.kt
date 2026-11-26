package com.example.proyecto_cloro.domain.model

data class SesionDemo(
    val correo: String,
    val rol: RolUsuario,
    val granjasAsignadas: Set<String>
)
