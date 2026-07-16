package com.marcroldan.rimemba.data.repository

import com.marcroldan.rimemba.core.TimeProvider
import com.marcroldan.rimemba.data.local.dao.ItemDao
import com.marcroldan.rimemba.data.local.entity.toDomain
import com.marcroldan.rimemba.data.local.entity.toEntity
import com.marcroldan.rimemba.domain.model.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Único punto que orquesta persistencia + efectos secundarios (programar/cancelar
 * alarmas, refrescar el widget de resumen). Evita el bug de "olvidé reprogramar
 * la alarma" al mutar un item desde una pantalla distinta.
 */
class ItemRepositoryImpl(
    private val itemDao: ItemDao,
    private val timeProvider: TimeProvider,
    private val effects: ItemChangeEffects = ItemChangeEffects.NONE
) : ItemRepository {

    override fun observeAll(): Flow<List<Item>> =
        itemDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeToday(): Flow<List<Item>> {
        val hoy = timeProvider.now().toLocalDate()
        val inicio = hoy.atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val fin = hoy.atTime(LocalTime.MAX).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        return itemDao.observeToday(inicio, fin).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun save(item: Item): Long {
        val id = if (item.id == 0L) {
            itemDao.insert(item.toEntity())
        } else {
            itemDao.update(item.toEntity())
            item.id
        }
        effects.onSaved(item, id)
        return id
    }

    override suspend fun delete(item: Item) {
        itemDao.delete(item.toEntity())
        effects.onDeleted(item)
    }

    override suspend fun setCompletado(id: Long, completado: Boolean) {
        itemDao.setCompletado(id, completado)
        effects.onCompletionChanged(id, completado)
    }

    override suspend fun getPendingReminders(): List<Item> =
        itemDao.getPendingReminders().map { it.toDomain() }

    override suspend fun getById(id: Long): Item? =
        itemDao.getById(id)?.toDomain()
}
