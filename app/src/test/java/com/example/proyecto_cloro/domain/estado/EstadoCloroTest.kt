package com.example.proyecto_cloro.domain.estado

import org.junit.Assert.assertEquals
import org.junit.Test

class EstadoCloroTest {
    @Test
    fun clasificaLimitesDeRango() {
        assertEquals("CRÍTICO", obtenerEstado(1.99))
        assertEquals("ADVERTENCIA", obtenerEstado(2.0))
        assertEquals("NORMAL", obtenerEstado(3.0))
        assertEquals("NORMAL", obtenerEstado(5.0))
        assertEquals("ADVERTENCIA", obtenerEstado(6.0))
        assertEquals("CRÍTICO", obtenerEstado(6.01))
    }
}
