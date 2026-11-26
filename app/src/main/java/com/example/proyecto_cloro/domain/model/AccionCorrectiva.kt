package com.example.proyecto_cloro.domain.model

data class AccionCorrectiva(
    val medicionId: Int,
    val descripcion: String,
    val responsable: String,
    val fecha: String,
    val fotoUri: String? = null,
    val cerrada: Boolean = false,
    val fechaCierre: String? = null,
    val medicionBaseId: Int = 0,
    val validador: String? = null
)
