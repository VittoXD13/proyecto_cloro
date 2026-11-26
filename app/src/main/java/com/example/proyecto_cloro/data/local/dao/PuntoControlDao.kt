package com.example.proyecto_cloro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.proyecto_cloro.data.local.entity.PuntoControlEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PuntoControlDao {
    @Query("SELECT * FROM puntos_control ORDER BY granja, galpon, codigo")
    fun obtenerTodos(): Flow<List<PuntoControlEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(punto: PuntoControlEntity): Long
}
