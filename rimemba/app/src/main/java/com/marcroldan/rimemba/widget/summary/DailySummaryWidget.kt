package com.marcroldan.rimemba.widget.summary

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.marcroldan.rimemba.R
import com.marcroldan.rimemba.RimembaApplication
import com.marcroldan.rimemba.domain.model.Item
import com.marcroldan.rimemba.domain.model.TipoItem
import kotlinx.coroutines.flow.first
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val horaFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale("es", "ES"))

/**
 * Widget de home screen con las notas/recordatorios de hoy. Se refresca de
 * forma event-driven (ver [WidgetRefreshEffects]), no con un ciclo periódico:
 * cada mutación de items dispara `updateAll` y esta función se vuelve a ejecutar.
 */
class DailySummaryWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val container = (context.applicationContext as RimembaApplication).container
        val items = container.itemRepository.observeToday().first()

        provideContent {
            if (items.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize().padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = context.getString(R.string.daily_summary_widget_description),
                        style = TextStyle(color = ColorProvider(Color.Gray))
                    )
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize().background(Color.White)) {
                    items(items, itemId = { it.id }) { item -> DailySummaryRow(item) }
                }
            }
        }
    }
}

@Composable
private fun DailySummaryRow(item: Item) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(
                actionRunCallback<ToggleCompleteAction>(
                    actionParametersOf(ToggleCompleteAction.itemIdKey to item.id)
                )
            ),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        val iconRes = when {
            item.completado -> R.drawable.ic_widget_check
            item.tipo == TipoItem.RECORDATORIO -> R.drawable.ic_widget_reminder
            else -> R.drawable.ic_widget_note
        }
        Image(provider = ImageProvider(iconRes), contentDescription = null)

        Spacer(modifier = GlanceModifier.width(8.dp))

        Column(modifier = GlanceModifier.fillMaxWidth()) {
            Text(text = item.texto, maxLines = 1)
            item.fechaHora?.let { fecha ->
                Text(
                    text = fecha.format(horaFormatter),
                    style = TextStyle(color = ColorProvider(Color.Gray))
                )
            }
        }
    }
}
