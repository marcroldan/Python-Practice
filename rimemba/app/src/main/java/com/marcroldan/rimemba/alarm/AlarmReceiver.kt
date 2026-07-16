package com.marcroldan.rimemba.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.marcroldan.rimemba.RimembaApplication
import com.marcroldan.rimemba.domain.scheduling.NextOccurrenceCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Recibe el disparo de una alarma programada por [com.marcroldan.rimemba.domain.scheduling.AlarmScheduler].
 * Solo lo puede disparar nuestro propio PendingIntent (exported=false en el manifest).
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val itemId = intent.getLongExtra(EXTRA_ITEM_ID, -1L)
        if (itemId == -1L) return

        val pendingResult = goAsync()
        val container = (context.applicationContext as RimembaApplication).container

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val item = container.itemRepository.getById(itemId) ?: return@launch
                if (item.completado) return@launch

                container.notificationHelper.showReminderNotification(item)

                val fechaHora = item.fechaHora ?: return@launch
                val siguiente = NextOccurrenceCalculator.next(fechaHora, item.recurrencia)
                if (siguiente != null) {
                    // save() dispara ItemChangeEffects.onSaved, que reprograma la
                    // alarma para la siguiente ocurrencia — no hace falta llamar
                    // a AlarmScheduler directamente aquí.
                    container.itemRepository.save(item.copy(fechaHora = siguiente))
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_ITEM_ID = "extra_item_id"
    }
}
