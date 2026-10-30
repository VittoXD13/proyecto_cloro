package com.example.proyecto_cloro.data.model

data class MedicionRespuesta(
    val id: Int,
    val granja: String,
    val punto: String,
    val cloro: Double,
    val fecha: String
)