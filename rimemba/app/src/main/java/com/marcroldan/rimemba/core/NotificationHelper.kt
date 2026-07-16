package com.marcroldan.rimemba.core

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import com.marcroldan.rimemba.R
import com.marcroldan.rimemba.alarm.NotificationActionReceiver
import com.marcroldan.rimemba.domain.model.Item

/**
 * Gestiona el canal de notificaciones y la notificación de recordatorio con
 * las acciones "Completar" y "Posponer" (esta última con [RemoteInput] para
 * que el usuario pueda dictar o elegir una sugerencia rápida).
 */
class NotificationHelper(private val context: Context) {

    init {
        crearCanal()
    }

    private fun crearCanal() {
        val canal = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_REMINDERS,
            context.getString(R.string.notification_channel_reminders_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_reminders_description)
        }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(canal)
    }

    fun showReminderNotification(item: Item) {
        val tienePermiso = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!tienePermiso) return

        val completeAction = NotificationCompat.Action.Builder(
            0,
            context.getString(R.string.notification_action_complete),
            completePendingIntent(item.id)
        ).build()

        val remoteInput = RemoteInput.Builder(NotificationActionReceiver.KEY_SNOOZE_REPLY)
            .setLabel(context.getString(R.string.notification_snooze_hint))
            .setChoices(
                arrayOf(
                    context.getString(R.string.notification_snooze_option_one_hour),
                    context.getString(R.string.notification_snooze_option_this_afternoon),
                    context.getString(R.string.notification_snooze_option_tomorrow)
                )
            )
            .build()

        val snoozeAction = NotificationCompat.Action.Builder(
            0,
            context.getString(R.string.notification_action_snooze),
            snoozePendingIntent(item.id)
        ).addRemoteInput(remoteInput).build()

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification_mic)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(item.texto)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .addAction(completeAction)
            .addAction(snoozeAction)
            .build()

        NotificationManagerCompat.from(context).notify(item.id.toInt(), notification)
    }

    fun updateAfterSnooze(itemId: Long, mensajeConfirmacion: String) {
        val tienePermiso = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!tienePermiso) return

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification_mic)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(mensajeConfirmacion)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(itemId.toInt(), notification)
    }

    fun cancel(itemId: Long) {
        NotificationManagerCompat.from(context).cancel(itemId.toInt())
    }

    private fun completePendingIntent(itemId: Long): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_COMPLETE
            data = Uri.parse("rimemba://item/$itemId/complete")
            putExtra(NotificationActionReceiver.EXTRA_ITEM_ID, itemId)
        }
        return PendingIntent.getBroadcast(
            context,
            itemId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun snoozePendingIntent(itemId: Long): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            data = Uri.parse("rimemba://item/$itemId/snooze")
            putExtra(NotificationActionReceiver.EXTRA_ITEM_ID, itemId)
        }
        // FLAG_MUTABLE es obligatorio aquí: RemoteInput necesita añadir los
        // resultados dictados al Intent antes de que se entregue al receiver.
        return PendingIntent.getBroadcast(
            context,
            itemId.toInt() + SNOOZE_REQUEST_CODE_OFFSET,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    private companion object {
        // Offset para que el requestCode del PendingIntent de posponer nunca
        // colisione con el de completar para el mismo item.id.
        const val SNOOZE_REQUEST_CODE_OFFSET = 1_000_000
    }
}
