package ru.spbstu.gusev.checkpoint.model.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ru.spbstu.gusev.checkpoint.model.CheckItem

@Dao
interface CheckDao {
    @Insert
    suspend fun insert(checkItem: CheckItem)

    @Query("SELECT * from check_table")
    fun getAll(): LiveData<List<CheckItem>>

    @Update
    suspend fun update(checkItem: CheckItem)

    @Insert
    suspend fun insertAll(checkList: List<CheckItem>)
}