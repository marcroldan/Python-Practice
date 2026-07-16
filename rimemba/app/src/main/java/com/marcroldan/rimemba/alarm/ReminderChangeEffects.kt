package com.marcroldan.rimemba.alarm

import com.marcroldan.rimemba.core.NotificationHelper
import com.marcroldan.rimemba.data.repository.ItemChangeEffects
import com.marcroldan.rimemba.domain.model.Item
import com.marcroldan.rimemba.domain.model.TipoItem
import com.marcroldan.rimemba.domain.scheduling.AlarmScheduler
import java.time.LocalDateTime

/**
 * Efectos secundarios reales sobre alarmas/notificaciones cuando cambian los items.
 * [com.marcroldan.rimemba.data.repository.ItemRepositoryImpl] solo garantiza que
 * se invoquen en el momento correcto; aquí vive la única lógica que programa o
 * cancela una alarma, para no repetirla en cada pantalla que mute un item.
 *
 * [getItemById] se recibe como lambda (en vez de una dependencia directa a
 * [com.marcroldan.rimemba.data.repository.ItemRepository]) para evitar un ciclo
 * de construcción: el repositorio necesita estos efectos, así que estos efectos
 * no pueden depender directamente del repositorio ya construido.
 */
class ReminderChangeEffects(
    private val alarmScheduler: AlarmScheduler,
    private val notificationHelper: NotificationHelper,
    private val getItemById: suspend (Long) -> Item?
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
            return
        }

        // Al reabrir un recordatorio (desmarcarlo) hay que volver a programar su
        // alarma si todavía tiene una fecha futura; si no, se quedaría pendiente
        // en la base de datos sin ninguna alarma que lo dispare.
        val item = getItemById(id) ?: return
        if (item.tipo == TipoItem.RECORDATORIO && item.fechaHora?.isAfter(LocalDateTime.now()) == true) {
            alarmScheduler.schedule(item)
        }
    }
}
