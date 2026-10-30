package com.example.proyecto_cloro.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificacionHelper {
    private const val CHANNEL_ID = "canal_alertas_cloro"
    private const val CHANNEL_NAME = "Alertas de Cloro"

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Canal para alertas de niveles de cloro fuera de rango"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun mostrarAlertaCloro(context: Context, granja: String, punto: String, cloro: Double, estado: String) {
        // Validación de permisos para Android 13+ (API 33 en adelante)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        crearCanal(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("¡Alerta de Cloro: $estado!")
            .setContentText("Granja: $granja ($punto) - Nivel: $cloro mg/L")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("El punto de medición '$punto' en '$granja' registró un nivel de cloro de $cloro mg/L, clasificado como $estado."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}