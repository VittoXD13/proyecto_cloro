package com.example.proyecto_cloro.domain.model

data class Medicion(
    val id: Int = 0,
    val granja: String,
    val galpon: String = "",
    val codigoPunto: String = "",
    val ubicacion: String = "",
    val tipoPunto: String = "",
    val punto: String,
    val cloro: Double,
    val ph: Double? = null,
    val temperatura: Double? = null,
    val turbidez: Double? = null,
    val fecha: String
)
