package com.marcroldan.rimemba.domain.parser.rules

import java.time.DayOfWeek

/**
 * Vocabulario compartido por las reglas del parser. Se aceptan variantes con y sin
 * tilde porque algunos motores de reconocimiento de voz no siempre las devuelven.
 */
internal object Keywords {

    val DIAS_SEMANA: Map<String, DayOfWeek> = mapOf(
        "lunes" to DayOfWeek.MONDAY,
        "martes" to DayOfWeek.TUESDAY,
        "miercoles" to DayOfWeek.WEDNESDAY,
        "miércoles" to DayOfWeek.WEDNESDAY,
        "jueves" to DayOfWeek.THURSDAY,
        "viernes" to DayOfWeek.FRIDAY,
        "sabado" to DayOfWeek.SATURDAY,
        "sábado" to DayOfWeek.SATURDAY,
        "domingo" to DayOfWeek.SUNDAY
    )

    /** Alternancia regex de todos los nombres de día, ordenada para evitar coincidencias parciales. */
    val DIA_SEMANA_ALTERNATIVA: String = DIAS_SEMANA.keys.joinToString("|")
}
