package com.example.proyecto_cloro.domain.model

data class PuntoControl(
    val id: Int = 0,
    val granja: String,
    val galpon: String,
    val codigo: String,
    val ubicacion: String,
    val tipo: String,
    val nombre: String
)
