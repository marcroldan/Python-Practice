package com.marcroldan.rimemba.data.repository

import com.marcroldan.rimemba.domain.model.Item
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun observeAll(): Flow<List<Item>>
    fun observeToday(): Flow<List<Item>>
    suspend fun save(item: Item): Long
    suspend fun delete(item: Item)
    suspend fun setCompletado(id: Long, completado: Boolean)
    suspend fun getPendingReminders(): List<Item>
    suspend fun getById(id: Long): Item?
}
