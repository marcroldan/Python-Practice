package com.marcroldan.rimemba.data.local

import androidx.room.TypeConverter
import com.marcroldan.rimemba.domain.model.TipoItem
import com.marcroldan.rimemba.domain.model.TipoRecurrencia
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Las fechas se guardan como String en formato ISO_LOCAL_DATE_TIME (ancho fijo,
 * ceros a la izquierda) para que sean ordenables lexicográficamente en SQL sin
 * necesitar funciones de fecha de SQLite.
 */
class Converters {

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? =
        value?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? =
        value?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }

    @TypeConverter
    fun fromTipoItem(value: TipoItem): String = value.name

    @TypeConverter
    fun toTipoItem(value: String): TipoItem = TipoItem.valueOf(value)

    @TypeConverter
    fun fromTipoRecurrencia(value: TipoRecurrencia): String = value.name

    @TypeConverter
    fun toTipoRecurrencia(value: String): TipoRecurrencia = TipoRecurrencia.valueOf(value)
}
