package com.marcroldan.rimemba.domain.model

import java.time.DayOfWeek

enum class TipoRecurrencia {
    NINGUNA,
    DIARIA,
    SEMANAL,
    PERSONALIZADA
}

data class Recurrencia(
    val tipo: TipoRecurrencia = TipoRecurrencia.NINGUNA,
    val diasPersonalizados: Set<DayOfWeek> = emptySet()
) {
    companion object {
        val NINGUNA = Recurrencia(TipoRecurrencia.NINGUNA)
        val DIARIA = Recurrencia(TipoRecurrencia.DIARIA)
        val SEMANAL = Recurrencia(TipoRecurrencia.SEMANAL)

        fun personalizada(dias: Set<DayOfWeek>): Recurrencia =
            Recurrencia(TipoRecurrencia.PERSONALIZADA, dias)
    }
}
