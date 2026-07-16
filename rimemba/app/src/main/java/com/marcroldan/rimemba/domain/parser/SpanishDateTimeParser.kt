package com.marcroldan.rimemba.domain.parser

import com.marcroldan.rimemba.core.Constants
import com.marcroldan.rimemba.domain.model.Recurrencia
import com.marcroldan.rimemba.domain.model.TipoItem
import com.marcroldan.rimemba.domain.model.TipoRecurrencia
import com.marcroldan.rimemba.domain.parser.rules.AbsoluteTimeRules
import com.marcroldan.rimemba.domain.parser.rules.RecurrenceRules
import com.marcroldan.rimemba.domain.parser.rules.RelativeDayRules
import com.marcroldan.rimemba.domain.parser.rules.RelativeTimeRules
import com.marcroldan.rimemba.domain.parser.rules.WeekdayRules
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Parser de fecha/hora/recurrencia en español, basado en reglas manuales
 * (sin librería externa: las librerías de parsing de fechas están orientadas
 * a inglés y no cubren bien "mañana", "pasado mañana", "el lunes que viene"…).
 *
 * El texto transcrito se guarda siempre completo tal cual en el Item — este
 * parser solo extrae metadatos (tipo/fecha/recurrencia), nunca recorta el texto.
 */
object SpanishDateTimeParser {

    fun parse(textoOriginal: String, ahora: LocalDateTime): ParseResult {
        val texto = normalizar(textoOriginal)
        val hoy = ahora.toLocalDate()

        RelativeTimeRules.detectDateTime(texto, ahora)?.let { fechaHora ->
            return ParseResult(
                tipo = TipoItem.RECORDATORIO,
                fechaHora = fechaHora,
                recurrencia = RecurrenceRules.detectRecurrencia(texto)
            )
        }

        val recurrencia = RecurrenceRules.detectRecurrencia(texto)
        val fechaExplicita: LocalDate? = if (recurrencia.tipo == TipoRecurrencia.PERSONALIZADA) {
            null
        } else {
            RelativeDayRules.detectDate(texto, hoy) ?: WeekdayRules.detectDate(texto, hoy)
        }
        val horaExplicita: LocalTime? = AbsoluteTimeRules.detectTime(texto)

        val haySenalDeRecordatorio =
            fechaExplicita != null || horaExplicita != null || recurrencia.tipo != TipoRecurrencia.NINGUNA

        if (!haySenalDeRecordatorio) {
            return ParseResult(tipo = TipoItem.NOTA, fechaHora = null)
        }

        val diasPermitidos = recurrencia.diasPersonalizados.takeIf {
            recurrencia.tipo == TipoRecurrencia.PERSONALIZADA
        }

        val fechaHora = combinarFechaHora(
            fechaBase = fechaExplicita,
            horaBase = horaExplicita,
            hoy = hoy,
            ahora = ahora,
            diasPermitidos = diasPermitidos
        )

        return ParseResult(tipo = TipoItem.RECORDATORIO, fechaHora = fechaHora, recurrencia = recurrencia)
    }

    private fun combinarFechaHora(
        fechaBase: LocalDate?,
        horaBase: LocalTime?,
        hoy: LocalDate,
        ahora: LocalDateTime,
        diasPermitidos: Set<DayOfWeek>?
    ): LocalDateTime {
        val hora = horaBase ?: LocalTime.of(Constants.DEFAULT_REMINDER_HOUR, 0)
        var candidata = fechaBase ?: hoy
        var fechaHora = LocalDateTime.of(candidata, hora)

        if (diasPermitidos != null) {
            var intentos = 0
            while ((candidata.dayOfWeek !in diasPermitidos || !fechaHora.isAfter(ahora)) && intentos < 8) {
                candidata = candidata.plusDays(1)
                fechaHora = LocalDateTime.of(candidata, hora)
                intentos++
            }
        } else if (!fechaHora.isAfter(ahora)) {
            candidata = candidata.plusDays(1)
            fechaHora = LocalDateTime.of(candidata, hora)
        }

        return fechaHora
    }

    private fun normalizar(texto: String): String =
        texto.trim().lowercase().replace(Regex("""\s+"""), " ")
}
