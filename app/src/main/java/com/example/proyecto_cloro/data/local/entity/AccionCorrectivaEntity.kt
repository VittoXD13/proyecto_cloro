package com.example.proyecto_cloro.data.local.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

@Entity(tableName = "acciones_correctivas")
data class AccionCorrectivaEntity(
    @PrimaryKey
    val medicionId: Int,
    val descripcion: String,
    @ColumnInfo(defaultValue = "''")
    val responsable: String = "",
    val fecha: String,
    val fotoUri: String? = null,
    val cerrada: Boolean = false,
    val fechaCierre: String? = null,
    @ColumnInfo(defaultValue = "0")
    val medicionBaseId: Int = 0,
    val validador: String? = null
)
