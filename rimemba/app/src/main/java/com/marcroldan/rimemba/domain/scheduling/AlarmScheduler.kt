package com.marcroldan.rimemba.domain.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import com.marcroldan.rimemba.alarm.AlarmReceiver
import com.marcroldan.rimemba.domain.model.Item
import java.time.ZoneId

/**
 * Programa/cancela las alarmas de los recordatorios. Usa alarmas exactas
 * cuando el permiso está concedido (API 31+); si no, hace fallback a una
 * alarma inexacta en vez de lanzar SecurityException — el recordatorio llega
 * igual, aunque con posible retraso, en vez de fallar silenciosamente.
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager? = context.getSystemService(AlarmManager::class.java)

    fun schedule(item: Item) {
        val fechaHora = item.fechaHora ?: return
        val triggerMillis = fechaHora.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pendingIntent = pendingIntentPara(item.id)

        val manager = alarmManager ?: return
        val puedeExacta = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()

        if (puedeExacta) {
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                manager,
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        } else {
            AlarmManagerCompat.setAndAllowWhileIdle(
                manager,
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }

    fun cancel(itemId: Long) {
        alarmManager?.cancel(pendingIntentPara(itemId))
    }

    private fun pendingIntentPara(itemId: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            data = Uri.parse("rimemba://item/$itemId")
            putExtra(AlarmReceiver.EXTRA_ITEM_ID, itemId)
        }
        return PendingIntent.getBroadcast(
            context,
            itemId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
