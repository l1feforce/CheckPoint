package ru.spbstu.gusev.checkpoint.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.spbstu.gusev.checkpoint.database.CheckDatabase
import javax.inject.Inject

class CheckRepository @Inject constructor
    (
    private val checkDatabase: CheckDatabase,
    private val firestoreRepository: FirestoreRepository,
    private val photosRepository: PhotosRepository
) {

    private val checkDao by lazy {
        checkDatabase.checkDao()
    }

    val checkList = firestoreRepository.checkList

    suspend fun insert(checkItem: CheckItem) =
        firestoreRepository.insert(checkItem)?.also {
            it.addOnSuccessListener { doc ->
                GlobalScope.launch(Dispatchers.IO) {
                    checkDao.insert(checkItem.apply { id = doc.id })
                    update(checkItem)
                }
                photosRepository.insert(checkItem)
            }
        }

    suspend fun update(checkItem: CheckItem) =
        firestoreRepository.update(checkItem)

    fun getAll() =
        firestoreRepository.getAll()?.also {
            it.addOnSuccessListener { snapshot ->
                val result = snapshot.toObjects(CheckItem::class.java)
                checkList.value = result
                GlobalScope.launch(Dispatchers.IO) {
                    val localData = checkDao.getAllAsync()
                    if (localData.isEmpty() && result.isNotEmpty()) {
                        checkDao.insertAll(result)
                    }
                }
                photosRepository.getAll(result)
            }
        }


    fun clearAll() {
        GlobalScope.launch {
            checkDatabase.clearAllTables()
            firestoreRepository.clearAll()
            photosRepository.clearAll()
        }
        checkList.value = listOf()
    }


}