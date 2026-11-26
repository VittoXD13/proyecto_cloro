package com.example.proyecto_cloro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_cloro.data.local.BaseDatosCloro
import com.example.proyecto_cloro.data.remote.ApiMedicionesSimulada
import com.example.proyecto_cloro.data.repository.MedicionRepository
import com.example.proyecto_cloro.domain.estado.obtenerEstado
import com.example.proyecto_cloro.domain.model.AccionCorrectiva
import com.example.proyecto_cloro.domain.model.Medicion
import com.example.proyecto_cloro.domain.model.PuntoControl
import com.example.proyecto_cloro.domain.model.RolUsuario
import com.example.proyecto_cloro.domain.model.SesionDemo
import com.example.proyecto_cloro.notifications.NotificacionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MedicionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MedicionRepository(
        BaseDatosCloro.obtenerInstancia(application)
    )
    private val apiSimulada = ApiMedicionesSimulada()
    private var sesionActual: SesionDemo? = null

    val mediciones = repository.mediciones.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val puntos = repository.puntos.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val acciones = repository.acciones.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.inicializarCatalogo()
            while (isActive) {
                val puntosDisponibles = repository.puntos.first()
                apiSimulada.consultarMediciones(puntosDisponibles).forEach { respuesta ->
                    repository.guardarRespuestaSimulada(respuesta)
                    if (
                        obtenerEstado(respuesta.cloro) == "CRÍTICO" &&
                        respuesta.granja in sesionActual?.granjasAsignadas.orEmpty()
                    ) {
                        NotificacionHelper.mostrarAlertaCloro(
                            context = getApplication<Application>().applicationContext,
                            granja = respuesta.granja,
                            punto = respuesta.punto,
                            cloro = respuesta.cloro,
                            estado = "CRÍTICO"
                        )
                    }
                }
                delay(5 * 60 * 1000L)
            }
        }
    }

    fun iniciarSesion(sesion: SesionDemo) {
        sesionActual = sesion
    }

    fun cerrarSesion() {
        sesionActual = null
    }

    fun guardar(medicion: Medicion, alCompletar: () -> Unit = {}) {
        val sesion = sesionActual
        check(
            sesion?.rol == RolUsuario.OPERARIO &&
                medicion.granja in sesion.granjasAsignadas
        ) { "La sesión no tiene permiso para registrar mediciones en esta granja." }
        viewModelScope.launch {
            repository.guardarMedicion(medicion)

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
            alCompletar()
        }
    }

    fun agregarPunto(punto: PuntoControl, alCompletar: (Boolean) -> Unit) {
        val sesion = sesionActual
        check(
            sesion?.rol == RolUsuario.SUPERVISOR &&
                punto.granja in sesion.granjasAsignadas
        ) { "La sesión no tiene permiso para administrar este punto." }
        viewModelScope.launch {
            alCompletar(repository.agregarPunto(punto))
        }
    }

    fun guardarAccion(accion: AccionCorrectiva, alCompletar: () -> Unit = {}) {
        val sesion = sesionActual
        val medicion = mediciones.value.firstOrNull { it.id == accion.medicionId }
        check(
            sesion?.rol in setOf(RolUsuario.OPERARIO, RolUsuario.SUPERVISOR) &&
                medicion?.granja in sesion?.granjasAsignadas.orEmpty()
        ) { "La sesión no tiene permiso para registrar esta acción correctiva." }
        viewModelScope.launch {
            repository.guardarAccion(accion)
            alCompletar()
        }
    }

    fun cerrarAccion(medicionId: Int, fechaCierre: String, validador: String) {
        val sesion = checkNotNull(sesionActual) { "No hay una sesión activa." }
        val accion = checkNotNull(acciones.value.firstOrNull { it.medicionId == medicionId }) {
            "La alerta no tiene una acción correctiva registrada."
        }
        val medicion = checkNotNull(mediciones.value.firstOrNull { it.id == medicionId }) {
            "La medición de la alerta ya no está disponible."
        }
        check(
            sesion.rol == RolUsuario.SUPERVISOR &&
                medicion.granja in sesion.granjasAsignadas &&
                !accion.cerrada
        ) { "La sesión no puede validar esta alerta." }
        val lecturaMasReciente = mediciones.value
            .filter {
                it.id > accion.medicionBaseId &&
                    it.granja == medicion.granja &&
                    it.codigoPunto == medicion.codigoPunto
            }
            .maxByOrNull { it.id }
        check(lecturaMasReciente?.let { obtenerEstado(it.cloro) == "NORMAL" } == true) {
            "Se necesita una medición normal posterior a la acción correctiva para cerrar la alerta."
        }
        viewModelScope.launch {
            repository.cerrarAccion(medicionId, fechaCierre, validador)
        }
    }
}