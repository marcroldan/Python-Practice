package com.marcroldan.rimemba.domain.parser

import java.time.LocalDateTime

/**
 * Interpreta la respuesta (dictada o elegida como sugerencia rápida) a la acción
 * "Posponer" de una notificación: "una hora", "esta tarde", "mañana", o texto más
 * rico como "el lunes a las 5" delegando en [SpanishDateTimeParser].
 */
object SnoozeReplyInterpreter {

    private val UNA_HORA = Regex("""^(una|1)\s+horas?$""")
    private val N_HORAS = Regex("""^(\d+)\s+horas?$""")
    private val N_MINUTOS = Regex("""^(\d+)\s+minutos?$""")
    private val ESTA_TARDE = Regex("""esta\s+tarde""")
    private val MANANA_SUELTA = Regex("""(?<!de la )(?<!por la )\bmañana\b""")

    fun interpret(texto: String, ahora: LocalDateTime): LocalDateTime? {
        val normalizado = texto.trim().lowercase().replace(Regex("""\s+"""), " ")

        UNA_HORA.find(normalizado)?.let { return ahora.plusHours(1) }
        N_HORAS.find(normalizado)?.let { return ahora.plusHours(it.groupValues[1].toLong()) }
        N_MINUTOS.find(normalizado)?.let { return ahora.plusMinutes(it.groupValues[1].toLong()) }

        if (ESTA_TARDE.containsMatchIn(normalizado)) {
            val hoyALasCinco = ahora.toLocalDate().atTime(17, 0)
            return if (hoyALasCinco.isAfter(ahora)) hoyALasCinco else hoyALasCinco.plusDays(1)
        }

        if (MANANA_SUELTA.containsMatchIn(normalizado)) {
            return ahora.toLocalDate().plusDays(1).atTime(9, 0)
        }

        return SpanishDateTimeParser.parse(texto, ahora).fechaHora
    }
}
