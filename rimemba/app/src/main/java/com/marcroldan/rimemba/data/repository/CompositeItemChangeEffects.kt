package com.marcroldan.rimemba.data.repository

import com.marcroldan.rimemba.domain.model.Item

/** Ejecuta varios [ItemChangeEffects] en orden para el mismo evento. */
class CompositeItemChangeEffects(
    private val delegates: List<ItemChangeEffects>
) : ItemChangeEffects {

    override suspend fun onSaved(item: Item, savedId: Long) {
        delegates.forEach { it.onSaved(item, savedId) }
    }

    override suspend fun onDeleted(item: Item) {
        delegates.forEach { it.onDeleted(item) }
    }

    override suspend fun onCompletionChanged(id: Long, completado: Boolean) {
        delegates.forEach { it.onCompletionChanged(id, completado) }
    }
}
