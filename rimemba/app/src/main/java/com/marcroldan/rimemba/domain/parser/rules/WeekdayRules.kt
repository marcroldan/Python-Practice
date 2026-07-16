package com.marcroldan.rimemba.domain.parser.rules

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Detecta "el lunes", "el próximo martes", "el miércoles que viene", etc.
 * Siempre resuelve a la próxima ocurrencia estrictamente futura de ese día
 * (si hoy es lunes y se dice "el lunes", se interpreta como el lunes siguiente,
 * no hoy mismo — evita la ambigüedad de "hoy es ese día, ¿te refieres a ahora?").
 */
internal object WeekdayRules {

    private val DIA_REGEX = Regex("""\b(${Keywords.DIA_SEMANA_ALTERNATIVA})\b""")

    fun detectDate(texto: String, hoy: LocalDate): LocalDate? {
        val match = DIA_REGEX.find(texto) ?: return null
        val objetivo = Keywords.DIAS_SEMANA[match.groupValues[1]] ?: return null
        return hoy.plusDays(diasHastaProximo(hoy.dayOfWeek, objetivo).toLong())
    }

    fun diasHastaProximo(desde: DayOfWeek, objetivo: DayOfWeek): Int {
        val delta = (objetivo.value - desde.value + 7) % 7
        return if (delta == 0) 7 else delta
    }
}
