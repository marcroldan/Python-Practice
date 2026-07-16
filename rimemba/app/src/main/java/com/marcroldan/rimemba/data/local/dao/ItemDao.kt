package com.marcroldan.rimemba.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.marcroldan.rimemba.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Insert
    suspend fun insert(item: ItemEntity): Long

    @Update
    suspend fun update(item: ItemEntity)

    @Delete
    suspend fun delete(item: ItemEntity)

    @Query(
        """
        SELECT * FROM items
        ORDER BY
            completado ASC,
            CASE WHEN tipo = 'RECORDATORIO' THEN 0 ELSE 1 END ASC,
            fechaHora IS NULL ASC,
            fechaHora ASC,
            creadoEn DESC
        """
    )
    fun observeAll(): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM items
        WHERE fechaHora BETWEEN :inicio AND :fin
        ORDER BY completado ASC, fechaHora ASC
        """
    )
    fun observeToday(inicio: String, fin: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE tipo = 'RECORDATORIO' AND completado = 0 AND fechaHora IS NOT NULL")
    suspend fun getPendingReminders(): List<ItemEntity>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getById(id: Long): ItemEntity?

    @Query("UPDATE items SET completado = :completado WHERE id = :id")
    suspend fun setCompletado(id: Long, completado: Boolean)
}
