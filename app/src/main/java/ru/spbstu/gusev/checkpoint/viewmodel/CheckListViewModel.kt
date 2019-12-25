package ru.spbstu.gusev.checkpoint.viewmodel

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import ru.spbstu.gusev.checkpoint.model.CheckItem
import ru.spbstu.gusev.checkpoint.model.CheckRepository
import ru.spbstu.gusev.checkpoint.viewmodel.base.BaseViewModel
import javax.inject.Inject

class CheckListViewModel : BaseViewModel() {
    @Inject
    lateinit var currentCheckItem: CheckItem
    @Inject
    lateinit var checkRepository: CheckRepository

    val allChecks = checkRepository.checkList

    init {
        FirebaseAuth.getInstance().addAuthStateListener {
            Log.v("tag", it.currentUser?.uid ?: "null")
            refreshData()
        }
    }

    suspend fun addCheck(check: CheckItem) {
        checkRepository.insert(check)
    }

    suspend fun updateCheck(check: CheckItem) = checkRepository.update(check)

    fun setCurrentCheckView(checkItem: CheckItem) {
        currentCheckItem.copy(checkItem)
    }

    fun refreshData() = checkRepository.getAll()

}