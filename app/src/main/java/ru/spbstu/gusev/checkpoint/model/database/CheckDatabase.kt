package ru.spbstu.gusev.checkpoint.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.spbstu.gusev.checkpoint.model.CheckItem

@Database(entities = [CheckItem::class], version = 1, exportSchema = false)
@TypeConverters(ProductItemListTypeConverter::class)
abstract class CheckDatabase: RoomDatabase() {
    abstract fun checkDao(): CheckDao
}