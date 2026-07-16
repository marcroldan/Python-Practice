package com.marcroldan.rimemba.domain.scheduling

import com.marcroldan.rimemba.domain.model.Recurrencia
import com.marcroldan.rimemba.domain.model.TipoRecurrencia
import java.time.LocalDateTime

/**
 * Calcula la siguiente ocurrencia de un recordatorio recurrente a partir de la
 * última vez que se disparó. Función pura: no depende de la hora actual, solo
 * de [base] (el momento en que sonó la alarma) y la [Recurrencia] configurada.
 */
object NextOccurrenceCalculator {

    private const val MAX_INTENTOS_PERSONALIZADA = 8

    fun next(base: LocalDateTime, recurrencia: Recurrencia): LocalDateTime? = when (recurrencia.tipo) {
        TipoRecurrencia.NINGUNA -> null
        TipoRecurrencia.DIARIA -> base.plusDays(1)
        TipoRecurrencia.SEMANAL -> base.plusDays(7)
        TipoRecurrencia.PERSONALIZADA -> siguientePersonalizada(base, recurrencia.diasPersonalizados)
    }

    private fun siguientePersonalizada(base: LocalDateTime, dias: Set<java.time.DayOfWeek>): LocalDateTime {
        if (dias.isEmpty()) return base.plusDays(7)
        var candidata = base.plusDays(1)
        var intentos = 0
        while (candidata.dayOfWeek !in dias && intentos < MAX_INTENTOS_PERSONALIZADA) {
            candidata = candidata.plusDays(1)
            intentos++
        }
        return candidata
    }
}
