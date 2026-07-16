package com.marcroldan.rimemba.domain.parser

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class SnoozeReplyInterpreterTest {

    // Jueves 16 de julio de 2026, 14:00.
    private val ahora = LocalDateTime.of(2026, 7, 16, 14, 0)

    @Test
    fun `una hora suma sesenta minutos desde ahora`() {
        assertEquals(ahora.plusHours(1), SnoozeReplyInterpreter.interpret("una hora", ahora))
    }

    @Test
    fun `esta tarde son las 17 si aun no han pasado`() {
        val resultado = SnoozeReplyInterpreter.interpret("esta tarde", ahora)
        assertEquals(ahora.toLocalDate(), resultado?.toLocalDate())
        assertEquals(17, resultado?.hour)
    }

    @Test
    fun `esta tarde salta a mañana si las 17 ya pasaron`() {
        val masTarde = ahora.withHour(20)
        val resultado = SnoozeReplyInterpreter.interpret("esta tarde", masTarde)
        assertEquals(masTarde.toLocalDate().plusDays(1), resultado?.toLocalDate())
    }

    @Test
    fun `mañana suelta es el dia siguiente a las 9`() {
        val resultado = SnoozeReplyInterpreter.interpret("mañana", ahora)
        assertEquals(ahora.toLocalDate().plusDays(1), resultado?.toLocalDate())
        assertEquals(9, resultado?.hour)
    }

    @Test
    fun `texto mas rico delega en el parser completo`() {
        val resultado = SnoozeReplyInterpreter.interpret("el lunes a las 5 de la tarde", ahora)
        assertEquals(17, resultado?.hour)
    }

    @Test
    fun `N horas y N minutos tambien se interpretan`() {
        assertEquals(ahora.plusHours(3), SnoozeReplyInterpreter.interpret("3 horas", ahora))
        assertEquals(ahora.plusMinutes(20), SnoozeReplyInterpreter.interpret("20 minutos", ahora))
    }
}
