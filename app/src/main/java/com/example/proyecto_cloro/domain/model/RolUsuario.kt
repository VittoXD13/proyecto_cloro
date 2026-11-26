package com.example.proyecto_cloro.domain.model

enum class RolUsuario(
    val etiqueta: String,
    val rutaInicial: String,
    val puedeRegistrarMediciones: Boolean,
    val puedeRevisarAlertas: Boolean,
    val puedeConsultarIndicadores: Boolean,
    val puedeAdministrarCatalogo: Boolean
) {
    OPERARIO("Operario", "mediciones", true, false, false, false),
    SUPERVISOR("Supervisor", "alertas", false, true, false, true),
    JEFATURA("Jefatura", "inicio", false, false, true, false)
}
