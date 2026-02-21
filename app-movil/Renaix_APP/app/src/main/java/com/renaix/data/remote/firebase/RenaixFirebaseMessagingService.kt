package com.renaix.data.remote.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.renaix.MainActivity
import com.renaix.R

/**
 * Servicio de Firebase Cloud Messaging para recibir notificaciones push
 *
 * Maneja:
 * - Recepción de notificaciones en foreground y background
 * - Actualización del token FCM
 * - Creación de notificaciones locales
 */
class RenaixFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "RenaixFCM"
        private const val CHANNEL_ID = "renaix_notifications"
        private const val CHANNEL_NAME = "Notificaciones de Renaix"
        private const val CHANNEL_DESCRIPTION = "Mensajes, ofertas y actualizaciones"
    }

    /**
     * Llamado cuando se recibe un nuevo token FCM
     * Este token debe enviarse al backend para poder recibir notificaciones
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM: $token")

        // TODO: Enviar token al backend cuando el usuario esté autenticado
        // Esto permite al servidor enviar notificaciones a este dispositivo
        sendTokenToServer(token)
    }

    /**
     * Llamado cuando se recibe un mensaje (notificación)
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Mensaje recibido de: ${remoteMessage.from}")

        // Verificar si el mensaje contiene datos
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Datos del mensaje: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Verificar si el mensaje contiene notificación
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Notificación: ${notification.title} - ${notification.body}")
            showNotification(
                title = notification.title ?: "Renaix",
                body = notification.body ?: "",
                data = remoteMessage.data
            )
        }
    }

    /**
     * Procesa los datos del mensaje para determinar la acción
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]
        val title = data["title"] ?: "Renaix"
        val body = data["body"] ?: ""

        when (type) {
            "new_message" -> {
                // Nuevo mensaje en el chat
                showNotification(
                    title = title,
                    body = body,
                    data = data
                )
            }
            "new_offer" -> {
                // Nueva oferta recibida
                showNotification(
                    title = "💰 $title",
                    body = body,
                    data = data
                )
            }
            "offer_accepted" -> {
                // Oferta aceptada
                showNotification(
                    title = "✅ $title",
                    body = body,
                    data = data
                )
            }
            "purchase_update" -> {
                // Actualización de compra
                showNotification(
                    title = "📦 $title",
                    body = body,
                    data = data
                )
            }
            else -> {
                // Notificación genérica
                if (title.isNotEmpty() || body.isNotEmpty()) {
                    showNotification(title, body, data)
                }
            }
        }
    }

    /**
     * Muestra una notificación local
     */
    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal de notificación (requerido para Android 8.0+)
        createNotificationChannel(notificationManager)

        // Intent para abrir la app al tocar la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Pasar datos para deep linking
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Construir notificación
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_renaix_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()

        // Mostrar notificación con ID único
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    /**
     * Crea el canal de notificación (requerido para Android 8.0+)
     */
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Envía el token FCM al backend
     * TODO: Implementar cuando el backend soporte guardar tokens
     */
    private fun sendTokenToServer(token: String) {
        // Aquí se enviaría el token al backend de Odoo
        // para que pueda enviar notificaciones a este dispositivo
        Log.d(TAG, "Token para enviar al servidor: $token")

        // Ejemplo de implementación:
        // scope.launch {
        //     try {
        //         api.registerFcmToken(token)
        //     } catch (e: Exception) {
        //         Log.e(TAG, "Error enviando token", e)
        //     }
        // }
    }
}
