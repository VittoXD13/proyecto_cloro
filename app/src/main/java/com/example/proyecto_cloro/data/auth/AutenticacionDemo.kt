package com.example.proyecto_cloro.data.auth

import com.example.proyecto_cloro.domain.model.RolUsuario
import com.example.proyecto_cloro.domain.model.SesionDemo

sealed interface ResultadoInicioSesion {
    data class Exitoso(val sesion: SesionDemo) : ResultadoInicioSesion
    data object CredencialesInvalidas : ResultadoInicioSesion
}

object AutenticacionDemo {
    const val CLAVE_DEMO = "cloro1234"

    private data class Cuenta(
        val correo: String,
        val rol: RolUsuario,
        val granjas: Set<String>
    )

    private val cuentas = buildList {
        repeat(5) { indice ->
            val granja = "Granja ${(indice % 3) + 1}"
            add(
                Cuenta(
                    correo = "operario${indice + 1}@demo.cloro",
                    rol = RolUsuario.OPERARIO,
                    granjas = setOf(granja)
                )
            )
            add(
                Cuenta(
                    correo = "supervisor${indice + 1}@demo.cloro",
                    rol = RolUsuario.SUPERVISOR,
                    granjas = setOf(granja)
                )
            )
            if (indice < 5) {
                add(
                    Cuenta(
                        correo = "jefatura${indice + 1}@demo.cloro",
                        rol = RolUsuario.JEFATURA,
                        granjas = setOf("Granja 1", "Granja 2", "Granja 3")
                    )
                )
            }
        }
    }

    fun iniciarSesion(correo: String, clave: String): ResultadoInicioSesion {
        val cuenta = cuentas.firstOrNull {
            it.correo.equals(correo.trim(), ignoreCase = true)
        }
        return if (cuenta != null && clave == CLAVE_DEMO) {
            ResultadoInicioSesion.Exitoso(
                SesionDemo(cuenta.correo, cuenta.rol, cuenta.granjas)
            )
        } else {
            ResultadoInicioSesion.CredencialesInvalidas
        }
    }
}
