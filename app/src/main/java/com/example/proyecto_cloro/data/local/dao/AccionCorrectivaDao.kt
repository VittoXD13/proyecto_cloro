package com.example.proyecto_cloro.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.proyecto_cloro.data.local.entity.AccionCorrectivaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccionCorrectivaDao {
    @Query("SELECT * FROM acciones_correctivas")
    fun obtenerTodas(): Flow<List<AccionCorrectivaEntity>>

    @Upsert
    suspend fun guardar(accion: AccionCorrectivaEntity)

    @Query(
        """
        UPDATE acciones_correctivas
        SET cerrada = 1, fechaCierre = :fechaCierre, validador = :validador
        WHERE medicionId = :medicionId
        """
    )
    suspend fun cerrar(medicionId: Int, fechaCierre: String, validador: String)
}
