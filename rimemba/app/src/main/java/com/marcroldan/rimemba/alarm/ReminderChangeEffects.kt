package com.marcroldan.rimemba.alarm

import com.marcroldan.rimemba.core.NotificationHelper
import com.marcroldan.rimemba.data.repository.ItemChangeEffects
import com.marcroldan.rimemba.domain.model.Item
import com.marcroldan.rimemba.domain.model.TipoItem
import com.marcroldan.rimemba.domain.scheduling.AlarmScheduler

/**
 * Efectos secundarios reales sobre alarmas/notificaciones cuando cambian los items.
 * [com.marcroldan.rimemba.data.repository.ItemRepositoryImpl] solo garantiza que
 * se invoquen en el momento correcto; aquí vive la única lógica que programa o
 * cancela una alarma, para no repetirla en cada pantalla que mute un item.
 */
class ReminderChangeEffects(
    private val alarmScheduler: AlarmScheduler,
    private val notificationHelper: NotificationHelper
) : ItemChangeEffects {

    override suspend fun onSaved(item: Item, savedId: Long) {
        val guardado = item.copy(id = savedId)
        val debeEstarProgramado = guardado.tipo == TipoItem.RECORDATORIO &&
            guardado.fechaHora != null &&
            !guardado.completado

        if (debeEstarProgramado) {
            alarmScheduler.schedule(guardado)
        } else {
            alarmScheduler.cancel(savedId)
        }
    }

    override suspend fun onDeleted(item: Item) {
        alarmScheduler.cancel(item.id)
        notificationHelper.cancel(item.id)
    }

    override suspend fun onCompletionChanged(id: Long, completado: Boolean) {
        if (completado) {
            alarmScheduler.cancel(id)
            notificationHelper.cancel(id)
        }
    }
}
