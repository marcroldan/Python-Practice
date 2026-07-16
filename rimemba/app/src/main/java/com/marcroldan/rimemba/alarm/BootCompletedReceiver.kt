package com.marcroldan.rimemba.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.marcroldan.rimemba.RimembaApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * AlarmManager pierde todas las alarmas al reiniciar el dispositivo, así que
 * este receptor relee los recordatorios pendientes de Room y los reprograma.
 * exported=true porque lo dispara el sistema (BOOT_COMPLETED), con un UID distinto.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        val container = (context.applicationContext as RimembaApplication).container

        CoroutineScope(Dispatchers.IO).launch {
            try {
                container.itemRepository.getPendingReminders().forEach { item ->
                    container.alarmScheduler.schedule(item)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
