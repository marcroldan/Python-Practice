package com.marcroldan.rimemba.domain.parser

import com.marcroldan.rimemba.domain.model.Recurrencia
import com.marcroldan.rimemba.domain.model.TipoItem
import java.time.LocalDateTime

data class ParseResult(
    val tipo: TipoItem,
    val fechaHora: LocalDateTime?,
    val recurrencia: Recurrencia = Recurrencia.NINGUNA
)
