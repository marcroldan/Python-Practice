package com.marcroldan.rimemba.domain.scheduling

import com.marcroldan.rimemba.domain.model.Recurrencia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

class NextOccurrenceCalculatorTest {

    // Jueves 16 de julio de 2026, 09:00.
    private val base = LocalDateTime.of(2026, 7, 16, 9, 0)

    @Test
    fun `sin recurrencia no hay proxima ocurrencia`() {
        assertNull(NextOccurrenceCalculator.next(base, Recurrencia.NINGUNA))
    }

    @Test
    fun `diaria suma un dia`() {
        assertEquals(base.plusDays(1), NextOccurrenceCalculator.next(base, Recurrencia.DIARIA))
    }

    @Test
    fun `semanal suma siete dias`() {
        assertEquals(base.plusDays(7), NextOccurrenceCalculator.next(base, Recurrencia.SEMANAL))
    }

    @Test
    fun `personalizada busca el siguiente dia marcado`() {
        val recurrencia = Recurrencia.personalizada(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))

        // base es jueves -> el siguiente día marcado es el lunes (+4 días).
        val siguiente = NextOccurrenceCalculator.next(base, recurrencia)

        assertEquals(base.plusDays(4), siguiente)
        assertEquals(DayOfWeek.MONDAY, siguiente?.dayOfWeek)
    }

    @Test
    fun `personalizada con wraparound cuando ya paso el ultimo dia marcado esta semana`() {
        // base es jueves, el único día marcado es el martes (ya pasó esta semana) -> +5 días.
        val recurrencia = Recurrencia.personalizada(setOf(DayOfWeek.TUESDAY))

        val siguiente = NextOccurrenceCalculator.next(base, recurrencia)

        assertEquals(DayOfWeek.TUESDAY, siguiente?.dayOfWeek)
        assertEquals(base.plusDays(5), siguiente)
    }
}
