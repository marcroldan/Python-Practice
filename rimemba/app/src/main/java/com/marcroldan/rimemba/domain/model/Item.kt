package com.marcroldan.rimemba.domain.model

import java.time.LocalDateTime

data class Item(
    val id: Long = 0L,
    val texto: String,
    val tipo: TipoItem,
    val fechaHora: LocalDateTime?,
    val recurrencia: Recurrencia = Recurrencia.NINGUNA,
    val completado: Boolean = false,
    val creadoEn: LocalDateTime
)
