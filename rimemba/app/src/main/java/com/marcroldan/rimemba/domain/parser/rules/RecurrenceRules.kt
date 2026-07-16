package com.marcroldan.rimemba.domain.parser.rules

import com.marcroldan.rimemba.domain.model.Recurrencia

/**
 * Detecta recurrencia: diaria ("todos los días"), semanal genérica ("cada semana")
 * o personalizada por días de la semana concretos ("cada lunes y miércoles").
 * Se comprueba diaria primero porque "todos los días" no debe confundirse con
 * una lista de días concretos.
 */
internal object RecurrenceRules {

    private val DIARIA = Regex("""todos\s+los\s+d[ií]as|cada\s+d[ií]a|diariamente|a\s+diario""")
    private val SEMANAL_GENERICA = Regex("""cada\s+semana|semanalmente|todas\s+las\s+semanas""")
    private val DIAS_LISTA = Regex("""\b(${Keywords.DIA_SEMANA_ALTERNATIVA})\b""")

    fun detectRecurrencia(texto: String): Recurrencia {
        if (DIARIA.containsMatchIn(texto)) return Recurrencia.DIARIA

        val diasMencionados = DIAS_LISTA.findAll(texto)
            .mapNotNull { Keywords.DIAS_SEMANA[it.groupValues[1]] }
            .toSet()

        if (diasMencionados.isNotEmpty() && (texto.contains("cada") || texto.contains("todos los"))) {
            return Recurrencia.personalizada(diasMencionados)
        }

        if (SEMANAL_GENERICA.containsMatchIn(texto)) return Recurrencia.SEMANAL

        return Recurrencia.NINGUNA
    }
}
