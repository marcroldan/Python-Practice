package com.marcroldan.rimemba.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.marcroldan.rimemba.RimembaApplication
import com.marcroldan.rimemba.domain.parser.SnoozeReplyInterpreter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Maneja las acciones "Completar" y "Posponer" de la notificación de recordatorio.
 *
 * "Posponer" usa [RemoteInput]: en un teléfono normal, el usuario dicta con el
 * micrófono del teclado del sistema sobre el campo de respuesta (no es una UI de
 * voz propia dentro de la notificación, eso es específico de Wear OS) — el
 * resultado ("dice 'una hora' y se reprograma") se cumple igual. Reutiliza
 * [SnoozeReplyInterpreter] (el mismo parser que la captura por voz normal).
 */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val itemId = intent.getLongExtra(EXTRA_ITEM_ID, -1L)
        if (itemId == -1L) return

        val pendingResult = goAsync()
        val container = (context.applicationContext as RimembaApplication).container

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_COMPLETE -> {
                        container.itemRepository.setCompletado(itemId, true)
                    }

                    ACTION_SNOOZE -> {
                        val textoDictado = RemoteInput.getResultsFromIntent(intent)
                            ?.getCharSequence(KEY_SNOOZE_REPLY)
                            ?.toString()
                        val item = container.itemRepository.getById(itemId)

                        if (item != null && !textoDictado.isNullOrBlank()) {
                            val nuevaFecha = SnoozeReplyInterpreter.interpret(textoDictado, container.timeProvider.now())
                            if (nuevaFecha != null) {
                                container.itemRepository.save(item.copy(fechaHora = nuevaFecha))
                                val hora = "%02d:%02d".format(nuevaFecha.hour, nuevaFecha.minute)
                                container.notificationHelper.updateAfterSnooze(itemId, "Pospuesto hasta las $hora")
                            } else {
                                container.notificationHelper.updateAfterSnooze(
                                    itemId,
                                    "No entendí cuándo posponerlo, sigue el recordatorio original"
                                )
                            }
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_COMPLETE = "com.marcroldan.rimemba.action.COMPLETE"
        const val ACTION_SNOOZE = "com.marcroldan.rimemba.action.SNOOZE"
        const val EXTRA_ITEM_ID = "extra_item_id"
        const val KEY_SNOOZE_REPLY = "key_snooze_reply"
    }
}
