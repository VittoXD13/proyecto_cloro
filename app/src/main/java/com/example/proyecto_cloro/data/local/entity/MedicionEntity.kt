package com.example.proyecto_cloro.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mediciones")
data class MedicionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val granja: String,
    val galpon: String = "",
    val codigoPunto: String = "",
    val ubicacion: String = "",
    val tipoPunto: String = "",
    val punto: String,
    val cloro: Double,
    // Parámetros opcionales adicionales
    val ph: Double? = null,
    val temperatura: Double? = null,
    val turbidez: Double? = null,
    val fecha: String
)