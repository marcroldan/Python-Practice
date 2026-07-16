package com.marcroldan.rimemba.data.repository

import com.marcroldan.rimemba.domain.model.Item

/**
 * Efectos secundarios que deben dispararse cuando cambian los items:
 * programar/cancelar alarmas (fase 5) y refrescar el widget de resumen (fase 8).
 * Se implementa fuera de la capa de datos para no acoplar Room a AlarmManager/Glance;
 * [ItemRepositoryImpl] solo garantiza que se invoque en el momento correcto.
 */
interface ItemChangeEffects {
    suspend fun onSaved(item: Item, savedId: Long)
    suspend fun onDeleted(item: Item)
    suspend fun onCompletionChanged(id: Long, completado: Boolean)

    companion object {
        val NONE: ItemChangeEffects = object : ItemChangeEffects {
            override suspend fun onSaved(item: Item, savedId: Long) = Unit
            override suspend fun onDeleted(item: Item) = Unit
            override suspend fun onCompletionChanged(id: Long, completado: Boolean) = Unit
        }
    }
}
