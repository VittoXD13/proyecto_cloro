package com.example.proyecto_cloro.data.remote

import com.example.proyecto_cloro.data.model.MedicionRespuesta
import com.example.proyecto_cloro.domain.model.PuntoControl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

class ApiMedicionesSimulada {
    private val idLocal = AtomicInteger(1)
    private val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun consultarMediciones(puntos: List<PuntoControl>): List<MedicionRespuesta> {
        val fecha = formatoFecha.format(Date())
        return puntos.map { punto ->
            val cloro = if (Random.nextInt(100) < 10) {
                Random.nextDouble(1.0, 7.0)
            } else {
                Random.nextDouble(3.0, 5.01)
            }
            MedicionRespuesta(
                id = idLocal.getAndIncrement(),
                granja = punto.granja,
                punto = punto.codigo,
                cloro = cloro,
                fecha = fecha
            )
        }
    }
}
