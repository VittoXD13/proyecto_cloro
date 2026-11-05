package com.example.proyecto_cloro.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RolUsuarioTest {
    @Test
    fun permisosDeCadaRolCorrespondenAlMvp() {
        assertEquals("mediciones", RolUsuario.OPERARIO.rutaInicial)
        assertTrue(RolUsuario.OPERARIO.puedeRegistrarMediciones)
        assertFalse(RolUsuario.OPERARIO.puedeRevisarAlertas)
        assertFalse(RolUsuario.OPERARIO.puedeConsultarIndicadores)

        assertEquals("historial", RolUsuario.SUPERVISOR.rutaInicial)
        assertFalse(RolUsuario.SUPERVISOR.puedeRegistrarMediciones)
        assertTrue(RolUsuario.SUPERVISOR.puedeRevisarAlertas)
        assertFalse(RolUsuario.SUPERVISOR.puedeConsultarIndicadores)

        assertEquals("inicio", RolUsuario.JEFATURA.rutaInicial)
        assertFalse(RolUsuario.JEFATURA.puedeRegistrarMediciones)
        assertFalse(RolUsuario.JEFATURA.puedeRevisarAlertas)
        assertTrue(RolUsuario.JEFATURA.puedeConsultarIndicadores)
    }
}
