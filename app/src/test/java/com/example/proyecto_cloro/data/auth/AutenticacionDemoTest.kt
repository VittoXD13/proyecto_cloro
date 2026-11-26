package com.example.proyecto_cloro.data.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.example.proyecto_cloro.domain.model.RolUsuario

class AutenticacionDemoTest {
    @Test
    fun autenticaCuentaDePruebaYAsignaRolSinSeleccionarloEnPantalla() {
        val resultado = AutenticacionDemo.iniciarSesion(
            "supervisor1@demo.cloro",
            AutenticacionDemo.CLAVE_DEMO
        )

        assertTrue(resultado is ResultadoInicioSesion.Exitoso)
        assertEquals(
            RolUsuario.SUPERVISOR,
            (resultado as ResultadoInicioSesion.Exitoso).sesion.rol
        )
    }

    @Test
    fun rechazaCredencialesIncorrectas() {
        assertEquals(
            ResultadoInicioSesion.CredencialesInvalidas,
            AutenticacionDemo.iniciarSesion("operario1@demo.cloro", "incorrecta")
        )
    }
}
