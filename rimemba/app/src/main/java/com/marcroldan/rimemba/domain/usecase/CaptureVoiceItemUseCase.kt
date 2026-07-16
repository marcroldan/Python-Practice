package com.marcroldan.rimemba.domain.usecase

import com.marcroldan.rimemba.core.TimeProvider
import com.marcroldan.rimemba.data.repository.ItemRepository
import com.marcroldan.rimemba.domain.model.Item
import com.marcroldan.rimemba.domain.model.TipoItem
import com.marcroldan.rimemba.domain.parser.SpanishDateTimeParser
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Orquesta el flujo compartido "texto transcrito -> parsear -> guardar", usado
 * tanto desde [com.marcroldan.rimemba.ui.MainActivity] como desde la actividad
 * trampolín del widget, para no duplicar esta lógica en dos sitios.
 */
class CaptureVoiceItemUseCase(
    private val repository: ItemRepository,
    private val timeProvider: TimeProvider
) {

    suspend operator fun invoke(textoTranscrito: String): Item {
        val ahora = timeProvider.now()
        val resultado = SpanishDateTimeParser.parse(textoTranscrito, ahora)
        val item = Item(
            texto = textoTranscrito,
            tipo = resultado.tipo,
            fechaHora = resultado.fechaHora,
            recurrencia = resultado.recurrencia,
            creadoEn = ahora
        )
        val id = repository.save(item)
        return item.copy(id = id)
    }

    /** Frase de confirmación hablada por TTS tras guardar. */
    fun buildConfirmationMessage(item: Item, ahora: LocalDateTime): String {
        val fecha = item.fechaHora
        if (item.tipo == TipoItem.NOTA || fecha == null) {
            return "Anotado: ${item.texto}"
        }
        val cuando = describirDia(fecha, ahora)
        val hora = "%02d:%02d".format(fecha.hour, fecha.minute)
        return "Recordatorio anotado: ${item.texto}, $cuando a las $hora"
    }

    private fun describirDia(fecha: LocalDateTime, ahora: LocalDateTime): String {
        val diasHasta = ChronoUnit.DAYS.between(ahora.toLocalDate(), fecha.toLocalDate())
        return when (diasHasta) {
            0L -> "hoy"
            1L -> "mañana"
            else -> NOMBRES_DIA[fecha.dayOfWeek] ?: "pronto"
        }
    }

    private companion object {
        val NOMBRES_DIA = mapOf(
            DayOfWeek.MONDAY to "el lunes",
            DayOfWeek.TUESDAY to "el martes",
            DayOfWeek.WEDNESDAY to "el miércoles",
            DayOfWeek.THURSDAY to "el jueves",
            DayOfWeek.FRIDAY to "el viernes",
            DayOfWeek.SATURDAY to "el sábado",
            DayOfWeek.SUNDAY to "el domingo"
        )
    }
}
