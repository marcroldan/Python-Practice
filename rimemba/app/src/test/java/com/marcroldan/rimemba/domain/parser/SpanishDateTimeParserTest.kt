package com.marcroldan.rimemba.domain.parser

import com.marcroldan.rimemba.domain.model.TipoItem
import com.marcroldan.rimemba.domain.model.TipoRecurrencia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

class SpanishDateTimeParserTest {

    // Jueves 16 de julio de 2026, 10:00 — para que los tests sean deterministas.
    private val ahora = LocalDateTime.of(2026, 7, 16, 10, 0)

    @Test
    fun `texto sin senal de fecha se interpreta como nota`() {
        val resultado = SpanishDateTimeParser.parse("comprar leche y pan", ahora)

        assertEquals(TipoItem.NOTA, resultado.tipo)
        assertNull(resultado.fechaHora)
    }

    @Test
    fun `mañana como dia siguiente se distingue de por la mañana como franja horaria`() {
        val comoDia = SpanishDateTimeParser.parse("recuerdame llamar al medico mañana", ahora)
        assertEquals(TipoItem.RECORDATORIO, comoDia.tipo)
        assertEquals(ahora.toLocalDate().plusDays(1), comoDia.fechaHora?.toLocalDate())

        // Usamos un "ahora" antes de las 9 para que la franja horaria por defecto (9:00)
        // todavía no haya pasado hoy y así aislar la ambigüedad día/franja del roll-forward.
        val temprano = ahora.withHour(7).withMinute(0)
        val comoFranja = SpanishDateTimeParser.parse("recuerdame llamar al medico por la mañana", temprano)
        assertEquals(TipoItem.RECORDATORIO, comoFranja.tipo)
        assertEquals(temprano.toLocalDate(), comoFranja.fechaHora?.toLocalDate())
        assertEquals(9, comoFranja.fechaHora?.hour)
    }

    @Test
    fun `pasado mañana suma dos dias`() {
        val resultado = SpanishDateTimeParser.parse("nota pasado mañana", ahora)
        assertEquals(ahora.toLocalDate().plusDays(2), resultado.fechaHora?.toLocalDate())
    }

    @Test
    fun `en N horas es un desplazamiento relativo exacto`() {
        val resultado = SpanishDateTimeParser.parse("recuerdame en 2 horas", ahora)
        assertEquals(ahora.plusHours(2), resultado.fechaHora)
    }

    @Test
    fun `en N minutos es un desplazamiento relativo exacto`() {
        val resultado = SpanishDateTimeParser.parse("recuerdame en 15 minutos", ahora)
        assertEquals(ahora.plusMinutes(15), resultado.fechaHora)
    }

    @Test
    fun `a las 5 de la tarde se interpreta como las 17`() {
        val resultado = SpanishDateTimeParser.parse("recuerdame llamar a las 5 de la tarde", ahora)
        assertEquals(17, resultado.fechaHora?.hour)
        assertEquals(0, resultado.fechaHora?.minute)
    }

    @Test
    fun `a las 5 sin calificador se interpreta literal en formato 24h`() {
        // Se pide a las 10:00, así que "a las 5" (05:00) ya pasó hoy -> se espera mañana.
        val resultado = SpanishDateTimeParser.parse("recuerdame llamar a las 5", ahora)
        assertEquals(5, resultado.fechaHora?.hour)
        assertEquals(ahora.toLocalDate().plusDays(1), resultado.fechaHora?.toLocalDate())
    }

    @Test
    fun `el lunes resuelve a la proxima ocurrencia futura`() {
        // ahora es jueves 16 de julio de 2026 -> el próximo lunes es el 20 de julio.
        val resultado = SpanishDateTimeParser.parse("recuerdame el lunes", ahora)
        assertEquals(DayOfWeek.MONDAY, resultado.fechaHora?.dayOfWeek)
        assertEquals(ahora.toLocalDate().plusDays(4), resultado.fechaHora?.toLocalDate())
    }

    @Test
    fun `recurrencia diaria se detecta con todos los dias`() {
        val resultado = SpanishDateTimeParser.parse("recuerdame tomar la pastilla todos los dias a las 9", ahora)
        assertEquals(TipoRecurrencia.DIARIA, resultado.recurrencia.tipo)
    }

    @Test
    fun `recurrencia semanal generica se detecta con cada semana`() {
        val resultado = SpanishDateTimeParser.parse("recuerdame regar las plantas cada semana", ahora)
        assertEquals(TipoRecurrencia.SEMANAL, resultado.recurrencia.tipo)
    }

    @Test
    fun `recurrencia personalizada detecta los dias mencionados con cada`() {
        val resultado = SpanishDateTimeParser.parse("recuerdame el gimnasio cada lunes y miercoles a las 7", ahora)

        assertEquals(TipoRecurrencia.PERSONALIZADA, resultado.recurrencia.tipo)
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), resultado.recurrencia.diasPersonalizados)
        assertTrue(resultado.fechaHora?.dayOfWeek in resultado.recurrencia.diasPersonalizados)
    }

    @Test
    fun `mencionar un solo dia sin cada no activa recurrencia`() {
        val resultado = SpanishDateTimeParser.parse("recuerdame el lunes ir al medico", ahora)
        assertEquals(TipoRecurrencia.NINGUNA, resultado.recurrencia.tipo)
    }

    @Test
    fun `al mediodia y a medianoche se interpretan correctamente`() {
        val mediodia = SpanishDateTimeParser.parse("recuerdame comer al mediodia", ahora)
        assertEquals(12, mediodia.fechaHora?.hour)

        val medianoche = SpanishDateTimeParser.parse("recuerdame algo a medianoche", ahora)
        assertEquals(0, medianoche.fechaHora?.hour)
    }

    @Test
    fun `sin dia ni hora explicita pero con recurrencia diaria usa hora por defecto`() {
        val resultado = SpanishDateTimeParser.parse("recuerdame estirar todos los dias", ahora)
        assertEquals(9, resultado.fechaHora?.hour)
    }
}
