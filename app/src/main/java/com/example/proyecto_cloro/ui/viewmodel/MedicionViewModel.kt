package com.example.proyecto_cloro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_cloro.data.local.BaseDatosCloro
import com.example.proyecto_cloro.data.local.entity.MedicionEntity
import com.example.proyecto_cloro.domain.estado.obtenerEstado
import com.example.proyecto_cloro.domain.model.Medicion
import com.example.proyecto_cloro.notifications.NotificacionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedicionViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = BaseDatosCloro
        .obtenerInstancia(application)
        .medicionDao()

    val mediciones = dao.obtenerTodas().map { lista ->
        lista.map { registro ->
            Medicion(
                id = registro.id,
                granja = registro.granja,
                galpon = registro.galpon,
                codigoPunto = registro.codigoPunto,
                ubicacion = registro.ubicacion,
                tipoPunto = registro.tipoPunto,
                punto = registro.punto,
                cloro = registro.cloro,
                ph = registro.ph,
                temperatura = registro.temperatura,
                turbidez = registro.turbidez,
                fecha = registro.fecha
            )
        }
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun guardar(medicion: Medicion) {
        viewModelScope.launch {
            dao.insertar(
                MedicionEntity(
                    granja = medicion.granja,
                    galpon = medicion.galpon,
                    codigoPunto = medicion.codigoPunto,
                    ubicacion = medicion.ubicacion,
                    tipoPunto = medicion.tipoPunto,
                    punto = medicion.punto,
                    cloro = medicion.cloro,
                    ph = medicion.ph,
                    temperatura = medicion.temperatura,
                    turbidez = medicion.turbidez,
                    fecha = medicion.fecha
                )
            )

            val estado = obtenerEstado(medicion.cloro)
            if (estado != "NORMAL") {
                val context = getApplication<Application>().applicationContext
                NotificacionHelper.mostrarAlertaCloro(
                    context = context,
                    granja = medicion.granja,
                    punto = medicion.punto,
                    cloro = medicion.cloro,
                    estado = estado
                )
            }
        }
    }
}