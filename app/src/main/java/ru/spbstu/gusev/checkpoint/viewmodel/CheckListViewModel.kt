package ru.spbstu.gusev.checkpoint.viewmodel

import androidx.lifecycle.LiveData
import ru.spbstu.gusev.checkpoint.model.CheckItem
import ru.spbstu.gusev.checkpoint.model.CheckRepository
import ru.spbstu.gusev.checkpoint.viewmodel.base.BaseViewModel
import javax.inject.Inject

class CheckListViewModel : BaseViewModel() {
    @Inject
    lateinit var currentCheckItem: CheckItem
    @Inject
    lateinit var checkRepository: CheckRepository

    val allChecks: LiveData<List<CheckItem>> by lazy { checkRepository.allChecks }

    suspend fun addCheck(check: CheckItem) {
        checkRepository.insert(check)
    }

    suspend fun updateCheck(check: CheckItem) = checkRepository.update(check)

    fun setCurrentCheckView(checkItem: CheckItem) {
        currentCheckItem.copy(checkItem)
    }

}