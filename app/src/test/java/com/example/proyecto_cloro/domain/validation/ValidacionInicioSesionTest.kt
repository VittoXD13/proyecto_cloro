package com.example.proyecto_cloro.domain.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionInicioSesionTest {
    @Test
    fun marcaCadaCampoRequeridoPorSeparado() {
        val resultado = validarInicioSesion("", "")

        assertFalse(resultado.esValido)
        assertTrue(resultado.errorCorreo != null)
        assertTrue(resultado.errorClave != null)
    }

    @Test
    fun aceptaCorreoBienFormadoYClaveNoVacia() {
        val resultado = validarInicioSesion("operario1@demo.cloro", "cloro1234")

        assertTrue(resultado.esValido)
        assertNull(resultado.errorCorreo)
        assertNull(resultado.errorClave)
    }
}
