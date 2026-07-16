package com.marcroldan.rimemba.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.marcroldan.rimemba.data.local.dao.ItemDao
import com.marcroldan.rimemba.data.local.entity.ItemEntity

@Database(entities = [ItemEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class RimembaDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    companion object {
        private const val DATABASE_NAME = "rimemba.db"

        @Volatile
        private var instance: RimembaDatabase? = null

        fun getInstance(context: Context): RimembaDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RimembaDatabase::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
    }
}
