package com.marcroldan.rimemba.di

import android.content.Context
import com.marcroldan.rimemba.alarm.ReminderChangeEffects
import com.marcroldan.rimemba.core.NotificationHelper
import com.marcroldan.rimemba.core.SystemTimeProvider
import com.marcroldan.rimemba.core.TimeProvider
import com.marcroldan.rimemba.data.local.RimembaDatabase
import com.marcroldan.rimemba.data.repository.CompositeItemChangeEffects
import com.marcroldan.rimemba.data.repository.ItemRepository
import com.marcroldan.rimemba.data.repository.ItemRepositoryImpl
import com.marcroldan.rimemba.domain.scheduling.AlarmScheduler
import com.marcroldan.rimemba.domain.usecase.CaptureVoiceItemUseCase
import com.marcroldan.rimemba.widget.summary.WidgetRefreshEffects

/**
 * Contenedor de dependencias manual: para el tamaño de este MVP se evita
 * Hilt/Dagger (procesador de anotaciones extra, más superficie de fallo de
 * compilación sin poder depurar con emulador en este entorno de desarrollo).
 */
class AppContainer(context: Context) {

    val timeProvider: TimeProvider = SystemTimeProvider()

    private val database: RimembaDatabase = RimembaDatabase.getInstance(context)

    val notificationHelper: NotificationHelper = NotificationHelper(context)

    val alarmScheduler: AlarmScheduler = AlarmScheduler(context)

    /**
     * Programa/cancela alarmas, refresca notificaciones y refresca el widget de
     * resumen (event-driven) cada vez que cambia un item.
     */
    private val itemChangeEffects = CompositeItemChangeEffects(
        listOf(
            // El lambda evita un ciclo de construcción: itemRepository (lazy)
            // necesita itemChangeEffects, así que estos efectos no pueden recibir
            // itemRepository directamente en el constructor, solo una referencia
            // diferida que se resuelve la primera vez que se invoca.
            ReminderChangeEffects(alarmScheduler, notificationHelper) { id -> itemRepository.getById(id) },
            WidgetRefreshEffects(context)
        )
    )

    val itemRepository: ItemRepository by lazy {
        ItemRepositoryImpl(
            itemDao = database.itemDao(),
            timeProvider = timeProvider,
            effects = itemChangeEffects
        )
    }

    val captureVoiceItemUseCase: CaptureVoiceItemUseCase by lazy {
        CaptureVoiceItemUseCase(itemRepository, timeProvider)
    }
}
