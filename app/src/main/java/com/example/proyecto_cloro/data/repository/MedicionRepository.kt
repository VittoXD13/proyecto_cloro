package com.example.proyecto_cloro.data.repository

import com.example.proyecto_cloro.data.local.BaseDatosCloro
import com.example.proyecto_cloro.data.local.entity.AccionCorrectivaEntity
import com.example.proyecto_cloro.data.local.entity.MedicionEntity
import com.example.proyecto_cloro.data.local.entity.PuntoControlEntity
import com.example.proyecto_cloro.data.model.MedicionRespuesta
import com.example.proyecto_cloro.domain.model.AccionCorrectiva
import com.example.proyecto_cloro.domain.model.Medicion
import com.example.proyecto_cloro.domain.model.PuntoControl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class MedicionRepository(database: BaseDatosCloro) {
    private val medicionesDao = database.medicionDao()
    private val puntosDao = database.puntoControlDao()
    private val accionesDao = database.accionCorrectivaDao()

    val mediciones: Flow<List<Medicion>> = medicionesDao.obtenerTodas().map { registros ->
        registros.map { it.toDomain() }
    }

    val puntos: Flow<List<PuntoControl>> = puntosDao.obtenerTodos().map { registros ->
        registros.map { it.toDomain() }
    }

    val acciones: Flow<List<AccionCorrectiva>> = accionesDao.obtenerTodas().map { registros ->
        registros.map { it.toDomain() }
    }

    suspend fun inicializarCatalogo() {
        (1..3).forEach { numeroGranja ->
            (1..4).forEach { numeroPunto ->
                puntosDao.insertar(
                    PuntoControlEntity(
                        granja = "Granja $numeroGranja",
                        galpon = "Galpón ${'A' + (numeroPunto - 1) / 2}",
                        codigo = "G$numeroGranja-P${numeroPunto.toString().padStart(2, '0')}",
                        ubicacion = "Bebedero Línea $numeroPunto",
                        tipo = "Bebedero",
                        nombre = "Línea ${if (numeroPunto % 2 == 0) "Final" else "Inicial"}"
                    )
                )
            }
        }
    }

    suspend fun guardarMedicion(medicion: Medicion): Int =
        medicionesDao.insertar(medicion.toEntity()).toInt()

    suspend fun guardarRespuestaSimulada(respuesta: MedicionRespuesta) {
        val puntoControl = puntosDao.obtenerTodos().first().firstOrNull {
            it.codigo == respuesta.punto && it.granja == respuesta.granja
        }?.toDomain() ?: throw IllegalStateException(
            "La respuesta simulada referencia un punto que no existe: ${respuesta.punto}"
        )
        guardarMedicion(
            Medicion(
                granja = puntoControl.granja,
                galpon = puntoControl.galpon,
                codigoPunto = puntoControl.codigo,
                ubicacion = puntoControl.ubicacion,
                tipoPunto = puntoControl.tipo,
                punto = puntoControl.nombre,
                cloro = respuesta.cloro,
                fecha = respuesta.fecha,
                sincronizacionPendiente = false
            )
        )
    }

    suspend fun agregarPunto(punto: PuntoControl): Boolean =
        puntosDao.insertar(punto.toEntity()) != -1L

    suspend fun guardarAccion(accion: AccionCorrectiva) {
        accionesDao.guardar(accion.toEntity())
    }

    suspend fun cerrarAccion(medicionId: Int, fechaCierre: String, validador: String) {
        accionesDao.cerrar(medicionId, fechaCierre, validador)
    }
}

private fun MedicionEntity.toDomain() = Medicion(
    id = id,
    granja = granja,
    galpon = galpon,
    codigoPunto = codigoPunto,
    ubicacion = ubicacion,
    tipoPunto = tipoPunto,
    punto = punto,
    cloro = cloro,
    ph = ph,
    temperatura = temperatura,
    turbidez = turbidez,
    fecha = fecha,
    sincronizacionPendiente = sincronizacionPendiente
)

private fun Medicion.toEntity() = MedicionEntity(
    granja = granja,
    galpon = galpon,
    codigoPunto = codigoPunto,
    ubicacion = ubicacion,
    tipoPunto = tipoPunto,
    punto = punto,
    cloro = cloro,
    ph = ph,
    temperatura = temperatura,
    turbidez = turbidez,
    fecha = fecha,
    sincronizacionPendiente = sincronizacionPendiente
)

private fun PuntoControlEntity.toDomain() = PuntoControl(
    id = id,
    granja = granja,
    galpon = galpon,
    codigo = codigo,
    ubicacion = ubicacion,
    tipo = tipo,
    nombre = nombre
)

private fun PuntoControl.toEntity() = PuntoControlEntity(
    id = id,
    granja = granja,
    galpon = galpon,
    codigo = codigo,
    ubicacion = ubicacion,
    tipo = tipo,
    nombre = nombre
)

private fun AccionCorrectivaEntity.toDomain() = AccionCorrectiva(
    medicionId = medicionId,
    descripcion = descripcion,
    responsable = responsable,
    fecha = fecha,
    fotoUri = fotoUri,
    cerrada = cerrada,
    fechaCierre = fechaCierre,
    medicionBaseId = medicionBaseId,
    validador = validador
)

private fun AccionCorrectiva.toEntity() = AccionCorrectivaEntity(
    medicionId = medicionId,
    descripcion = descripcion,
    responsable = responsable,
    fecha = fecha,
    fotoUri = fotoUri,
    cerrada = cerrada,
    fechaCierre = fechaCierre,
    medicionBaseId = medicionBaseId,
    validador = validador
)
