package com.example.proyecto_cloro.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "puntos_control",
    indices = [Index(value = ["codigo"], unique = true)]
)
data class PuntoControlEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val granja: String,
    val galpon: String,
    val codigo: String,
    val ubicacion: String,
    val tipo: String,
    val nombre: String
)
