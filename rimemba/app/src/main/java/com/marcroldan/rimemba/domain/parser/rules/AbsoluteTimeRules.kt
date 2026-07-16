package com.marcroldan.rimemba.domain.parser.rules

import java.time.LocalTime

/**
 * Detecta una hora del reloj explícita: "a las 5 de la tarde", "a las 17:30",
 * "a las 5 y media", "al mediodía", "a medianoche". Si no hay hora explícita
 * pero sí una franja horaria suelta ("por la mañana"/"por la tarde"/"por la
 * noche"), usa una hora por defecto para esa franja.
 *
 * Simplificación deliberada del MVP: sin calificador de mañana/tarde/noche,
 * la hora 1-12 se interpreta literalmente en formato 24h (ej. "a las 5" = 05:00),
 * no se intenta adivinar AM/PM. Es una regla simple, predecible y testeable;
 * se puede afinar más adelante si los usuarios lo piden.
 */
internal object AbsoluteTimeRules {

    private val MEDIODIA = Regex("""medio\s*d[ií]a""")
    private val MEDIANOCHE = Regex("""medianoche""")

    private val HORA_EXPLICITA = Regex(
        """a\s+las?\s+(\d{1,2})(?::(\d{2})|\s+y\s+media|\s+y\s+cuarto)?\s*(de la mañana|de la tarde|de la noche|por la mañana|por la tarde|por la noche)?"""
    )

    private val FRANJA_SUELTA = Regex("""(de la|por la)\s+(mañana|tarde|noche)""")

    fun detectTime(texto: String): LocalTime? {
        if (MEDIODIA.containsMatchIn(texto)) return LocalTime.NOON
        if (MEDIANOCHE.containsMatchIn(texto)) return LocalTime.MIDNIGHT

        HORA_EXPLICITA.find(texto)?.let { match ->
            var hora = match.groupValues[1].toInt().coerceIn(0, 23)
            val minutos = when {
                match.groupValues[2].isNotEmpty() -> match.groupValues[2].toInt()
                match.value.contains("y media") -> 30
                match.value.contains("y cuarto") -> 15
                else -> 0
            }
            val franja = match.groupValues[3]
            if (hora in 1..11 && (franja.contains("tarde") || franja.contains("noche"))) {
                hora += 12
            }
            return LocalTime.of(hora, minutos)
        }

        FRANJA_SUELTA.find(texto)?.let { match ->
            return when (match.groupValues[2]) {
                "mañana" -> LocalTime.of(9, 0)
                "tarde" -> LocalTime.of(17, 0)
                "noche" -> LocalTime.of(21, 0)
                else -> null
            }
        }

        return null
    }
}
