package com.marcroldan.rimemba.domain.parser.rules

import java.time.LocalDateTime

/** Detecta "en 10 minutos", "en 2 horas", "en 3 días" como desplazamiento desde ahora. */
internal object RelativeTimeRules {

    private val EN_MINUTOS = Regex("""en\s+(\d+)\s+minutos?""")
    private val EN_HORAS = Regex("""en\s+(\d+)\s+horas?""")
    private val EN_DIAS = Regex("""en\s+(\d+)\s+d[ií]as?""")

    fun detectDateTime(texto: String, ahora: LocalDateTime): LocalDateTime? {
        EN_MINUTOS.find(texto)?.let { return ahora.plusMinutes(it.groupValues[1].toLong()) }
        EN_HORAS.find(texto)?.let { return ahora.plusHours(it.groupValues[1].toLong()) }
        EN_DIAS.find(texto)?.let { return ahora.plusDays(it.groupValues[1].toLong()) }
        return null
    }
}
