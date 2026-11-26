package com.example.proyecto_cloro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.proyecto_cloro.data.local.dao.AccionCorrectivaDao
import com.example.proyecto_cloro.data.local.dao.MedicionDao
import com.example.proyecto_cloro.data.local.dao.PuntoControlDao
import com.example.proyecto_cloro.data.local.entity.AccionCorrectivaEntity
import com.example.proyecto_cloro.data.local.entity.MedicionEntity
import com.example.proyecto_cloro.data.local.entity.PuntoControlEntity

@Database(
    entities = [
        MedicionEntity::class,
        PuntoControlEntity::class,
        AccionCorrectivaEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class BaseDatosCloro : RoomDatabase() {

    abstract fun medicionDao(): MedicionDao
    abstract fun puntoControlDao(): PuntoControlDao
    abstract fun accionCorrectivaDao(): AccionCorrectivaDao

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
                    .addMigrations(
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7
                    )
                    .build().also {
                        INSTANCIA = it
                    }
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS puntos_control (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        granja TEXT NOT NULL,
                        galpon TEXT NOT NULL,
                        codigo TEXT NOT NULL,
                        ubicacion TEXT NOT NULL,
                        tipo TEXT NOT NULL,
                        nombre TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_puntos_control_codigo ON puntos_control(codigo)"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS acciones_correctivas (
                        medicionId INTEGER NOT NULL PRIMARY KEY,
                        descripcion TEXT NOT NULL,
                        fecha TEXT NOT NULL,
                        fotoUri TEXT,
                        cerrada INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE acciones_correctivas ADD COLUMN responsable TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "ALTER TABLE acciones_correctivas ADD COLUMN fechaCierre TEXT"
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE acciones_correctivas ADD COLUMN medicionBaseId INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE acciones_correctivas ADD COLUMN validador TEXT"
                )
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE mediciones ADD COLUMN sincronizacionPendiente INTEGER NOT NULL DEFAULT 1"
                )
            }
        }
    }
}
