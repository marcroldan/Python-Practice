package com.marcroldan.rimemba.widget.summary

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.marcroldan.rimemba.RimembaApplication

/** Marca/desmarca un item como completado al tocarlo en el widget de resumen. */
class ToggleCompleteAction : ActionCallback {

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val itemId = parameters[itemIdKey] ?: return
        val container = (context.applicationContext as RimembaApplication).container
        val item = container.itemRepository.getById(itemId) ?: return
        container.itemRepository.setCompletado(itemId, !item.completado)
    }

    companion object {
        val itemIdKey = ActionParameters.Key<Long>("item_id")
    }
}
