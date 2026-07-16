package com.marcroldan.rimemba.domain.parser.rules

import java.time.LocalDate

/**
 * Detecta "hoy" / "mañana" / "pasado mañana" como desplazamiento de día.
 *
 * Se comprueba "pasado mañana" antes que "mañana" suelta porque la segunda es
 * una subcadena de la primera. Y "mañana" suelta se excluye explícitamente si
 * va precedida de "de la "/"por la ", que es la franja horaria ("por la mañana"),
 * no el día siguiente — esa es la ambigüedad clave del idioma que hay que resolver
 * con precedencia explícita en vez de dejar que ambas reglas disparen a la vez.
 */
internal object RelativeDayRules {

    private val PASADO_MANANA = Regex("""pasado\s+mañana""")
    private val MANANA_DIA = Regex("""(?<!de la )(?<!por la )\bmañana\b""")
    private val HOY = Regex("""\bhoy\b""")

    fun detectDate(texto: String, hoy: LocalDate): LocalDate? = when {
        PASADO_MANANA.containsMatchIn(texto) -> hoy.plusDays(2)
        MANANA_DIA.containsMatchIn(texto) -> hoy.plusDays(1)
        HOY.containsMatchIn(texto) -> hoy
        else -> null
    }
}
