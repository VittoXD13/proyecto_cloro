package com.example.proyecto_cloro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.proyecto_cloro.data.local.dao.MedicionDao
import com.example.proyecto_cloro.data.local.entity.MedicionEntity

@Database(
    entities = [MedicionEntity::class],
    version = 2, // Incrementar versión por cambios en la tabla
    exportSchema = false
)
abstract class BaseDatosCloro : RoomDatabase() {

    abstract fun medicionDao(): MedicionDao

    companion object {
        @Volatile
        private var INSTANCIA: BaseDatosCloro? = null

        fun obtenerInstancia(context: Context): BaseDatosCloro {
            return INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,
                    BaseDatosCloro::class.java,
                    "base_datos_cloro"
                )
                    .fallbackToDestructiveMigration() // Recrea la BD si cambia la versión
                    .build().also {
                        INSTANCIA = it
                    }
            }
        }
    }
}