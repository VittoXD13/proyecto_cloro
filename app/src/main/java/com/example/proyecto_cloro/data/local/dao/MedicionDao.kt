package com.example.proyecto_cloro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.proyecto_cloro.data.local.entity.MedicionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicionDao {

    @Insert
    suspend fun insertar(medicion: MedicionEntity)

    @Query("SELECT * FROM mediciones ORDER BY id DESC")
    fun obtenerTodas(): Flow<List<MedicionEntity>>

    @Query("SELECT COUNT(*) FROM mediciones")
    suspend fun contar(): Int

    @Query("DELETE FROM mediciones")
    suspend fun eliminarTodas()

    @Query("""
    SELECT COUNT(*) FROM mediciones
    WHERE granja = :granja
    AND punto = :punto
    AND fecha = :fecha
""")
    suspend fun existeMedicion(
        granja: String,
        punto: String,
        fecha: String
    ): Int
}