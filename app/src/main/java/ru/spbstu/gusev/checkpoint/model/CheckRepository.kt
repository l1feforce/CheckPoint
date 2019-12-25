package ru.spbstu.gusev.checkpoint.model

import ru.spbstu.gusev.checkpoint.database.CheckDatabase
import javax.inject.Inject

class CheckRepository @Inject constructor
    (
    private val checkDatabase: CheckDatabase,
    private val firestoreRepository: FirestoreRepository
) {

    private val checkDao by lazy {
        checkDatabase.checkDao()
    }

    val checkList = firestoreRepository.checkList

    fun insert(checkItem: CheckItem) =
        firestoreRepository.insert(checkItem)


    suspend fun update(checkItem: CheckItem) {
        firestoreRepository.update(checkItem)
    }


    suspend fun insertAll(checkItemsList: List<CheckItem>) {
        checkDao.insertAll(checkItemsList)
    }

    fun getAll() =
        firestoreRepository.getAll()

}