package com.example.proyecto_cloro.domain.validation

enum class CampoMedicion {
    CLORO,
    PH,
    TEMPERATURA,
    TURBIDEZ
}

data class ResultadoValidacionMedicion(
    val cloro: Double?,
    val ph: Double?,
    val temperatura: Double?,
    val turbidez: Double?,
    val errores: Map<CampoMedicion, String>
) {
    val esValido: Boolean
        get() = errores.isEmpty()
}

fun validarMedicion(
    cloroTexto: String,
    phTexto: String,
    temperaturaTexto: String,
    turbidezTexto: String
): ResultadoValidacionMedicion {
    val errores = mutableMapOf<CampoMedicion, String>()

    fun numero(texto: String, campo: CampoMedicion, obligatorio: Boolean): Double? {
        if (texto.isBlank()) {
            if (obligatorio) errores[campo] = "Este campo es obligatorio."
            return null
        }
        val valor = texto.trim().replace(',', '.').toDoubleOrNull()
        if (valor == null || !valor.isFinite()) {
            errores[campo] = "Ingresa un número válido."
            return null
        }
        return valor
    }

    val cloro = numero(cloroTexto, CampoMedicion.CLORO, obligatorio = true)
    if (cloro != null && cloro !in 0.0..10.0) {
        errores[CampoMedicion.CLORO] = "El cloro debe estar entre 0 y 10 ppm."
    }

    val ph = numero(phTexto, CampoMedicion.PH, obligatorio = false)
    if (ph != null && ph !in 0.0..14.0) {
        errores[CampoMedicion.PH] = "El pH debe estar entre 0 y 14."
    }

    val temperatura = numero(temperaturaTexto, CampoMedicion.TEMPERATURA, obligatorio = false)
    if (temperatura != null && temperatura !in -50.0..100.0) {
        errores[CampoMedicion.TEMPERATURA] =
            "La temperatura debe estar entre -50 y 100 °C."
    }

    val turbidez = numero(turbidezTexto, CampoMedicion.TURBIDEZ, obligatorio = false)
    if (turbidez != null && turbidez < 0.0) {
        errores[CampoMedicion.TURBIDEZ] = "La turbidez no puede ser negativa."
    }

    return ResultadoValidacionMedicion(cloro, ph, temperatura, turbidez, errores)
}
