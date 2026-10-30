package com.example.proyecto_cloro.domain.estado

fun obtenerEstado(cloro: Double): String = when {
    cloro < 2.0 || cloro > 6.0 -> "CRÍTICO"
    (cloro >= 2.0 && cloro < 3.0) || (cloro > 5.0 && cloro <= 6.0) -> "ADVERTENCIA"
    else -> "NORMAL"
}
