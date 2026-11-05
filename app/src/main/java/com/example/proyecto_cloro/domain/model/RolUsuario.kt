package com.example.proyecto_cloro.domain.model

enum class RolUsuario(
    val etiqueta: String,
    val rutaInicial: String,
    val puedeRegistrarMediciones: Boolean,
    val puedeRevisarAlertas: Boolean,
    val puedeConsultarIndicadores: Boolean
) {
    OPERARIO("Operario", "mediciones", true, false, false),
    SUPERVISOR("Supervisor", "historial", false, true, false),
    JEFATURA("Jefatura", "inicio", false, false, true)
}
