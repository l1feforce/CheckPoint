package ru.spbstu.gusev.checkpoint.model

import ru.spbstu.gusev.checkpoint.model.database.CheckDatabase
import javax.inject.Inject

class CheckRepository @Inject constructor(val checkDatabase: CheckDatabase){

    private val checkDao by lazy {
        checkDatabase.checkDao()
    }

    val allChecks = checkDao.getAll()


    suspend fun insert(checkItem: CheckItem) {
        checkDao.insert(checkItem)
    }

    suspend fun update(checkItem: CheckItem) {
        checkDao.update(checkItem)
    }

    suspend fun insertAll(checkItemsList: List<CheckItem>) {
        checkDao.insertAll(checkItemsList)
    }
}