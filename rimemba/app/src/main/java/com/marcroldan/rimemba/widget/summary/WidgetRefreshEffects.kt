package com.marcroldan.rimemba.widget.summary

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.marcroldan.rimemba.data.repository.ItemChangeEffects
import com.marcroldan.rimemba.domain.model.Item

/**
 * Refresca el widget de resumen del día cada vez que cambian los items
 * (event-driven, no periódico) para que se vea siempre al día sin abrir la app.
 */
class WidgetRefreshEffects(private val context: Context) : ItemChangeEffects {

    override suspend fun onSaved(item: Item, savedId: Long) {
        DailySummaryWidget().updateAll(context)
    }

    override suspend fun onDeleted(item: Item) {
        DailySummaryWidget().updateAll(context)
    }

    override suspend fun onCompletionChanged(id: Long, completado: Boolean) {
        DailySummaryWidget().updateAll(context)
    }
}
