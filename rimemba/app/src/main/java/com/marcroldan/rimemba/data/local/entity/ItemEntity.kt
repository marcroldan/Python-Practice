package com.marcroldan.rimemba.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.marcroldan.rimemba.domain.model.Item
import com.marcroldan.rimemba.domain.model.Recurrencia
import com.marcroldan.rimemba.domain.model.TipoItem
import com.marcroldan.rimemba.domain.model.TipoRecurrencia
import java.time.DayOfWeek
import java.time.LocalDateTime

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val texto: String,
    val tipo: TipoItem,
    val fechaHora: LocalDateTime?,
    val recurrenciaTipo: TipoRecurrencia = TipoRecurrencia.NINGUNA,
    val recurrenciaDias: String? = null,
    val completado: Boolean = false,
    val creadoEn: LocalDateTime
)

fun ItemEntity.toDomain(): Item = Item(
    id = id,
    texto = texto,
    tipo = tipo,
    fechaHora = fechaHora,
    recurrencia = Recurrencia(
        tipo = recurrenciaTipo,
        diasPersonalizados = recurrenciaDias.toDayOfWeekSet()
    ),
    completado = completado,
    creadoEn = creadoEn
)

fun Item.toEntity(): ItemEntity = ItemEntity(
    id = id,
    texto = texto,
    tipo = tipo,
    fechaHora = fechaHora,
    recurrenciaTipo = recurrencia.tipo,
    recurrenciaDias = recurrencia.diasPersonalizados.toCsv(),
    completado = completado,
    creadoEn = creadoEn
)

private fun String?.toDayOfWeekSet(): Set<DayOfWeek> =
    this?.takeIf { it.isNotBlank() }
        ?.split(",")
        ?.map { DayOfWeek.of(it.trim().toInt()) }
        ?.toSet()
        ?: emptySet()

private fun Set<DayOfWeek>.toCsv(): String? =
    takeIf { it.isNotEmpty() }?.joinToString(",") { it.value.toString() }
