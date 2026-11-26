package com.example.proyecto_cloro.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionMedicionTest {
    @Test
    fun aceptaComaDecimalYParametrosOpcionalesVacios() {
        val resultado = validarMedicion("3,50", "", "", "")

        assertTrue(resultado.esValido)
        assertEquals(3.5, resultado.cloro!!, 0.0)
    }

    @Test
    fun informaErroresPorCampoIncluyendoValoresFueraDeRango() {
        val resultado = validarMedicion("11", "15", "", "-1")

        assertFalse(resultado.esValido)
        assertTrue(resultado.errores.containsKey(CampoMedicion.CLORO))
        assertTrue(resultado.errores.containsKey(CampoMedicion.PH))
        assertTrue(resultado.errores.containsKey(CampoMedicion.TURBIDEZ))
    }
}
